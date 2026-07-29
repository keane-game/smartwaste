package sn.smartwaste.collect.platform.application.service.impl;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.platform.domain.model.CollectionSubscription;
import sn.smartwaste.collect.platform.domain.repository.CollectionSubscriptionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Abonnement aux rappels de collecte.
 *
 * <p><b>Ce que ces tests empêchent de revenir.</b> Une première version acceptait l'adresse du
 * destinataire dans le corps de la requête. Un compte authentifié pouvait donc inscrire l'adresse
 * de n'importe qui à des e-mails récurrents, expédiés au nom du service — un vecteur d'envoi non
 * sollicité. Le signalement est venu de la revue de sécurité automatique, sur ce commit.
 *
 * <p>L'abonné, comme le destinataire, est désormais l'utilisateur du jeton, un point c'est tout.
 */
@ExtendWith(MockitoExtension.class)
class CollectionSubscriptionServiceImplTest {

    private static final UUID USER = UUID.randomUUID();
    private static final UUID QUARTIER = UUID.randomUUID();

    @Mock
    private CollectionSubscriptionRepository repository;
    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private CollectionSubscriptionServiceImpl service;

    private CollectionSubscription captureSaved() {
        ArgumentCaptor<CollectionSubscription> saved = ArgumentCaptor.forClass(CollectionSubscription.class);
        verify(repository).save(saved.capture());
        return saved.getValue();
    }

    @Test
    @DisplayName("l'abonnement est rattaché à l'utilisateur du jeton, et ne porte aucune adresse")
    void subscriptionBelongsToAuthenticatedUserAndCarriesNoEmail() {
        when(currentUserProvider.requireCurrentUserId()).thenReturn(USER);
        when(repository.findByUserIdAndQuartierId(USER, QUARTIER)).thenReturn(Optional.empty());

        service.subscribe(QUARTIER);

        CollectionSubscription saved = captureSaved();
        assertThat(saved.getUserId()).isEqualTo(USER);
        assertThat(saved.getQuartierId()).isEqualTo(QUARTIER);
        assertThat(saved.isActive()).isTrue();
        // L'entité n'a plus de champ e-mail du tout : l'adresse est résolue à l'envoi auprès du
        // contexte identité. C'est ce qui rend impossible d'abonner l'adresse d'un tiers.
        assertThat(CollectionSubscription.class.getDeclaredFields())
                .noneMatch(f -> f.getName().toLowerCase().contains("mail"));
    }

    @Test
    @DisplayName("se réabonner réactive l'abonnement existant au lieu d'en créer un doublon")
    void resubscribingReactivatesInsteadOfDuplicating() {
        var existing = new CollectionSubscription();
        existing.setUserId(USER);
        existing.setQuartierId(QUARTIER);
        existing.setActive(false);
        when(currentUserProvider.requireCurrentUserId()).thenReturn(USER);
        when(repository.findByUserIdAndQuartierId(USER, QUARTIER)).thenReturn(Optional.of(existing));

        service.subscribe(QUARTIER);

        // Sans cela, le réabonnement échouerait sur la contrainte d'unicité (userId, quartierId).
        assertThat(captureSaved()).isSameAs(existing);
        assertThat(existing.isActive()).isTrue();
    }

    @Test
    @DisplayName("le désabonnement désactive sans effacer, et reste idempotent")
    void unsubscribeDeactivatesAndIsIdempotent() {
        var existing = new CollectionSubscription();
        existing.setActive(true);
        when(currentUserProvider.requireCurrentUserId()).thenReturn(USER);
        when(repository.findByUserIdAndQuartierId(USER, QUARTIER)).thenReturn(Optional.of(existing));

        service.unsubscribe(QUARTIER);

        assertThat(existing.isActive()).isFalse();
        verify(repository).save(existing);
    }

    @Test
    @DisplayName("se désabonner d'un quartier non suivi ne fait rien et ne lève pas")
    void unsubscribeUnknownIsNoOp() {
        when(currentUserProvider.requireCurrentUserId()).thenReturn(USER);
        when(repository.findByUserIdAndQuartierId(USER, QUARTIER)).thenReturn(Optional.empty());

        service.unsubscribe(QUARTIER);

        verify(repository, org.mockito.Mockito.never()).save(any());
    }
}
