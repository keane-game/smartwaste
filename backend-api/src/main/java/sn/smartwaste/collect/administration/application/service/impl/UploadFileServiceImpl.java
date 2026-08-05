package sn.smartwaste.collect.administration.application.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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

import sn.smartwaste.collect.administration.application.service.CoordinateProjector;
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
    private final CoordinateProjector projector;

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

    @Override
    public String uploadDataDepotoirs(List<? extends MultipartFile> files) {
        // Le dédoublonnage doit porter sur l'ENSEMBLE des fichiers, pas sur chacun : les 7 exports
        // se recouvrent (132 entrées pour 71 positions distinctes). D'où l'état partagé, local à
        // l'appel — un champ d'instance fuirait d'un import à l'autre.
        // Les contours sont lus UNE fois : les recharger par point rendait l'import interminable.
        var contours = territory.communeBoundaries();
        var dejaVues = new HashSet<String>();
        int importes = 0;
        int doublons = 0;
        int sansCommune = 0;

        for (MultipartFile file : files) {
            for (ImportedFeature feature : readFeatures(file)) {
                if (!dejaVues.add(cleDePoint(feature))) {
                    doublons++;
                    continue;
                }
                UUID communeId = resolveCommune(feature, "Commune", contours);
                if (communeId == null) {
                    sansCommune++;
                }
                waste.importDepotoir(feature, communeId);
                importes++;
            }
        }

        // Un import qui perd la moitié de ses entrées sans le dire est exactement ce qu'on cherche
        // à ne plus avoir : les trois compteurs sont rendus, pas seulement le succès.
        String rapport = "%d importé(s), %d doublon(s) ignoré(s), %d sans commune"
                .formatted(importes, doublons, sansCommune);
        log.info("Import des points de collecte : {}", rapport);
        return rapport;
    }

    /**
     * Rattache une entité à sa commune — <b>par sa position d'abord</b> (ADR-0018).
     *
     * <p>Le rapprochement par libellé était inexploitable : sur les 71 points réels, il donnait
     * 31 rattachements certains, <b>25 tirés au hasard</b> parmi plusieurs candidats (« Pikine »
     * correspond à trois communes du référentiel) et 15 sans rattachement. Les 25 étaient les
     * pires : invisibles et faux, ils corrompaient la tournée et le rapport de deux communes à la
     * fois. La position, elle, ne se discute pas — 69 des 71 points tombent dans exactement une
     * commune.
     *
     * <p>Le nom reste le repli pour les entités sans géométrie exploitable, et ne tranche plus
     * lorsqu'il est ambigu.
     */
    private UUID resolveCommune(ImportedFeature feature, String communeAttribute,
                                List<TerritoryImportPort.CommuneBoundary> contours) {
        var position = firstPointOf(feature);
        if (position != null) {
            var dedans = contours.stream()
                    .filter(c -> c.contains(position[0], position[1]))
                    .toList();
            // Une position n'appartient qu'a une commune. Plusieurs reponses signalent des contours
            // qui se chevauchent : mieux vaut ne rien affirmer que trancher au hasard — c'est
            // precisement le defaut que ce rattachement remplace.
            if (dedans.size() == 1) {
                return dedans.getFirst().communeId();
            }
        }
        return territory.findCommuneIdByName(feature.text(communeAttribute)).orElse(null);
    }

    /** Première position de l'entité, en WGS84 ; {@code null} si elle n'en porte aucune. */
    private double[] firstPointOf(ImportedFeature feature) {
        if (feature.geometry() == null || feature.geometry().points().isEmpty()) {
            return null;
        }
        var point = feature.geometry().points().getFirst();
        try {
            return new double[] { Double.parseDouble(point.latitude()),
                                  Double.parseDouble(point.longitude()) };
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    /**
     * Identité d'un point de collecte : sa position, au mètre, et son type.
     *
     * <p>Pas l'{@code OBJECTID} : il est attribué par couche d'export et se répète d'un fichier à
     * l'autre pour des points différents. La position, elle, identifie physiquement le point. Le
     * type entre dans la clé parce qu'un bac de rue et une caisse polybenne peuvent partager une
     * adresse sans être le même équipement.
     */
    private String cleDePoint(ImportedFeature feature) {
        var points = feature.geometry() == null ? List.<ImportedFeature.GeoPoint>of()
                : feature.geometry().points();
        String position = points.isEmpty() ? "?" : "%.5f/%.5f".formatted(
                Double.parseDouble(points.getFirst().latitude()),
                Double.parseDouble(points.getFirst().longitude()));
        return position + "|" + String.valueOf(feature.text("Type_de_Mo")).trim().toLowerCase(Locale.ROOT);
    }

    /** Lit les entités d'un fichier sans les dispatcher : le tri est fait par l'appelant. */
    private List<ImportedFeature> readFeatures(MultipartFile file) {
        var features = new ArrayList<ImportedFeature>();
        if (file.isEmpty()) {
            return features;
        }
        try {
            var jsonArray = new JSONArray(readContent(file.getInputStream()));
            var shapeHeader = jsonArray.getJSONObject(0);
            for (int i = 0; i < jsonArray.length(); i++) {
                var root = jsonArray.getJSONObject(i);
                if (!root.has("features") || !(root.get("features") instanceof JSONArray liste)) {
                    continue;
                }
                for (int j = 0; j < liste.length(); j++) {
                    var feature = liste.getJSONObject(j);
                    if (!feature.has("attributes") || !feature.has("geometry")) {
                        continue;
                    }
                    features.add(new ImportedFeature(
                            toMap(feature.getJSONObject("attributes")),
                            toShape(shapeHeader, feature.getJSONObject("geometry"))));
                }
            }
        } catch (IOException e) {
            log.error(READ_FAILED, e);
        } catch (Exception e) {
            log.error(PARSE_FAILED, e);
        }
        return features;
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
            var contours = communeAttribute == null ? List.<TerritoryImportPort.CommuneBoundary>of()
                    : territory.communeBoundaries();
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

                    UUID communeId = communeAttribute == null ? null
                            : resolveCommune(imported$, communeAttribute, contours);
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
            points.add(toWgs84(geometry.getDouble("x"), geometry.getDouble("y")));
        }
        return points;
    }

    /** Un anneau ou un tracé est un tableau de tableaux [x, y]. */
    private void addAll(List<ImportedFeature.GeoPoint> points, JSONArray ringsOrPaths) {
        for (int n = 0; n < ringsOrPaths.length(); n++) {
            JSONArray ringOrPath = ringsOrPaths.getJSONArray(n);
            for (int k = 0; k < ringOrPath.length(); k++) {
                JSONArray pair = ringOrPath.getJSONArray(k);
                points.add(toWgs84(pair.getDouble(0), pair.getDouble(1)));
            }
        }
    }

    /**
     * Seul point de passage des coordonnées du fichier vers le modèle (ADR-0015 §1).
     *
     * <p>Les fichiers sont en UTM 28N, en <b>mètres</b>. Ces valeurs partaient telles quelles dans un
     * record {@code (latitude, longitude)} — et dans l'ordre {@code (x, y)}, ce qui faisait de
     * l'abscisse une latitude. Chaque point aurait atterri à « latitude 239 504 », hors de la Terre
     * pour tout consommateur WGS84. Les deux formes de géométrie passaient par ce défaut : on les
     * fait donc converger ici, pour qu'aucune ne puisse être corrigée sans l'autre.
     */
    private ImportedFeature.GeoPoint toWgs84(double easting, double northing) {
        var wgs84 = projector.toWgs84(easting, northing);
        return new ImportedFeature.GeoPoint(
                String.valueOf(wgs84.latitude()), String.valueOf(wgs84.longitude()));
    }
}
