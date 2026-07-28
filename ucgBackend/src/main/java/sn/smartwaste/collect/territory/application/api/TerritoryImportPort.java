package sn.smartwaste.collect.territory.application.api;

import java.util.Optional;
import java.util.UUID;

import sn.smartwaste.collect.shared.domain.model.ImportedFeature;

/**
 * Écritures d'import publiées par le référentiel territorial.
 *
 * <p>Remplace l'accès direct de l'import à {@code territory.domain.model} et
 * {@code territory.domain.repository} : ce contexte reçoit désormais des attributs bruts et décide
 * seul de leur signification. C'est ce qui permet de refermer les interfaces nommées ouvertes en
 * transition.
 */
public interface TerritoryImportPort {

    /** Vrai si au moins une région existe — préalable à l'import des départements. */
    boolean hasRegion();

    /** Vrai si au moins un département existe — préalable à l'import des communes. */
    boolean hasDepartment();

    /** Importe un département, rattaché à la première région enregistrée. */
    void importDepartment(ImportedFeature feature);

    /** Importe une commune, rattachée au premier département enregistré. */
    void importCommune(ImportedFeature feature);

    /** Importe un quartier ; sa commune est résolue depuis l'attribut {@code CCRCA}. */
    void importQuartier(ImportedFeature feature);

    /**
     * Résout une commune par son nom, de façon tolérante (égalité, puis correspondance partielle).
     *
     * <p>Publié parce que le cœur métier déchets rattache ses circuits et points de collecte à une
     * commune <b>par son nom</b>, tel qu'écrit dans les fichiers source.
     */
    Optional<UUID> findCommuneIdByName(String name);

    // ---------- Compteurs « déjà importé ? » ----------

    long countDepartments();

    long countCommunes();

    long countQuartiers();
}
