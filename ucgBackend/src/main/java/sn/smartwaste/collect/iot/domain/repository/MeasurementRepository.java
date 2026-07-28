package sn.smartwaste.collect.iot.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sn.smartwaste.collect.iot.domain.model.Measurement;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, UUID> {

    List<Measurement> findByDepotoirIdOrderByMeasuredAtDesc(Long depotoirId);

    /** Idempotence : un capteur qui réémet la même mesure ne doit pas la dupliquer. */
    boolean existsBySensorIdAndMeasuredAt(UUID sensorId, Instant measuredAt);

    long countByMeasuredAtAfter(Instant since);
}
