package sn.smartwaste.collect.administration.application.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import sn.smartwaste.collect.administration.application.service.UploadFileService;
import sn.smartwaste.collect.shared.domain.model.ImportedFeature;
import sn.smartwaste.collect.territory.application.api.TerritoryImportPort;
import sn.smartwaste.collect.waste.application.api.WasteImportPort;

/**
 * Import des données de référence GeoJSON.
 *
 * <p><b>Ce service ne connaît plus aucune entité ni aucun repository.</b> Il lit un fichier, en
 * extrait des {@link ImportedFeature} — des attributs bruts et un contour — et les remet au
 * contexte propriétaire, qui décide seul de leur signification. C'est ce qui a permis de refermer
 * les trois interfaces nommées ouvertes en transition
 * ({@code waste.domain.model}, {@code waste.domain.repository}, {@code territory.domain.model}).
 *
 * <p>Auparavant il écrivait directement dans <b>12 repositories</b> de deux contextes et savait que
 * {@code COD_DEPT} est un code de département ou que {@code Type_de_Mo} est un type de point de
 * collecte. Ce savoir appartenait aux contextes, pas à l'import : il y est retourné
 * ({@code TerritoryImportAdapter}, {@code WasteImportAdapter}).
 *
 * <p><b>Ce qui reste ici est le seul métier de l'import</b> : lire du GeoJSON ArcGIS
 * ({@code features} / {@code attributes} / {@code geometry}, anneaux {@code rings} ou tracés
 * {@code paths}) et signaler ce qui s'est passé.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UploadFileServiceImpl implements UploadFileService {

    private static final String EMPTY = "File is empty";
    private static final String OK = "File uploaded and processed successfully";
    private static final String READ_FAILED = "Failed to read file";
    private static final String PARSE_FAILED = "Error processing JSON file";

    private final TerritoryImportPort territory;
    private final WasteImportPort waste;

    @Override
    public String uploadDataDepartment(MultipartFile file) {
        if (!territory.hasRegion()) {
            return "Region not found";
        }
        return process(file, territory::importDepartment);
    }

    @Override
    public String uploadDataCommune(MultipartFile file) {
        if (!territory.hasDepartment()) {
            return "Department not found";
        }
        return process(file, territory::importCommune);
    }

    @Override
    public String uploadDataQuartier(MultipartFile file) {
        return process(file, territory::importQuartier);
    }

    @Override
    public String uploadDataCircuitCollect(MultipartFile file) {
        return processWithCommune(file, "Sectection", waste::importCircuitCollect);
    }

    @Override
    public String uploadDataCircuitBalayage(MultipartFile file) {
        return processWithCommune(file, "commune", waste::importCircuitBalayage);
    }

    @Override
    public String uploadDataDepotoir(MultipartFile file) {
        return processWithCommune(file, "Commune", waste::importDepotoir);
    }

    // ------------------------------------------------------------------------------------

    /** Parcourt le fichier et remet chaque entité au contexte propriétaire. */
    private String process(MultipartFile file, Consumer<ImportedFeature> sink) {
        return forEachFeature(file, (feature, ignored) -> sink.accept(feature), null);
    }

    /**
     * Variante pour les entités rattachées à une commune : le nom lu dans le fichier est résolu en
     * identifiant par le référentiel territorial, seul habilité à le faire.
     */
    private String processWithCommune(MultipartFile file, String communeAttribute,
                                      BiConsumer<ImportedFeature, UUID> sink) {
        return forEachFeature(file, sink, communeAttribute);
    }

    private String forEachFeature(MultipartFile file, BiConsumer<ImportedFeature, UUID> sink,
                                  String communeAttribute) {
        if (file.isEmpty()) {
            return EMPTY;
        }
        try {
            var jsonArray = new JSONArray(readContent(file.getInputStream()));
            int imported = 0;

            for (int i = 0; i < jsonArray.length(); i++) {
                var root = jsonArray.getJSONObject(i);
                if (!root.has("features") || !(root.get("features") instanceof JSONArray features)) {
                    continue;
                }
                // Le type de géométrie et le référentiel spatial sont déclarés une fois, à la
                // racine du fichier, et valent pour toutes ses entités.
                var shapeHeader = jsonArray.getJSONObject(0);

                for (int j = 0; j < features.length(); j++) {
                    var feature = features.getJSONObject(j);
                    if (!feature.has("attributes") || !feature.has("geometry")) {
                        continue;
                    }
                    var attributes = toMap(feature.getJSONObject("attributes"));
                    var imported$ = new ImportedFeature(attributes,
                            toShape(shapeHeader, feature.getJSONObject("geometry")));

                    UUID communeId = null;
                    if (communeAttribute != null) {
                        communeId = territory.findCommuneIdByName(imported$.text(communeAttribute))
                                .orElse(null);
                    }
                    sink.accept(imported$, communeId);
                    imported++;
                }
            }
            log.info("Import termine : {} entites traitees", imported);
            return OK;

        } catch (IOException e) {
            log.error(READ_FAILED, e);
            return READ_FAILED;
        } catch (Exception e) {
            log.error(PARSE_FAILED, e);
            return PARSE_FAILED;
        }
    }

    private String readContent(InputStream inputStream) throws IOException {
        var content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }
        return content.toString();
    }

    private Map<String, Object> toMap(JSONObject attributes) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (String key : attributes.keySet()) {
            Object value = attributes.get(key);
            map.put(key, JSONObject.NULL.equals(value) ? null : value);
        }
        return map;
    }

    private ImportedFeature.GeoShape toShape(JSONObject header, JSONObject geometry) {
        if (!header.has("geometryType") || !header.has("spatialReference")) {
            return null;
        }
        return new ImportedFeature.GeoShape(
                header.getString("geometryType"),
                header.getJSONObject("spatialReference").toString(),
                toPoints(geometry));
    }

    private List<ImportedFeature.GeoPoint> toPoints(JSONObject geometry) {
        List<ImportedFeature.GeoPoint> points = new ArrayList<>();
        if (geometry.has("rings")) {
            addAll(points, geometry.getJSONArray("rings"));
        } else if (geometry.has("paths")) {
            addAll(points, geometry.getJSONArray("paths"));
        } else if (geometry.has("x") && geometry.has("y")) {
            points.add(new ImportedFeature.GeoPoint(
                    String.valueOf(geometry.getDouble("x")), String.valueOf(geometry.getDouble("y"))));
        }
        return points;
    }

    /** Un anneau ou un tracé est un tableau de tableaux [x, y]. */
    private void addAll(List<ImportedFeature.GeoPoint> points, JSONArray ringsOrPaths) {
        for (int n = 0; n < ringsOrPaths.length(); n++) {
            JSONArray ringOrPath = ringsOrPaths.getJSONArray(n);
            for (int k = 0; k < ringOrPath.length(); k++) {
                JSONArray pair = ringOrPath.getJSONArray(k);
                points.add(new ImportedFeature.GeoPoint(
                        String.valueOf(pair.getDouble(0)), String.valueOf(pair.getDouble(1))));
            }
        }
    }
}
