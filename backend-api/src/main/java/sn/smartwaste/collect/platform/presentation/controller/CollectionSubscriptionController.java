package sn.smartwaste.collect.platform.presentation.controller;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import sn.smartwaste.collect.platform.application.service.CollectionSubscriptionService;

/**
 * Abonnement des habitants aux passages de collecte (`/v1/collection-subscriptions`).
 *
 * <p>Endpoints authentifiés : l'abonné est l'utilisateur du jeton, jamais un identifiant — ni une
 * adresse e-mail — transmis dans le corps de la requête. Sans quoi n'importe qui pourrait abonner
 * un tiers, ou inscrire une adresse arbitraire à des rappels récurrents.
 */
@RestController
@RequestMapping("/v1/collection-subscriptions")
public class CollectionSubscriptionController {

    private final CollectionSubscriptionService subscriptionService;

    public CollectionSubscriptionController(CollectionSubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Operation(summary = "S'abonner aux passages de collecte d'un quartier")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void subscribe(@RequestBody SubscriptionRequest request) {
        subscriptionService.subscribe(request.quartierId());
    }

    @Operation(summary = "Se désabonner d'un quartier")
    @DeleteMapping("/{quartierId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@PathVariable("quartierId") UUID quartierId) {
        subscriptionService.unsubscribe(quartierId);
    }

    @Operation(summary = "Mes quartiers suivis")
    @GetMapping
    public List<UUID> mySubscriptions() {
        return subscriptionService.mySubscriptions();
    }

    /**
     * Le destinataire n'y figure pas : c'est l'utilisateur du jeton, et son adresse est résolue
     * côté serveur. L'accepter ici revenait à laisser abonner l'adresse d'un tiers.
     */
    public record SubscriptionRequest(UUID quartierId) { }
}
