package sn.smartwaste.collect.administration.infrastructure.geojson;

import sn.smartwaste.collect.administration.application.service.GeoJsonImportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Rejoue l'import GeoJSON au démarrage lorsque {@code sonaged.import.geojson.on-startup=true} (P1-5).
 *
 * <p>Désactivé par défaut : l'import est idempotent (saute les entités déjà peuplées) mais
 * reste une opération lourde ; on préfère un déclenchement explicite (endpoint) en général.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GeoJsonImportRunner implements ApplicationRunner {

    private final GeoJsonImportService importService;

    @Value("${sonaged.import.geojson.on-startup:false}")
    private boolean onStartup;

    @Override
    public void run(ApplicationArguments args) {
        if (!onStartup) {
            return;
        }
        log.info("Import GeoJSON au démarrage (sonaged.import.geojson.on-startup=true)…");
        importService.importAll(false).forEach((step, result) -> log.info("  {} -> {}", step, result));
    }
}
