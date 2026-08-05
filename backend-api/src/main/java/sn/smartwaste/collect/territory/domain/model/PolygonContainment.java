package sn.smartwaste.collect.territory.domain.model;

import java.util.List;

/**
 * Appartenance d'un point à un contour, par lancer de rayon (ADR-0018).
 *
 * <p><b>Ce que cela remplace.</b> L'import rattachait un point de collecte à sa commune en
 * comparant des <i>libellés</i> — ceux des fichiers SIG et ceux du référentiel, qui divergent.
 * Sur les 71 points réels : 31 rattachements certains, <b>25 tirés au hasard</b> parmi plusieurs
 * candidats, 15 sans rattachement. La position, elle, ne se discute pas : 69 sur 71 tombent dans
 * exactement une commune.
 *
 * <p><b>Pourquoi le lancer de rayon et non un rectangle englobant.</b> Les communes de Pikine ne
 * sont pas rectangulaires ; un test d'englobant rattacherait au territoire des points situés dans
 * ses concavités. L'algorithme compte les intersections d'un rayon horizontal avec le contour :
 * impair, le point est dedans.
 *
 * <p>Les coordonnées sont en <b>WGS84</b> depuis l'ADR-0015, et l'ordre est
 * {@code (longitude, latitude)} — l'abscisse d'abord, comme en géométrie plane. À l'échelle d'une
 * commune, traiter des degrés comme un plan est sans conséquence sur l'appartenance.
 */
public final class PolygonContainment {

    private PolygonContainment() {
    }

    /**
     * @param ring      contour fermé implicitement, chaque élément étant {@code [longitude, latitude]}
     * @param longitude abscisse du point testé
     * @param latitude  ordonnée du point testé
     * @return {@code false} pour un contour dégénéré : le référentiel n'est pas parfait, et une
     *         géométrie vide ne doit pas interrompre l'import de tout un fichier
     */
    public static boolean contains(List<double[]> ring, double longitude, double latitude) {
        if (ring == null || ring.size() < 3) {
            return false;
        }
        boolean dedans = false;
        int j = ring.size() - 1;
        for (int i = 0; i < ring.size(); i++) {
            double xi = ring.get(i)[0];
            double yi = ring.get(i)[1];
            double xj = ring.get(j)[0];
            double yj = ring.get(j)[1];

            // Le rayon part du point vers les x croissants : on ne compte le segment que s'il
            // enjambe l'ordonnée du point, et seulement du côté droit.
            if ((yi > latitude) != (yj > latitude)
                    && longitude < (xj - xi) * (latitude - yi) / (yj - yi) + xi) {
                dedans = !dedans;
            }
            j = i;
        }
        return dedans;
    }
}
