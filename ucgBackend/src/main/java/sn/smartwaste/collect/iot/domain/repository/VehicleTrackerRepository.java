package sn.smartwaste.collect.iot.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sn.smartwaste.collect.iot.domain.model.VehicleTracker;

@Repository
public interface VehicleTrackerRepository extends JpaRepository<VehicleTracker, UUID> {

    Optional<VehicleTracker> findByApiKeyHash(String apiKeyHash);
}
