package sn.smartwaste.collect.administration.application.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import sn.smartwaste.collect.administration.application.service.CoordinateProjector;
import sn.smartwaste.collect.administration.infrastructure.geojson.InMemoryMultipartFile;
import sn.smartwaste.collect.shared.domain.model.ImportedFeature;
import sn.smartwaste.collect.territory.application.api.TerritoryImportPort;
import sn.smartwaste.collect.waste.application.api.WasteImportPort;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * L'import livre aux contextes des coordonnées WGS84, pas les mètres projetés du fichier
 * (ADR-0015 §1).
 *
 * <p>{@link sn.smartwaste.collect.administration.application.service.CoordinateProjector} est vérifié
 * séparément sur des points de contrôle. Ce test-ci garde la <b>jonction</b> : que la conversion soit
 * réellement appliquée au moment de la lecture, et pour les deux formes de géométrie — les points
 * isolés ({@code x}/{@code y}, dépôts) comme les anneaux et tracés ({@code rings}/{@code paths},
 * communes, quartiers, circuits). L'erreur d'origine touchait les deux chemins.
 */
class UploadFileProjectionTest {

    /** Point réel de {@code depotoir.json}, en UTM 28N. */
    private static final double EASTING_REEL = 239504.8017;
    private static final double NORTHING_REEL = 1631115.84;

    private final CapturingTerritoryPort territory = new CapturingTerritoryPort();
    private final UploadFileServiceImpl service =
            new UploadFileServiceImpl(territory, new NoOpWastePort(), new CoordinateProjector());

    @Test
    @DisplayName("un point isolé est livré en degrés, latitude puis longitude")
    void projectsStandalonePoints() {
        service.uploadDataQuartier(geoJson("""
                {"geometryType":"esriGeometryPoint","spatialReference":{"wkid":32628}},
                {"features":[{"attributes":{"CCRCA":"Pikine"},
                              "geometry":{"x":%s,"y":%s}}]}
                """.formatted(EASTING_REEL, NORTHING_REEL)));

        var point = territory.captured.getFirst().geometry().points().getFirst();

        assertThat(Double.parseDouble(point.latitude())).isBetween(14.6, 14.9);
        assertThat(Double.parseDouble(point.longitude())).isBetween(-17.5, -17.1);
    }

    @Test
    @DisplayName("les anneaux d'un polygone sont projetés comme les points isolés")
    void projectsPolygonRings() {
        // Les communes, quartiers et circuits passent par ce chemin : le laisser en mètres ferait
        // une carte dont les points seraient justes et les contours absents.
        service.uploadDataQuartier(geoJson("""
                {"geometryType":"esriGeometryPolygon","spatialReference":{"wkid":32628}},
                {"features":[{"attributes":{"CCRCA":"Pikine"},
                              "geometry":{"rings":[[[%s,%s]]]}}]}
                """.formatted(EASTING_REEL, NORTHING_REEL)));

        var point = territory.captured.getFirst().geometry().points().getFirst();

        assertThat(Double.parseDouble(point.latitude())).isBetween(14.6, 14.9);
        assertThat(Double.parseDouble(point.longitude())).isBetween(-17.5, -17.1);
    }

    private InMemoryMultipartFile geoJson(String body) {
        return new InMemoryMultipartFile("f.json", "application/json",
                ("[" + body + "]").getBytes(StandardCharsets.UTF_8));
    }

    /** Capture ce que l'import remet au contexte territorial. */
    private static final class CapturingTerritoryPort implements TerritoryImportPort {
        private final List<ImportedFeature> captured = new ArrayList<>();

        @Override public void importQuartier(ImportedFeature feature) { captured.add(feature); }

        @Override public boolean hasRegion() { return true; }
        @Override public boolean hasDepartment() { return true; }
        @Override public void importDepartment(ImportedFeature feature) { captured.add(feature); }
        @Override public void importCommune(ImportedFeature feature) { captured.add(feature); }
        @Override public Optional<UUID> findCommuneIdByName(String name) { return Optional.empty(); }
        @Override public long countDepartments() { return 0; }
        @Override public long countCommunes() { return 0; }
        @Override public long countQuartiers() { return 0; }
        @Override public sn.smartwaste.collect.territory.domain.model.GeometryEntity
                newGeometry(ImportedFeature feature) { return null; }
        @Override public List<CommuneBoundary> communeBoundaries() { return List.of(); }
    }

    private static final class NoOpWastePort implements WasteImportPort {
        @Override public void importCircuitCollect(ImportedFeature f, UUID communeId) { }
        @Override public void importCircuitBalayage(ImportedFeature f, UUID communeId) { }
        @Override public void importDepotoir(ImportedFeature f, UUID communeId) { }
        @Override public long countCircuitCollects() { return 0; }
        @Override public long countCircuitBalayages() { return 0; }
        @Override public long countDepotoirs() { return 0; }
    }
}
