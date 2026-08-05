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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
    /**
     * Tous les fichiers de points de collecte (ADR-0015 §3).
     *
     * <p>Ils portent le <b>même schéma d'attributs</b> que {@code depotoir.json}
     * ({@code FID, OBJECTID, R_gion, Commune, Type_de_Mo, Adresse_de, X, Y}) et sont déjà
     * discriminés par {@code Type_de_Mo} ({@code PP}, {@code PRN}, {@code Bac de rue},
     * {@code Caisse Polybenne}) : ils passent donc par le chemin d'import existant, sans analyseur
     * nouveau. Seul {@code depotoir.json} était lu — les 6 autres, soit 112 entrées de données
     * réelles, ne l'étaient par personne.
     *
     * <p>{@code pre_collecte.json} reste hors périmètre : son schéma
     * ({@code FID, Id, Nom, Commune}) ne porte ni type ni adresse et décrit une organisation de
     * pré-collecte, pas un point physique.
     */
    private static final List<String> FICHIERS_POINTS = List.of(
            "depotoir.json",
            "depotoir2.json",
            "bac_rue.json",
            "point_pp.json",
            "CAISSES POLYBENNE.json",
            "ppef_cp.json",
            "pp_pnr_pp-pnr.json");

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
        results.put("depotoir", stepPoints(waste.countDepotoirs(), force));
        return results;
    }

    /**
     * Les points de collecte, en une seule passe sur tous les fichiers (ADR-0015 §2-3).
     *
     * <p>Ces 7 exports ne sont pas disjoints : 132 entrées pour 71 positions distinctes, 49
     * coordonnées figurant dans plusieurs fichiers. Les importer par appels successifs les
     * dupliquerait — le dédoublonnage n'a de sens qu'à l'échelle de l'ensemble.
     */
    private String stepPoints(long existingCount, boolean force) {
        if (existingCount > 0 && !force) {
            return "ignoré (déjà %d enregistrement(s))".formatted(existingCount);
        }
        var files = new ArrayList<MultipartFile>();
        var manquants = new ArrayList<String>();
        for (String filename : FICHIERS_POINTS) {
            try {
                files.add(load(filename));
            } catch (IOException e) {
                // Un fichier absent ne doit pas emporter les six autres.
                log.warn("Fichier de points introuvable, ignoré : {}", filename);
                manquants.add(filename);
            }
        }
        if (files.isEmpty()) {
            return "erreur : aucun fichier de points lisible";
        }
        String rapport = uploadFileService.uploadDataDepotoirs(files);
        return manquants.isEmpty() ? rapport
                : rapport + " ; fichiers absents : " + String.join(", ", manquants);
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
