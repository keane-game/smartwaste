package sn.smartwaste.collect.shared.infrastructure.persistence;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.uuid.UuidValueGenerator;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Générateur d'identifiants <b>UUID version 7</b> (RFC 9562), sans dépendance externe.
 *
 * <p><b>Pourquoi pas un UUID v4 ?</b> Un v4 est entièrement aléatoire : chaque insertion tombe à
 * un endroit imprévisible de l'index B-tree de PostgreSQL. L'index se fragmente, les pages sont
 * salies un peu partout, et le débit d'écriture s'effondre à mesure que la table grossit — un
 * effet très concret sur des milliers de points de collecte alimentés par de la télémétrie.
 * Un UUID v7 préfixe l'identifiant d'un horodatage : les valeurs successives restent voisines,
 * on retrouve la localité d'insertion d'un {@code BIGSERIAL} tout en gardant des identifiants
 * non devinables et générables côté client (utile pour un futur découpage en microservices,
 * où l'on ne peut plus compter sur une séquence centrale).
 *
 * <p>Disposition binaire (RFC 9562 §5.7) :
 * <pre>
 *   48 bits  horodatage Unix en millisecondes
 *    4 bits  version (0b0111 = 7)
 *   12 bits  aléatoire (rand_a)
 *    2 bits  variante (0b10)
 *   62 bits  aléatoire (rand_b)
 * </pre>
 *
 * <p>{@link SecureRandom} et non {@code Random} : ces identifiants circulent dans les URL de
 * l'API ; une suite prédictible permettrait d'énumérer les ressources d'autrui.
 */
public final class UuidV7Generator implements UuidValueGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    /** Masque des 48 bits d'horodatage. */
    private static final long TIMESTAMP_MASK = 0xFFFF_FFFF_FFFFL;

    @Override
    public UUID generateUuid(SharedSessionContractImplementor session) {
        return generate();
    }

    /** Génère un UUID v7. Utilisable hors Hibernate (tests, identifiants assignés côté application). */
    public static UUID generate() {
        byte[] random = new byte[10];
        RANDOM.nextBytes(random);

        long timestamp = System.currentTimeMillis() & TIMESTAMP_MASK;

        // Poids fort : horodatage (bits 63..16) | version 7 (bits 15..12) | rand_a (bits 11..0)
        long mostSignificant = timestamp << 16
                | 0x7000L
                | ((random[0] & 0x0FL) << 8)
                | (random[1] & 0xFFL);

        // Poids faible : variante 0b10 (bits 63..62) | rand_b (bits 61..0)
        long leastSignificant = 0L;
        for (int i = 2; i < random.length; i++) {
            leastSignificant = (leastSignificant << 8) | (random[i] & 0xFFL);
        }
        leastSignificant &= 0x3FFF_FFFF_FFFF_FFFFL;
        leastSignificant |= 0x8000_0000_0000_0000L;

        return new UUID(mostSignificant, leastSignificant);
    }

    /**
     * Constructeur public <b>obligatoire</b> : Hibernate instancie la classe par réflexion
     * lorsqu'elle est déclarée dans {@code @UuidGenerator(algorithm = ...)}. Le rendre privé
     * compilerait sans erreur mais ferait échouer le démarrage.
     */
    public UuidV7Generator() {
        // sans état
    }
}
