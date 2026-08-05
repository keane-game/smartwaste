package sn.smartwaste.collect.analytics.presentation.controller;

import sn.smartwaste.collect.analytics.application.dto.SupervisionStats;
import sn.smartwaste.collect.analytics.application.service.SupervisionStatsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Indicateurs avancés de supervision (P2-5).
 *
 * <p>Exposé sous {@code /v1/**} — donc authentifié — contrairement aux compteurs historiques
 * de {@code DashboardController}, servis sous {@code /data/**} qui est public.
 */
@RestController
@RequestMapping("/v1/supervision")
public class SupervisionStatsController {

    private final SupervisionStatsService supervisionStatsService;

    public SupervisionStatsController(SupervisionStatsService supervisionStatsService) {
        this.supervisionStatsService = supervisionStatsService;
    }

    @Operation(
            summary = "Indicateurs de supervision",
            description = """
                    Alertes par jour et par code, répartition des points de collecte par type et
                    par tranche de remplissage, circuits par commune, état de la corbeille et
                    nombre de flux temps réel ouverts.

                    Deux blocs disent ce que les compteurs d'alertes ne peuvent pas dire :
                    `ingestion` signale les capteurs muets — une panne ne produit aucune alerte et
                    ressemble donc, dans les chiffres, à un point de collecte qui va bien — et
                    `citizenReports` mesure le délai de traitement des signalements des habitants.""")
    @GetMapping("/stats")
    public SupervisionStats stats(
            @Parameter(description = "Fenêtre d'analyse en jours (défaut 30, maximum 365)")
            @RequestParam(value = "windowDays", required = false) Integer windowDays) {
        return supervisionStatsService.compute(windowDays);
    }
}
