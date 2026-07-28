package sn.smartwaste.collect.shared.domain.model;

import java.util.List;
import java.util.Map;

/**
 * Une entité géographique brute, telle que lue dans un fichier GeoJSON, avant toute interprétation.
 *
 * <p><b>Raison d'être.</b> L'import écrit dans plusieurs contextes. S'il construisait lui-même les
 * entités, il devrait connaître leurs champs et leurs repositories — ce qui obligeait à exposer les
 * modèles et les dépôts de {@code territory} et de {@code waste}, contrairement à l'ADR-0013 §3.
 * En transportant les attributs bruts, l'import se limite à <b>lire un fichier</b> ; c'est le
 * contexte propriétaire qui décide ce que « COD_DEPT » ou « Type_de_Mo » signifie chez lui.
 *
 * <p>Types volontairement neutres ({@code Map}, {@code String}) : aucune dépendance JPA, JSON ou
 * Spring ne franchit la frontière.
 *
 * @param attributes attributs de l'entité, tels que nommés dans le fichier source
 * @param geometry   contour associé, {@code null} si absent
 */
public record ImportedFeature(Map<String, Object> attributes, GeoShape geometry) {

    /** Lit un attribut texte, {@code null} s'il est absent. */
    public String text(String key) {
        Object value = attributes.get(key);
        return value == null ? null : String.valueOf(value);
    }

    /** Lit un attribut numérique, {@code null} s'il est absent ou illisible. */
    public Double number(String key) {
        Object value = attributes.get(key);
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return value == null ? null : Double.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Lit un attribut entier, {@code null} s'il est absent ou illisible. */
    public Integer integer(String key) {
        Double d = number(key);
        return d == null ? null : d.intValue();
    }

    /**
     * Contour géographique brut.
     *
     * @param type              type de géométrie déclaré par la source ({@code esriGeometryPolygon}…)
     * @param spatialReference  référentiel spatial, conservé tel quel
     * @param points            points du contour, dans l'ordre du fichier
     */
    public record GeoShape(String type, String spatialReference, List<GeoPoint> points) { }

    /** Un point. Latitude et longitude restent des chaînes : c'est ce que porte le modèle actuel. */
    public record GeoPoint(String latitude, String longitude) { }
}
