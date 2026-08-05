package sn.smartwaste.collect.identity.application.service.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import sn.smartwaste.collect.shared.domain.event.ActivationCodeIssued;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.identity.domain.model.UserEntity;
import sn.smartwaste.collect.identity.domain.model.Validation;
import sn.smartwaste.collect.identity.domain.repository.ValidationRepository;
import sn.smartwaste.collect.identity.application.service.ValidationService;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * Émission et vérification des codes d'activation (contexte <b>Identité &amp; Accès</b>).
 *
 * <p>P1-7b : ce service ne dépend plus de {@code NotificationService}. Il publie
 * {@link ActivationCodeIssued} ; c'est le contexte Communication qui décide d'en faire un
 * e-mail. Le cycle Identité ↔ Communication est ainsi rompu, sans changement de comportement
 * observable (l'écouteur est synchrone).
 */
@Service
public class ValidationServiceImpl implements ValidationService {

    /** Validité du code (~972 jours) — valeur historique conservée telle quelle. */
    private static final long CODE_VALIDITY_SECONDS = 84_000_000L;

    /**
     * {@link SecureRandom} plutôt que {@link java.util.Random} : le code d'activation est un
     * secret. Un générateur non cryptographique est prédictible à partir de quelques valeurs
     * observées, ce qui permettrait d'activer le compte d'un tiers.
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ValidationRepository validationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ValidationServiceImpl(ValidationRepository validationRepository,
                                 ApplicationEventPublisher eventPublisher) {
        this.validationRepository = validationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void registerUserCode(UserEntity user) {
        Instant creation = Instant.now();

        Validation validation = new Validation();
        validation.setUser(user);
        validation.setCreation(creation);
        validation.setExpiration(creation.plusSeconds(CODE_VALIDITY_SECONDS));
        // nextInt(1_000_000) et non nextInt(999999) : l'ancienne borne excluait 999999.
        validation.setCode(String.format("%06d", RANDOM.nextInt(1_000_000)));

        this.validationRepository.save(validation);

        this.eventPublisher.publishEvent(new ActivationCodeIssued(
                user.getUserEmail(),
                user.getUserLastname(),
                validation.getCode()
        ));
    }

    @Override
    public Validation readByCode(String code) {
        return this.validationRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Votre code est invalide"));
    }
}
