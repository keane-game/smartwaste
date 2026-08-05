package sn.smartwaste.collect.analytics.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import sn.smartwaste.collect.waste.application.api.CollectionPerformance;

/**
 * Rapport d'efficacité rendu à la supervision (G5 du backlog).
 *
 * <p>Compose ce que le contexte « Déchets » a calculé et ce que le référentiel territorial sait
 * nommer : un rapport qui dirait « commune 019fb3cc-78fb… » ne serait lu par personne.
 *
 * @param communeName     {@code null} lorsque le rapport porte sur l'ensemble du référentiel
 * @param completionRate  part des points desservis, entre 0 et 1
 */
public record PerformanceReport(UUID communeId,
                                String communeName,
                                Instant from,
                                Instant to,
                                long alertsRaised,
                                long alertsResolved,
                                Double averageResolutionHours,
                                long stops,
                                long served,
                                long collected,
                                long inaccessible,
                                double completionRate,
                                List<CollectionPerformance.ProblemPoint> chronicPoints) {

    public static PerformanceReport of(UUID communeId, String communeName,
                                       Instant from, Instant to,
                                       CollectionPerformance.PerformanceReport source) {
        return new PerformanceReport(communeId, communeName, from, to,
                source.alertsRaised(), source.alertsResolved(), source.averageResolutionHours(),
                source.stops(), source.served(), source.collected(), source.inaccessible(),
                source.completionRate(), source.chronicPoints());
    }
}
