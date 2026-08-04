package sn.smartwaste.collect.iot.application.service.impl;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.iot.application.api.IngestionMetrics;
import sn.smartwaste.collect.iot.domain.model.Sensor;
import sn.smartwaste.collect.iot.domain.repository.MeasurementRepository;
import sn.smartwaste.collect.iot.domain.repository.SensorRepository;

/** Implémentation des métriques publiées par le contexte « Ingestion IoT ». */
@Service
@Transactional(readOnly = true)
public class IngestionMetricsAdapter implements IngestionMetrics {

    private final SensorRepository sensorRepository;
    private final MeasurementRepository measurementRepository;

    public IngestionMetricsAdapter(SensorRepository sensorRepository,
                                   MeasurementRepository measurementRepository) {
        this.sensorRepository = sensorRepository;
        this.measurementRepository = measurementRepository;
    }

    @Override
    public long countActiveSensors() {
        return sensorRepository.findAll().stream().filter(Sensor::isActive).count();
    }

    @Override
    public long countInstrumentedCollectionPoints() {
        return sensorRepository.findAll().stream()
                .filter(Sensor::isActive)
                .map(Sensor::getDepotoirId)
                .distinct()
                .count();
    }

    @Override
    public long countMeasurementsSince(Instant since) {
        return measurementRepository.countByMeasuredAtAfter(since);
    }

    @Override
    public List<SilentSensor> silentSensors(Instant since) {
        return sensorRepository.findAll().stream()
                .filter(Sensor::isActive)
                // `lastSeenAt` nul = enrole mais n'a jamais emis. C'est le cas le plus grave :
                // l'installation n'a peut-etre jamais fonctionne, et personne ne s'en est apercu.
                .filter(s -> s.getLastSeenAt() == null || s.getLastSeenAt().isBefore(since))
                .map(s -> new SilentSensor(s.getDeviceCode(), s.getDepotoirId(), s.getLastSeenAt()))
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    public List<RecordedMeasurement> measurementsFor(Long depotoirId, java.time.Instant from,
                                                     java.time.Instant to) {
        return measurementRepository.findByDepotoirIdOrderByMeasuredAtDesc(depotoirId).stream()
                .filter(m -> !m.getMeasuredAt().isBefore(from) && m.getMeasuredAt().isBefore(to))
                .map(m -> new RecordedMeasurement(m.getMeasuredAt(), m.getFillLevelPercent(),
                        m.getTemperatureCelsius(), m.getHumidityPercent(),
                        m.getSource() == null ? null : m.getSource().name()))
                .toList();
    }
}
