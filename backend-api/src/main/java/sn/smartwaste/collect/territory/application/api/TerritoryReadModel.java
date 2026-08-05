package sn.smartwaste.collect.territory.application.api;

/**
 * Lectures publiées par le référentiel territorial à l'usage des autres contextes.
 *
 * <p>Premier pas hors de la concession {@code @NamedInterface("repositories")} : les appelants
 * passent par un contrat applicatif au lieu d'atteindre les repositories du référentiel.
 */
public interface TerritoryReadModel {

    /**
     * Département servant de fond de carte à la supervision — le premier par ordre alphabétique.
     *
     * @return {@code null} si le référentiel est vide (aucun import GeoJSON encore joué)
     */
    DepartmentMaps firstDepartmentForMap();

    /**
     * Nom d'une commune, {@code null} si elle n'existe plus.
     *
     * <p>Publié pour que les rapports de supervision (G5) puissent <b>nommer</b> le territoire
     * observé : « commune 019fb3cc-78fb… » ne serait lu par personne. Le contrat ne rend qu'une
     * chaîne — aucun appelant ne voit {@code CommuneEntity}, ce que
     * {@code modules.verify()} refuse justement, et ce qui a fait échouer une première version de
     * ce rapport.
     */
    String communeNameOf(java.util.UUID communeId);
}
