package sn.smartwaste.collect.waste.application.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Priorisation des tournées de collecte.
 *
 * <p>« Optimiser les tournées » est un objectif produit annoncé depuis l'origine et jamais tenu :
 * les circuits étaient stockés, aucun ordre n'en sortait. Ce service rend l'ordre de passage
 * exploitable maintenant que les niveaux de remplissage arrivent des capteurs.
 *
 * <p><b>Ce n'est pas une optimisation de trajet</b> (voyageur de commerce) : c'est une priorisation
 * par urgence, qui est le besoin réel exprimé — « quels points dois-je vider aujourd'hui, et dans
 * quel ordre ». Optimiser la distance suppose un graphe routier qu'on n'a pas.
 */
public interface CollectionRouteService {

    /** Ordre de passage recommandé sur une commune. */
    List<RouteStop> planForCommune(UUID communeId);

    /**
     * Un arrêt de tournée.
     *
     * @param priority         urgence calculée, la clé du tri
     * @param fillLevelPercent dernier niveau connu, {@code null} si le point n'a jamais été mesuré
     * @param lastMeasuredAt   date de ce niveau, {@code null} si jamais mesuré
     * @param reason           pourquoi ce point est là — un ordre qu'on ne peut pas expliquer
     *                         n'est pas suivi sur le terrain
     */
    record RouteStop(Long depotoirId, String address, String typeName,
                     StopPriority priority, Integer fillLevelPercent,
                     Instant lastMeasuredAt, String reason) { }

    /** Niveaux d'urgence, du plus au moins pressant. */
    enum StopPriority {
        /** Seuil dépassé : déborde ou va déborder. */
        DEBORDEMENT,
        /** Jamais mesuré, ou mesure trop ancienne — état réellement inconnu. */
        ETAT_INCONNU,
        /** Se remplit, sans urgence. */
        A_SURVEILLER,
        /** Vidé récemment. */
        RIEN_A_FAIRE
    }
}
