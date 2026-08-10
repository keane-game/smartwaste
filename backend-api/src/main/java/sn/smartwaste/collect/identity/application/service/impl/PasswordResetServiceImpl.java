package sn.smartwaste.collect.identity.application.service.impl;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.identity.application.service.PasswordResetService;
import sn.smartwaste.collect.identity.application.service.SessionService;
import sn.smartwaste.collect.identity.domain.model.PasswordResetToken;
import sn.smartwaste.collect.identity.domain.repository.PasswordResetTokenRepository;
import sn.smartwaste.collect.identity.domain.repository.UserRepository;
import sn.smartwaste.collect.shared.domain.event.PasswordResetRequested;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;

/**
 * Réinitialisation de mot de passe (ADR-0021, pont avant Keycloak).
 *
 * <p>Même choix de jeton que {@link SessionServiceImpl} et pour la même raison : un secret aléatoire
 * de 256 bits, stocké haché (SHA-256, réutilisé depuis {@link SessionServiceImpl#hash}) — jamais en
 * clair en base. La différence est la durée de vie, volontairement courte : c'est un secret qui
 * transite par e-mail, pas un jeton de session applicatif.
 */
@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    /** 256 bits d'entropie : hors de portée d'une énumération, même sur une fenêtre courte. */
    private static final int TOKEN_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final ApplicationEventPublisher eventPublisher;
    private final Duration ttl;

    public PasswordResetServiceImpl(UserRepository userRepository,
                                    PasswordResetTokenRepository tokenRepository,
                                    BCryptPasswordEncoder passwordEncoder,
                                    SessionService sessionService,
                                    ApplicationEventPublisher eventPublisher,
                                    @Value("${sonaged.security.password-reset.ttl-minutes:60}") long ttlMinutes) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
        this.eventPublisher = eventPublisher;
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    /**
     * Silencieux par construction : {@code ifPresent} est la seule branche. Répondre différemment
     * pour un e-mail inconnu renseignerait un appelant anonyme sur les comptes existants — même
     * raisonnement que {@code SessionServiceImpl.refresh} pour un jeton invalide.
     */
    @Override
    @Transactional
    public void requestReset(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        userRepository.findByUserEmail(email).ifPresent(user -> {
            // Un jeton non consommé issu d'une demande précédente resterait valide en parallèle du
            // nouveau : invalider tout ce qui traîne évite qu'un lien e-mail oublié dans une boîte
            // de réception reste exploitable indéfiniment.
            var previous = tokenRepository.findByUserIdAndUsedAtIsNull(user.getUserId());
            Instant now = Instant.now();
            previous.forEach(t -> t.setUsedAt(now));
            tokenRepository.saveAll(previous);

            String rawToken = newToken();
            PasswordResetToken token = new PasswordResetToken();
            token.setUserId(user.getUserId());
            token.setTokenHash(SessionServiceImpl.hash(rawToken));
            token.setExpiresAt(now.plus(ttl));
            tokenRepository.save(token);

            eventPublisher.publishEvent(new PasswordResetRequested(
                    user.getUserEmail(), user.getUserLastname(), rawToken));
        });
    }

    @Override
    @Transactional
    public void confirmReset(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new ResourceNotFoundException("Jeton de réinitialisation invalide ou expiré");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new ResourceNotFoundException("Le nouveau mot de passe est obligatoire");
        }

        // Message volontairement identique pour un jeton inconnu, déjà utilisé ou expiré :
        // distinguer les cas renseignerait un attaquant sur la validité d'un jeton intercepté.
        PasswordResetToken token = tokenRepository.findByTokenHash(SessionServiceImpl.hash(rawToken))
                .orElseThrow(() -> new ResourceNotFoundException("Jeton de réinitialisation invalide ou expiré"));
        if (!token.isUsable(Instant.now())) {
            throw new ResourceNotFoundException("Jeton de réinitialisation invalide ou expiré");
        }

        var user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur inconnu"));
        user.setUserPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        tokenRepository.save(token);

        // Un mot de passe oublié puis réinitialisé doit fermer tout accès obtenu entre-temps avec
        // l'ancien — même geste que le changement de mot de passe self-service.
        sessionService.revokeAllForUser(user.getUserId());
    }

    private static String newToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
