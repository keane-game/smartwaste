package sn.smartwaste.collect.waste.application.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Gestion de la flotte de collecte. */
public interface VehicleService {

    List<VehicleDto> readAll();

    VehicleDto create(VehicleDto vehicle);

    VehicleDto update(UUID vehicleId, VehicleDto vehicle);

    /** Retire de la circulation sans supprimer : l'historique et les traceurs restent rattachés. */
    void deactivate(UUID vehicleId);

    /**
     * @param registration   immatriculation, obligatoire et unique — l'identifiant du terrain
     * @param lastPositionAt en lecture seule : alimenté par les traceurs, jamais par l'API
     */
    record VehicleDto(UUID vehicleId, String registration, String label, UUID circuitCollectId,
                      Boolean active, Double lastLatitude, Double lastLongitude, Instant lastPositionAt) { }
}
