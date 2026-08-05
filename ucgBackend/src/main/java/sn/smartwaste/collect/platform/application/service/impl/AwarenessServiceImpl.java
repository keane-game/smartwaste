package sn.smartwaste.collect.platform.application.service.impl;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.platform.application.service.AwarenessService;
import sn.smartwaste.collect.platform.application.service.PushNotificationService;
import sn.smartwaste.collect.platform.domain.model.AwarenessMessage;
import sn.smartwaste.collect.platform.domain.model.CollectionSubscription;
import sn.smartwaste.collect.platform.domain.repository.AwarenessMessageRepository;
import sn.smartwaste.collect.platform.domain.repository.CollectionSubscriptionRepository;

/**
 * Programme et diffuse les messages de sensibilisation (G3 du backlog).
 *
 * <p><b>Ce qui décide de la qualité de cette fonction n'est pas l'envoi.</b> Une fonction de
 * diffusion de masse est aussi une fonction de nuisance : un citoyen qu'on ne peut pas faire taire
 * désinstalle l'application, et l'outil perd d'un coup tous ses destinataires — y compris pour les
 * rappels de collecte, qui eux sont utiles. Trois garde-fous en découlent :
 *
 * <p><b>1. Un plafond de fréquence par territoire.</b> Un second message vers le même quartier dans
 * la fenêtre est refusé, quel qu'en soit l'auteur.
 *
 * <p><b>2. L'abonnement existant fait foi.</b> Le message suit les abonnements aux quartiers : se
 * désabonner suffit à ne plus rien recevoir. Aucun canal parallèle qu'on ne pourrait pas fermer.
 *
 * <p><b>3. Ce qui est parti est tracé.</b> Le message survit à son envoi, avec le nombre d'appareils
 * réellement joints — ce qui a été fait, et non ce qui était visé. Un envoi qui ne laisse aucune
 * trace ne se corrige pas et ne se défend pas.
 */
@Service
public class AwarenessServiceImpl implements AwarenessService {

    private static final Logger log = LoggerFactory.getLogger(AwarenessServiceImpl.class);

    private final AwarenessMessageRepository messageRepository;
    private final CollectionSubscriptionRepository subscriptionRepository;
    private final PushNotificationService push;
    private final CurrentUserProvider currentUserProvider;
    private final Clock clock;
    private final int frequencyCapHours;

    public AwarenessServiceImpl(AwarenessMessageRepository messageRepository,
                                CollectionSubscriptionRepository subscriptionRepository,
                                PushNotificationService push,
                                CurrentUserProvider currentUserProvider,
                                Clock clock,
                                @Value("${sonaged.awareness.frequency-cap-hours:24}") int frequencyCapHours) {
        this.messageRepository = messageRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.push = push;
        this.currentUserProvider = currentUserProvider;
        this.clock = clock;
        this.frequencyCapHours = frequencyCapHours;
    }

    @Override
    @Transactional
    public AwarenessMessage schedule(String title, String body, UUID quartierId,
                                     Instant scheduledAt) {
        Instant depuis = Instant.now(clock).minus(Duration.ofHours(frequencyCapHours));
        if (!messageRepository.findByQuartierIdAndScheduledAtGreaterThanEqual(quartierId, depuis)
                .isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Un message a deja ete adresse a ce territoire dans les %d dernieres heures"
                            .formatted(frequencyCapHours));
        }

        var message = new AwarenessMessage();
        message.setTitle(title);
        message.setBody(body);
        message.setQuartierId(quartierId);
        message.setScheduledAt(scheduledAt == null ? Instant.now(clock) : scheduledAt);
        message.setAuthorId(currentUserProvider.requireCurrentUserId());
        return messageRepository.save(message);
    }

    @Override
    @Transactional
    public void dispatchDue() {
        Instant now = Instant.now(clock);
        for (AwarenessMessage message : messageRepository
                .findBySentAtIsNullAndScheduledAtLessThanEqual(now)) {

            var destinataires = audienceOf(message);
            int joints = destinataires.isEmpty() ? 0
                    : push.notify(destinataires, message.getTitle(), message.getBody());

            // Marque comme parti MEME sans destinataire : sinon le planificateur le reprendrait a
            // chaque passage, indefiniment.
            message.setSentAt(now);
            message.setRecipientCount(joints);
            messageRepository.save(message);

            log.info("Sensibilisation « {} » diffusee : {} appareil(s) joint(s)",
                    message.getTitle(), joints);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AwarenessMessage> receivedBy(UUID userId) {
        // Ce que le citoyen a pu recevoir : les messages de ses quartiers, et ceux adresses a tous.
        var quartiers = new java.util.ArrayList<UUID>();
        quartiers.add(null);
        subscriptionRepository.findByActiveTrue().stream()
                .filter(s -> userId.equals(s.getUserId()))
                .map(CollectionSubscription::getQuartierId)
                .forEach(quartiers::add);
        return messageRepository
                .findBySentAtIsNotNullAndQuartierIdInOrderBySentAtDesc(quartiers);
    }

    /** Les abonnés visés : ceux du quartier, ou tous si le message ne cible personne en particulier. */
    private List<UUID> audienceOf(AwarenessMessage message) {
        var abonnements = message.getQuartierId() == null
                ? subscriptionRepository.findByActiveTrue()
                : subscriptionRepository.findByQuartierIdAndActiveTrue(message.getQuartierId());
        return abonnements.stream().map(CollectionSubscription::getUserId).distinct().toList();
    }
}
