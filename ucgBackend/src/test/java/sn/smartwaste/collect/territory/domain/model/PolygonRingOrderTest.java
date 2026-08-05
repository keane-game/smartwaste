package sn.smartwaste.collect.territory.domain.model;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * L'ordre des points d'un contour <b>est</b> sa forme.
 *
 * <p><b>Le défaut que cela documente.</b> {@code GeometryEntity.coordinates} est un
 * {@code @OneToMany} sans {@code @OrderBy} : rien ne garantissait l'ordre de relecture des points.
 * PostgreSQL rend souvent les lignes dans l'ordre d'insertion, ce qui a suffi à faire fonctionner
 * 69 rattachements sur 71 — mais ce n'est pas une garantie, c'est une coïncidence de plan
 * d'exécution. Un tri différent, un index, une jointure, et le contour se retrouve mélangé.
 *
 * <p>Un polygone mélangé ne lève aucune erreur : il décrit simplement une <b>autre</b> forme, et le
 * rattachement territorial devient faux en silence. C'est ce qui explique que deux circuits de
 * balayage dont le premier point tombe pourtant dans une commune ne lui aient pas été rattachés.
 *
 * <p>Ce test ne teste pas JPA — il fixe l'invariant que la persistance doit préserver, et montre
 * ce qu'on perd en le laissant au hasard.
 */
class PolygonRingOrderTest {

    /** Un carré, dans l'ordre du fichier source. */
    private static final List<double[]> CARRE = List.of(
            new double[]{0, 0}, new double[]{0, 10}, new double[]{10, 10}, new double[]{10, 0});

    /** Les mêmes quatre points, dans un ordre différent : un nœud papillon, pas un carré. */
    private static final List<double[]> MELANGE = List.of(
            new double[]{0, 0}, new double[]{10, 10}, new double[]{0, 10}, new double[]{10, 0});

    @Test
    @DisplayName("un point du carre est dedans")
    void pointIsInsideTheSquare() {
        assertThat(PolygonContainment.contains(CARRE, 2, 5)).isTrue();
    }

    @Test
    @DisplayName("les memes points dans un autre ordre decrivent une autre forme")
    void shufflingTheRingChangesTheShape() {
        // Meme ensemble de sommets, meme aire englobante, reponse OPPOSEE. C'est pourquoi l'ordre
        // ne peut pas etre laisse au hasard de la base : rien ne signalerait l'erreur.
        //
        // Le point est choisi avec soin : un premier essai portait sur (5, 2), qui tombe dans les
        // DEUX formes — le test passait au vert sans rien demontrer. Deux polygones distincts ne
        // different pas partout, seulement quelque part.
        assertThat(PolygonContainment.contains(MELANGE, 2, 5)).isFalse();
    }
}
