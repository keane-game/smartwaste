package sn.smartwaste.collect.tenant.presentation.controller;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.identity.application.api.UserDirectory;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.tenant.application.api.CurrentTenantProvider;
import sn.smartwaste.collect.tenant.domain.model.Organization;
import sn.smartwaste.collect.tenant.domain.model.OrganizationMembership;
import sn.smartwaste.collect.tenant.domain.model.OrganizationStatus;
import sn.smartwaste.collect.tenant.domain.repository.OrganizationMembershipRepository;
import sn.smartwaste.collect.tenant.domain.repository.OrganizationRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * `OrganizationController` — le contrôle « SUPER_ADMIN, ou ADMIN de cette collectivité »
 * (ADR-0020 §5) n'est pas exprimable en {@code @PreAuthorize} déclaratif ({@code
 * CurrentTenantProvider} dépend de la collectivité du chemin, pas d'une expression statique), et
 * rien dans ce dépôt ne teste {@code CurrentUserProvider.requireCurrentUserId()} via un principal
 * Spring Security réel (aucun {@code @WithMockUser} ne pose une vraie {@code UserEntity} comme
 * principal — {@link CurrentUserProvider}/{@link CurrentTenantProvider} sont donc mockés
 * directement ici, comme {@code TerritorialAccessGuard} l'est déjà pour {@code
 * CollectionRouteServiceImplTest}, plutôt qu'une intégration HTTP complète.
 */
@ExtendWith(MockitoExtension.class)
class OrganizationControllerTest {

    private static final UUID PIKINE = UUID.randomUUID();
    private static final UUID OTHER_ORG = UUID.randomUUID();
    private static final UUID SOME_USER = UUID.randomUUID();

    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private OrganizationMembershipRepository membershipRepository;
    @Mock
    private UserDirectory userDirectory;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private CurrentTenantProvider currentTenantProvider;

    private OrganizationController controller() {
        return new OrganizationController(organizationRepository, membershipRepository,
                userDirectory, currentUserProvider, currentTenantProvider);
    }

    private static Organization organization(UUID id) {
        var org = new Organization();
        org.setOrganizationId(id);
        org.setName("Ville de Pikine");
        org.setCode("PIKINE");
        org.setStatus(OrganizationStatus.ACTIVE);
        return org;
    }

    @Test
    @DisplayName("SUPER_ADMIN consulte n'importe quelle collectivité")
    void superAdminReadsAnyOrganization() {
        when(currentUserProvider.currentUserHasAnyRole("SUPER_ADMIN")).thenReturn(true);
        when(organizationRepository.findById(PIKINE)).thenReturn(Optional.of(organization(PIKINE)));

        assertThat(controller().read(PIKINE).getOrganizationId()).isEqualTo(PIKINE);
    }

    @Test
    @DisplayName("un ADMIN consulte sa propre collectivité")
    void adminReadsOwnOrganization() {
        when(currentUserProvider.currentUserHasAnyRole("SUPER_ADMIN")).thenReturn(false);
        lenient().when(currentUserProvider.currentUserHasAnyRole("ADMIN")).thenReturn(true);
        when(currentTenantProvider.currentOrganizationId()).thenReturn(Optional.of(PIKINE));
        when(organizationRepository.findById(PIKINE)).thenReturn(Optional.of(organization(PIKINE)));

        assertThat(controller().read(PIKINE).getOrganizationId()).isEqualTo(PIKINE);
    }

    @Test
    @DisplayName("un ADMIN ne peut PAS consulter une autre collectivité que la sienne")
    void adminCannotReadAnotherOrganization() {
        when(currentUserProvider.currentUserHasAnyRole("SUPER_ADMIN")).thenReturn(false);
        lenient().when(currentUserProvider.currentUserHasAnyRole("ADMIN")).thenReturn(true);
        when(currentTenantProvider.currentOrganizationId()).thenReturn(Optional.of(PIKINE));

        assertThatThrownBy(() -> controller().read(OTHER_ORG))
                .isInstanceOf(AccessDeniedException.class);
        verify(organizationRepository, never()).findById(any());
    }

    @Test
    @DisplayName("un compte sans rôle ADMIN/SUPER_ADMIN est refusé, même rattaché à la collectivité visée")
    void plainUserIsRefusedRegardlessOfMembership() {
        when(currentUserProvider.currentUserHasAnyRole("SUPER_ADMIN")).thenReturn(false);
        when(currentUserProvider.currentUserHasAnyRole("ADMIN")).thenReturn(false);

        assertThatThrownBy(() -> controller().read(PIKINE))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("un code déjà utilisé est refusé (409) avant toute écriture")
    void createRejectsDuplicateCode() {
        var requested = new Organization();
        requested.setCode("PIKINE");
        when(organizationRepository.findByCode("PIKINE")).thenReturn(Optional.of(organization(PIKINE)));

        assertThatThrownBy(() -> controller().create(requested))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
        verify(organizationRepository, never()).save(any());
    }

    @Test
    @DisplayName("un id ou un statut fourni par l'appelant est ignoré à la création")
    void createIgnoresClientSuppliedIdAndStatus() {
        var requested = new Organization();
        requested.setOrganizationId(UUID.randomUUID());
        requested.setCode("NOUVELLE");
        requested.setStatus(OrganizationStatus.SUSPENDED);
        when(organizationRepository.findByCode("NOUVELLE")).thenReturn(Optional.empty());
        when(organizationRepository.save(any(Organization.class))).thenAnswer(i -> i.getArgument(0));

        var created = controller().create(requested);

        assertThat(created.getOrganizationId()).isNull();
        assertThat(created.getStatus()).isEqualTo(OrganizationStatus.ACTIVE);
    }

    @Test
    @DisplayName("rattacher un utilisateur inconnu de l'identité échoue")
    void attachUnknownUserFails() {
        when(organizationRepository.findById(PIKINE)).thenReturn(Optional.of(organization(PIKINE)));
        when(userDirectory.emailOf(SOME_USER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller().attach(PIKINE, new OrganizationController.MembershipRequest(SOME_USER)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(membershipRepository, never()).save(any());
    }

    @Test
    @DisplayName("rattacher un utilisateur déjà rattaché ailleurs échoue (409)")
    void attachAlreadyMemberElsewhereFails() {
        when(organizationRepository.findById(PIKINE)).thenReturn(Optional.of(organization(PIKINE)));
        when(userDirectory.emailOf(SOME_USER)).thenReturn(Optional.of("agent@sonaged.sn"));
        var existing = new OrganizationMembership();
        existing.setOrganizationId(OTHER_ORG);
        when(membershipRepository.findByUserId(SOME_USER)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> controller().attach(PIKINE, new OrganizationController.MembershipRequest(SOME_USER)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
    }

    @Test
    @DisplayName("détacher un utilisateur rattaché à une AUTRE collectivité échoue plutôt que de le détacher à tort")
    void detachRefusesMembershipFromAnotherOrganization() {
        var membership = new OrganizationMembership();
        membership.setOrganizationId(OTHER_ORG);
        when(membershipRepository.findByUserId(SOME_USER)).thenReturn(Optional.of(membership));

        assertThatThrownBy(() -> controller().detach(PIKINE, SOME_USER))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(membershipRepository, never()).delete(any());
    }
}
