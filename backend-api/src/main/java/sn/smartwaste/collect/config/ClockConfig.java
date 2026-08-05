package sn.smartwaste.collect.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Horloge injectable.
 *
 * <p>Les traitements planifies (rappels de collecte, purge du soft-delete) dependent de l'heure.
 * Appeler {@code LocalTime.now()} en dur les rendrait intestables autrement qu'en attendant le bon
 * moment de la journee ; un {@link Clock} injecte permet de figer l'instant dans un test.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }
}
