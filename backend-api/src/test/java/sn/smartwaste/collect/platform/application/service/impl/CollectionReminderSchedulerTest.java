package sn.smartwaste.collect.platform.application.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.smartwaste.collect.identity.application.api.UserDirectory;
import sn.smartwaste.collect.platform.application.service.NotificationService;
import sn.smartwaste.collect.platform.domain.model.CollectionSubscription;
import sn.smartwaste.collect.platform.domain.repository.CollectionSubscriptionRepository;
import sn.smartwaste.collect.waste.application.api.WasteReadModel;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Rappel de collecte aux habitants.
 *
 * <p>Ce qui est verrouille ici decide si le service est utile ou devient une nuisance. Les
 * repondants a l'enquete reprochent precisement au klaxon des camions d'etre intrusif : un rappel
 * envoye en double, ou apres le passage, reproduirait le defaut qu'on cherche a corriger.
 */
@ExtendWith(MockitoExtension.class)
class CollectionReminderSchedulerTest {

    private static final UUID QUARTIER = UUID.randomUUID();
    private static final int LEAD_MINUTES = 90;
    /** Mardi 2026-07-28, 08:00. */
    private static final ZoneId ZONE = ZoneId.systemDefault();

    @Mock
    private WasteReadModel wasteReadModel;
    @Mock
    private CollectionSubscriptionRepository subscriptionRepository;

    /** G2 : le rappel part aussi vers les appareils. Le comportement du canal est
     *  verifie separement (PushNotificationServiceImplTest). */
    @Mock
    private sn.smartwaste.collect.platform.application.service.PushNotificationService pushNotificationService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private UserDirectory userDirectory;

    private Clock clockAt(String time) {
        LocalDateTime moment = LocalDateTime.parse("2026-07-28T" + time);
        return Clock.fixed(moment.atZone(ZONE).toInstant(), ZONE);
    }

    private CollectionReminderScheduler schedulerAt(String time) {
        return new CollectionReminderScheduler(wasteReadModel, subscriptionRepository, pushNotificationService,
                notificationService, userDirectory, clockAt(time), LEAD_MINUTES);
    }

    private void scheduled(String passageTime) {
        when(wasteReadModel.collectionsScheduledOn(any())).thenReturn(
                List.of(new WasteReadModel.ScheduledCollection(QUARTIER, LocalTime.parse(passageTime))));
    }

    /** Abonnés du quartier ; leur adresse est résolue à l'envoi, jamais stockée avec l'abonnement. */
    private void subscribers(String... emails) {
        List<CollectionSubscription> list = new java.util.ArrayList<>();
        for (String email : emails) {
            var s = new CollectionSubscription();
            s.setQuartierId(QUARTIER);
            s.setUserId(UUID.randomUUID());
            list.add(s);
        }
        // Stubbing en deux temps : imbriquer un when() dans un autre fait echouer Mockito.
        for (int i = 0; i < list.size(); i++) {
            lenient().when(userDirectory.emailOf(list.get(i).getUserId()))
                    .thenReturn(Optional.of(emails[i]));
        }
        lenient().when(subscriptionRepository.findByQuartierIdAndActiveTrue(QUARTIER)).thenReturn(list);
    }

    @Test
    @DisplayName("un passage dans la fenetre previent tous les abonnes du quartier")
    void upcomingPassageNotifiesSubscribers() {
        scheduled("09:00");                 // dans 60 min, fenetre = 90 min
        subscribers("awa@example.sn", "moussa@example.sn");

        schedulerAt("08:00").sendUpcomingReminders();

        verify(notificationService).sendCollectionReminder(eq("awa@example.sn"), eq(LocalTime.of(9, 0)));
        verify(notificationService).sendCollectionReminder(eq("moussa@example.sn"), eq(LocalTime.of(9, 0)));
    }

    @Test
    @DisplayName("un passage deja effectue ne declenche rien")
    void pastPassageIsIgnored() {
        scheduled("07:00");                 // il y a une heure
        subscribers("awa@example.sn");

        schedulerAt("08:00").sendUpcomingReminders();

        // Prevenir apres coup est pire que ne rien envoyer : l'habitant a rate le camion.
        verify(notificationService, never()).sendCollectionReminder(any(), any());
    }

    @Test
    @DisplayName("un passage encore trop lointain attend la prochaine execution")
    void farAwayPassageWaits() {
        scheduled("14:00");                 // dans 6 h
        subscribers("awa@example.sn");

        schedulerAt("08:00").sendUpcomingReminders();

        // Prevenir 6 h a l'avance ne sert a rien : l'habitant aura oublie.
        verify(notificationService, never()).sendCollectionReminder(any(), any());
    }

    @Test
    @DisplayName("deux executations dans la meme fenetre n'envoient qu'un seul rappel")
    void reminderIsNotSentTwice() {
        scheduled("09:00");
        subscribers("awa@example.sn");
        var scheduler = schedulerAt("08:00");

        // La tache tourne toutes les 15 min, la fenetre en fait 90 : sans garde, le meme passage
        // serait notifie six fois de suite.
        scheduler.sendUpcomingReminders();
        scheduler.sendUpcomingReminders();
        scheduler.sendUpcomingReminders();

        verify(notificationService, times(1)).sendCollectionReminder(any(), any());
    }

    @Test
    @DisplayName("sans abonne, aucun envoi et aucune erreur")
    void noSubscriberMeansNoSend() {
        scheduled("09:00");
        subscribers();

        schedulerAt("08:00").sendUpcomingReminders();

        verify(notificationService, never()).sendCollectionReminder(any(), any());
    }

    @Test
    @DisplayName("un abonne dont le compte a disparu ne recoit rien, et n'interrompt pas les autres")
    void subscriptionWithoutAccountIsSkipped() {
        scheduled("09:00");
        var orphan = new CollectionSubscription();
        orphan.setQuartierId(QUARTIER);
        orphan.setUserId(UUID.randomUUID());
        when(subscriptionRepository.findByQuartierIdAndActiveTrue(QUARTIER)).thenReturn(List.of(orphan));
        when(userDirectory.emailOf(orphan.getUserId())).thenReturn(Optional.empty());

        // Un abonnement peut survivre a la suppression de son titulaire : on s'abstient
        // plutot que d'echouer et de bloquer les rappels des autres quartiers.
        schedulerAt("08:00").sendUpcomingReminders();

        verify(notificationService, never()).sendCollectionReminder(any(), any());
    }
}
