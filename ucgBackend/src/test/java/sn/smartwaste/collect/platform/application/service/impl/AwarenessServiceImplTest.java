package sn.smartwaste.collect.platform.application.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.platform.application.service.PushNotificationService;
import sn.smartwaste.collect.platform.domain.model.AwarenessMessage;
import sn.smartwaste.collect.platform.domain.model.CollectionSubscription;
import sn.smartwaste.collect.platform.domain.repository.AwarenessMessageRepository;
import sn.smartwaste.collect.platform.domain.repository.CollectionSubscriptionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Messages de sensibilisation (G3 du backlog).
 *
 * <p>Le mémoire les liste parmi les fonctionnalités du système (§3.1.5.2) et cite le manque de
 * sensibilisation comme <b>cause</b> du problème. Rien n'existait.
 *
 * <p><b>Ce qui décide de la qualité de cette fonction n'est pas l'envoi.</b> Une fonction de
 * diffusion de masse est aussi une fonction de nuisance : un citoyen qu'on ne peut pas faire taire
 * désinstalle l'application, et l'outil perd d'un coup tous ses destinataires. D'où un <b>plafond
 * de fréquence</b> par territoire, une <b>trace</b> de ce qui est parti, et le respect de
 * l'abonnement existant — se désabonner d'un quartier suffit à ne plus rien recevoir.
 */
@ExtendWith(MockitoExtension.class)
class AwarenessServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-08-05T10:00:00Z");
    private static final UUID QUARTIER = UUID.randomUUID();
    private static final UUID AUTEUR = UUID.randomUUID();
    private static final int PLAFOND_HEURES = 24;

    @Mock private AwarenessMessageRepository messageRepository;
    @Mock private CollectionSubscriptionRepository subscriptionRepository;
    @Mock private PushNotificationService push;
    @Mock private CurrentUserProvider currentUserProvider;

    private AwarenessServiceImpl service() {
        return new AwarenessServiceImpl(messageRepository, subscriptionRepository, push,
                currentUserProvider, Clock.fixed(NOW, ZoneId.of("UTC")), PLAFOND_HEURES);
    }

    private CollectionSubscription abonne(UUID userId) {
        var s = new CollectionSubscription();
        s.setUserId(userId);
        s.setQuartierId(QUARTIER);
        s.setActive(true);
        return s;
    }

    private AwarenessMessage message(UUID quartierId, Instant scheduledAt) {
        var m = new AwarenessMessage();
        m.setMessageId(UUID.randomUUID());
        m.setTitle("Trions nos dechets");
        m.setBody("Un bac plein n'est pas une decharge.");
        m.setQuartierId(quartierId);
        m.setScheduledAt(scheduledAt);
        return m;
    }

    @Test
    @DisplayName("un message programme est enregistre, pas envoye tout de suite")
    void schedulingDoesNotSendImmediately() {
        lenient().when(currentUserProvider.requireCurrentUserId()).thenReturn(AUTEUR);
        when(messageRepository.findByQuartierIdAndScheduledAtGreaterThanEqual(any(), any()))
                .thenReturn(List.of());

        service().schedule("Trions nos dechets", "Un bac plein n'est pas une decharge.",
                QUARTIER, NOW.plusSeconds(3600));

        var enregistre = ArgumentCaptor.forClass(AwarenessMessage.class);
        verify(messageRepository).save(enregistre.capture());
        assertThat(enregistre.getValue().getSentAt()).isNull();
        assertThat(enregistre.getValue().getAuthorId()).isEqualTo(AUTEUR);
        verify(push, never()).notify(anyList(), anyString(), anyString());
    }

    @Test
    @DisplayName("un second message vers le meme quartier dans la journee est refuse")
    void frequencyCapProtectsTheCitizen() {
        // Le garde-fou qui compte : une fonction d'envoi de masse est aussi une fonction de
        // nuisance, et un citoyen qu'on ne peut pas faire taire desinstalle l'application.
        lenient().when(currentUserProvider.requireCurrentUserId()).thenReturn(AUTEUR);
        when(messageRepository.findByQuartierIdAndScheduledAtGreaterThanEqual(any(), any()))
                .thenReturn(List.of(message(QUARTIER, NOW.minusSeconds(3600))));

        assertThatThrownBy(() -> service().schedule("Encore", "un message", QUARTIER, NOW))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("24");

        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("un message du est envoye aux abonnes du quartier, et trace")
    void dueMessageReachesSubscribersAndIsRecorded() {
        var du = message(QUARTIER, NOW.minusSeconds(60));
        when(messageRepository.findBySentAtIsNullAndScheduledAtLessThanEqual(NOW))
                .thenReturn(List.of(du));
        var citoyen = UUID.randomUUID();
        when(subscriptionRepository.findByQuartierIdAndActiveTrue(QUARTIER))
                .thenReturn(List.of(abonne(citoyen)));
        when(push.notify(List.of(citoyen), "Trions nos dechets",
                "Un bac plein n'est pas une decharge.")).thenReturn(3);

        service().dispatchDue();

        // La trace porte ce qui a ete FAIT — appareils joints — et non ce qui etait vise.
        assertThat(du.getSentAt()).isEqualTo(NOW);
        assertThat(du.getRecipientCount()).isEqualTo(3);
        verify(messageRepository).save(du);
    }

    @Test
    @DisplayName("un message deja parti n'est jamais renvoye")
    void anAlreadySentMessageIsNotResent() {
        // `sentAt` est la seule garde : sans elle, chaque passage du planificateur rediffuserait
        // tout l'historique.
        when(messageRepository.findBySentAtIsNullAndScheduledAtLessThanEqual(NOW))
                .thenReturn(List.of());

        service().dispatchDue();

        verify(push, never()).notify(anyList(), anyString(), anyString());
        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("un message sans quartier vise tous les abonnes")
    void messageWithoutQuartierReachesEveryone() {
        var du = message(null, NOW.minusSeconds(60));
        when(messageRepository.findBySentAtIsNullAndScheduledAtLessThanEqual(NOW))
                .thenReturn(List.of(du));
        var a = UUID.randomUUID();
        var b = UUID.randomUUID();
        when(subscriptionRepository.findByActiveTrue())
                .thenReturn(List.of(abonne(a), abonne(b)));
        when(push.notify(anyList(), anyString(), anyString())).thenReturn(2);

        service().dispatchDue();

        assertThat(du.getRecipientCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("un quartier sans abonne ne fait pas echouer la diffusion")
    void emptyAudienceIsNotAFailure() {
        // Cas majoritaire aujourd'hui : presque personne n'est abonne.
        var du = message(QUARTIER, NOW.minusSeconds(60));
        when(messageRepository.findBySentAtIsNullAndScheduledAtLessThanEqual(NOW))
                .thenReturn(List.of(du));
        when(subscriptionRepository.findByQuartierIdAndActiveTrue(QUARTIER)).thenReturn(List.of());

        service().dispatchDue();

        // Marque comme parti : sinon le planificateur le reprendrait indefiniment.
        assertThat(du.getSentAt()).isEqualTo(NOW);
        assertThat(du.getRecipientCount()).isZero();
        verify(push, never()).notify(anyList(), anyString(), anyString());
    }
}
