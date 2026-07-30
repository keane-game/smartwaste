package sn.smartwaste.collect.administration.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Reprojection des coordonnées du référentiel (ADR-0015 §1).
 *
 * <p><b>Ce que ces tests protègent.</b> Les 13 fichiers de {@code datas/} déclarent
 * {@code spatialReference: {wkid: 32628}} — UTM zone 28N, en <b>mètres</b> — et l'import les
 * stockait tels quels dans des champs nommés {@code latitude}/{@code longitude}, en intervertissant
 * de surcroît les axes ({@code x} devenait la latitude). Tout point importé se serait retrouvé à
 * « latitude 239 504, longitude 1 631 116 » : hors de la Terre pour la carte web, Flutter et les
 * read-models. Le défaut n'a jamais été visible parce que l'import n'a jamais tourné.
 *
 * <p><b>D'où viennent les valeurs attendues.</b> Elles ne sont pas issues de ce code : on part de
 * points au sol <i>connus</i> (Pikine, centre de Dakar) et de leur projection calculée
 * indépendamment par les formules directes de Snyder. Les eastings/northings ainsi obtenus tombent
 * dans la plage exacte des fichiers de données réels (x ≈ 239 504 … 251 473, y ≈ 1 631 116), ce qui
 * corrobore à la fois le système déclaré et l'ancrage géographique. Un test qui se contenterait de
 * vérifier l'aller-retour de ce même code ne prouverait que sa cohérence interne : il passerait avec
 * un méridien central faux ou des axes inversés.
 */
class CoordinateProjectorTest {

    /** ~1,1 cm : on compare des degrés, la conversion doit être exacte à la précision numérique. */
    private static final double TOLERANCE_DEGRES = 1e-7;

    private final CoordinateProjector projector = new CoordinateProjector();

    @Test
    @DisplayName("un point de Pikine retrouve sa latitude et sa longitude réelles")
    void convertsAPikinePoint() {
        var wgs84 = projector.toWgs84(242712.7316, 1633647.0419);

        assertThat(wgs84.latitude()).isCloseTo(14.7645, within(TOLERANCE_DEGRES));
        assertThat(wgs84.longitude()).isCloseTo(-17.3900, within(TOLERANCE_DEGRES));
    }

    @Test
    @DisplayName("un second point de contrôle écarte un ajustement fortuit sur le premier")
    void convertsADakarPoint() {
        var wgs84 = projector.toWgs84(236519.4545, 1625776.2798);

        assertThat(wgs84.latitude()).isCloseTo(14.6928, within(TOLERANCE_DEGRES));
        assertThat(wgs84.longitude()).isCloseTo(-17.4467, within(TOLERANCE_DEGRES));
    }

    @Test
    @DisplayName("sur le méridien central, la longitude vaut exactement celle de la zone 28")
    void eastingAtFalseOriginGivesTheCentralMeridian() {
        // Ancrage indépendant des points au sol : par définition de la projection, un easting de
        // 500 000 est sur le méridien central, soit -15° pour la zone 28. Ce cas échouerait si la
        // zone (donc le méridien) était fausse, ce qu'aucun aller-retour ne révélerait.
        var wgs84 = projector.toWgs84(500_000.0, 1_631_116.0);

        assertThat(wgs84.longitude()).isCloseTo(-15.0, within(TOLERANCE_DEGRES));
    }

    @Test
    @DisplayName("les points réels du référentiel tombent dans l'emprise de Pikine")
    void realReferentialPointsLandOnPikine() {
        // Coordonnée effectivement présente dans depotoir.json / point_pp.json / pp_pnr_pp-pnr.json.
        // Garde-fou contre l'inversion des axes : intervertir x et y sortirait de l'emprise, alors
        // que la conversion resterait « mathématiquement » cohérente.
        var wgs84 = projector.toWgs84(239504.8017, 1631115.84);

        assertThat(wgs84.latitude()).isBetween(14.6, 14.9);
        assertThat(wgs84.longitude()).isBetween(-17.5, -17.1);
    }

    @Test
    @DisplayName("l'ordre des axes est bien easting puis northing")
    void axisOrderIsEastingThenNorthing() {
        // L'erreur d'origine : `new GeoPoint(x, y)` alimentait un record `(latitude, longitude)`.
        // Le northing est grand (~1,6 million), l'easting petit (~240 000) : les intervertir
        // produirait une latitude aberrante.
        var wgs84 = projector.toWgs84(242712.7316, 1633647.0419);

        assertThat(wgs84.latitude()).isLessThan(90.0);
        assertThat(wgs84.latitude()).isGreaterThan(0.0);
    }
}
