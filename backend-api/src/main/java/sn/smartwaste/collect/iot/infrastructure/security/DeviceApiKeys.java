package sn.smartwaste.collect.iot.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Empreinte des clés d'API des équipements de terrain.
 *
 * <p>Factorisé parce qu'un second type d'équipement est arrivé (les traceurs de véhicules) : deux
 * implémentations divergentes du hachage seraient le meilleur moyen de rendre un jour des clés
 * invérifiables.
 *
 * <p>SHA-256 sans sel, volontairement : une clé de device est un secret à forte entropie, pas un
 * mot de passe. Un hachage lent coûterait à chaque message reçu — et un capteur en émet en continu —
 * sans rien apporter contre un dictionnaire, qui n'a aucune prise sur 256 bits d'aléa.
 */
public final class DeviceApiKeys {

    private DeviceApiKeys() {
        // utilitaire
    }

    public static String hash(String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(apiKey.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible sur cette JVM", e);
        }
    }
}
