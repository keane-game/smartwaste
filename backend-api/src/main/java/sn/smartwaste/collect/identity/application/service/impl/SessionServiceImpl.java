package sn.smartwaste.collect.identity.application.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.identity.application.dto.AuthTokens;
import sn.smartwaste.collect.identity.application.service.SessionService;
import sn.smartwaste.collect.identity.domain.model.UserEntity;
import sn.smartwaste.collect.identity.domain.model.UserSession;
import sn.smartwaste.collect.identity.domain.repository.UserSessionRepository;
import sn.smartwaste.collect.identity.infrastructure.security.JwtService;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;

/**
 * Implémentation des sessions.
 *
 * <p>Deux choix méritent d'être explicités.
 *
 * <p><b>Le jeton de rafraîchissement est opaque, pas un JWT.</b> Un JWT se vérifie sans la base,
 * ce qui est précisément ce qu'on ne veut pas ici : c'est ce jeton-là qui doit être révocable. Un
 * secret aléatoire de 256 bits, stocké haché, oblige à passer par la base — donc à constater une
 * révocation.
 *
 * <p><b>Rotation à chaque rafraîchissement.</b> Le jeton présenté est révoqué et remplacé. Si un
 * jeton volé est utilisé, la première des deux parties à s'en servir invalide l'autre : l'anomalie
 * devient visible (le titulaire légitime est déconnecté) au lieu de rester silencieuse.
 */
@Service
@Transactional
public class SessionServiceImpl implements SessionService {

    /** 256 bits d'entropie : hors de portée d'une énumération. */
    private static final int REFRESH_TOKEN_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserSessionRepository sessionRepository;
    private final JwtService jwtService;
    private final Duration refreshTokenTtl;

    public SessionServiceImpl(UserSessionRepository sessionRepository,
                              JwtService jwtService,
                              @Value("${sonaged.security.session.refresh-ttl-days:30}") long refreshTtlDays) {
        this.sessionRepository = sessionRepository;
        this.jwtService = jwtService;
        this.refreshTokenTtl = Duration.ofDays(refreshTtlDays);
    }

    @Override
    public AuthTokens openSession(UserEntity user) {
        Instant now = Instant.now();
        String refreshToken = newRefreshToken();

        UserSession session = new UserSession();
        session.setUserId(user.getUserId());
        session.setRefreshTokenHash(hash(refreshToken));
        session.setIssuedAt(now);
        session.setExpiresAt(now.plus(refreshTokenTtl));
        session.setLastUsedAt(now);
        UserSession saved = sessionRepository.save(session);

        return new AuthTokens(jwtService.issueAccessToken(user, saved.getSessionId()), refreshToken);
    }

    /**
     * Échoue en {@code 401}, jamais en {@code 404}.
     *
     * <p>Un jeton de rafraîchissement inconnu, expiré ou déjà rejoué ne décrit pas une ressource
     * absente : il décrit un appelant qui n'est pas (ou plus) authentifié. Le {@code 404} que
     * produisait {@code ResourceNotFoundException} était trompeur pour le client — et il l'était
     * pour l'utilisateur : le front n'y reconnaissait pas une fin de session, affichait une popup
     * « Requête refusée par le serveur » et laissait les appels périodiques réessayer en boucle.
     */
    @Override
    public AuthTokens refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadCredentialsException("Session invalide ou expirée");
        }
        Instant now = Instant.now();
        UserSession session = sessionRepository.findByRefreshTokenHash(hash(refreshToken))
                // Message volontairement identique dans tous les cas d'échec : distinguer
                // « inconnu » de « expiré » renseignerait un attaquant sur la validité d'un jeton.
                .orElseThrow(() -> new BadCredentialsException("Session invalide ou expirée"));

        if (!session.isActive(now)) {
            throw new BadCredentialsException("Session invalide ou expirée");
        }

        UserEntity user = jwtService.loadUser(session.getUserId());

        // Rotation : le jeton présenté ne resservira pas.
        String rotated = newRefreshToken();
        session.setRefreshTokenHash(hash(rotated));
        session.setLastUsedAt(now);
        sessionRepository.save(session);

        return new AuthTokens(jwtService.issueAccessToken(user, session.getSessionId()), rotated);
    }

    @Override
    public void revoke(UUID sessionId) {
        if (sessionId == null) {
            return;
        }
        // Idempotent : se déconnecter deux fois n'est pas une erreur.
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.revoke(Instant.now());
            sessionRepository.save(session);
        });
    }

    @Override
    public int revokeAllForUser(UUID userId) {
        Instant now = Instant.now();
        List<UserSession> open = sessionRepository.findByUserIdAndRevokedAtIsNull(userId);
        open.forEach(session -> session.revoke(now));
        sessionRepository.saveAll(open);
        return open.size();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isActive(UUID sessionId) {
        if (sessionId == null) {
            return false;
        }
        return sessionRepository.findById(sessionId)
                .map(session -> session.isActive(Instant.now()))
                .orElse(false);
    }

    private static String newRefreshToken() {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * SHA-256 sans sel, volontairement : le jeton est déjà 256 bits d'aléa cryptographique, donc
     * hors de portée d'un dictionnaire ou d'une table arc-en-ciel. Un hachage lent type bcrypt
     * n'apporterait rien ici et coûterait à chaque rafraîchissement — le raisonnement qui vaut
     * pour un mot de passe (faible entropie) ne s'applique pas.
     */
    static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible sur cette JVM", e);
        }
    }
}
