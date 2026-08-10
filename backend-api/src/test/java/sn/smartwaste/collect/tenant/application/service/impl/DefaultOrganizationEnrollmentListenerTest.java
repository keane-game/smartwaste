package sn.smartwaste.collect.tenant.application.service.impl;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.smartwaste.collect.shared.domain.event.UserAccountCreated;
import sn.smartwaste.collect.tenant.application.api.CurrentTenantProvider;
import sn.smartwaste.collect.tenant.domain.model.OrganizationMembership;
import sn.smartwaste.collect.tenant.domain.repository.OrganizationMembershipRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Rattachement automatique à Pikine (ADR-0020 §4, décision du 2026-08-10).
 *
 * <p>Ce test verrouille la raison d'être du composant : sans lui, tout nouveau compte serait sans
 * organisation, et le filtre {@code organizationFilter} — une fois activé — le rendrait aveugle à
 * son propre référentiel dès sa première connexion.
 */
@ExtendWith(MockitoExtension.class)
class DefaultOrganizationEnrollmentListenerTest {

    @Mock
    private OrganizationMembershipRepository membershipRepository;

    @InjectMocks
    private DefaultOrganizationEnrollmentListener listener;

    @Test
    @DisplayName("un compte tout juste créé est rattaché à Pikine")
    void newAccountIsAttachedToPikine() {
        UUID userId = UUID.randomUUID();
        when(membershipRepository.findByUserId(userId)).thenReturn(Optional.empty());

        listener.onUserAccountCreated(new UserAccountCreated(userId));

        ArgumentCaptor<OrganizationMembership> saved = ArgumentCaptor.forClass(OrganizationMembership.class);
        verify(membershipRepository).save(saved.capture());
        assertThat(saved.getValue().getUserId()).isEqualTo(userId);
        assertThat(saved.getValue().getOrganizationId()).isEqualTo(CurrentTenantProvider.PIKINE_ORGANIZATION_ID);
    }

    @Test
    @DisplayName("un compte déjà rattaché n'est pas dupliqué (événement rejoué)")
    void alreadyAttachedAccountIsNotDuplicated() {
        UUID userId = UUID.randomUUID();
        var existing = new OrganizationMembership();
        existing.setUserId(userId);
        when(membershipRepository.findByUserId(userId)).thenReturn(Optional.of(existing));

        listener.onUserAccountCreated(new UserAccountCreated(userId));

        verify(membershipRepository, never()).save(any());
    }
}
