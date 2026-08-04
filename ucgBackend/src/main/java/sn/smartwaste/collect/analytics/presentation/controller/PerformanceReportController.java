package sn.smartwaste.collect.analytics.presentation.controller;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import sn.smartwaste.collect.analytics.application.dto.PerformanceReport;
import sn.smartwaste.collect.analytics.application.service.PerformanceReportService;

/**
 * Rapports d'efficacité de la collecte (`/v1/supervision/reports`, G5 du backlog).
 *
 * <p>Répond au cas d'utilisation « générer des rapports de performance » (mémoire §3.1.6.1).
 * {@code /v1/supervision/stats} ne donne que des compteurs <b>instantanés</b> : rien n'y disait si
 * la situation s'améliorait d'un mois sur l'autre.
 *
 * <p><b>Comparer deux périodes se fait par deux appels.</b> Un endpoint de comparaison n'ajouterait
 * qu'une soustraction, que le client fait mieux — c'est lui qui sait quelles périodes il affiche
 * côte à côte.
 *
 * <p>L'autorisation reste celle de la surface d'administration, portée par
 * {@code SecurityConfiguration} : {@code /v1/supervision/**} y est réservé à {@code ADMIN} et
 * {@code SUPER_ADMIN}.
 */
@RestController
@RequestMapping("/v1/supervision/reports")
public class PerformanceReportController {

    /** Période retenue quand l'appelant n'en donne pas : le mois écoulé. */
    private static final Duration PERIODE_PAR_DEFAUT = Duration.ofDays(30);

    private final PerformanceReportService reportService;
    private final Clock clock;

    public PerformanceReportController(PerformanceReportService reportService, Clock clock) {
        this.reportService = reportService;
        this.clock = clock;
    }

    @Operation(summary = "Rapport d'efficacite de la collecte",
               description = """
                    Sur une periode et un territoire : alertes levees et resolues, delai moyen
                    entre l'alerte et le vidage, taux de realisation des tournees, et points les
                    plus souvent en debordement.
                    Le delai ne porte que sur les alertes de COLLECTE : une alerte de capteur muet
                    mesure la reactivite de la maintenance, et les melanger produirait une moyenne
                    qui ne decrit ni l'une ni l'autre.
                    Sans periode, le mois ecoule. Sans commune, l'ensemble du referentiel.""")
    @GetMapping
    public PerformanceReport report(
            @Parameter(description = "Territoire observe ; absent = tout le referentiel")
            @RequestParam(value = "communeId", required = false) UUID communeId,
            @Parameter(description = "Debut de periode (ISO-8601) ; defaut : il y a 30 jours")
            @RequestParam(value = "from", required = false) Instant from,
            @Parameter(description = "Fin de periode (ISO-8601) ; defaut : maintenant")
            @RequestParam(value = "to", required = false) Instant to) {
        return reportService.reportFor(communeId, debut(from, to), fin(to));
    }

    @Operation(summary = "Le meme rapport en CSV",
               description = "Format qu'un tableur ouvre sans rien installer, separateur point-virgule.")
    @GetMapping(value = "/csv", produces = "text/csv")
    public ResponseEntity<String> csv(
            @RequestParam(value = "communeId", required = false) UUID communeId,
            @RequestParam(value = "from", required = false) Instant from,
            @RequestParam(value = "to", required = false) Instant to) {
        var report = reportService.reportFor(communeId, debut(from, to), fin(to));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"rapport-collecte.csv\"")
                .body(reportService.asCsv(report));
    }

    private Instant fin(Instant to) {
        return to == null ? Instant.now(clock) : to;
    }

    /**
     * Début de période, avec la seule validation qui compte : une période inversée rendrait un
     * rapport vide, ce qui se lirait comme « rien ne s'est passé » plutôt que comme une erreur
     * d'appel.
     */
    private Instant debut(Instant from, Instant to) {
        Instant debut = from == null ? fin(to).minus(PERIODE_PAR_DEFAUT) : from;
        if (!debut.isBefore(fin(to))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fin de periode doit etre posterieure au debut");
        }
        return debut;
    }
}
