package sn.smartwaste.collect.waste.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sn.smartwaste.collect.waste.domain.model.Vehicle;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    Optional<Vehicle> findByRegistrationIgnoreCase(String registration);

    /**
     * Véhicules actifs vus récemment.
     *
     * <p>Le filtre temporel n'est pas cosmétique : afficher un camion à sa position d'il y a trois
     * heures comme s'il y était encore est pire que ne rien afficher — on enverrait quelqu'un le
     * rejoindre.
     */
    List<Vehicle> findByActiveTrueAndLastPositionAtAfter(Instant since);
}
