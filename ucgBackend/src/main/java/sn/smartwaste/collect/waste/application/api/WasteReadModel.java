package sn.smartwaste.collect.waste.application.api;

import java.time.LocalDateTime;
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

    /** Points de collecte actifs, réduits à leur type. */
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

    /**
     * @param code      code de l'alerte, {@code null} si non renseigné
     * @param createdAt date de création, {@code null} si l'audit n'a pas été alimenté
     */
    record ActiveAlert(String code, LocalDateTime createdAt) { }

    /** @param typeName libellé du type de point de collecte, {@code null} si non rattaché */
    record ActiveCollectionPoint(String typeName) { }

    /** @param communeId commune de rattachement, {@code null} si le circuit n'est pas affecté */
    record ActiveCircuit(UUID communeId) { }
}
