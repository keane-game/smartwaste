package sn.smartwaste.collect.waste.domain.model;

/**
 * Distance à vol d'oiseau entre deux positions WGS84 (ADR-0017).
 *
 * <p>Haversine sur une sphère. À l'échelle d'une commune de Pikine — quelques kilomètres — l'écart
 * avec un calcul ellipsoïdal se compte en mètres, sur des trajets qui empruntent de toute façon des
 * rues et non des lignes droites. Ce qu'on cherche ici est un <b>ordre de passage</b>, pas une
 * distance routière : seule la comparaison entre éloignements compte.
 */
public final class GeoDistance {

    /** Rayon moyen de la Terre, en mètres. */
    private static final double RAYON_TERRESTRE = 6_371_008.8;

    private GeoDistance() {
    }

    public static double metersBetween(double latitudeA, double longitudeA,
                                       double latitudeB, double longitudeB) {
        double dLat = Math.toRadians(latitudeB - latitudeA);
        double dLon = Math.toRadians(longitudeB - longitudeA);
        double latA = Math.toRadians(latitudeA);
        double latB = Math.toRadians(latitudeB);

        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(latA) * Math.cos(latB);
        return 2 * RAYON_TERRESTRE * Math.asin(Math.min(1.0, Math.sqrt(h)));
    }
}
