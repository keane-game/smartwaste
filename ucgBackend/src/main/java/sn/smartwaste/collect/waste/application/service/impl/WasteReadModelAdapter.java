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
import sn.smartwaste.collect.waste.domain.repository.VehicleRepository;
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
    private final sn.smartwaste.collect.waste.domain.repository.CollectionPassageRepository passageRepository;
    private final DepotoirService depotoirService;
    private final CollectionScheduleRepository collectionScheduleRepository;
    private final VehicleRepository vehicleRepository;

    /** Au-dela, une position n'est plus consideree comme representative du terrain. */
    private final java.time.Duration vehicleFreshness;

    public WasteReadModelAdapter(DepotoirRepository depotoirRepository,
                                 MoblierUrbainRepository moblierUrbainRepository,
                                 CircuitCollectRepository circuitCollectRepository,
                                 CircuitBalayageRepository circuitBalayageRepository,
                                 AlertRepository alertRepository,
                                 sn.smartwaste.collect.waste.domain.repository.CollectionPassageRepository passageRepository,
                                 DepotoirService depotoirService,
                                 CollectionScheduleRepository collectionScheduleRepository,
                                 VehicleRepository vehicleRepository,
                                 @org.springframework.beans.factory.annotation.Value("${sonaged.fleet.position-freshness-minutes:15}") long freshnessMinutes) {
        this.depotoirRepository = depotoirRepository;
        this.moblierUrbainRepository = moblierUrbainRepository;
        this.circuitCollectRepository = circuitCollectRepository;
        this.circuitBalayageRepository = circuitBalayageRepository;
        this.alertRepository = alertRepository;
        this.passageRepository = passageRepository;
        this.depotoirService = depotoirService;
        this.collectionScheduleRepository = collectionScheduleRepository;
        this.vehicleRepository = vehicleRepository;
        this.vehicleFreshness = java.time.Duration.ofMinutes(freshnessMinutes);
    }

    @Override
    public long countCollectionPoints() {
        return depotoirRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean collectionPointExists(Long depotoirId) {
        return depotoirId != null && depotoirRepository.existsById(depotoirId);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Set<Long> existingCollectionPoints(java.util.Collection<Long> depotoirIds) {
        var connus = depotoirIds.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (connus.isEmpty()) {
            return java.util.Set.of();
        }
        return depotoirRepository.findAllById(connus).stream()
                .map(sn.smartwaste.collect.waste.domain.model.DepotoirEntity::getDepotoirId)
                .collect(java.util.stream.Collectors.toSet());
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

    @Override
    public List<VehicleOnMap> vehiclesOnMap() {
        var since = java.time.Instant.now().minus(vehicleFreshness);
        return vehicleRepository.findByActiveTrueAndLastPositionAtAfter(since).stream()
                .map(v -> new VehicleOnMap(v.getVehicleId(), v.getRegistration(), v.getLabel(),
                        v.getLastLatitude(), v.getLastLongitude(), v.getLastPositionAt()))
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    public List<PointEvent> pointEvents(Long depotoirId, java.time.Instant from,
                                        java.time.Instant to) {
        var events = new java.util.ArrayList<PointEvent>();
        var zone = java.time.ZoneId.systemDefault();

        var debut = java.time.LocalDateTime.ofInstant(from, zone);
        var fin = java.time.LocalDateTime.ofInstant(to, zone);
        for (var alert : alertRepository.findTouchingPeriod(depotoirId, debut, fin)) {
            // Une alerte peut n'avoir qu'un de ses deux temps dans la fenetre : on ne
            // rapporte que ceux qui s'y sont reellement produits, sinon le journal daterait
            // un fait hors periode.
            if (!alert.getCreatedDate().isBefore(debut) && alert.getCreatedDate().isBefore(fin)) {
                events.add(new PointEvent(alert.getCreatedDate().atZone(zone).toInstant(),
                        "ALERTE_LEVEE", alert.getObject(), alert.getMessage()));
            }
            // La resolution est un fait distinct, et souvent le plus interessant : c'est lui qui
            // dit combien de temps le probleme a dure.
            if (alert.getResolvedAt() != null
                    && !alert.getResolvedAt().isBefore(debut)
                    && alert.getResolvedAt().isBefore(fin)) {
                events.add(new PointEvent(alert.getResolvedAt().atZone(zone).toInstant(),
                        "ALERTE_RESOLUE", alert.getObject(), alert.getResolvedBy()));
            }
        }

        for (var passage : passageRepository.findByDepotoirIdInAndOccurredAtBetween(
                List.of(depotoirId), from, to)) {
            boolean collecte = passage.getOutcome()
                    == sn.smartwaste.collect.waste.domain.model.PassageOutcome.COLLECTED;
            events.add(new PointEvent(passage.getOccurredAt(),
                    collecte ? "COLLECTE" : "INACCESSIBLE",
                    collecte ? "Point collecte" : "Point inaccessible",
                    passage.getReason()));
        }
        return List.copyOf(events);
    }
}
