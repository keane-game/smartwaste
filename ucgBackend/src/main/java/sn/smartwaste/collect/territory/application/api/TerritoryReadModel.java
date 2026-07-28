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
}
