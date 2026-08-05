package sn.smartwaste.collect.administration.application.service;

import org.springframework.stereotype.Component;

/**
 * Convertit les coordonnées du référentiel vers WGS84 (ADR-0015 §1).
 *
 * <p><b>Pourquoi ce composant existe.</b> Les fichiers de {@code datas/} sont des exports ArcGIS en
 * <b>UTM zone 28N</b> ({@code wkid: 32628}), c'est-à-dire en mètres projetés. L'import les stockait
 * tels quels dans des champs nommés {@code latitude}/{@code longitude}, axes intervertis de surcroît.
 * Des noms de champs qui <i>promettent</i> du WGS84 doivent contenir du WGS84 : la conversion est
 * faite une fois, à l'écriture, plutôt que répétée par chaque consommateur (carte web, Flutter,
 * read-models, futurs calculs de distance) dont le premier à l'oublier produirait une carte fausse
 * sans erreur.
 *
 * <p><b>Pourquoi pas une bibliothèque.</b> La transverse de Mercator inverse est une formule fermée
 * (Snyder, <i>Map Projections — A Working Manual</i>, §8). L'ajouter en une trentaine de lignes
 * testées coûte moins qu'une dépendance de plus dans un projet qui vient d'élaguer les siennes
 * (ADR-0009). C'est le <b>seul</b> endroit du code qui connaît une projection.
 */
@Component
public class CoordinateProjector {

    // Ellipsoïde WGS84.
    private static final double DEMI_GRAND_AXE = 6_378_137.0;
    private static final double APLATISSEMENT = 1 / 298.257223563;

    // Paramètres UTM zone 28N — celle que déclarent les fichiers (wkid 32628).
    private static final double FACTEUR_ECHELLE = 0.9996;
    private static final double FAUX_EST = 500_000.0;
    /** Méridien central de la zone 28 : 6 × 28 − 183. */
    private static final double MERIDIEN_CENTRAL_RAD = Math.toRadians(-15.0);

    private static final double E2 = APLATISSEMENT * (2 - APLATISSEMENT);
    private static final double EP2 = E2 / (1 - E2);

    /** Un point en WGS84, dans l'ordre où on le lit : latitude puis longitude. */
    public record LatLon(double latitude, double longitude) { }

    /**
     * @param easting  abscisse projetée, en mètres (le {@code x} des fichiers)
     * @param northing ordonnée projetée, en mètres (le {@code y} des fichiers)
     */
    public LatLon toWgs84(double easting, double northing) {
        double m = northing / FACTEUR_ECHELLE;
        double mu = m / (DEMI_GRAND_AXE
                * (1 - E2 / 4 - 3 * E2 * E2 / 64 - 5 * E2 * E2 * E2 / 256));

        double e1 = (1 - Math.sqrt(1 - E2)) / (1 + Math.sqrt(1 - E2));
        double e1_2 = e1 * e1;
        double e1_3 = e1_2 * e1;
        double e1_4 = e1_3 * e1;

        // Latitude du pied de la perpendiculaire : la série qui inverse l'arc de méridien.
        double phi1 = mu
                + (3 * e1 / 2 - 27 * e1_3 / 32) * Math.sin(2 * mu)
                + (21 * e1_2 / 16 - 55 * e1_4 / 32) * Math.sin(4 * mu)
                + (151 * e1_3 / 96) * Math.sin(6 * mu)
                + (1097 * e1_4 / 512) * Math.sin(8 * mu);

        double sinPhi1 = Math.sin(phi1);
        double cosPhi1 = Math.cos(phi1);
        double tanPhi1 = Math.tan(phi1);

        double c1 = EP2 * cosPhi1 * cosPhi1;
        double t1 = tanPhi1 * tanPhi1;
        double n1 = DEMI_GRAND_AXE / Math.sqrt(1 - E2 * sinPhi1 * sinPhi1);
        double r1 = DEMI_GRAND_AXE * (1 - E2) / Math.pow(1 - E2 * sinPhi1 * sinPhi1, 1.5);
        double d = (easting - FAUX_EST) / (n1 * FACTEUR_ECHELLE);

        double d2 = d * d;
        double d3 = d2 * d;
        double d4 = d3 * d;
        double d5 = d4 * d;
        double d6 = d5 * d;

        double latitude = phi1 - (n1 * tanPhi1 / r1) * (d2 / 2
                - (5 + 3 * t1 + 10 * c1 - 4 * c1 * c1 - 9 * EP2) * d4 / 24
                + (61 + 90 * t1 + 298 * c1 + 45 * t1 * t1 - 252 * EP2 - 3 * c1 * c1) * d6 / 720);

        double longitude = MERIDIEN_CENTRAL_RAD + (d
                - (1 + 2 * t1 + c1) * d3 / 6
                + (5 - 2 * c1 + 28 * t1 - 3 * c1 * c1 + 8 * EP2 + 24 * t1 * t1) * d5 / 120) / cosPhi1;

        return new LatLon(Math.toDegrees(latitude), Math.toDegrees(longitude));
    }
}
