package sn.smartwaste.collect.platform.application.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.platform.application.service.CollectionSubscriptionService;
import sn.smartwaste.collect.platform.domain.model.CollectionSubscription;
import sn.smartwaste.collect.platform.domain.repository.CollectionSubscriptionRepository;

/**
 * Gestion des abonnements.
 *
 * <p>L'abonné est <b>toujours</b> l'utilisateur authentifié, jamais un identifiant fourni par le
 * client : sans cela, n'importe qui pourrait abonner — ou désabonner — un tiers. Même raisonnement
 * que pour l'auteur d'un avis.
 *
 * <p><b>Et pas davantage l'adresse e-mail.</b> Une première version acceptait le destinataire dans
 * le corps de la requête : un compte authentifié pouvait alors inscrire l'adresse de n'importe qui
 * à des rappels récurrents — un vecteur d'envoi non sollicité, avec le nom du service en
 * expéditeur. L'adresse est désormais résolue à l'envoi auprès du contexte « Identité & Accès »,
 * seul à la détenir vérifiée.
 */
@Service
@Transactional
public class CollectionSubscriptionServiceImpl implements CollectionSubscriptionService {

    private final CollectionSubscriptionRepository repository;
    private final CurrentUserProvider currentUserProvider;

    public CollectionSubscriptionServiceImpl(CollectionSubscriptionRepository repository,
                                             CurrentUserProvider currentUserProvider) {
        this.repository = repository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public void subscribe(UUID quartierId) {
        UUID userId = currentUserProvider.requireCurrentUserId();
        // Réabonnement : on réactive l'existant plutôt que d'échouer sur la contrainte d'unicité.
        var subscription = repository.findByUserIdAndQuartierId(userId, quartierId)
                .orElseGet(CollectionSubscription::new);
        subscription.setUserId(userId);
        subscription.setQuartierId(quartierId);
        subscription.setActive(true);
        repository.save(subscription);
    }

    @Override
    public void unsubscribe(UUID quartierId) {
        UUID userId = currentUserProvider.requireCurrentUserId();
        repository.findByUserIdAndQuartierId(userId, quartierId).ifPresent(s -> {
            s.setActive(false);
            repository.save(s);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> mySubscriptions() {
        return repository.findByUserIdAndActiveTrue(currentUserProvider.requireCurrentUserId())
                .stream().map(CollectionSubscription::getQuartierId).toList();
    }
}
