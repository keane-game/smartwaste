package sn.smartwaste.collect.administration.application.service.impl;

import sn.smartwaste.collect.administration.application.service.GeoJsonImportService;
import sn.smartwaste.collect.administration.infrastructure.geojson.InMemoryMultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sn.smartwaste.collect.administration.application.service.UploadFileService;
import sn.smartwaste.collect.territory.application.api.TerritoryImportPort;
import sn.smartwaste.collect.waste.application.api.WasteImportPort;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Import automatisé des GeoJSON (P1-5).
 *
 * <p>Réutilise {@link UploadFileService} (parsing déjà en place) en enveloppant chaque fichier
 * dans un {@link InMemoryMultipartFile}. Les fichiers sont résolus via {@link ResourceLoader}
 * à partir de {@code sonaged.import.geojson.location} (préfixe {@code file:} ou {@code classpath:}).
 * L'ordre respecte les dépendances (un import s'appuie sur les entités créées par le précédent).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GeoJsonImportServiceImpl implements GeoJsonImportService {

    // Noms des fichiers dans datas/ (repère : lancement du backend depuis ucgBackend/, datas/ à la racine).
    private static final String FILE_DEPARTMENT = "LIMITE DELEGATION DEPARTEMENTALE DE PIKINE.json";
    private static final String FILE_COMMUNE = "commune.json";
    private static final String FILE_QUARTIER = "QUARTIERS.json";
    private static final String FILE_CIRCUIT_COLLECT = "circuit_collect.json";
    private static final String FILE_CIRCUIT_BALAYAGE = "circuit_balay.json";
    private static final String FILE_DEPOTOIR = "depotoir.json";

    private final UploadFileService uploadFileService;
    private final ResourceLoader resourceLoader;

    /** Compteurs publies par les contextes proprietaires : plus aucun repository ici. */
    private final TerritoryImportPort territory;
    private final WasteImportPort waste;

    @Value("${sonaged.import.geojson.location:file:../datas}")
    private String location;

    @Override
    public Map<String, String> importAll(boolean force) {
        var results = new LinkedHashMap<String, String>();
        results.put("department", step(FILE_DEPARTMENT, territory.countDepartments(), force,
                uploadFileService::uploadDataDepartment));
        results.put("commune", step(FILE_COMMUNE, territory.countCommunes(), force,
                uploadFileService::uploadDataCommune));
        results.put("quartier", step(FILE_QUARTIER, territory.countQuartiers(), force,
                uploadFileService::uploadDataQuartier));
        results.put("circuitCollect", step(FILE_CIRCUIT_COLLECT, waste.countCircuitCollects(), force,
                uploadFileService::uploadDataCircuitCollect));
        results.put("circuitBalayage", step(FILE_CIRCUIT_BALAYAGE, waste.countCircuitBalayages(), force,
                uploadFileService::uploadDataCircuitBalayage));
        results.put("depotoir", step(FILE_DEPOTOIR, waste.countDepotoirs(), force,
                uploadFileService::uploadDataDepotoir));
        return results;
    }

    private String step(String filename, long existingCount, boolean force, Function<MultipartFile, String> importer) {
        if (existingCount > 0 && !force) {
            return "ignoré (déjà %d enregistrement(s))".formatted(existingCount);
        }
        try {
            MultipartFile file = load(filename);
            String result = importer.apply(file);
            log.info("Import GeoJSON [{}] : {}", filename, result);
            return result;
        } catch (IOException e) {
            log.error("Import GeoJSON échoué pour {}", filename, e);
            return "erreur : " + e.getMessage();
        }
    }

    private MultipartFile load(String filename) throws IOException {
        String base = location.endsWith("/") ? location : location + "/";
        Resource resource = resourceLoader.getResource(base + filename);
        if (!resource.exists()) {
            throw new FileNotFoundException("Ressource GeoJSON introuvable : " + base + filename);
        }
        try (InputStream in = resource.getInputStream()) {
            return new InMemoryMultipartFile(filename, "application/json", in.readAllBytes());
        }
    }
}
