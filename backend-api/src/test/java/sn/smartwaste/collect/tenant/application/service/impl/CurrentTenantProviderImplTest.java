package sn.smartwaste.collect.tenant.application.service.impl;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.tenant.domain.model.OrganizationMembership;
import sn.smartwaste.collect.tenant.domain.repository.OrganizationMembershipRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Résolution de la collectivité courante.
 *
 * <p>Les trois cas testés sont ceux qui décident du cloisonnement : un utilisateur rattaché, un
 * utilisateur sans rattachement, et une requête non authentifiée. Le dernier est le plus délicat —
 * {@code CurrentUserProvider} lève alors {@code IllegalStateException}, et laisser cette exception
 * remonter ferait échouer en 500 tout endpoint public qui interrogerait le tenant.
 */
@ExtendWith(MockitoExtension.class)
class CurrentTenantProviderImplTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID ORGANIZATION_ID = UUID.randomUUID();

    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private OrganizationMembershipRepository membershipRepository;

    @InjectMocks
    private CurrentTenantProviderImpl currentTenantProvider;

    private static OrganizationMembership membership() {
        OrganizationMembership membership = new OrganizationMembership();
        membership.setUserId(USER_ID);
        membership.setOrganizationId(ORGANIZATION_ID);
        return membership;
    }

    @Test
    @DisplayName("un utilisateur rattaché résout sa collectivité")
    void resolvesOrganizationOfMemberUser() {
        when(currentUserProvider.requireCurrentUserId()).thenReturn(USER_ID);
        when(membershipRepository.findByUserId(USER_ID)).thenReturn(Optional.of(membership()));

        assertThat(currentTenantProvider.currentOrganizationId()).contains(ORGANIZATION_ID);
    }

    @Test
    @DisplayName("un utilisateur sans rattachement ne résout aucune collectivité")
    void unattachedUserResolvesToEmpty() {
        when(currentUserProvider.requireCurrentUserId()).thenReturn(USER_ID);
        when(membershipRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        // `empty` n'autorise rien : c'est à l'appelant qui cloisonne de refuser.
        assertThat(currentTenantProvider.currentOrganizationId()).isEmpty();
    }

    @Test
    @DisplayName("une requête non authentifiée rend empty au lieu de propager une exception")
    void unauthenticatedRequestYieldsEmptyInsteadOfThrowing() {
        when(currentUserProvider.requireCurrentUserId())
                .thenThrow(new IllegalStateException("Aucun utilisateur authentifié"));

        assertThat(currentTenantProvider.currentOrganizationId()).isEmpty();
        // Inutile d'interroger la base : il n'y a pas d'utilisateur sur qui la questionner.
        verify(membershipRepository, never()).findByUserId(any());
    }
}
