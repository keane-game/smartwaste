package sn.smartwaste.collect.analytics.application.service;

import java.time.Instant;
import java.util.UUID;

import sn.smartwaste.collect.analytics.application.dto.PerformanceReport;

/**
 * Rapports d'efficacité de la collecte (G5 du backlog).
 *
 * <p>Répond au cas d'utilisation « générer des rapports de performance pour surveiller
 * l'efficacité du système » (mémoire §3.1.6.1), auquel {@code /v1/supervision/stats} ne répondait
 * pas : il ne donne que des compteurs <b>instantanés</b>, et rien ne disait si la situation
 * s'améliorait.
 *
 * <p><b>Comparer deux périodes se fait par deux appels.</b> Un endpoint dédié n'apporterait que la
 * soustraction, que le client fait mieux — il sait quelles périodes il affiche côte à côte.
 */
public interface PerformanceReportService {

    /**
     * @param communeId territoire observé, {@code null} pour l'ensemble du référentiel
     * @param from      début de période, inclus
     * @param to        fin de période, exclue
     */
    PerformanceReport reportFor(UUID communeId, Instant from, Instant to);

    /** Le même rapport, en CSV — le format qu'un tableur ouvre sans rien installer. */
    String asCsv(PerformanceReport report);
}
