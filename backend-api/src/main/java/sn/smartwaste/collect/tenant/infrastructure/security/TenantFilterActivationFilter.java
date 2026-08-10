package sn.smartwaste.collect.tenant.infrastructure.security;

import java.io.IOException;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.tenant.application.api.CurrentTenantProvider;

/**
 * Active le filtre Hibernate {@code organizationFilter} pour la durée de la requête (ADR-0020 §4).
 *
 * <p><b>Défaut fermé.</b> Un compte sans organisation courante ne doit voir aucune ligne cloisonnée,
 * jamais toutes : {@link #NO_ORGANIZATION} est un identifiant qui ne correspond à aucune collectivité
 * réelle, posé explicitement plutôt que de laisser le filtre désactivé (ce qui aurait montré la
 * totalité des données, l'exact inverse du cloisonnement recherché).
 *
 * <p><b>Exception : {@code SUPER_ADMIN} n'est pas filtré.</b> C'est l'exploitant de la plateforme
 * (SONAGED), pas l'administration d'une seule collectivité — il doit pouvoir superviser l'ensemble
 * sans changer de compte. Voir {@code TenantFilterActivationTest} pour la vérification dédiée que ce
 * point mérite : il inverse la règle générale de cloisonnement.
 *
 * <p><b>Pourquoi ce n'est PAS câblé dans {@code SecurityConfiguration} (module {@code identity}).</b>
 * Le contexte {@code tenant} dépend déjà de {@code identity} pour résoudre l'utilisateur courant
 * ({@code CurrentTenantProviderImpl} → {@code CurrentUserProvider}) — un appel en sens inverse
 * depuis {@code SecurityConfiguration} vers ce filtre formerait une dépendance circulaire entre
 * modules, que Spring Modulith refuse (même famille de problème que celui déjà rencontré entre
 * {@code JwtService} et {@code UserService}). La solution retenue laisse Spring Boot enregistrer ce
 * {@code @Component} comme un filtre servlet ordinaire : sans {@code @Order} explicite, il prend
 * l'ordre le plus bas (s'exécute en dernier), donc après la totalité de la chaîne Spring Security
 * (enregistrée, elle, à un ordre très prioritaire) — et donc après {@code JwtFilter}, dont dépend la
 * résolution de l'utilisateur courant. Aucune dépendance nouvelle entre {@code identity} et
 * {@code tenant} n'est introduite dans un sens comme dans l'autre.
 */
@Component
public class TenantFilterActivationFilter extends OncePerRequestFilter {

    /** N'existe dans aucun changelog : ferme la vue par défaut plutôt que de l'ouvrir. */
    private static final UUID NO_ORGANIZATION = new UUID(0L, 0L);

    @PersistenceContext
    private EntityManager entityManager;

    private final CurrentUserProvider currentUserProvider;
    private final CurrentTenantProvider currentTenantProvider;

    public TenantFilterActivationFilter(CurrentUserProvider currentUserProvider,
                                        CurrentTenantProvider currentTenantProvider) {
        this.currentUserProvider = currentUserProvider;
        this.currentTenantProvider = currentTenantProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!currentUserProvider.currentUserHasAnyRole("SUPER_ADMIN")) {
            UUID organizationId = currentTenantProvider.currentOrganizationId().orElse(NO_ORGANIZATION);
            entityManager.unwrap(Session.class)
                    .enableFilter("organizationFilter")
                    .setParameter("organizationId", organizationId);
        }
        filterChain.doFilter(request, response);
    }
}
