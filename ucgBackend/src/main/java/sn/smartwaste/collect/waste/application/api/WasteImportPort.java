package sn.smartwaste.collect.waste.application.api;

import java.util.UUID;

import sn.smartwaste.collect.shared.domain.model.ImportedFeature;

/**
 * Écritures d'import publiées par le cœur métier déchets.
 *
 * <p>Même raison d'être que son homologue territorial : l'import ne connaît plus ni les entités ni
 * les repositories de ce contexte. Le {@code communeId} lui est fourni parce que sa résolution
 * appartient au référentiel territorial (ADR-0012 — référence par identifiant).
 */
public interface WasteImportPort {

    /** @param communeId commune de rattachement, {@code null} si elle n'a pas pu être résolue */
    void importCircuitCollect(ImportedFeature feature, UUID communeId);

    void importCircuitBalayage(ImportedFeature feature, UUID communeId);

    /** Le type de point de collecte est créé à la volée s'il n'existe pas encore. */
    void importDepotoir(ImportedFeature feature, UUID communeId);
}
