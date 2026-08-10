package sn.smartwaste.collect.identity.application.service.impl;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import sn.smartwaste.collect.identity.application.dto.User;
import sn.smartwaste.collect.identity.application.service.SessionService;
import sn.smartwaste.collect.identity.application.service.ValidationService;
import sn.smartwaste.collect.identity.domain.model.AuthorityEntity;
import sn.smartwaste.collect.identity.domain.model.UserEntity;
import sn.smartwaste.collect.identity.domain.repository.AuthorityRepository;
import sn.smartwaste.collect.identity.domain.repository.UserRepository;
import sn.smartwaste.collect.identity.infrastructure.security.JwtService;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.shared.domain.model.DeletionStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests de l'inscription publique.
 *
 * <p>Raison d'être : {@code /auth/register} est en {@code permitAll}, et l'ancienne
 * implémentation reprenait le rôle **depuis le corps de la requête**
 * ({@code user.setAuthority(user.getAuthority())}). N'importe qui pouvait donc s'inscrire
 * SUPER_ADMIN, recevoir le code d'activation à sa propre adresse et activer le compte. Le premier
 * test ci-dessous verrouille cette voie ; c'est le seul garde-fou tant que l'auth n'est pas
 * externalisée vers Keycloak (P0-A / ADR-0011).
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final UUID SUPER_ADMIN_ID = UUID.randomUUID();
    private static final UUID USER_ROLE_ID = UUID.randomUUID();

    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private ValidationService validationService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthorityRepository authorityRepository;
    @Mock
    private SessionService sessionService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AuthServiceImpl authService;

    private static AuthorityEntity authority(UUID id, String name) {
        AuthorityEntity authority = new AuthorityEntity();
        authority.setAuthorityId(id);
        authority.setName(name);
        return authority;
    }

    private static User registration(AuthorityEntity requestedAuthority) {
        User user = new User();
        user.setUserEmail("awa@example.sn");
        user.setUserFirstname("Awa");
        user.setUserLastname("Diop");
        user.setUserPassword("un-mot-de-passe");
        user.setAuthority(requestedAuthority);
        return user;
    }

    private void givenEmailFreeAndRolesSeeded() {
        lenient().when(userRepository.findByUserEmail("awa@example.sn")).thenReturn(Optional.empty());
        lenient().when(passwordEncoder.encode(any())).thenReturn("$2a$hache");
        lenient().when(authorityRepository.findByNameAndDeletionStatus("USER", DeletionStatus.ACTIVE))
                .thenReturn(Optional.of(authority(USER_ROLE_ID, "USER")));
        lenient().when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));
    }

    private UserEntity captureSavedUser() {
        ArgumentCaptor<UserEntity> saved = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(saved.capture());
        return saved.getValue();
    }

    @Test
    @DisplayName("un rôle privilégié fourni dans la requête d'inscription est ignoré au profit de USER")
    void register_ignoresClientSuppliedAuthority() {
        givenEmailFreeAndRolesSeeded();

        authService.register(registration(authority(SUPER_ADMIN_ID, "SUPER_ADMIN")));

        UserEntity saved = captureSavedUser();
        assertThat(saved.getAuthority().getName()).isEqualTo("USER");
        assertThat(saved.getAuthority().getAuthorityId()).isEqualTo(USER_ROLE_ID);
    }

    @Test
    @DisplayName("un identifiant fourni dans la requête d'inscription est ignoré : jamais un UPDATE déguisé en INSERT")
    void register_ignoresClientSuppliedUserId() {
        givenEmailFreeAndRolesSeeded();

        // L'id d'un compte EXISTANT quelconque : sans le correctif, Spring Data verrait un @Id
        // non nul et ferait un merge (UPDATE de ce compte) au lieu d'un persist (INSERT).
        User request = registration(null);
        request.setUserId(UUID.randomUUID());

        authService.register(request);

        assertThat(captureSavedUser().getUserId()).isNull();
    }

    @Test
    @DisplayName("une inscription sans rôle aboutit : le serveur en attribue un (le formulaire n'en envoie pas)")
    void register_assignsDefaultRoleWhenNoneSupplied() {
        givenEmailFreeAndRolesSeeded();

        authService.register(registration(null));

        // Avant le correctif, `authority` restait nul alors que la colonne est `nullable=false` :
        // tout signup légitime échouait sur une violation de contrainte.
        assertThat(captureSavedUser().getAuthority()).isNotNull();
    }

    @Test
    @DisplayName("le compte est créé inactif et le mot de passe est haché, jamais stocké en clair")
    void register_createsInactiveAccountWithHashedPassword() {
        givenEmailFreeAndRolesSeeded();

        authService.register(registration(null));

        UserEntity saved = captureSavedUser();
        assertThat(saved.isActivated()).isFalse();
        assertThat(saved.getUserPassword()).isEqualTo("$2a$hache");
        verify(validationService).registerUserCode(saved);
    }

    @Test
    @DisplayName("une adresse déjà utilisée est refusée avant toute écriture")
    void register_rejectsDuplicateEmail() {
        when(userRepository.findByUserEmail("awa@example.sn")).thenReturn(Optional.of(new UserEntity()));

        assertThatThrownBy(() -> authService.register(registration(null)))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any());
        verify(validationService, never()).registerUserCode(any());
    }

    @Test
    @DisplayName("un mot de passe vide est refusé avant toute écriture")
    void register_rejectsBlankPassword() {
        when(userRepository.findByUserEmail("awa@example.sn")).thenReturn(Optional.empty());

        User user = registration(null);
        user.setUserPassword("   ");

        assertThatThrownBy(() -> authService.register(user))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("si le rôle par défaut n'est pas semé, l'inscription échoue au lieu de créer un compte sans rôle")
    void register_failsLoudlyWhenDefaultRoleMissing() {
        when(userRepository.findByUserEmail("awa@example.sn")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("$2a$hache");
        when(authorityRepository.findByNameAndDeletionStatus("USER", DeletionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(registration(null)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("USER");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("changer son mot de passe exige le mot de passe actuel et revoque toutes les sessions")
    void changePassword_hashesNewPasswordAndRevokesAllSessions() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setUserPassword("$2a$ancien");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("ancien-mdp", "$2a$ancien")).thenReturn(true);
        when(passwordEncoder.encode("nouveau-mdp")).thenReturn("$2a$nouveau");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        authService.changePassword(userId, "ancien-mdp", "nouveau-mdp");

        assertThat(captureSavedUser().getUserPassword()).isEqualTo("$2a$nouveau");
        // Le point du correctif (ADR-0021) : un changement de mot de passe deconnecte partout.
        verify(sessionService).revokeAllForUser(userId);
    }

    @Test
    @DisplayName("un mot de passe actuel incorrect est refuse avant toute ecriture")
    void changePassword_rejectsWrongCurrentPassword() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setUserPassword("$2a$ancien");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("faux-mdp", "$2a$ancien")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(userId, "faux-mdp", "nouveau-mdp"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any());
        verify(sessionService, never()).revokeAllForUser(any());
    }
}
