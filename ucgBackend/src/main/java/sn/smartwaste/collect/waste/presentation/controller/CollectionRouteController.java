package sn.smartwaste.collect.waste.presentation.controller;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import sn.smartwaste.collect.waste.application.service.CollectionRouteService;
import sn.smartwaste.collect.waste.application.service.CollectionRouteService.RouteStop;

/**
 * Ordre de passage recommandé (`/v1/collection-routes`).
 *
 * <p>Répond à la question que se pose un superviseur chaque matin : « quels points dois-je vider
 * aujourd'hui, et dans quel ordre ». Chaque arrêt porte sa raison — un ordre qu'on ne peut pas
 * expliquer n'est pas suivi sur le terrain.
 */
@RestController
@RequestMapping("/v1/collection-routes")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class CollectionRouteController {

    private final CollectionRouteService routeService;

    public CollectionRouteController(CollectionRouteService routeService) {
        this.routeService = routeService;
    }

    @Operation(summary = "Ordre de passage recommande sur une commune",
               description = "Priorise par urgence : debordement, puis etat inconnu, puis a surveiller.")
    @GetMapping
    public List<RouteStop> planForCommune(@RequestParam("communeId") UUID communeId) {
        return routeService.planForCommune(communeId);
    }
}
