package sn.smartwaste.collect.iot.application.service.impl;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.iot.domain.model.Sensor;
import sn.smartwaste.collect.iot.domain.repository.SensorRepository;
import sn.smartwaste.collect.shared.domain.event.SensorBackOnline;
import sn.smartwaste.collect.shared.domain.event.SensorSilenceDetected;

/**
 * Surveille le silence des capteurs (G7 du backlog).
 *
 * <p><b>Le trou que cela ferme.</b> {@code Sensor} portait déjà {@code lastSeenAt}, et l'ingestion
 * publiait ses métriques — mais rien ne se déclenchait quand un capteur cessait d'émettre. Le point
 * de collecte retombait silencieusement en {@code ETAT_INCONNU}, ce que la tournée traite
 * correctement, sans que personne apprenne que le capteur était mort. Un parc se dégrade alors tout
 * seul, et l'angle mort grandit.
 *
 * <p><b>Ce composant constate, il ne juge pas.</b> Il publie « ce capteur n'a rien dit depuis
 * 10 h » ; c'est le contexte « déchets » qui décide qu'une alerte s'impose. Même frontière que
 * pour l'ingestion d'une mesure : la règle métier n'appartient pas à la chaîne IoT.
 */
@Service
public class SensorHealthMonitor {

    private static final Logger log = LoggerFactory.getLogger(SensorHealthMonitor.class);

    private final SensorRepository sensorRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;
    private final Duration silenceThreshold;

    public SensorHealthMonitor(SensorRepository sensorRepository,
                               ApplicationEventPublisher eventPublisher,
                               Clock clock,
                               @Value("${sonaged.iot.silence-threshold-hours:6}") long thresholdHours) {
        this.sensorRepository = sensorRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
        this.silenceThreshold = Duration.ofHours(thresholdHours);
    }

    @Scheduled(cron = "${sonaged.iot.silence-check-cron:0 */15 * * * *}")
    @Transactional
    public void checkSilentSensors() {
        Instant now = Instant.now(clock);
        for (Sensor sensor : sensorRepository.findAll()) {
            if (!isSilent(sensor, now)) {
                continue;
            }
            sensor.setSilenceReportedAt(now);
            sensorRepository.save(sensor);
            log.warn("Capteur muet : {} (dernier contact {})",
                    sensor.getDeviceCode(), sensor.getLastSeenAt());
            eventPublisher.publishEvent(new SensorSilenceDetected(
                    sensor.getSensorId(), sensor.getDeviceCode(), sensor.getDepotoirId(),
                    sensor.getLastSeenAt(), now));
        }
    }

    /**
     * Constate l'activité d'un capteur, et lève le signalement s'il y en avait un.
     *
     * <p>Appelé par l'ingestion à chaque mesure reçue : c'est le seul moment où l'on sait de source
     * sûre qu'un capteur est vivant.
     */
    public void noteActivity(Sensor sensor) {
        if (sensor.getSilenceReportedAt() == null) {
            return;
        }
        sensor.setSilenceReportedAt(null);
        sensorRepository.save(sensor);
        log.info("Capteur {} de nouveau en ligne", sensor.getDeviceCode());
        eventPublisher.publishEvent(new SensorBackOnline(
                sensor.getSensorId(), sensor.getDeviceCode(), sensor.getDepotoirId(),
                Instant.now(clock)));
    }

    private boolean isSilent(Sensor sensor, Instant now) {
        if (!sensor.isActive()) {
            // Un capteur retire du service est muet par construction : le signaler serait du bruit.
            return false;
        }
        if (sensor.getLastSeenAt() == null) {
            // Un capteur enrole a l'instant n'a pas encore emis. Le declarer muet accueillerait
            // chaque installation par une alerte — meme piege que « jamais mesure n'est pas vide ».
            return false;
        }
        if (sensor.getSilenceReportedAt() != null) {
            // Episode deja signale : on ne repete pas. C'est ce qui fait desactiver ce genre de
            // surveillance quand on l'oublie.
            return false;
        }
        return sensor.getLastSeenAt().isBefore(now.minus(silenceThreshold));
    }
}
