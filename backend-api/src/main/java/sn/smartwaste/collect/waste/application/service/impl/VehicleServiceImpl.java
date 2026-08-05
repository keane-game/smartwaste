package sn.smartwaste.collect.waste.application.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.waste.application.service.VehicleService;
import sn.smartwaste.collect.waste.domain.model.Vehicle;
import sn.smartwaste.collect.waste.domain.repository.VehicleRepository;

/**
 * Gestion de la flotte.
 *
 * <p><b>La position n'est jamais acceptée par l'API.</b> Elle est alimentée exclusivement par les
 * traceurs, via la chaîne d'ingestion. Permettre de la fixer à la main autoriserait à placer un
 * camion où l'on veut sur la carte de supervision, sans qu'il y soit — une information fausse est
 * pire qu'une information absente.
 */
@Service
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleDto> readAll() {
        return vehicleRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public VehicleDto create(VehicleDto dto) {
        String registration = normalize(dto.registration());
        vehicleRepository.findByRegistrationIgnoreCase(registration).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Un vehicule porte deja l'immatriculation " + registration);
        });
        var vehicle = new Vehicle();
        vehicle.setRegistration(registration);
        vehicle.setLabel(dto.label());
        vehicle.setCircuitCollectId(dto.circuitCollectId());
        vehicle.setActive(dto.active() == null || dto.active());
        return toDto(vehicleRepository.save(vehicle));
    }

    @Override
    public VehicleDto update(UUID vehicleId, VehicleDto dto) {
        var vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicule [%s] introuvable".formatted(vehicleId)));
        if (dto.registration() != null) {
            vehicle.setRegistration(normalize(dto.registration()));
        }
        if (dto.label() != null) {
            vehicle.setLabel(dto.label());
        }
        if (dto.circuitCollectId() != null) {
            vehicle.setCircuitCollectId(dto.circuitCollectId());
        }
        if (dto.active() != null) {
            vehicle.setActive(dto.active());
        }
        // La position n'est deliberement pas reprise du DTO : elle appartient aux traceurs.
        return toDto(vehicleRepository.save(vehicle));
    }

    @Override
    public void deactivate(UUID vehicleId) {
        var vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicule [%s] introuvable".formatted(vehicleId)));
        vehicle.setActive(false);
        vehicleRepository.save(vehicle);
    }

    /** Immatriculations en majuscules sans espaces : « dk-1234-a » et « DK 1234 A » sont le meme camion. */
    private String normalize(String registration) {
        if (registration == null || registration.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'immatriculation est obligatoire");
        }
        return registration.replaceAll("\\s+", "").toUpperCase();
    }

    private VehicleDto toDto(Vehicle v) {
        return new VehicleDto(v.getVehicleId(), v.getRegistration(), v.getLabel(), v.getCircuitCollectId(),
                v.isActive(), v.getLastLatitude(), v.getLastLongitude(), v.getLastPositionAt());
    }
}
