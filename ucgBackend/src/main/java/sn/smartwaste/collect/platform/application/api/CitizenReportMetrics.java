package sn.smartwaste.collect.platform.application.api;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Traitement des signalements citoyens, à destination des rapports de supervision.
 *
 * <p>Publié pour que « Supervision &amp; Analytique » puisse rendre compte du service rendu aux
 * habitants sans connaître le modèle des avis.
 */
public interface CitizenReportMetrics {

    /** Nombre de signalements par état. */
    Map<String, Long> countByStatus();

    /**
     * Délai <b>médian</b> entre dépôt et clôture.
     *
     * <p>Médiane et non moyenne : un seul signalement oublié six mois suffit à rendre une moyenne
     * flatteuse ou catastrophique selon le volume, alors que la médiane décrit ce que vit
     * réellement la majorité des habitants.
     *
     * @return {@link Optional#empty()} si rien n'a encore été clos
     */
    Optional<Duration> medianResolutionTime();

    /** Signalements encore ouverts depuis plus longtemps que le délai donné. */
    long countOpenOlderThan(Duration age);
}
