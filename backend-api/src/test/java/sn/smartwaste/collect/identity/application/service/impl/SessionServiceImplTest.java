package sn.smartwaste.collect.identity.application.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.smartwaste.collect.identity.application.dto.AuthTokens;
import sn.smartwaste.collect.identity.domain.model.UserEntity;
import sn.smartwaste.collect.identity.domain.model.UserSession;
import sn.smartwaste.collect.identity.domain.repository.UserSessionRepository;
import sn.smartwaste.collect.identity.infrastructure.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Sessions d'authentification révocables.
 *
 * <p>Ce qui est vérifié ici n'est pas du confort : avant les sessions, la déconnexion n'existait pas
 * côté serveur — le client vidait son stockage local et le jeton restait valable dix jours. Les
 * quatre propriétés testées sont celles dont dépend la réalité de cette révocation :
 *
 * <ol>
 *   <li>le jeton de rafraîchissement n'est <b>jamais</b> persisté en clair ;</li>
 *   <li>il est <b>tourné</b> à chaque usage, pour qu'un jeton volé cesse de servir ;</li>
 *   <li>une session révoquée ou expirée est <b>refusée</b>, et les deux cas sont indiscernables
 *       de l'extérieur ;</li>
 *   <li>{@code isActive} — appelé à chaque requête par {@code JwtFilter} — dit non dès la révocation.</li>
 * </ol>
 *
 * <p><b>Le refus est une {@link BadCredentialsException}, plus une {@code ResourceNotFoundException}</b>
 * (2026-08-11). Un jeton de rafraîchissement mort n'est pas une ressource absente : la première se
 * traduit en 401, la seconde en 404. Le frontend ne déclenchait donc jamais sa procédure de fin de
 * session sur un rafraîchissement échoué — il réessayait en boucle, ce qui produisait la tempête de
 * popups d'erreur constatée côté navigateur.
 */
@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID SESSION_ID = UUID.randomUUID();

    @Mock
    private UserSessionRepository sessionRepository;
    @Mock
    private JwtService jwtService;

    private SessionServiceImpl newService() {
        return new SessionServiceImpl(sessionRepository, jwtService, 30);
    }

    private static UserEntity user() {
        UserEntity user = new UserEntity();
        user.setUserId(USER_ID);
        user.setUserEmail("awa@example.sn");
        return user;
    }

    /** Session ouverte, dont l'empreinte correspond au jeton donné. */
    private static UserSession openSessionFor(String refreshToken) {
        UserSession session = new UserSession();
        session.setSessionId(SESSION_ID);
        session.setUserId(USER_ID);
        session.setRefreshTokenHash(SessionServiceImpl.hash(refreshToken));
        session.setIssuedAt(Instant.now());
        session.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
        return session;
    }

    private UserSession captureSaved() {
        ArgumentCaptor<UserSession> saved = ArgumentCaptor.forClass(UserSession.class);
        verify(sessionRepository).save(saved.capture());
        return saved.getValue();
    }

    @Test
    @DisplayName("l'ouverture ne persiste que l'empreinte du jeton, jamais le jeton lui-même")
    void openSessionStoresOnlyTheHash() {
        when(sessionRepository.save(any(UserSession.class))).thenAnswer(i -> {
            UserSession s = i.getArgument(0);
            s.setSessionId(SESSION_ID);
            return s;
        });
        when(jwtService.issueAccessToken(any(), eq(SESSION_ID))).thenReturn("jwt-acces");

        AuthTokens tokens = newService().openSession(user());

        UserSession saved = captureSaved();
        assertThat(tokens.bearer()).isEqualTo("jwt-acces");
        assertThat(tokens.refresh()).isNotBlank();
        // Une base exfiltrée ne doit pas permettre de rejouer les sessions.
        assertThat(saved.getRefreshTokenHash()).isNotEqualTo(tokens.refresh());
        assertThat(saved.getRefreshTokenHash()).isEqualTo(SessionServiceImpl.hash(tokens.refresh()));
        assertThat(saved.getRefreshTokenHash()).hasSize(64); // SHA-256 en hexadécimal
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getExpiresAt()).isAfter(saved.getIssuedAt());
    }

    @Test
    @DisplayName("deux ouvertures produisent deux jetons distincts")
    void refreshTokensAreUnpredictable() {
        lenient().when(sessionRepository.save(any(UserSession.class))).thenAnswer(i -> {
            UserSession s = i.getArgument(0);
            s.setSessionId(UUID.randomUUID());
            return s;
        });
        lenient().when(jwtService.issueAccessToken(any(), any())).thenReturn("jwt");

        SessionServiceImpl service = newService();
        assertThat(service.openSession(user()).refresh())
                .isNotEqualTo(service.openSession(user()).refresh());
    }

    @Test
    @DisplayName("le rafraîchissement tourne le jeton : l'ancien ne resservira pas")
    void refreshRotatesTheToken() {
        String presented = "jeton-presente";
        UserSession session = openSessionFor(presented);
        when(sessionRepository.findByRefreshTokenHash(SessionServiceImpl.hash(presented)))
                .thenReturn(Optional.of(session));
        when(jwtService.loadUser(USER_ID)).thenReturn(user());
        when(jwtService.issueAccessToken(any(), eq(SESSION_ID))).thenReturn("jwt-neuf");
        when(sessionRepository.save(any(UserSession.class))).thenAnswer(i -> i.getArgument(0));

        AuthTokens rotated = newService().refresh(presented);

        assertThat(rotated.refresh()).isNotEqualTo(presented);
        // L'empreinte stockée est celle du NOUVEAU jeton : rejouer l'ancien ne trouvera plus rien.
        assertThat(captureSaved().getRefreshTokenHash())
                .isEqualTo(SessionServiceImpl.hash(rotated.refresh()))
                .isNotEqualTo(SessionServiceImpl.hash(presented));
    }

    @Test
    @DisplayName("une session révoquée refuse le rafraîchissement, sans dire pourquoi")
    void revokedSessionCannotRefresh() {
        String presented = "jeton-presente";
        UserSession session = openSessionFor(presented);
        session.revoke(Instant.now());
        when(sessionRepository.findByRefreshTokenHash(SessionServiceImpl.hash(presented)))
                .thenReturn(Optional.of(session));

        assertThatThrownBy(() -> newService().refresh(presented))
                .isInstanceOf(BadCredentialsException.class)
                // Message identique à celui d'un jeton inconnu : distinguer les deux renseignerait
                // un attaquant sur la validité d'un jeton en sa possession.
                .hasMessage("Session invalide ou expirée");
        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("une session expirée refuse le rafraîchissement")
    void expiredSessionCannotRefresh() {
        String presented = "jeton-presente";
        UserSession session = openSessionFor(presented);
        session.setExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES));
        when(sessionRepository.findByRefreshTokenHash(SessionServiceImpl.hash(presented)))
                .thenReturn(Optional.of(session));

        assertThatThrownBy(() -> newService().refresh(presented))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Session invalide ou expirée");
    }

    @Test
    @DisplayName("un jeton inconnu, nul ou vide est refusé sans requête inutile")
    void unknownOrMissingTokenIsRejected() {
        SessionServiceImpl service = newService();

        assertThatThrownBy(() -> service.refresh(null)).isInstanceOf(BadCredentialsException.class);
        assertThatThrownBy(() -> service.refresh("  ")).isInstanceOf(BadCredentialsException.class);
        verify(sessionRepository, never()).findByRefreshTokenHash(any());

        when(sessionRepository.findByRefreshTokenHash(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.refresh("inconnu"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Session invalide ou expirée");
    }

    @Test
    @DisplayName("isActive : vrai tant que la session est ouverte, faux dès la révocation")
    void isActiveReflectsRevocation() {
        UserSession session = openSessionFor("t");
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        SessionServiceImpl service = newService();

        assertThat(service.isActive(SESSION_ID)).isTrue();

        session.revoke(Instant.now());
        // C'est cet appel que fait JwtFilter à chaque requête : c'est lui qui rend la
        // déconnexion réelle.
        assertThat(service.isActive(SESSION_ID)).isFalse();
    }

    @Test
    @DisplayName("isActive : un identifiant nul ou inconnu n'autorise pas")
    void isActiveDeniesUnknownSession() {
        SessionServiceImpl service = newService();
        assertThat(service.isActive(null)).isFalse();

        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());
        // Un jeton émis avant l'introduction des sessions n'en porte aucune : il doit être refusé,
        // sans quoi les anciens jetons resteraient utilisables et échapperaient à la révocation.
        assertThat(service.isActive(SESSION_ID)).isFalse();
    }

    @Test
    @DisplayName("la révocation est idempotente et conserve l'horodatage initial")
    void revokeIsIdempotent() {
        UserSession session = openSessionFor("t");
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        SessionServiceImpl service = newService();

        service.revoke(SESSION_ID);
        Instant first = session.getRevokedAt();
        service.revoke(SESSION_ID);

        assertThat(first).isNotNull();
        // Se déconnecter deux fois n'est pas une erreur, et ne réécrit pas la date de fermeture.
        assertThat(session.getRevokedAt()).isEqualTo(first);
    }

    @Test
    @DisplayName("revokeAllForUser ferme toutes les sessions ouvertes et rend leur nombre")
    void revokeAllClosesEveryOpenSession() {
        UserSession a = openSessionFor("a");
        UserSession b = openSessionFor("b");
        when(sessionRepository.findByUserIdAndRevokedAtIsNull(USER_ID)).thenReturn(List.of(a, b));

        assertThat(newService().revokeAllForUser(USER_ID)).isEqualTo(2);

        assertThat(a.getRevokedAt()).isNotNull();
        assertThat(b.getRevokedAt()).isNotNull();
        verify(sessionRepository).saveAll(List.of(a, b));
    }
}
