package sn.smartwaste.collect.identity.application.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import sn.smartwaste.collect.identity.application.service.SessionService;
import sn.smartwaste.collect.identity.domain.model.PasswordResetToken;
import sn.smartwaste.collect.identity.domain.model.UserEntity;
import sn.smartwaste.collect.identity.domain.repository.PasswordResetTokenRepository;
import sn.smartwaste.collect.identity.domain.repository.UserRepository;
import sn.smartwaste.collect.shared.domain.event.PasswordResetRequested;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Réinitialisation de mot de passe (ADR-0021).
 *
 * <p>Deux comportements méritent d'être verrouillés par test : la réponse ne doit jamais varier
 * selon qu'un e-mail est connu ou non (sinon un appelant anonyme énumère les comptes), et un jeton
 * consommé ou expiré doit être refusé au même titre qu'un jeton inconnu.
 */
@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private SessionService sessionService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PasswordResetServiceImpl service() {
        return new PasswordResetServiceImpl(userRepository, tokenRepository, passwordEncoder,
                sessionService, eventPublisher, 60L);
    }

    @Test
    @DisplayName("une adresse connue declenche l'emission d'un jeton et l'evenement de notification")
    void requestReset_knownEmailIssuesToken() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setUserEmail("awa@example.sn");
        user.setUserLastname("Diop");
        when(userRepository.findByUserEmail("awa@example.sn")).thenReturn(Optional.of(user));
        when(tokenRepository.findByUserIdAndUsedAtIsNull(userId)).thenReturn(List.of());

        service().requestReset("awa@example.sn");

        verify(tokenRepository).save(any(PasswordResetToken.class));
        ArgumentCaptor<PasswordResetRequested> event = ArgumentCaptor.forClass(PasswordResetRequested.class);
        verify(eventPublisher).publishEvent(event.capture());
        assertThat(event.getValue().recipientEmail()).isEqualTo("awa@example.sn");
        // Le jeton en clair ne doit exister que dans l'evenement destine a l'e-mail, jamais en base.
        assertThat(event.getValue().token()).isNotBlank();
    }

    @Test
    @DisplayName("une adresse inconnue ne publie aucun evenement et n'ecrit rien, silencieusement")
    void requestReset_unknownEmailStaysSilent() {
        when(userRepository.findByUserEmail("inconnu@example.sn")).thenReturn(Optional.empty());

        service().requestReset("inconnu@example.sn");

        verify(tokenRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("une nouvelle demande invalide les jetons precedents non consommes")
    void requestReset_invalidatesPreviousUnusedTokens() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setUserEmail("awa@example.sn");
        user.setUserLastname("Diop");
        PasswordResetToken stale = new PasswordResetToken();
        stale.setExpiresAt(Instant.now().plusSeconds(3600));
        when(userRepository.findByUserEmail("awa@example.sn")).thenReturn(Optional.of(user));
        when(tokenRepository.findByUserIdAndUsedAtIsNull(userId)).thenReturn(List.of(stale));

        service().requestReset("awa@example.sn");

        assertThat(stale.getUsedAt()).isNotNull();
        verify(tokenRepository).saveAll(List.of(stale));
    }

    @Test
    @DisplayName("confirmer avec un jeton inconnu echoue sans reveler la raison")
    void confirmReset_unknownTokenFails() {
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().confirmReset("un-jeton", "nouveau-mdp"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("confirmer avec un jeton deja consomme echoue")
    void confirmReset_usedTokenFails() {
        PasswordResetToken used = new PasswordResetToken();
        used.setUserId(UUID.randomUUID());
        used.setExpiresAt(Instant.now().plusSeconds(3600));
        used.setUsedAt(Instant.now().minusSeconds(60));
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(used));

        assertThatThrownBy(() -> service().confirmReset("un-jeton", "nouveau-mdp"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("confirmer avec un jeton expire echoue")
    void confirmReset_expiredTokenFails() {
        PasswordResetToken expired = new PasswordResetToken();
        expired.setUserId(UUID.randomUUID());
        expired.setExpiresAt(Instant.now().minusSeconds(1));
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service().confirmReset("un-jeton", "nouveau-mdp"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("un jeton valide applique le nouveau mot de passe et revoque toutes les sessions")
    void confirmReset_validTokenAppliesPasswordAndRevokesSessions() {
        UUID userId = UUID.randomUUID();
        PasswordResetToken token = new PasswordResetToken();
        token.setUserId(userId);
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setUserPassword("$2a$ancien");
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("nouveau-mdp")).thenReturn("$2a$nouveau");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        service().confirmReset("un-jeton", "nouveau-mdp");

        ArgumentCaptor<UserEntity> saved = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getUserPassword()).isEqualTo("$2a$nouveau");
        assertThat(token.getUsedAt()).isNotNull();
        verify(sessionService).revokeAllForUser(userId);
    }
}
