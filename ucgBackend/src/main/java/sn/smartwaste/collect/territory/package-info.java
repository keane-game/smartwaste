/**
 * Module <b>Référentiel Territorial</b> (données de référence, en amont).
 *
 * <p>Hiérarchie Région → Département → Commune → Quartier, contours géographiques et fonds de
 * carte. Ne référence aucun autre module : c'est la source de vérité dont tout le reste dépend.
 *
 * <p>Découplage (ADR-0012) : les modules aval référencent {@code communeId} / {@code quartierId}.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Référentiel Territorial")
package sn.smartwaste.collect.territory;
