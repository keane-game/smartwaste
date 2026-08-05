package sn.smartwaste.collect.identity.application.service.impl;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import sn.smartwaste.collect.identity.application.api.AdminAccountProvisioning.Outcome;
import sn.smartwaste.collect.identity.domain.model.AuthorityEntity;
import sn.smartwaste.collect.identity.domain.model.UserEntity;
import sn.smartwaste.collect.identity.domain.repository.AuthorityRepository;
import sn.smartwaste.collect.identity.domain.repository.UserRepository;
import sn.smartwaste.collect.shared.domain.model.DeletionStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Création du compte d'administration initial (ADR-0014 §2).
 *
 * <p><b>Le trou que cela ferme.</b> Après les migrations UUID, la table {@code users} est vide —
 * {@code 2.1.0-3} la purge avant de convertir la clé primaire, et le seed d'origine n'a jamais été
 * rejoué sous la nouvelle forme, alors que les rôles, eux, l'ont été. Aucun chemin ne menait à un
 * compte {@code ADMIN} : {@code /auth/register} impose {@code USER} et l'activation exige un code
 * envoyé par courriel. Toute la surface d'administration était inatteignable.
 */
@ExtendWith(MockitoExtension.class)
class AdminAccountProvisioningImplTest {

    private static final String EMAIL = "admin@sonaged.sn";
    private static final String MOT_DE_PASSE = "un-mot-de-passe-fourni-par-l-environnement";

    @Mock private UserRepository userRepository;
    @Mock private AuthorityRepository authorityRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private AdminAccountProvisioningImpl provisioning() {
        return new AdminAccountProvisioningImpl(userRepository, authorityRepository, encoder);
    }

    private void superAdminExists() {
        var authority = new AuthorityEntity();
        authority.setName("SUPER_ADMIN");
        when(authorityRepository.findByNameAndDeletionStatus("SUPER_ADMIN", DeletionStatus.ACTIVE))
                .thenReturn(Optional.of(authority));
    }

    @Test
    @DisplayName("le compte est créé activé et en SUPER_ADMIN")
    void createsAnActivatedSuperAdmin() {
        superAdminExists();
        when(userRepository.findByUserEmail(EMAIL)).thenReturn(Optional.empty());

        var outcome = provisioning().createAdministratorIfAbsent(EMAIL, MOT_DE_PASSE);

        var saved = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(saved.capture());
        assertThat(outcome).isEqualTo(Outcome.CREATED);
        assertThat(saved.getValue().getUserEmail()).isEqualTo(EMAIL);
        assertThat(saved.getValue().isActivated()).isTrue();
        assertThat(saved.getValue().getAuthority().getName()).isEqualTo("SUPER_ADMIN");
    }

    @Test
    @DisplayName("le mot de passe est haché, jamais stocké en clair")
    void hashesThePassword() {
        superAdminExists();
        when(userRepository.findByUserEmail(EMAIL)).thenReturn(Optional.empty());

        provisioning().createAdministratorIfAbsent(EMAIL, MOT_DE_PASSE);

        var saved = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getUserPassword()).isNotEqualTo(MOT_DE_PASSE);
        assertThat(encoder.matches(MOT_DE_PASSE, saved.getValue().getUserPassword())).isTrue();
    }

    @Test
    @DisplayName("un compte déjà présent n'est pas réécrit")
    void neverOverwritesAnExistingAccount() {
        // Le cas qui compte : sans cette garde, chaque redémarrage réinitialiserait le mot de passe
        // de l'administrateur à la valeur de l'environnement — y compris après une rotation.
        when(userRepository.findByUserEmail(EMAIL)).thenReturn(Optional.of(new UserEntity()));

        var outcome = provisioning().createAdministratorIfAbsent(EMAIL, MOT_DE_PASSE);

        assertThat(outcome).isEqualTo(Outcome.ALREADY_PRESENT);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("sans rôle semé, aucun compte n'est créé sans habilitation")
    void createsNothingWhenTheRoleIsMissing() {
        when(userRepository.findByUserEmail(EMAIL)).thenReturn(Optional.empty());
        when(authorityRepository.findByNameAndDeletionStatus("SUPER_ADMIN", DeletionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        var outcome = provisioning().createAdministratorIfAbsent(EMAIL, MOT_DE_PASSE);

        assertThat(outcome).isEqualTo(Outcome.ROLE_MISSING);
        verify(userRepository, never()).save(any());
    }
}
