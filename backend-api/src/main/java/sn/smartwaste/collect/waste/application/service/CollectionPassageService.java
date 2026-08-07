package sn.smartwaste.collect.waste.application.service;

import java.util.UUID;

/**
 * Déclaration des passages d'agent sur les points de collecte (G1 du backlog).
 *
 * <p>Ferme la boucle métier « détecter → alerter → <b>collecter</b> → constater », dont le
 * quatrième temps n'existait pas.
 */
public interface CollectionPassageService {

    /** Le point a été vidé : niveau remis à zéro, alertes de collecte refermées. */
    void markCollected(UUID depotoirId);

    /** Le point n'a pas pu être desservi : rien n'est vidé, rien n'est refermé, le motif est gardé. */
    void markInaccessible(UUID depotoirId, String reason);

    /**
     * Avancement de la tournée du jour sur une commune.
     *
     * @param stops     points de la tournée
     * @param served    points ayant reçu un passage aujourd'hui, quelle qu'en soit l'issue
     * @param collected points effectivement vidés
     */
    record Completion(long stops, long served, long collected) {

        /** Part des points desservis, entre 0 et 1 ; {@code 0} si la tournée est vide. */
        public double rate() {
            return stops == 0 ? 0 : (double) served / stops;
        }
    }
}
