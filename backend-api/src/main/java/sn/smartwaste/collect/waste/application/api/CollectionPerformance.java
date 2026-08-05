package sn.smartwaste.collect.waste.application.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Efficacité de la collecte sur une période et un territoire (G5 du backlog).
 *
 * <p><b>Pourquoi le calcul appartient à ce contexte.</b> « Combien de temps entre l'alerte et le
 * passage » se lit dans les alertes et les passages, qui sont ses entités. Faire remonter ces
 * lignes vers {@code analytics} pour qu'il les agrège traverserait une frontière que
 * {@code modules.verify()} refuse, et transporterait des milliers d'objets là où trois nombres
 * suffisent. Ce contexte agrège, {@code analytics} compose et présente.
 *
 * <p><b>Ce que ce port ne pouvait pas rendre avant le lot 2.</b> Sans passage enregistré, aucun
 * délai n'est calculable et le taux de réalisation n'a pas de numérateur : le rapport n'aurait
 * affiché que des dénominateurs.
 */
public interface CollectionPerformance {

    /**
     * @param communeId territoire observé, {@code null} pour l'ensemble du référentiel
     * @param from      début de période, inclus
     * @param to        fin de période, exclue
     */
    PerformanceReport reportFor(UUID communeId, Instant from, Instant to);

    /**
     * Ce qui s'est passé sur la période.
     *
     * <p>Les délais ne portent que sur les alertes de <b>collecte</b>. Une alerte de capteur muet
     * mesure la réactivité de la maintenance, pas celle des tournées : les mélanger produirait une
     * moyenne qui ne décrit rien.
     *
     * @param averageResolutionHours délai moyen entre l'alerte et sa résolution, {@code null} si
     *                               aucune alerte n'a été résolue — et non zéro, qui se lirait
     *                               comme une réactivité parfaite
     * @param stops                  points de collecte du territoire
     * @param served                 points ayant reçu au moins un passage, quelle qu'en soit l'issue
     * @param collected              points effectivement vidés
     * @param chronicPoints          points ayant débordé le plus souvent sur la période
     */
    record PerformanceReport(long alertsRaised,
                             long alertsResolved,
                             Double averageResolutionHours,
                             long stops,
                             long served,
                             long collected,
                             long inaccessible,
                             List<ProblemPoint> chronicPoints) {

        /** Part des points desservis, entre 0 et 1 ; {@code 0} si le territoire est vide. */
        public double completionRate() {
            return stops == 0 ? 0 : (double) served / stops;
        }
    }

    /** Un point qui revient trop souvent — c'est là que se décide un renforcement de tournée. */
    record ProblemPoint(Long depotoirId, String address, long overflowCount) { }
}
