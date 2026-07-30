package sn.smartwaste.collect.waste.presentation.controller;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import sn.smartwaste.collect.waste.application.service.CollectionPassageService;
import sn.smartwaste.collect.waste.application.service.CollectionRouteService;
import sn.smartwaste.collect.waste.application.service.CollectionRouteService.RouteStop;

/**
 * Ordre de passage recommandé et déclaration des passages (`/v1/collection-routes`).
 *
 * <p>Répond à la question que se pose un superviseur chaque matin : « quels points dois-je vider
 * aujourd'hui, et dans quel ordre ». Chaque arrêt porte sa raison — un ordre qu'on ne peut pas
 * expliquer n'est pas suivi sur le terrain.
 *
 * <p><b>L'agent de collecte est le troisième acteur du mémoire</b> (§3.1.5.1) et n'existait nulle
 * part (G1 du backlog). Il lit sa tournée et déclare ses passages ; il ne touche à rien d'autre,
 * d'où une autorisation posée méthode par méthode plutôt qu'en tête de classe.
 */
@RestController
@RequestMapping("/v1/collection-routes")
public class CollectionRouteController {

    private static final String EXPLOITATION = "hasAnyRole('AGENT','ADMIN','SUPER_ADMIN')";

    private final CollectionRouteService routeService;
    private final CollectionPassageService passageService;

    public CollectionRouteController(CollectionRouteService routeService,
                                     CollectionPassageService passageService) {
        this.routeService = routeService;
        this.passageService = passageService;
    }

    @Operation(summary = "Ordre de passage recommande sur une commune",
               description = "Priorise par urgence : debordement, puis etat inconnu, puis a "
                       + "surveiller. A urgence egale, l'ordre suit la geographie (ADR-0017).")
    @GetMapping
    @PreAuthorize(EXPLOITATION)
    public List<RouteStop> planForCommune(@RequestParam("communeId") UUID communeId) {
        return routeService.planForCommune(communeId);
    }

    @Operation(summary = "Declarer un point collecte",
               description = "Remet le niveau a zero, horodate le passage et referme les alertes de "
                       + "collecte du point. Les alertes de maintenance survivent : vider un bac ne "
                       + "repare pas le capteur qui l'observe.")
    @PostMapping("/stops/{depotoirId}/collected")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(EXPLOITATION)
    public void markCollected(@PathVariable("depotoirId") Long depotoirId) {
        passageService.markCollected(depotoirId);
    }

    @Operation(summary = "Declarer un point inaccessible",
               description = "Rien n'est vide, rien n'est referme : le point reparait dans la "
                       + "tournee du lendemain, avec son motif.")
    @PostMapping("/stops/{depotoirId}/inaccessible")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(EXPLOITATION)
    public void markInaccessible(@PathVariable("depotoirId") Long depotoirId,
                                 @RequestBody(required = false) InaccessibleReason body) {
        passageService.markInaccessible(depotoirId, body == null ? null : body.reason());
    }

    @Operation(summary = "Avancement de la tournee du jour",
               description = "Points desservis sur points prevus. Un point inaccessible compte "
                       + "comme desservi — l'agent y est alle — mais pas comme collecte.")
    @GetMapping("/completion")
    @PreAuthorize(EXPLOITATION)
    public CollectionPassageService.Completion completion(
            @RequestParam("communeId") UUID communeId) {
        return routeService.completionForCommune(communeId);
    }

    /** Motif d'inaccessibilite : sans lui, l'echec n'apprend rien. */
    public record InaccessibleReason(String reason) { }
}
