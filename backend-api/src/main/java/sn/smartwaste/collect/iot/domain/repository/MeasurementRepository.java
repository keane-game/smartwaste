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

    /**
     * Mesures d'un point sur une période.
     *
     * <p>Le filtre est dans la requête, et non appliqué après coup : un capteur émettant au quart
     * d'heure produit ~35 000 lignes par an, et toutes étaient chargées pour en afficher trente
     * jours.
     */
    List<Measurement> findByDepotoirIdAndMeasuredAtBetweenOrderByMeasuredAtDesc(
            Long depotoirId, java.time.Instant from, java.time.Instant to);

    /** Idempotence : un capteur qui réémet la même mesure ne doit pas la dupliquer. */
    boolean existsBySensorIdAndMeasuredAt(UUID sensorId, Instant measuredAt);

    long countByMeasuredAtAfter(Instant since);
}
