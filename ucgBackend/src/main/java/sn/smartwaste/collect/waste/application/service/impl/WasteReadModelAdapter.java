package sn.smartwaste.collect.waste.application.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.shared.domain.model.DeletionStatus;
import sn.smartwaste.collect.waste.application.api.DepotoirMaps;
import sn.smartwaste.collect.waste.application.api.WasteReadModel;
import sn.smartwaste.collect.waste.application.service.DepotoirService;
import sn.smartwaste.collect.waste.domain.repository.AlertRepository;
import sn.smartwaste.collect.waste.domain.repository.CollectionScheduleRepository;
import sn.smartwaste.collect.waste.domain.repository.CircuitBalayageRepository;
import sn.smartwaste.collect.waste.domain.repository.CircuitCollectRepository;
import sn.smartwaste.collect.waste.domain.repository.DepotoirRepository;
import sn.smartwaste.collect.waste.domain.repository.MoblierUrbainRepository;

/**
 * Implémentation du contrat de lecture publié par le contexte « Déchets ».
 *
 * <p>C'est désormais le <b>seul</b> chemin par lequel un autre contexte atteint ces données. Les
 * repositories et les entités ne franchissent plus la frontière.
 *
 * <p>{@code @Transactional(readOnly = true)} n'est pas décoratif : {@code Depotoir.typeDepotoir}
 * est LAZY depuis P1-2, et sa résolution doit avoir lieu ici — dans la transaction du contexte
 * propriétaire — et non chez l'appelant.
 */
@Service
@Transactional(readOnly = true)
public class WasteReadModelAdapter implements WasteReadModel {

    private final DepotoirRepository depotoirRepository;
    private final MoblierUrbainRepository moblierUrbainRepository;
    private final CircuitCollectRepository circuitCollectRepository;
    private final CircuitBalayageRepository circuitBalayageRepository;
    private final AlertRepository alertRepository;
    private final DepotoirService depotoirService;
    private final CollectionScheduleRepository collectionScheduleRepository;

    public WasteReadModelAdapter(DepotoirRepository depotoirRepository,
                                 MoblierUrbainRepository moblierUrbainRepository,
                                 CircuitCollectRepository circuitCollectRepository,
                                 CircuitBalayageRepository circuitBalayageRepository,
                                 AlertRepository alertRepository,
                                 DepotoirService depotoirService,
                                 CollectionScheduleRepository collectionScheduleRepository) {
        this.depotoirRepository = depotoirRepository;
        this.moblierUrbainRepository = moblierUrbainRepository;
        this.circuitCollectRepository = circuitCollectRepository;
        this.circuitBalayageRepository = circuitBalayageRepository;
        this.alertRepository = alertRepository;
        this.depotoirService = depotoirService;
        this.collectionScheduleRepository = collectionScheduleRepository;
    }

    @Override
    public long countCollectionPoints() {
        return depotoirRepository.count();
    }

    @Override
    public long countStreetFurniture() {
        return moblierUrbainRepository.count();
    }

    @Override
    public long countCircuits() {
        return circuitCollectRepository.count() + circuitBalayageRepository.count();
    }

    @Override
    public long countCollectionPointsByTypeNameContaining(String typeNameFragment) {
        return depotoirRepository.findByTypeDepotoir_NameContainingIgnoreCase(typeNameFragment).size();
    }

    @Override
    public List<ActiveAlert> activeAlerts() {
        return alertRepository.findByDeletionStatus(DeletionStatus.ACTIVE).stream()
                .map(a -> new ActiveAlert(
                        a.getCode() == null ? null : a.getCode().name(),
                        a.getCreatedDate()))
                .toList();
    }

    @Override
    public List<ActiveCollectionPoint> activeCollectionPoints() {
        return depotoirRepository.findByDeletionStatus(DeletionStatus.ACTIVE).stream()
                .map(d -> {
                    var type = d.getTypeDepotoir();
                    return new ActiveCollectionPoint(type == null ? null : type.getName(),
                                                    d.getFillLevelPercent());
                })
                .toList();
    }

    @Override
    public List<ActiveCircuit> activeCircuits() {
        List<ActiveCircuit> circuits = new ArrayList<>();
        circuitCollectRepository.findByDeletionStatus(DeletionStatus.ACTIVE)
                .forEach(c -> circuits.add(new ActiveCircuit(c.getCommuneId())));
        circuitBalayageRepository.findByDeletionStatus(DeletionStatus.ACTIVE)
                .forEach(c -> circuits.add(new ActiveCircuit(c.getCommuneId())));
        return circuits;
    }

    @Override
    public List<DepotoirMaps> collectionPointsForMap() {
        return depotoirService.getDepotoirMap();
    }

    @Override
    public List<ScheduledCollection> collectionsScheduledOn(java.time.DayOfWeek dayOfWeek) {
        return collectionScheduleRepository.findByDayOfWeekAndActiveTrue(dayOfWeek).stream()
                .map(s -> new ScheduledCollection(s.getQuartierId(), s.getPassageTime()))
                .toList();
    }
}
