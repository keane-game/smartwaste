package sn.smartwaste.collect.territory.application.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.shared.domain.model.ImportedFeature;
import sn.smartwaste.collect.territory.application.api.TerritoryImportPort;
import sn.smartwaste.collect.territory.domain.model.CommuneEntity;
import sn.smartwaste.collect.territory.domain.model.CoordinateEntity;
import sn.smartwaste.collect.territory.domain.model.DepartmentEntity;
import sn.smartwaste.collect.territory.domain.model.GeometryEntity;
import sn.smartwaste.collect.territory.domain.model.QuartierEntity;
import sn.smartwaste.collect.territory.domain.repository.CommuneRepository;
import sn.smartwaste.collect.territory.domain.repository.DepartmentRepository;
import sn.smartwaste.collect.territory.domain.repository.QuartierRepository;
import sn.smartwaste.collect.territory.domain.repository.RegionRepository;

/**
 * Traduit les entités brutes d'un fichier GeoJSON en entités du référentiel territorial.
 *
 * <p>Le savoir déplacé ici est celui qui n'appartenait pas à l'import : que {@code COD_DEPT} est le
 * code d'un département, que {@code CCRCA} désigne la commune d'un quartier, que la hiérarchie se
 * rattache à la première région enregistrée. Seul ce contexte peut en décider.
 */
@Service
@Transactional
public class TerritoryImportAdapter implements TerritoryImportPort {

    private static final Logger log = LoggerFactory.getLogger(TerritoryImportAdapter.class);
    private static final String SYSTEM = "system";

    private final RegionRepository regionRepository;
    private final DepartmentRepository departmentRepository;
    private final CommuneRepository communeRepository;
    private final QuartierRepository quartierRepository;

    public TerritoryImportAdapter(RegionRepository regionRepository,
                                  DepartmentRepository departmentRepository,
                                  CommuneRepository communeRepository,
                                  QuartierRepository quartierRepository) {
        this.regionRepository = regionRepository;
        this.departmentRepository = departmentRepository;
        this.communeRepository = communeRepository;
        this.quartierRepository = quartierRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasRegion() {
        return regionRepository.findAll().stream().findFirst().isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasDepartment() {
        return departmentRepository.findAll().stream().findFirst().isPresent();
    }

    @Override
    public void importDepartment(ImportedFeature feature) {
        // Les PK du référentiel sont des UUID : l'identifiant « 1 » du script de seed n'existe
        // plus. Le jeu de données ne porte qu'une région (Dakar) — on prend la première.
        var region = regionRepository.findAll().stream().findFirst().orElse(null);
        if (region == null) {
            return;
        }
        var department = new DepartmentEntity();
        department.setCode(feature.text("COD_DEPT"));
        department.setName(feature.text("DEPT"));
        department.setArea(feature.number("SUPERFICIE"));
        Integer effectif = feature.integer("EFFECTIF");
        if (effectif != null) {
            department.setEffectif(effectif);
        }
        department.setRegion(region);
        stamp(department::setCreatedBy, department::setLastModifiedBy,
              department::setCreatedDate, department::setLastModifiedDate, department::setArchived);
        department.setGeometry(toGeometry(feature));
        departmentRepository.saveAndFlush(department);
    }

    @Override
    public void importCommune(ImportedFeature feature) {
        var department = departmentRepository.findAll().stream().findFirst().orElse(null);
        if (department == null) {
            return;
        }
        var commune = new CommuneEntity();
        commune.setCode(feature.text("commune_id"));
        commune.setName(feature.text("Commune"));
        commune.setSt(feature.text("ST"));
        commune.setTotal(feature.text("TOTAL"));
        commune.setWomen(feature.text("Feminin"));
        commune.setMen(feature.text("Masculin"));
        commune.setLength(feature.text("Shape_Leng"));
        commune.setArea(feature.text("Shape_Area"));
        commune.setDepartment(department);
        stamp(commune::setCreatedBy, commune::setLastModifiedBy,
              commune::setCreatedDate, commune::setLastModifiedDate, commune::setArchived);
        commune.setGeometry(toGeometry(feature));
        communeRepository.saveAndFlush(commune);
    }

    @Override
    public void importQuartier(ImportedFeature feature) {
        var quartier = new QuartierEntity();
        quartier.setCode(feature.text("COD_CAV"));
        quartier.setName(feature.text("QRT_VLG_HA"));
        quartier.setCav(feature.text("CAV"));
        quartier.setCodeCav(feature.text("COD_CAV"));
        quartier.setCCrca(feature.text("CCRCA"));
        quartier.setCodeCcrca(feature.text("COD_CCRCA"));
        quartier.setCodeEntity(feature.text("COD_ENTITE"));
        quartier.setNumerozr(feature.text("NUM_ZR"));
        quartier.setCodeSzr(feature.text("COD_SZR"));
        quartier.setPoucentage(feature.text("pourcentag"));
        quartier.setZoneCoron(feature.text("Zone_coron"));
        quartier.setLength(feature.text("Shape_Leng"));
        quartier.setArea(feature.text("Shape_Area"));
        quartier.setCommune(resolveCommune(feature.text("CCRCA")));
        stamp(quartier::setCreatedBy, quartier::setLastModifiedBy,
              quartier::setCreatedDate, quartier::setLastModifiedDate, quartier::setArchived);
        quartier.setGeometry(toGeometry(feature));
        quartierRepository.saveAndFlush(quartier);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> findCommuneIdByName(String name) {
        return Optional.ofNullable(resolveCommune(name)).map(CommuneEntity::getCommuneId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countDepartments() {
        return departmentRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countCommunes() {
        return communeRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countQuartiers() {
        return quartierRepository.count();
    }

    /**
     * Résolution tolérante : égalité stricte, puis correspondance partielle. Les noms des fichiers
     * source ne coïncident pas toujours exactement avec ceux du référentiel.
     */
    private CommuneEntity resolveCommune(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        var commune = communeRepository.findByNameIgnoreCase(name);
        if (commune != null) {
            return commune;
        }
        var candidates = communeRepository.findByNameContainingIgnoreCase(name);
        if (candidates.isEmpty()) {
            return null;
        }
        if (candidates.size() > 1) {
            log.warn("Plusieurs communes correspondent a « {} » : la premiere est retenue", name);
        }
        return candidates.getFirst();
    }

    /**
     * Publie la construction de géométrie pour les autres contextes (ADR-0016).
     *
     * <p>Le contexte « déchets » pose désormais une géométrie sur ses dépotoirs et ses circuits ;
     * il l'obtient d'ici plutôt que de réécrire chez lui la même construction, ce qui ferait
     * diverger deux définitions du même objet.
     */
    @Override
    public GeometryEntity newGeometry(ImportedFeature feature) {
        return toGeometry(feature);
    }

    private GeometryEntity toGeometry(ImportedFeature feature) {
        var shape = feature.geometry();
        if (shape == null) {
            return null;
        }
        var geometry = new GeometryEntity();
        geometry.setType(shape.type());
        geometry.setSpatialReference(shape.spatialReference());
        stamp(geometry::setCreatedBy, geometry::setLastModifiedBy,
              geometry::setCreatedDate, geometry::setLastModifiedDate, geometry::setArchived);

        List<CoordinateEntity> coordinates = new ArrayList<>();
        for (var point : shape.points()) {
            var coordinate = new CoordinateEntity();
            coordinate.setLatitude(point.latitude());
            coordinate.setLongitude(point.longitude());
            stamp(coordinate::setCreatedBy, coordinate::setLastModifiedBy,
                  coordinate::setCreatedDate, coordinate::setLastModifiedDate, coordinate::setArchived);
            coordinates.add(coordinate);
        }
        geometry.setCoordinates(coordinates);
        return geometry;
    }

    private void stamp(java.util.function.Consumer<String> createdBy,
                       java.util.function.Consumer<String> modifiedBy,
                       java.util.function.Consumer<LocalDateTime> createdDate,
                       java.util.function.Consumer<LocalDateTime> modifiedDate,
                       java.util.function.Consumer<Boolean> archived) {
        LocalDateTime now = LocalDateTime.now();
        createdBy.accept(SYSTEM);
        modifiedBy.accept(SYSTEM);
        createdDate.accept(now);
        modifiedDate.accept(now);
        archived.accept(false);
    }
}
