package sn.smartwaste.collect.platform.application.service.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.identity.application.api.UserDirectory;
import sn.smartwaste.collect.platform.application.service.NotificationService;
import sn.smartwaste.collect.platform.domain.repository.CollectionSubscriptionRepository;
import sn.smartwaste.collect.waste.application.api.WasteReadModel;

/**
 * Prévient les habitants avant le passage du camion.
 *
 * <p>C'est l'aboutissement de l'abonnement : sans ce déclencheur, s'abonner ne produirait rien.
 *
 * <p><b>Pourquoi une fenêtre et non l'heure exacte.</b> Prévenir à l'instant du passage serait
 * inutile — le but est que l'habitant ait le temps de sortir sa poubelle. Le rappel part donc un
 * délai d'avance configurable (90 min par défaut), et la tâche s'exécute à intervalle régulier en
 * ne retenant que les passages tombant dans la fenêtre à venir.
 *
 * <p><b>Anti-répétition.</b> La tâche tourne plus souvent que la fenêtre n'est large : sans garde,
 * le même passage serait notifié à chaque exécution. Les couples (quartier, heure) déjà traités
 * dans la journée sont mémorisés, et la mémoire est purgée au changement de jour — un rappel
 * envoyé deux fois transforme un service utile en nuisance, exactement ce que les répondants
 * reprochent au klaxon.
 */
@Component
public class CollectionReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(CollectionReminderScheduler.class);

    private final WasteReadModel wasteReadModel;
    private final CollectionSubscriptionRepository subscriptionRepository;
    private final sn.smartwaste.collect.platform.application.service.PushNotificationService pushNotificationService;
    private final NotificationService notificationService;
    private final UserDirectory userDirectory;
    private final Clock clock;
    private final int leadTimeMinutes;

    /** Passages déjà notifiés aujourd'hui, pour ne pas prévenir deux fois. */
    private final Set<String> notifiedToday = new HashSet<>();
    private LocalDate notifiedOn;

    public CollectionReminderScheduler(WasteReadModel wasteReadModel,
                                       CollectionSubscriptionRepository subscriptionRepository,
                                       sn.smartwaste.collect.platform.application.service.PushNotificationService pushNotificationService,
                                       NotificationService notificationService,
                                       UserDirectory userDirectory,
                                       Clock clock,
                                       @Value("${sonaged.collection.reminder.lead-minutes:90}") int leadTimeMinutes) {
        this.wasteReadModel = wasteReadModel;
        this.subscriptionRepository = subscriptionRepository;
        this.pushNotificationService = pushNotificationService;
        this.notificationService = notificationService;
        this.userDirectory = userDirectory;
        this.clock = clock;
        this.leadTimeMinutes = leadTimeMinutes;
    }

    @Scheduled(cron = "${sonaged.collection.reminder.cron:0 */15 * * * *}")
    @Transactional(readOnly = true)
    public void sendUpcomingReminders() {
        LocalDate today = LocalDate.now(clock);
        LocalTime now = LocalTime.now(clock);
        LocalTime until = now.plusMinutes(leadTimeMinutes);

        if (!today.equals(notifiedOn)) {
            notifiedToday.clear();
            notifiedOn = today;
        }

        for (var collection : wasteReadModel.collectionsScheduledOn(today.getDayOfWeek())) {
            LocalTime passage = collection.passageTime();
            // Fenêtre [maintenant, maintenant + délai] : on ignore ce qui est déjà passé et ce
            // qui est encore trop loin.
            if (passage.isBefore(now) || passage.isAfter(until)) {
                continue;
            }
            String key = collection.quartierId() + "@" + passage;
            if (!notifiedToday.add(key)) {
                continue;
            }
            var subscribers = subscriptionRepository.findByQuartierIdAndActiveTrue(collection.quartierId());
            // L'adresse est résolue ici, et non figée à l'abonnement : elle appartient au
            // contexte « Identité & Accès », qui seul la détient vérifiée. Un compte supprimé
            // n'a plus d'adresse — on s'abstient plutôt que d'échouer.
            subscribers.forEach(s -> userDirectory.emailOf(s.getUserId())
                    .ifPresent(email -> notificationService.sendCollectionReminder(email, passage)));

            // G2 : le courriel ne suffit pas. Le rappel doit atteindre un telephone dont
            // l'application est fermee — c'est-a-dire le cas normal.
            int appareils = pushNotificationService.notify(
                    subscribers.stream().map(s -> s.getUserId()).toList(),
                    "Sortez vos ordures",
                    "Le camion passera dans votre quartier a " + passage + ".");
            if (!subscribers.isEmpty()) {
                log.info("Rappel de collecte : {} habitant(s) prevenu(s) ({} appareil(s) joint(s)) "
                        + "pour le quartier {} a {}",
                        subscribers.size(), appareils, collection.quartierId(), passage);
            }
        }
    }
}
