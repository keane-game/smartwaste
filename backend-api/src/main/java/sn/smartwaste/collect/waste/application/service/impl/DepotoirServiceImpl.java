package sn.smartwaste.collect.waste.application.service.impl;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sn.smartwaste.collect.waste.application.api.DepotoirMaps;
import sn.smartwaste.collect.shared.domain.model.DeletionStatus;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.waste.application.dto.Depotoir;
import sn.smartwaste.collect.territory.application.mapper.CommuneMapper;
import sn.smartwaste.collect.territory.application.mapper.CoordinateMapper;
import sn.smartwaste.collect.waste.application.mapper.DepotoirMapper;
import sn.smartwaste.collect.waste.domain.model.DepotoirEntity;
import sn.smartwaste.collect.territory.domain.model.GeometryEntity;
import sn.smartwaste.collect.waste.domain.repository.DepotoirRepository;
import sn.smartwaste.collect.territory.domain.repository.GeometryRepository;
import sn.smartwaste.collect.territory.domain.repository.QuartierRepository;
import sn.smartwaste.collect.waste.domain.repository.TypeDepotoirRepository;
import sn.smartwaste.collect.waste.application.service.CrossContextReferenceValidator;
import sn.smartwaste.collect.waste.application.service.DepotoirService;
import sn.smartwaste.collect.shared.domain.service.SoftDeleteService;
import sn.smartwaste.collect.tenant.application.api.CurrentTenantProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class DepotoirServiceImpl implements DepotoirService {
    private final GeometryRepository geometryRepository;
    private final QuartierRepository quartierRepository;
    private final TypeDepotoirRepository typeDepotoirRepository;

    private final DepotoirRepository depotoirRepository;
    private final SoftDeleteService softDeleteService;
    private final CrossContextReferenceValidator crossContextReferenceValidator;
    private final CurrentTenantProvider currentTenantProvider;

    @Override
    public Depotoir readDepotoir(UUID depotoirId) {
        var depotoir  = depotoirRepository.findById(depotoirId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Depotoir with id [%s] not found ".formatted(depotoirId)
                ));
        return DepotoirMapper.DETMP.asDto(depotoir);
    }


    @Override
    public List<Depotoir> readAllDepotoir() {
        // n'expose que les dépotoirs actifs (les soft-deletés sont masqués des listes normales)
        var depotoirList = depotoirRepository.findByDeletionStatus(DeletionStatus.ACTIVE);
        return DepotoirMapper.DETMP.asListDto(depotoirList);
    }

    @Override
    public Page<Depotoir> readAllDepotoir(Pageable pageable) {
        //return depotoirRepository.findAll (pageable).map (DepotoirMapper.DETMP::asDto);
       return readAllDepotoirs(pageable);
    }
    public Page<Depotoir> readAllDepotoirs(Pageable pageable) {
        return depotoirRepository.findByDeletionStatus(DeletionStatus.ACTIVE, pageable)
                .map(depotoir -> {
                    Depotoir dto = DepotoirMapper.DETMP.asDto(depotoir);
                    UUID geometryId = dto.getGeometry().getGeometryId();

                    geometryRepository.findById(geometryId).ifPresent(geometry -> {
                        dto.setCoordinates(CoordinateMapper.CODMP.asListDto(geometry.getCoordinates()));
                    });

                    return dto;
                });
    }

    @Override
    public List<DepotoirMaps> getDepotoirMap() {
        var depotoirs = depotoirRepository.findByDeletionStatus (DeletionStatus.ACTIVE);
        List<DepotoirMaps> depotoirMaps = new ArrayList<>();
        depotoirs.forEach (d -> {
            // Un point de collecte sans géométrie n'est pas plaçable sur la carte : on l'omet.
            // L'ancien code déréférençait getGeometry() sans garde — or la géométrie est
            // `nullable = true`, et un dépotoir créé depuis les écrans CRUD n'en a aucune.
            // Une seule ligne de ce genre faisait donc répondre 500 à TOUT l'endpoint carte.
            var geometry = d.getGeometry ();
            if (geometry == null) {
                return;
            }
            var depotoirMap = new DepotoirMaps (  );
            depotoirMap.setAddress (d.getAddress ());
            depotoirMap.setTypeGeo (geometry.getType ());
            // Même prudence sur le type : référentiel partagé, association LAZY et facultative.
            var type = d.getTypeDepotoir ();
            depotoirMap.setTypeDepot (type == null ? null : type.getName ());
            depotoirMap.setCoordinates (CoordinateMapper.CODMP.asListDto (geometry.getCoordinates () ));
            depotoirMap.setFillLevelPercent (d.getFillLevelPercent ());
            depotoirMap.setLastMeasuredAt (d.getLastMeasuredAt ());
            depotoirMaps.add (depotoirMap);
        });
        return depotoirMaps;
    }
    @Override
    public Depotoir createDepotoir(Depotoir depotoir) {
        // P1-7 / ADR-0012 : `communeId` et `quartierId` sont des références par identifiant vers
        // le « Référentiel territorial ». Les FK physiques ayant été retirées (changelog 1.5.0),
        // leur existence est vérifiée ici, au niveau applicatif.
        crossContextReferenceValidator.requireCommuneExists(depotoir.getCommuneId());
        crossContextReferenceValidator.requireQuartierExists(depotoir.getQuartierId());
        var depotoirToCreate = DepotoirMapper.DETMP.asModel(depotoir);
        // ADR-0020 : jamais depuis le DTO client.
        depotoirToCreate.setOrganizationId(currentTenantProvider.currentOrganizationId()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucune collectivité rattachée au compte courant : impossible de créer un dépotoir")));
        var savedDepotoir = depotoirRepository.save(depotoirToCreate);
        return DepotoirMapper.DETMP.asDto(savedDepotoir);
    }


    @Override
    public Depotoir updateDepotoir(UUID depotoirId, Depotoir depotoir) {
        var existedDepotoir = depotoirRepository.findById(depotoirId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Depotoir with id [%s] not found to update ".formatted(depotoirId)
                ));

        existedDepotoir.setAddress(depotoir.getAddress());
        var updateDepotoir = depotoirRepository.save(existedDepotoir);
        return DepotoirMapper.DETMP.asDto(updateDepotoir);
    }


    @Override
    public void deleteDepotoir(UUID depotoirId) {
        // Suppression logique : passe en PENDING_DELETION (purge définitive par le planificateur
        // après la période de rétention). Restaurable via restoreDepotoir tant que le délai court.
        softDeleteService.softDelete(depotoirRepository, depotoirId);
    }

    @Override
    public Depotoir restoreDepotoir(UUID depotoirId) {
        var restored = softDeleteService.restore(depotoirRepository, depotoirId);
        return toDtoWithDeletionInfo(restored);
    }

    @Override
    public List<Depotoir> readPendingDeletions() {
        return depotoirRepository.findByDeletionStatus(DeletionStatus.PENDING_DELETION).stream()
                .map(this::toDtoWithDeletionInfo)
                .toList();
    }

    /** Mappe l'entité en DTO et complète la date de purge prévue (non portée par le mapper). */
    private Depotoir toDtoWithDeletionInfo(DepotoirEntity entity) {
        Depotoir dto = DepotoirMapper.DETMP.asDto(entity);
        dto.setPurgeDueAt(softDeleteService.purgeDueAt(entity));
        return dto;
    }

}
