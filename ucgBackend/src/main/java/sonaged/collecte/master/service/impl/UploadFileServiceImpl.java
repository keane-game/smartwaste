package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sonaged.collecte.master.enums.CircuitShift;
import sonaged.collecte.master.model.*;
// Le référentiel territorial a migré vers son module dédié : l'import « étoile » sur le package
// hérité ne le couvre plus, les entités territoriales sont donc importées nommément.
import sn.smartwaste.collect.territory.domain.model.CommuneEntity;
import sn.smartwaste.collect.territory.domain.model.CoordinateEntity;
import sn.smartwaste.collect.territory.domain.model.DepartmentEntity;
import sn.smartwaste.collect.territory.domain.model.GeometryEntity;
import sn.smartwaste.collect.territory.domain.model.QuartierEntity;
import sn.smartwaste.collect.territory.domain.model.RegionEntity;
import sonaged.collecte.master.repository.*;
// Le référentiel territorial a migré vers son module dédié : l'import « étoile » sur le package
// hérité ne couvre plus ses repositories, importés nommément ci-dessous.
import sn.smartwaste.collect.territory.domain.repository.CommuneRepository;
import sn.smartwaste.collect.territory.domain.repository.CoordinateRepository;
import sn.smartwaste.collect.territory.domain.repository.DepartmentRepository;
import sn.smartwaste.collect.territory.domain.repository.GeometryRepository;
import sn.smartwaste.collect.territory.domain.repository.QuartierRepository;
import sn.smartwaste.collect.territory.domain.repository.RegionRepository;
import sonaged.collecte.master.service.UploadFileService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UploadFileServiceImpl implements UploadFileService {

    final CircuitRepository circuitRepository;
    final CommuneRepository communeRepository;
    final CircuitCollectRepository circuitCollectRepository;
    final CircuitBalayageRepository circuitBalayageRepository;
    final DepartmentRepository departmentRepository;
    final RegionRepository regionRepository;
    final QuartierRepository quartierRepository;
    final TypeDepotoirRepository typeDepotoirRepository;
    final DepotoirRepository depotoirRepository;
    final GeometryRepository geometryRepository;
    final CoordinateRepository coordinateRepository;
    final MoblierUrbainRepository moblierUrbainRepository;

    private static final String CREATED_BY = "system";
    private static final String LAST_MODIFIED_BY = "system";


    @Override
    public String uploadDataDepartment(MultipartFile file) {
        if (file.isEmpty()) {
            return "File is empty";
        }

        try {
            var jsonContent = readContent(file.getInputStream());
            var jsonArray = new JSONArray(jsonContent);

            // Les PK du référentiel sont des UUID : l'identifiant « 1 » du script de seed
            // n'existe plus. L'import vise la région de référence unique du jeu de données
            // (Dakar) — on prend donc la première enregistrée plutôt qu'un identifiant codé en dur.
            var regionOpt = regionRepository.findAll().stream().findFirst();
            if (regionOpt.isEmpty ()) {
                return "Department not found";
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                var jsonObject = jsonArray.getJSONObject(i);

                if (jsonObject.has("features") && jsonObject.get("features") instanceof JSONArray) {
                    var geometry = parseGeometries(jsonArray.getJSONObject(0));
                    var featuresArray = jsonObject.getJSONArray("features");
                    for (int j = 0; j < featuresArray.length(); j++) {
                        var jsonObjectFeatures = featuresArray.getJSONObject(j);
                        log.info("featuresArray {}", featuresArray.length());

                        if (jsonObjectFeatures.has("attributes") && jsonObjectFeatures.has("geometry")) {
                            var department = parseDepartment(jsonObjectFeatures.getJSONObject("attributes"), regionOpt.get ());
                            var coordList = parseCoordinates(jsonObjectFeatures.getJSONObject("geometry"));

                            assert geometry != null;
                            geometry.setCoordinates(coordList);
                            department.setGeometry(geometry);
                            departmentRepository.saveAndFlush (department);
                        }
                    }
                }
            }

            return "File uploaded and processed successfully";

        } catch (IOException e) {
            log.error("Failed to read file", e);
            return "Failed to read file";
        } catch (Exception e) {
            log.error("Error processing JSON file", e);
            return "Error processing JSON file";
        }
    }

    @Override
    public String uploadDataCommune(MultipartFile file) {
        if (file.isEmpty()) {
            return "File is empty";
        }

        try {
            var jsonContent = readContent(file.getInputStream());
            var jsonArray = new JSONArray(jsonContent);

            // Idem : plus d'identifiant numérique codé en dur depuis le passage aux UUID.
            var departmentOpt = departmentRepository.findAll().stream().findFirst();
            if (departmentOpt.isEmpty ()) {
                return "Department not found";
            }
            var department = departmentOpt.get();

            for (int i = 0; i < jsonArray.length(); i++) {
                var jsonObject = jsonArray.getJSONObject(i);



                if (jsonObject.has("features") && jsonObject.get("features") instanceof JSONArray) {
                    var featuresArray = jsonObject.getJSONArray("features");

                    for (int j = 0; j < featuresArray.length(); j++) {
                        var geometry = parseGeometries(jsonArray.getJSONObject(0));
                        var jsonObjectFeatures = featuresArray.getJSONObject(j);

                        if (jsonObjectFeatures.has("attributes") && jsonObjectFeatures.has("geometry")) {
                            CommuneEntity commune = parseCommune(jsonObjectFeatures.getJSONObject("attributes"), department);
                            List<CoordinateEntity> coordList = parseCoordinates(jsonObjectFeatures.getJSONObject("geometry"));

                            assert geometry != null;
                            geometry.setGeometryId (null);
                            geometry.setCoordinates (coordList);

                            commune.setGeometry(geometry);
                            communeRepository.saveAndFlush(commune);

                           // log.info("commune {}", commune);
                        }
                    }
                }
            }

            return "File uploaded and processed successfully";

        } catch (IOException e) {
            log.error("Failed to read file", e);
            return "Failed to read file";
        } catch (Exception e) {
            log.error("Error processing JSON file", e);
            return "Error processing JSON file";
        }
    }

    @Override
    public String uploadDataQuartier(MultipartFile file) {

        if (file.isEmpty()) {
            return "File is empty";
        }
        log.info("test {}", "tert");
        try {
            var jsonContent = readContent(file.getInputStream());
            var jsonArray = new JSONArray(jsonContent);

            for (int i = 0; i < jsonArray.length(); i++) {
                var jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.has("features") && jsonObject.get("features") instanceof JSONArray) {
                    var featuresArray = jsonObject.getJSONArray("features");

                    for (int j = 0; j < featuresArray.length(); j++) {
                        var geometry = parseGeometries(jsonArray.getJSONObject(0));
                        var jsonObjectFeatures = featuresArray.getJSONObject(j);
                        log.info("test {}", "tert");

                        if (jsonObjectFeatures.has("attributes") && jsonObjectFeatures.has("geometry")) {
                            log.info("test1 {}", "tert");
                            var quartier = parseQuartier(jsonObjectFeatures.getJSONObject("attributes"));
                            var coordList = parseCoordinates(jsonObjectFeatures.getJSONObject("geometry"));

                            assert geometry != null;
                            geometry.setGeometryId (null);
                            geometry.setCoordinates (coordList);

                            // Ensure the coordinate entities are saved
                             //coordinateRepository.saveAll(coordList);

                            // Set the geometry to commune
                            quartier.setGeometry(geometry);

                            // Save the commune
                            quartierRepository.saveAndFlush(quartier);

                            log.info("commune {}", quartier);
                        }
                    }
                }
            }

            return "File uploaded and processed successfully";

        } catch (IOException e) {
            log.error("Failed to read file", e);
            return "Failed to read file";
        } catch (Exception e) {
            log.error("Error processing JSON file", e);
            return "Error processing JSON file";
        }
    }

    @Override
    public String uploadDataCircuitCollect(MultipartFile file) {
        if (file.isEmpty()) {
            return "File is empty";
        }

        try {
            var jsonContent = readContent(file.getInputStream());
            var jsonArray = new JSONArray(jsonContent);

            for (int i = 0; i < jsonArray.length(); i++) {
                var jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.has("features") && jsonObject.get("features") instanceof JSONArray) {
                    var featuresArray = jsonObject.getJSONArray("features");

                    for (int j = 0; j < featuresArray.length(); j++) {
                        var geometry = parseGeometries(jsonArray.getJSONObject(0));
                        var jsonObjectFeatures = featuresArray.getJSONObject(j);
                        //log.info("featuresArray {}", jsonObjectFeatures);

                        if (jsonObjectFeatures.has("attributes") && jsonObjectFeatures.has("geometry")) {
                            var nameCommune = jsonObjectFeatures.getJSONObject("attributes").getString ("Sectection");
                            var communeOpt = findCommuneByName (nameCommune);

                            var circuitCollect = parseCircuitCollect (jsonObjectFeatures.getJSONObject("attributes"),  communeOpt);
                            var coordList = parseCoordinates(jsonObjectFeatures.getJSONObject("geometry"));

                            assert geometry != null;
                            geometry.setGeometryId (null);
                            geometry.setCoordinates (coordList);

                            // Ensure the coordinate entities are saved
                            // coordinateRepository.saveAll(coordList);

                            // Set the geometry to commune
                            circuitCollect.setGeometry(geometry);

                            // Save the commune
                            circuitCollectRepository.saveAndFlush(circuitCollect);

                            log.info("commune {}", circuitCollect);
                        }
                    }
                }
            }

            return "File uploaded and processed successfully";

        } catch (IOException e) {
            log.error("Failed to read file", e);
            return "Failed to read file";
        } catch (Exception e) {
            log.error("Error processing JSON file", e);
            return "Error processing JSON file";
        }
    }

    @Override
    public String uploadDataCircuitBalayage(MultipartFile file) {
        if (file.isEmpty()) {
            return "File is empty";
        }
        try {
            var jsonContent = readContent(file.getInputStream());
            var jsonArray = new JSONArray(jsonContent);

            for (int i = 0; i < jsonArray.length(); i++) {
                var jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.has("features") && jsonObject.get("features") instanceof JSONArray) {
                    var featuresArray = jsonObject.getJSONArray("features");

                    for (int j = 0; j < featuresArray.length(); j++) {
                        var geometry = parseGeometries(jsonArray.getJSONObject(0));
                        var jsonObjectFeatures = featuresArray.getJSONObject(j);
                        //log.info("featuresArray {}", jsonObjectFeatures);

                        if (jsonObjectFeatures.has("attributes") && jsonObjectFeatures.has("geometry")) {
                            var nameCommune = jsonObjectFeatures.getJSONObject("attributes").getString ("commune");
                            var communeOpt = findCommuneByName (nameCommune);

                            var circuitBalayage = parseCircuitBalayage (jsonObjectFeatures.getJSONObject("attributes"),  communeOpt);
                            var coordList = parseCoordinates(jsonObjectFeatures.getJSONObject("geometry"));

                            assert geometry != null;
                            geometry.setGeometryId (null);
                            geometry.setCoordinates (coordList);

                            // Ensure the coordinate entities are saved
                            // coordinateRepository.saveAll(coordList);

                            // Set the geometry to commune
                            circuitBalayage.setGeometry(geometry);

                            // Save the commune
                            circuitBalayageRepository.saveAndFlush(circuitBalayage);

                            log.info("commune {}", circuitBalayage);
                        }
                    }
                }
            }

            return "File uploaded and processed successfully";

        } catch (IOException e) {
            log.error("Failed to read file", e);
            return "Failed to read file";
        } catch (Exception e) {
            log.error("Error processing JSON file", e);
            return "Error processing JSON file";
        }
    }

    @Override
    public String uploadDataDepotoir(MultipartFile file) {
        if (file.isEmpty()) {
            return "File is empty";
        }

        try {
            var jsonContent = readContent(file.getInputStream());
            var jsonArray = new JSONArray(jsonContent);

            for (int i = 0; i < jsonArray.length(); i++) {
                var jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.has("features") && jsonObject.get("features") instanceof JSONArray) {
                    var featuresArray = jsonObject.getJSONArray("features");

                    for (int j = 0; j < featuresArray.length(); j++) {
                        var geometry = parseGeometries(jsonArray.getJSONObject(0));
                        var jsonObjectFeatures = featuresArray.getJSONObject(j);
                        //log.info("featuresArray {}", jsonObjectFeatures);

                        if (jsonObjectFeatures.has("attributes") && jsonObjectFeatures.has("geometry")) {
                            var nameCommune = jsonObjectFeatures.getJSONObject("attributes").getString ("Commune");
                            var communeOpt = findCommuneByName (nameCommune);
                            if (communeOpt == null){
                                continue;
                            }
                            var depotoir = parseDepotoir(jsonObjectFeatures.getJSONObject("attributes"),  communeOpt);
                            var coordList = parseCoordinates(jsonObjectFeatures.getJSONObject("geometry"));

                            assert geometry != null;
                            geometry.setGeometryId (null);
                            geometry.setCoordinates (coordList);

                            // Ensure the coordinate entities are saved
                            // coordinateRepository.saveAll(coordList);

                            // Set the geometry to commune
                            depotoir.setGeometry(geometry);

                            // Save the commune
                            depotoirRepository.saveAndFlush(depotoir);

                            log.info("commune {}", depotoir);
                        }
                    }
                }
            }

            return "File uploaded and processed successfully";

        } catch (IOException e) {
            log.error("Failed to read file", e);
            return "Failed to read file";
        } catch (Exception e) {
            log.error("Error processing JSON file", e);
            return "Error processing JSON file";
        }
    }

    private String readContent(InputStream inputStream) throws IOException {
        var jsonContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
        }
        return jsonContent.toString();
    }

    private GeometryEntity parseGeometries(JSONObject jsonObject) {
        if (!jsonObject.has("geometryType") || !jsonObject.has("spatialReference")) {
            return null;
        }

        var geometry = new GeometryEntity();
        geometry.setType(jsonObject.getString("geometryType"));
        geometry.setSpatialReference(jsonObject.getJSONObject("spatialReference").toString());
        geometry.setCreatedBy ( "system");
        geometry.setLastModifiedBy ("system");
        geometry.setCreatedDate (LocalDateTime.now ());
        geometry.setLastModifiedDate (LocalDateTime.now ());
        geometry.setArchived (false);
        return geometry;
    }

    private DepartmentEntity parseDepartment(JSONObject attributesObject, RegionEntity region) {
        var department = new DepartmentEntity();
        department.setCode(attributesObject.getString("COD_DEPT"));
        department.setName(attributesObject.getString("DEPT"));
        department.setArea(attributesObject.getDouble("SUPERFICIE"));
        department.setEffectif(attributesObject.getInt("EFFECTIF"));
        department.setCreatedBy ( "system");
        department.setLastModifiedBy ("system");
        department.setCreatedDate (LocalDateTime.now ());
        department.setLastModifiedDate (LocalDateTime.now ());
        department.setArchived (false);
        department.setRegion (region);
        return department;
    }

    private CommuneEntity parseCommune(JSONObject attributesObject, DepartmentEntity department) {
        var commune = new CommuneEntity();
        commune.setCode(String.valueOf (attributesObject.getInt ("commune_id")));
        commune.setName(attributesObject.getString("Commune"));
        commune.setSt(String.valueOf (attributesObject.getString ("ST")));
        commune.setTotal(String.valueOf (attributesObject.getInt ("TOTAL")));
        commune.setWomen(String.valueOf (attributesObject.getInt ("Feminin")));
        commune.setMen(String.valueOf (attributesObject.getInt ("Masculin")));
        commune.setLength(String.valueOf (attributesObject.getDouble ("Shape_Leng")));
        commune.setArea(String.valueOf (attributesObject.getDouble ("Shape_Area")));
        commune.setCreatedBy ( "system");
        commune.setLastModifiedBy ("system");
        commune.setCreatedDate (LocalDateTime.now ());
        commune.setLastModifiedDate (LocalDateTime.now ());
        commune.setArchived (false);
        commune.setDepartment(department);
        return commune;
    }

    private QuartierEntity parseQuartier(JSONObject attributesObject) {
        var quartier = new QuartierEntity();
        quartier.setCode(String.valueOf (attributesObject.getString ("COD_CAV")));
        quartier.setName(attributesObject.getString("QRT_VLG_HA"));
        quartier.setCav(String.valueOf (attributesObject.getString ("CAV")));
        quartier.setCodeCav (String.valueOf (attributesObject.getString ("COD_CAV")));
        quartier.setCCrca(String.valueOf (attributesObject.getString ("CCRCA")));
        quartier.setCodeCcrca (attributesObject.getString("COD_CCRCA"));
        quartier.setCodeEntity (String.valueOf (attributesObject.getString ("COD_ENTITE")));
        quartier.setNumerozr (String.valueOf (attributesObject.getString ("NUM_ZR")));
        quartier.setCodeSzr (String.valueOf (attributesObject.getString ("COD_SZR")));
        quartier.setPoucentage (String.valueOf (attributesObject.getInt ("pourcentag")));
        quartier.setZoneCoron (String.valueOf (attributesObject.getInt ("Zone_coron")));
        quartier.setLength(String.valueOf (attributesObject.getDouble ("Shape_Leng")));
        quartier.setArea(String.valueOf (attributesObject.getDouble ("Shape_Area")));
        quartier.setCreatedBy ( "system");
        quartier.setLastModifiedBy ("system");
        quartier.setCreatedDate (LocalDateTime.now ());
        quartier.setLastModifiedDate (LocalDateTime.now ());
        quartier.setArchived (false);
        quartier.setCommune (findCommuneByName(attributesObject.getString ("CCRCA")));
        return quartier;
    }

    private CircuitBalayageEntity parseCircuitBalayage(JSONObject attributesObject, CommuneEntity commune) {
        var circuitBalayage = new CircuitBalayageEntity();
        circuitBalayage.setCode((attributesObject.getString ("code")));
        circuitBalayage.setName(attributesObject.getString("nomcircuit"));
        circuitBalayage.setShift(CircuitShift.valueOf (attributesObject.getString ("shift")));
        circuitBalayage.setLength(String.valueOf (attributesObject.getDouble ("longueur")));
        circuitBalayage.setCommuneId (commune == null ? null : commune.getCommuneId ()); // ADR-0012 : référence par identifiant
        circuitBalayage.setCreatedBy ( "system");
        circuitBalayage.setLastModifiedBy ("system");
        circuitBalayage.setCreatedDate (LocalDateTime.now ());
        circuitBalayage.setLastModifiedDate (LocalDateTime.now ());
        circuitBalayage.setArchived (false);
        return circuitBalayage;
    }

    private CircuitCollectEntity parseCircuitCollect(JSONObject attributesObject, CommuneEntity commune) {
        var circuitCollect = new CircuitCollectEntity();
        circuitCollect.setCode(String.valueOf (attributesObject.getString ("Code")));
        circuitCollect.setName(attributesObject.getString("nom"));
        circuitCollect.setLength(String.valueOf (attributesObject.getDouble ("Shape_Leng")));
        circuitCollect.setFrequence (attributesObject.getString ("Frequence"));
        circuitCollect.setLatiPointA (String.valueOf (attributesObject.getDouble ("LatipointA")));
        circuitCollect.setLatiPointD (String.valueOf (attributesObject.getDouble ("LatipointD")));
        circuitCollect.setLongPointA (String.valueOf (attributesObject.getDouble ("LongpointA")));
        circuitCollect.setLongPointD (String.valueOf (attributesObject.getDouble ("LongpointD")));
        circuitCollect.setType(String.valueOf (attributesObject.getInt ("type_id")));
        circuitCollect.setCat(String.valueOf (attributesObject.getInt ("cat_id")));
        circuitCollect.setRotation (attributesObject.getString ("Rotation"));
        circuitCollect.setSection (String.valueOf (attributesObject.getInt ("Section_id")));
        circuitCollect.setSectection (String.valueOf (attributesObject.getString ("Sectection")));
        circuitCollect.setCreatedBy ( "system");
        circuitCollect.setLastModifiedBy ("system");
        circuitCollect.setCreatedDate (LocalDateTime.now ());
        circuitCollect.setLastModifiedDate (LocalDateTime.now ());
        circuitCollect.setArchived (false);
        circuitCollect.setCommuneId (commune == null ? null : commune.getCommuneId ()); // ADR-0012 : référence par identifiant
        return circuitCollect;
    }

    private DepotoirEntity parseDepotoir(JSONObject attributesObject, CommuneEntity commune) {
        var depotoir = new DepotoirEntity();
        depotoir.setAddress (String.valueOf (attributesObject.getString ("Adresse_de")));
        depotoir.setCreatedBy ( "system");
        depotoir.setLastModifiedBy ("system");
        depotoir.setCreatedDate (LocalDateTime.now ());
        depotoir.setLastModifiedDate (LocalDateTime.now ());
        depotoir.setArchived (false);
        depotoir.setCommuneId (commune == null ? null : commune.getCommuneId ()); // ADR-0012 : référence par identifiant
        depotoir.setTypeDepotoir (findTypeDepotoirByName( attributesObject.getString ("Type_de_Mo")));
        return depotoir;
    }

    private TypeDepotoirEntity findTypeDepotoirByName(String name){
        var typeDepotoir = typeDepotoirRepository.findByNameIgnoreCase (name);
        if (typeDepotoir == null){
            typeDepotoir = new TypeDepotoirEntity();
            typeDepotoir.setName (name);
            typeDepotoir.setCreatedBy ( "system");
            typeDepotoir.setLastModifiedBy ("system");
            typeDepotoir.setCreatedDate (LocalDateTime.now ());
            typeDepotoir.setLastModifiedDate (LocalDateTime.now ());
            typeDepotoir.setArchived (false);
        }
        return typeDepotoir;
    }

    private CommuneEntity findCommuneByName(String name){
        var commune = communeRepository.findByNameIgnoreCase (name);
        if (commune == null){
            var communes =  communeRepository.findByNameContainingIgnoreCase (name);
            if (communes.isEmpty()) {
                return null;
            } else if (communes.size() > 1) {
                log.warn("Multiple communes found with name containing: {}", name);
                commune = communes.getFirst ();
            }
            log.info ("query {}", commune);
        }
        return commune;
    }

    private List<CoordinateEntity> parseCoordinate(JSONObject geometryObject) {
        List<CoordinateEntity> coordList = new ArrayList<>();

        if (geometryObject.has("rings")) {
            JSONArray ringsArray = geometryObject.getJSONArray("rings");
            for (int n = 0; n < ringsArray.length(); n++) {
                JSONArray ring = ringsArray.getJSONArray(n);
                for (int k = 0; k < ring.length(); k++) {
                    JSONArray coordinateArray = ring.getJSONArray(k);
                    CoordinateEntity coordinate = new CoordinateEntity();
                    coordinate.setLatitude(String.valueOf(coordinateArray.getDouble(0)));
                    coordinate.setLongitude(String.valueOf(coordinateArray.getDouble(1)));
                    coordinate.setCreatedBy ( "system");
                    coordinate.setLastModifiedBy ("system");
                    coordinate.setCreatedDate (LocalDateTime.now ());
                    coordinate.setLastModifiedDate (LocalDateTime.now ());
                    coordinate.setArchived (false);
                    coordList.add(coordinate);
                }
            }
        }else {
            CoordinateEntity coordinate = new CoordinateEntity();
            coordinate.setLatitude(String.valueOf(geometryObject.getString ("x")));
            coordinate.setLongitude(String.valueOf(geometryObject.getString("y")));
            coordinate.setCreatedBy ( "system");
            coordinate.setLastModifiedBy ("system");
            coordinate.setCreatedDate (LocalDateTime.now ());
            coordinate.setLastModifiedDate (LocalDateTime.now ());
            coordinate.setArchived (false);
            coordList.add(coordinate);
        }

        return coordList;
    }


    private List<CoordinateEntity> parseCoordinates(JSONObject geometryObject) {
        List<CoordinateEntity> coordList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        if (geometryObject.has("rings")) {
            coordList.addAll(parseRingOrPathCoordinates(geometryObject.getJSONArray("rings"), now));
        } else if (geometryObject.has("paths")) {
            coordList.addAll(parseRingOrPathCoordinates(geometryObject.getJSONArray("paths"), now));
        } else if (geometryObject.has("x") && geometryObject.has("y")) {
            coordList.add(createCoordinateEntity(geometryObject.getDouble("x"), geometryObject.getDouble("y"), now));
        }

        return coordList;
    }

    private List<CoordinateEntity> parseRingOrPathCoordinates(JSONArray array, LocalDateTime now) {
        List<CoordinateEntity> coordList = new ArrayList<>();

        for (int n = 0; n < array.length(); n++) {
            JSONArray ringOrPath = array.getJSONArray(n);
            for (int k = 0; k < ringOrPath.length(); k++) {
                JSONArray coordinateArray = ringOrPath.getJSONArray(k);
                coordList.add(createCoordinateEntity(coordinateArray.getDouble(0), coordinateArray.getDouble(1), now));
            }
        }

        return coordList;
    }

    private CoordinateEntity createCoordinateEntity(double latitude, double longitude, LocalDateTime now) {
        CoordinateEntity coordinate = new CoordinateEntity();
        coordinate.setLatitude(String.valueOf(latitude));
        coordinate.setLongitude(String.valueOf(longitude));
        coordinate.setCreatedBy(CREATED_BY);
        coordinate.setLastModifiedBy(LAST_MODIFIED_BY);
        coordinate.setCreatedDate(now);
        coordinate.setLastModifiedDate(now);
        coordinate.setArchived(false);
        return coordinate;
    }
}
