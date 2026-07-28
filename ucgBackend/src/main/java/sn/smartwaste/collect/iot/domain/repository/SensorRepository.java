package sn.smartwaste.collect.iot.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sn.smartwaste.collect.iot.domain.model.Sensor;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, UUID> {

    /** Authentification du capteur : recherche par empreinte, la clé ne descend jamais en base. */
    Optional<Sensor> findByApiKeyHash(String apiKeyHash);

    Optional<Sensor> findByDeviceCode(String deviceCode);
}
