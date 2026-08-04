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
     * Construit la géométrie d'une entité importée, sans l'enregistrer (ADR-0016).
     *
     * <p><b>Pourquoi le référentiel territorial la fabrique pour les autres.</b> La géométrie et
     * ses coordonnées sont son modèle : c'est lui qui sait qu'un contour se compose de points, et
     * comment ils s'horodatent. Le contexte « déchets » a besoin d'en poser une sur ses dépotoirs
     * et ses circuits — il l'obtient ici plutôt que de réécrire la même construction chez lui, ce
     * qui ferait diverger deux définitions de la même chose.
     *
     * <p>Rend {@code null} si l'entité source ne porte pas de contour : une entité sans géométrie
     * reste importable, avec son adresse, son type et sa commune.
     */
    sn.smartwaste.collect.territory.domain.model.GeometryEntity newGeometry(ImportedFeature feature);

    /**
     * Résout une commune par son nom, de façon tolérante (égalité, puis correspondance partielle).
     *
     * <p>Publié parce que le cœur métier déchets rattache ses circuits et points de collecte à une
     * commune <b>par son nom</b>, tel qu'écrit dans les fichiers source.
     */
    Optional<UUID> findCommuneIdByName(String name);

    /**
     * Contours des communes, en une seule lecture (ADR-0018).
     *
     * <p><b>Pourquoi un instantané et non une résolution point par point.</b> Une première version
     * publiait {@code findCommuneIdAt(lat, lon)} : élégante à l'appel, elle rechargeait les
     * 12 communes, leur géométrie et leurs coordonnées <b>pour chaque entité importée</b>, soit
     * une vingtaine de requêtes multipliées par 279 entités. L'import ne terminait plus. Les
     * contours ne changent pas pendant un import : ils se lisent une fois.
     *
     * <p>Rendre l'instantané au lieu de le mémoriser dans ce service évite un cache de singleton
     * qui se périmerait silencieusement à la première modification de commune.
     */
    java.util.List<CommuneBoundary> communeBoundaries();

    /**
     * Le contour d'une commune, réduit à ce qu'il faut pour situer un point.
     *
     * <p>Ne transporte aucune entité : {@code ring} est une suite de {@code [longitude, latitude]}
     * en WGS84.
     */
    record CommuneBoundary(UUID communeId, java.util.List<double[]> ring) {

        /** Vrai si ce point tombe à l'intérieur du contour. */
        public boolean contains(double latitude, double longitude) {
            return sn.smartwaste.collect.territory.domain.model.PolygonContainment
                    .contains(ring, longitude, latitude);
        }
    }

    // ---------- Compteurs « déjà importé ? » ----------

    long countDepartments();

    long countCommunes();

    long countQuartiers();
}
