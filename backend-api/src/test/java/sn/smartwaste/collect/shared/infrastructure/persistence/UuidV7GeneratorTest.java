package sn.smartwaste.collect.shared.infrastructure.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Vérifie que le générateur produit de véritables UUID v7 : c'est la propriété d'ordonnancement
 * temporel qui justifie tout le choix (localité d'insertion en index PostgreSQL). Un générateur
 * qui retomberait silencieusement sur de l'aléatoire pur passerait inaperçu sans ces contrôles.
 */
class UuidV7GeneratorTest {

    @Test
    @DisplayName("respecte la version 7 et la variante RFC 9562")
    void generate_hasVersion7AndRfcVariant() {
        UUID uuid = UuidV7Generator.generate();

        assertThat(uuid.version()).isEqualTo(7);
        // variante 0b10 => valeur 2 selon java.util.UUID
        assertThat(uuid.variant()).isEqualTo(2);
    }

    @Test
    @DisplayName("encode l'horodatage courant dans les 48 bits de poids fort")
    void generate_encodesCurrentTimestamp() {
        long before = System.currentTimeMillis();
        UUID uuid = UuidV7Generator.generate();
        long after = System.currentTimeMillis();

        long timestamp = uuid.getMostSignificantBits() >>> 16;

        assertThat(timestamp).isBetween(before, after);
    }

    @Test
    @DisplayName("croît avec le temps : c'est la propriété qui préserve la localité en index")
    void generate_isTimeOrdered() throws InterruptedException {
        UUID first = UuidV7Generator.generate();
        Thread.sleep(2); // franchit une frontière de milliseconde
        UUID second = UuidV7Generator.generate();

        // Comparaison non signée : UUID.compareTo compare des long SIGNÉS et donnerait un
        // résultat faux dès que le bit de poids fort bascule.
        assertThat(Long.compareUnsigned(first.getMostSignificantBits(),
                second.getMostSignificantBits())).isNegative();
    }

    @Test
    @DisplayName("ne produit pas de collision sur un gros volume")
    void generate_isUnique() {
        int count = 50_000;
        List<UUID> generated = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            generated.add(UuidV7Generator.generate());
        }

        Set<UUID> distinct = new HashSet<>(generated);
        assertThat(distinct).hasSize(count);
    }
}
