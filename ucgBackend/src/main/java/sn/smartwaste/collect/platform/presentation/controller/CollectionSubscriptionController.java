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
 * <p>Endpoints authentifiés : l'abonné est l'utilisateur du jeton, jamais un identifiant transmis
 * dans le corps de la requête — sans quoi n'importe qui pourrait abonner ou désabonner un tiers.
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
        subscriptionService.subscribe(request.quartierId(), request.email());
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

    /** @param email adresse de notification, figée à l'abonnement */
    public record SubscriptionRequest(UUID quartierId, String email) { }
}
