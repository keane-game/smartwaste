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
 * Dédoublonnage des points de collecte répartis sur plusieurs fichiers (ADR-0015 §2-3).
 *
 * <p><b>Le fait qui impose ce comportement.</b> Les 7 fichiers de points de {@code datas/} ne sont
 * pas des jeux disjoints : ce sont des exports successifs des mêmes couches SIG, filtrés
 * différemment. Ils totalisent <b>132 entrées pour 71 coordonnées distinctes</b> — 49 coordonnées
 * figurent dans plusieurs fichiers. Le point {@code (239504.80, 1631115.84)} est par exemple présent
 * dans {@code depotoir.json}, {@code point_pp.json} et {@code pp_pnr_pp-pnr.json}. Les importer
 * fichier par fichier gonflerait le référentiel de 86 % en doublons.
 *
 * <p>L'idempotence existante ne peut rien y faire : elle compare un {@code count()} global à zéro et
 * saute l'étape entière, ce qui règle le cas du second import mais pas celui de deux fichiers qui se
 * recouvrent au sein du même passage.
 */
class UploadFileDepotoirDedupTest {

    private static final double EASTING = 239504.8017;
    private static final double NORTHING = 1631115.84;

    private final CountingWastePort waste = new CountingWastePort();
    private final UploadFileServiceImpl service =
            new UploadFileServiceImpl(new StubTerritoryPort(), waste, new CoordinateProjector());

    @Test
    @DisplayName("une coordonnée présente dans deux fichiers n'est importée qu'une fois")
    void importsASharedPointOnlyOnce() {
        var rapport = service.uploadDataDepotoirs(List.of(
                pointFile(EASTING, NORTHING, "PP"),
                pointFile(EASTING, NORTHING, "PP")));

        assertThat(waste.imported).hasSize(1);
        assertThat(rapport).contains("1 importé").contains("1 doublon");
    }

    @Test
    @DisplayName("deux points distincts du même fichier sont tous les deux importés")
    void keepsDistinctPoints() {
        service.uploadDataDepotoirs(List.of(
                pointFile(EASTING, NORTHING, "PP"),
                pointFile(245580.2036, 1631605.9815, "PP")));

        assertThat(waste.imported).hasSize(2);
    }

    @Test
    @DisplayName("une même position portant deux types de mobilier reste deux points")
    void distinguishesByType() {
        // Un bac de rue et une caisse polybenne peuvent partager une adresse : c'est le couple
        // (position, type) qui identifie un point de collecte, pas la position seule.
        service.uploadDataDepotoirs(List.of(
                pointFile(EASTING, NORTHING, "PP"),
                pointFile(EASTING, NORTHING, "Bac de rue")));

        assertThat(waste.imported).hasSize(2);
    }

    @Test
    @DisplayName("un point dont la commune est introuvable est importé et signalé")
    void reportsPointsWithoutCommune() {
        // Les libellés de commune des fichiers divergent de ceux du référentiel (« Dalifort » contre
        // « daliford », « Guinaw rails » contre « guinaw rail nord »). Rejeter ces points les ferait
        // disparaître en silence ; on les garde et on le dit.
        var rapport = service.uploadDataDepotoirs(List.of(pointFile(EASTING, NORTHING, "PP")));

        assertThat(waste.imported).hasSize(1);
        assertThat(rapport).contains("1 sans commune");
    }

    private InMemoryMultipartFile pointFile(double x, double y, String type) {
        String body = """
                [{"geometryType":"esriGeometryPoint","spatialReference":{"wkid":32628}},
                 {"features":[{"attributes":{"Commune":"Introuvable","Type_de_Mo":"%s",
                                             "Adresse_de":"quelque part"},
                               "geometry":{"x":%s,"y":%s}}]}]
                """.formatted(type, x, y);
        return new InMemoryMultipartFile("p.json", "application/json",
                body.getBytes(StandardCharsets.UTF_8));
    }

    private static final class CountingWastePort implements WasteImportPort {
        private final List<ImportedFeature> imported = new ArrayList<>();

        @Override public void importDepotoir(ImportedFeature f, UUID communeId) { imported.add(f); }

        @Override public void importCircuitCollect(ImportedFeature f, UUID communeId) { }
        @Override public void importCircuitBalayage(ImportedFeature f, UUID communeId) { }
        @Override public long countCircuitCollects() { return 0; }
        @Override public long countCircuitBalayages() { return 0; }
        @Override public long countDepotoirs() { return 0; }
    }

    private static final class StubTerritoryPort implements TerritoryImportPort {
        @Override public Optional<UUID> findCommuneIdByName(String name) { return Optional.empty(); }
        @Override public boolean hasRegion() { return true; }
        @Override public boolean hasDepartment() { return true; }
        @Override public void importDepartment(ImportedFeature f) { }
        @Override public void importCommune(ImportedFeature f) { }
        @Override public void importQuartier(ImportedFeature f) { }
        @Override public long countDepartments() { return 0; }
        @Override public long countCommunes() { return 0; }
        @Override public long countQuartiers() { return 0; }
    }
}
