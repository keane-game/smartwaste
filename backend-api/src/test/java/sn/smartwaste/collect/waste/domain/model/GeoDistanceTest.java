package sn.smartwaste.collect.waste.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Distance entre deux points de collecte (ADR-0017).
 *
 * <p>Le tri géographique d'une tournée a besoin de comparer des éloignements. À l'échelle de Pikine
 * — quelques kilomètres — la formule de haversine sur une sphère est très largement suffisante :
 * l'écart avec un calcul ellipsoïdal se compte en mètres sur des distances qu'on parcourt en
 * camion-benne dans des rues qui ne sont de toute façon pas des lignes droites.
 *
 * <p>Les attendus ne viennent pas de ce code : ce sont des écarts vérifiables indépendamment (un
 * degré de latitude vaut ~111,2 km partout ; un degré de longitude vaut ~107,6 km à la latitude de
 * Pikine, soit 111,2 × cos 14,76°).
 */
class GeoDistanceTest {

    private static final double LAT_PIKINE = 14.7645;
    private static final double LON_PIKINE = -17.3900;

    @Test
    @DisplayName("un degré de latitude vaut environ 111 km")
    void oneDegreeOfLatitude() {
        double metres = GeoDistance.metersBetween(14.0, LON_PIKINE, 15.0, LON_PIKINE);

        assertThat(metres).isCloseTo(111_195, within(500.0));
    }

    @Test
    @DisplayName("un degré de longitude est resserré par la latitude de Pikine")
    void oneDegreeOfLongitude() {
        // 111,2 km x cos(14,7645°) ~ 107,5 km. Ce cas echouerait si la formule ignorait le cosinus
        // de la latitude — l'erreur classique d'une distance calculee comme si la Terre etait plate.
        double metres = GeoDistance.metersBetween(LAT_PIKINE, -17.0, LAT_PIKINE, -18.0);

        assertThat(metres).isCloseTo(107_527, within(500.0));
    }

    @Test
    @DisplayName("la distance d'un point à lui-même est nulle")
    void samePointIsZero() {
        assertThat(GeoDistance.metersBetween(LAT_PIKINE, LON_PIKINE, LAT_PIKINE, LON_PIKINE))
                .isZero();
    }

    @Test
    @DisplayName("la distance est symétrique")
    void isSymmetric() {
        double aller = GeoDistance.metersBetween(14.74, -17.42, 14.78, -17.30);
        double retour = GeoDistance.metersBetween(14.78, -17.30, 14.74, -17.42);

        assertThat(aller).isCloseTo(retour, within(0.001));
    }
}
