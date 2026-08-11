package sn.smartwaste.collect.tenant.infrastructure.security;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.tenant.application.api.CurrentTenantProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Active/désactive le cloisonnement selon qui fait la requête (ADR-0020 §4).
 *
 * <p><b>Pourquoi ce test existe séparément d'un test bout-en-bout.</b> Le composant lit le principal
 * authentifié via {@code SecurityContextHolder}, ce que {@code @WithMockUser} ne peut pas simuler
 * fidèlement ici : {@code CurrentUserProvider} exige un vrai {@code UserEntity} comme principal, pas
 * l'utilisateur générique de Spring Security. Ce test verrouille donc directement le seul point qui
 * inverse la règle générale de cloisonnement — le contournement {@code SUPER_ADMIN} — et le défaut
 * fermé quand aucune organisation n'est résolue, sans réaction en chaîne sur toute la pile HTTP.
 *
 * <p>⚠️ <b>Ce que ces tests ne prouvent PAS.</b> La {@code Session} est un mock : ils vérifient que
 * {@code enableFilter} est <i>appelé</i>, jamais qu'une requête réelle est filtrée. C'est
 * exactement pour cela que le cloisonnement a pu rester totalement inopérant jusqu'au 2026-08-11
 * — six mois de tests au vert sur un filtre qui ne filtrait rien, parce qu'aucun
 * {@code EntityManager} n'était lié au thread à cet instant. La preuve du filtrage effectif exige
 * une base réelle ; elle a été faite manuellement (un ADMIN d'une autre collectivité passe de 71 à
 * 0 dépotoirs) et reste à automatiser le jour où l'environnement de test disposera de PostgreSQL.
 */
@ExtendWith(MockitoExtension.class)
class TenantFilterActivationFilterTest {

    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private CurrentTenantProvider currentTenantProvider;
    @Mock
    private EntityManager entityManager;
    @Mock
    private Session session;
    @Mock
    private org.hibernate.Filter hibernateFilter;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private TenantFilterActivationFilter filter() {
        var f = new TenantFilterActivationFilter(currentUserProvider, currentTenantProvider);
        org.springframework.test.util.ReflectionTestUtils.setField(f, "entityManager", entityManager);
        return f;
    }

    @Test
    @DisplayName("SUPER_ADMIN n'est pas filtré : le filtre Hibernate n'est jamais activé")
    void superAdminBypassesTheFilter() throws Exception {
        when(currentUserProvider.currentUserHasAnyRole("SUPER_ADMIN")).thenReturn(true);

        filter().doFilterInternal(request, response, filterChain);

        verify(entityManager, never()).unwrap(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("un compte rattaché voit son organisation appliquée comme paramètre du filtre")
    void attachedAccountFiltersByItsOrganization() throws Exception {
        UUID orgId = UUID.randomUUID();
        when(currentUserProvider.currentUserHasAnyRole("SUPER_ADMIN")).thenReturn(false);
        when(currentTenantProvider.currentOrganizationId()).thenReturn(Optional.of(orgId));
        when(entityManager.unwrap(Session.class)).thenReturn(session);
        when(session.enableFilter("organizationFilter")).thenReturn(hibernateFilter);
        when(hibernateFilter.setParameter("organizationId", orgId)).thenReturn(hibernateFilter);

        filter().doFilterInternal(request, response, filterChain);

        verify(hibernateFilter).setParameter("organizationId", orgId);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("sans organisation courante, le filtre se ferme sur une sentinelle plutot que de tout montrer")
    void missingOrganizationClosesTheViewInsteadOfOpeningIt() throws Exception {
        when(currentUserProvider.currentUserHasAnyRole("SUPER_ADMIN")).thenReturn(false);
        when(currentTenantProvider.currentOrganizationId()).thenReturn(Optional.empty());
        when(entityManager.unwrap(Session.class)).thenReturn(session);
        when(session.enableFilter("organizationFilter")).thenReturn(hibernateFilter);
        when(hibernateFilter.setParameter(org.mockito.ArgumentMatchers.eq("organizationId"), any()))
                .thenReturn(hibernateFilter);

        filter().doFilterInternal(request, response, filterChain);

        ArgumentCaptor<UUID> applied = ArgumentCaptor.forClass(UUID.class);
        verify(hibernateFilter).setParameter(org.mockito.ArgumentMatchers.eq("organizationId"), applied.capture());
        // Le point du defaut ferme : cette sentinelle ne correspond a AUCUNE collectivite reelle,
        // donc la requete ne voit rien plutot que tout.
        assertThat(applied.getValue()).isEqualTo(new UUID(0L, 0L));
    }

    @Test
    @DisplayName("SUPER_ADMIN avec en-tete de perimetre : filtre sur la collectivite demandee")
    void superAdminCanScopeToOneOrganization() throws Exception {
        UUID requested = UUID.randomUUID();
        when(currentUserProvider.currentUserHasAnyRole("SUPER_ADMIN")).thenReturn(true);
        when(request.getHeader(TenantFilterActivationFilter.SCOPE_HEADER)).thenReturn(requested.toString());
        when(entityManager.unwrap(Session.class)).thenReturn(session);
        when(session.enableFilter("organizationFilter")).thenReturn(hibernateFilter);
        when(hibernateFilter.setParameter("organizationId", requested)).thenReturn(hibernateFilter);

        filter().doFilterInternal(request, response, filterChain);

        // L'exploitant observe UNE collectivite sans changer de compte : c'est la moitie « choisir
        // un perimetre » de la demande, l'autre etant la vue complete sans en-tete.
        verify(hibernateFilter).setParameter("organizationId", requested);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("En-tete de perimetre illisible : vue fermee, jamais ouverte")
    void malformedScopeHeaderClosesTheView() throws Exception {
        when(currentUserProvider.currentUserHasAnyRole("SUPER_ADMIN")).thenReturn(true);
        when(request.getHeader(TenantFilterActivationFilter.SCOPE_HEADER)).thenReturn("pas-un-uuid");
        when(entityManager.unwrap(Session.class)).thenReturn(session);
        when(session.enableFilter("organizationFilter")).thenReturn(hibernateFilter);
        when(hibernateFilter.setParameter(org.mockito.ArgumentMatchers.eq("organizationId"), any()))
                .thenReturn(hibernateFilter);

        filter().doFilterInternal(request, response, filterChain);

        ArgumentCaptor<UUID> applied = ArgumentCaptor.forClass(UUID.class);
        verify(hibernateFilter).setParameter(org.mockito.ArgumentMatchers.eq("organizationId"), applied.capture());
        // Un perimetre demande mais incomprehensible ne doit pas retomber sur « tout montrer ».
        assertThat(applied.getValue()).isEqualTo(new UUID(0L, 0L));
    }

    @Test
    @DisplayName("Un ADMIN ne peut pas detourner l en-tete pour lire une autre collectivite")
    void nonSuperAdminCannotUseTheScopeHeader() throws Exception {
        UUID own = UUID.randomUUID();
        when(currentUserProvider.currentUserHasAnyRole("SUPER_ADMIN")).thenReturn(false);
        when(currentTenantProvider.currentOrganizationId()).thenReturn(Optional.of(own));
        when(entityManager.unwrap(Session.class)).thenReturn(session);
        when(session.enableFilter("organizationFilter")).thenReturn(hibernateFilter);
        when(hibernateFilter.setParameter("organizationId", own)).thenReturn(hibernateFilter);

        filter().doFilterInternal(request, response, filterChain);

        // L en-tete n est meme pas lu : sinon, ajouter une ligne a sa requete suffirait a un ADMIN
        // pour lire chez le voisin.
        verify(request, never()).getHeader(TenantFilterActivationFilter.SCOPE_HEADER);
        verify(hibernateFilter).setParameter("organizationId", own);
    }
}
