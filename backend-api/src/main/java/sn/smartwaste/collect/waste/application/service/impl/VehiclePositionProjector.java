package sn.smartwaste.collect.waste.application.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.shared.domain.event.VehiclePositionRecorded;
import sn.smartwaste.collect.waste.domain.repository.VehicleRepository;

/**
 * Tient à jour la dernière position connue de la flotte.
 *
 * <p>Ne conserve que le dernier point : la trace complète serait un autre volume et un autre besoin.
 *
 * <p><b>Une position arriérée n'écrase pas une plus récente.</b> Un traceur qui perd le réseau
 * accumule et rejoue à la reconnexion : sans cette garde, le camion reculerait sur la carte, ce qui
 * est bien pire qu'une position figée — un superviseur agirait sur une information fausse.
 */
@Component
public class VehiclePositionProjector {

    private static final Logger log = LoggerFactory.getLogger(VehiclePositionProjector.class);

    private final VehicleRepository vehicleRepository;

    public VehiclePositionProjector(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @EventListener
    @Transactional
    public void on(VehiclePositionRecorded event) {
        var vehicle = vehicleRepository.findById(event.vehicleId()).orElse(null);
        if (vehicle == null) {
            // Traceur rattaché à un véhicule retiré de la flotte : rien à mettre à jour.
            log.warn("Position recue pour un vehicule inconnu ({})", event.vehicleId());
            return;
        }
        if (vehicle.getLastPositionAt() != null && event.recordedAt().isBefore(vehicle.getLastPositionAt())) {
            return;
        }
        vehicle.setLastLatitude(event.latitude());
        vehicle.setLastLongitude(event.longitude());
        vehicle.setLastPositionAt(event.recordedAt());
        vehicleRepository.save(vehicle);
    }
}
