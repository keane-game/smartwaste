package sn.smartwaste.collect.waste.application.api;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Lectures publiées par le contexte « Déchets » à l'usage des autres contextes.
 *
 * <p>Toutes les méthodes ne portent que sur les éléments <b>actifs</b> : les ressources en attente
 * de purge (soft-delete, cf. {@code DeletionStatus}) sont exclues, sauf pour les compteurs
 * {@code count*} qui reprennent volontairement la sémantique historique des indicateurs du tableau
 * de bord — ils comptaient tout, corbeille comprise, et le changer ici modifierait les chiffres
 * affichés sans que personne l'ait demandé.
 */
public interface WasteReadModel {

    // ---------- Compteurs du tableau de bord ----------

    /** Nombre total de points de collecte (dépotoirs). */
    long countCollectionPoints();

    /** Nombre total de mobiliers urbains (bennes). */
    long countStreetFurniture();

    /** Nombre total de circuits, collecte et balayage confondus. */
    long countCircuits();

    /**
     * Nombre de points de collecte dont le <b>nom de type</b> contient le fragment donné,
     * insensible à la casse — c'est ainsi que sont dénombrés les bacs, PRN, PP et caisses
     * polybennes.
     *
     * <p>Le filtrage par fragment de libellé est fragile (renommer un type change les
     * indicateurs) mais c'est le comportement existant : il est publié tel quel plutôt que
     * corrigé en douce à l'occasion d'un déplacement de packages.
     */
    long countCollectionPointsByTypeNameContaining(String typeNameFragment);

    // ---------- Projections pour les statistiques de supervision ----------

    /** Alertes actives, réduites à ce dont la supervision a besoin. */
    List<ActiveAlert> activeAlerts();

    /** Points de collecte actifs, réduits à leur type et à leur dernier niveau connu. */
    List<ActiveCollectionPoint> activeCollectionPoints();

    /** Circuits actifs (collecte + balayage), réduits à leur commune de rattachement. */
    List<ActiveCircuit> activeCircuits();

    // ---------- Read-model de carte ----------

    /**
     * Points de collecte à afficher sur la carte de supervision.
     *
     * <p>Publié ici parce que le read-model est <b>produit</b> par ce contexte et seulement
     * <b>assemblé</b> par la supervision : c'est le contexte propriétaire qui sait résoudre une
     * géométrie et un type, dans sa propre transaction.
     */
    List<DepotoirMaps> collectionPointsForMap();

    // ---------- Horaires de collecte ----------

    /**
     * Passages de collecte prévus un jour donné.
     *
     * <p>Publié pour que le contexte Plateforme puisse prévenir les habitants abonnés. Il rend un
     * quartier et une heure — pas un circuit : l'habitant s'abonne à son quartier, qu'il connaît,
     * pas au circuit qui le dessert.
     */
    List<ScheduledCollection> collectionsScheduledOn(DayOfWeek dayOfWeek);

    // ---------- Flotte ----------

    /**
     * Véhicules à afficher sur la carte de suivi.
     *
     * <p>Seuls ceux <b>vus récemment</b> sont rendus : afficher un camion à sa position d'il y a
     * trois heures comme s'il y était encore est pire que ne rien afficher — on enverrait quelqu'un
     * le rejoindre. La fraîcheur est décidée par le contexte propriétaire, pas par l'appelant.
     */
    List<VehicleOnMap> vehiclesOnMap();

    /**
     * @param code      code de l'alerte, {@code null} si non renseigné
     * @param createdAt date de création, {@code null} si l'audit n'a pas été alimenté
     */
    record ActiveAlert(String code, LocalDateTime createdAt) { }

    /**
     * @param typeName         libellé du type de point de collecte, {@code null} si non rattaché
     * @param fillLevelPercent dernier niveau connu, {@code null} si le point n'est pas instrumenté
     */
    record ActiveCollectionPoint(String typeName, Integer fillLevelPercent) { }

    /** @param communeId commune de rattachement, {@code null} si le circuit n'est pas affecté */
    record ActiveCircuit(UUID communeId) { }

    /**
     * @param quartierId  quartier desservi
     * @param passageTime heure de passage prévue
     */
    record ScheduledCollection(UUID quartierId, LocalTime passageTime) { }

    /**
     * @param registration   immatriculation, l'identifiant que le terrain utilise
     * @param lastPositionAt date de la position — l'appelant peut l'afficher pour lever le doute
     */
    record VehicleOnMap(UUID vehicleId, String registration, String label,
                        double latitude, double longitude, java.time.Instant lastPositionAt) { }
}
