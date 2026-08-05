package sn.smartwaste.collect.platform.application.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.platform.application.service.PushNotificationService;
import sn.smartwaste.collect.platform.domain.model.DeviceToken;
import sn.smartwaste.collect.platform.domain.repository.DeviceTokenRepository;
import sn.smartwaste.collect.platform.infrastructure.notification.PushTransport;
import sn.smartwaste.collect.shared.domain.model.DeletionStatus;

/**
 * Pousse une notification vers les appareils d'un citoyen (G2 du backlog).
 *
 * <p><b>Deux règles décident si la fonction est utilisable</b>, et elles sont plus importantes que
 * l'envoi lui-même :
 *
 * <p><b>1. Un échec n'interrompt rien.</b> Le rappel « sortez vos ordures » vise tout un quartier ;
 * un seul jeton périmé ne peut pas faire rater les autres. Chaque envoi est donc isolé.
 *
 * <p><b>2. Un jeton déclaré invalide est révoqué, un échec temporaire ne l'est pas.</b> Confondre
 * « cet appareil a disparu » et « le réseau est tombé » désabonnerait des citoyens à la première
 * panne du fournisseur. Sans la révocation, à l'inverse, la file d'envoi se remplit d'appareils
 * désinstallés et le taux d'échec cesse de vouloir dire quelque chose.
 */
@Service
public class PushNotificationServiceImpl implements PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationServiceImpl.class);

    private final DeviceTokenRepository tokenRepository;
    private final PushTransport transport;

    public PushNotificationServiceImpl(DeviceTokenRepository tokenRepository,
                                       PushTransport transport) {
        this.tokenRepository = tokenRepository;
        this.transport = transport;
    }

    @Override
    @Transactional
    public void register(UUID userId, String token, String platform) {
        // Une reinstallation redonne souvent le meme jeton : on le rattache plutot que d'en creer
        // un second, qui ferait sonner l'appareil deux fois.
        var appareil = tokenRepository.findByToken(token).orElseGet(DeviceToken::new);
        appareil.setUserId(userId);
        appareil.setToken(token);
        appareil.setPlatform(platform);
        appareil.restore();
        tokenRepository.save(appareil);
    }

    @Override
    @Transactional
    public void revoke(UUID userId, String token) {
        tokenRepository.findByToken(token)
                .filter(t -> userId.equals(t.getUserId()))
                .ifPresent(t -> {
                    t.markForDeletion(java.time.LocalDateTime.now());
                    tokenRepository.save(t);
                });
    }

    @Override
    @Transactional
    public int notify(List<UUID> userIds, String title, String body) {
        if (userIds.isEmpty()) {
            return 0;
        }
        var appareils = tokenRepository.findByUserIdInAndDeletionStatus(
                userIds, DeletionStatus.ACTIVE);

        int joints = 0;
        for (DeviceToken appareil : appareils) {
            PushTransport.Result resultat;
            try {
                resultat = transport.send(appareil.getToken(), title, body);
            } catch (RuntimeException e) {
                // Regle 1 : le suivant doit partir quand meme.
                log.warn("Envoi impossible vers un appareil de {} : {}",
                        appareil.getUserId(), e.getMessage());
                continue;
            }

            switch (resultat) {
                case DELIVERED -> {
                    appareil.setLastUsedAt(Instant.now());
                    joints++;
                }
                // Regle 2 : seul un jeton declare inconnu est revoque.
                case INVALID_TOKEN -> {
                    log.info("Jeton invalide revoque pour l'utilisateur {}", appareil.getUserId());
                    appareil.markForDeletion(java.time.LocalDateTime.now());
                    tokenRepository.save(appareil);
                }
                case FAILED -> log.warn("Echec temporaire vers un appareil de {} — jeton conserve",
                        appareil.getUserId());
            }
        }
        return joints;
    }
}
