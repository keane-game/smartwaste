package sonaged.collecte.master.service.impl;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.dto.DepotoirMaps;
import sonaged.collecte.master.enums.DeletionStatus;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.dto.Depotoir;
import sn.smartwaste.collect.territory.application.mapper.CommuneMapper;
import sn.smartwaste.collect.territory.application.mapper.CoordinateMapper;
import sonaged.collecte.master.mapper.DepotoirMapper;
import sonaged.collecte.master.model.DepotoirEntity;
import sn.smartwaste.collect.territory.domain.model.GeometryEntity;
import sonaged.collecte.master.repository.DepotoirRepository;
import sn.smartwaste.collect.territory.domain.repository.GeometryRepository;
import sn.smartwaste.collect.territory.domain.repository.QuartierRepository;
import sonaged.collecte.master.repository.TypeDepotoirRepository;
import sonaged.collecte.master.service.CrossContextReferenceValidator;
import sonaged.collecte.master.service.DepotoirService;
import sonaged.collecte.master.service.SoftDeleteService;

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

    @Override
    public Depotoir readDepotoir(Long depotoirId) {
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
            var depotoirMap = new DepotoirMaps (  );
            depotoirMap.setAddress (d.getAddress ());
            depotoirMap.setTypeGeo (d.getGeometry().getType ());
            depotoirMap.setTypeDepot (d.getTypeDepotoir ().getName ());
            depotoirMap.setCoordinates (CoordinateMapper.CODMP.asListDto (d.getGeometry().getCoordinates () ));
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
        var savedDepotoir = depotoirRepository.save(DepotoirMapper.DETMP.asModel(depotoir));
        return DepotoirMapper.DETMP.asDto(savedDepotoir);
    }


    @Override
    public Depotoir updateDepotoir(Long depotoirId, Depotoir depotoir) {
        var existedDepotoir = depotoirRepository.findById(depotoirId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Depotoir with id [%s] not found to update ".formatted(depotoirId)
                ));

        existedDepotoir.setAddress(depotoir.getAddress());
        var updateDepotoir = depotoirRepository.save(existedDepotoir);
        return DepotoirMapper.DETMP.asDto(updateDepotoir);
    }


    @Override
    public void deleteDepotoir(Long depotoirId) {
        // Suppression logique : passe en PENDING_DELETION (purge définitive par le planificateur
        // après la période de rétention). Restaurable via restoreDepotoir tant que le délai court.
        softDeleteService.softDelete(depotoirRepository, depotoirId);
    }

    @Override
    public Depotoir restoreDepotoir(Long depotoirId) {
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
