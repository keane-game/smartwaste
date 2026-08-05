package sn.smartwaste.collect.territory.domain.model;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Appartenance d'un point à un contour (ADR-0018).
 *
 * <p><b>Pourquoi cette géométrie remplace un rapprochement par nom.</b> L'import rattachait chaque
 * point de collecte à sa commune en comparant des <i>libellés</i> — ceux des fichiers SIG et ceux du
 * référentiel, qui divergent. Sur les 71 points réels : 31 rattachements certains, <b>25 tirés au
 * hasard</b> parmi plusieurs candidats (« Pikine » correspond à trois communes, et le code retenait
 * la première), et 15 sans rattachement. Les 25 sont les pires : invisibles et faux, ils
 * corrompaient la tournée et le rapport de deux communes à la fois.
 *
 * <p>La position, elle, ne se discute pas. Le même jeu de données résolu par la géométrie donne
 * <b>69 points sur 71 dans exactement une commune</b>, et aucune ambiguïté.
 *
 * <p>Ce n'était possible qu'après l'ADR-0016 : avant, les points de collecte n'avaient aucune
 * position.
 */
class PolygonContainmentTest {

    /** Carré unité, dans l'ordre du fichier source. */
    private static final List<double[]> CARRE = List.of(
            new double[]{0, 0}, new double[]{0, 10}, new double[]{10, 10}, new double[]{10, 0});

    /** Contour en L : le rectangle englobant ne suffit pas à décider. */
    private static final List<double[]> FORME_EN_L = List.of(
            new double[]{0, 0}, new double[]{0, 10}, new double[]{4, 10},
            new double[]{4, 4}, new double[]{10, 4}, new double[]{10, 0});

    @Test
    @DisplayName("un point interieur est dans le contour")
    void insideIsInside() {
        assertThat(PolygonContainment.contains(CARRE, 5, 5)).isTrue();
    }

    @Test
    @DisplayName("un point exterieur ne l'est pas")
    void outsideIsOutside() {
        assertThat(PolygonContainment.contains(CARRE, 15, 5)).isFalse();
        assertThat(PolygonContainment.contains(CARRE, -1, 5)).isFalse();
    }

    @Test
    @DisplayName("le creux d'une forme concave est exterieur")
    void concaveHoleIsOutside() {
        // Un test de rectangle englobant repondrait « dedans » : c'est l'erreur qu'evite le
        // lancer de rayon, et les communes de Pikine sont loin d'etre rectangulaires.
        assertThat(PolygonContainment.contains(FORME_EN_L, 8, 8)).isFalse();
        assertThat(PolygonContainment.contains(FORME_EN_L, 2, 8)).isTrue();
        assertThat(PolygonContainment.contains(FORME_EN_L, 8, 2)).isTrue();
    }

    @Test
    @DisplayName("un contour degenere ne fait pas echouer le rattachement")
    void degenerateRingIsHandled() {
        // Le referentiel n'est pas parfait : une geometrie vide ou reduite a deux points ne doit
        // pas interrompre l'import de tout un fichier.
        assertThat(PolygonContainment.contains(List.of(), 5, 5)).isFalse();
        assertThat(PolygonContainment.contains(
                List.of(new double[]{0, 0}, new double[]{1, 1}), 0.5, 0.5)).isFalse();
    }

    @Test
    @DisplayName("un point de Pikine tombe dans une emprise realiste")
    void realisticPikineExtent() {
        // Ordres de grandeur reels : longitude negative, latitude ~14,7 — le cas ou une inversion
        // d'axes se verrait immediatement.
        var commune = List.of(
                new double[]{-17.45, 14.70}, new double[]{-17.45, 14.80},
                new double[]{-17.30, 14.80}, new double[]{-17.30, 14.70});

        assertThat(PolygonContainment.contains(commune, -17.40, 14.76)).isTrue();
        assertThat(PolygonContainment.contains(commune, -17.20, 14.76)).isFalse();
    }
}
