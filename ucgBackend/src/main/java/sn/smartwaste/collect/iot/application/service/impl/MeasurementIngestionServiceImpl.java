package sn.smartwaste.collect.iot.application.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import sn.smartwaste.collect.iot.application.service.MeasurementIngestionService;
import sn.smartwaste.collect.iot.domain.model.Measurement;
import sn.smartwaste.collect.iot.domain.model.MeasurementSource;
import sn.smartwaste.collect.iot.domain.model.Sensor;
import sn.smartwaste.collect.iot.domain.repository.MeasurementRepository;
import sn.smartwaste.collect.iot.domain.repository.SensorRepository;
import sn.smartwaste.collect.shared.domain.event.MeasurementRecorded;

/**
 * Ingestion des mesures.
 *
 * <p><b>Authentification par clé de device, distincte du JWT utilisateur</b> (ADR-0004 §2) : un
 * capteur n'est pas une personne, il n'a pas de session, et lui distribuer un jeton utilisateur
 * reviendrait à donner à un objet posé dans la rue les droits d'un compte.
 *
 * <p><b>Idempotence.</b> Un capteur en zone de mauvaise couverture réémet : sans garde, chaque
 * réémission créerait une mesure et fausserait les moyennes. Le couple (capteur, horodatage de
 * mesure) fait office de clé naturelle ; un doublon est accepté en silence — répondre en erreur
 * pousserait le firmware à réessayer indéfiniment.
 *
 * <p><b>Ce service n'évalue aucun seuil et ne crée aucune alerte</b> : il publie
 * {@link MeasurementRecorded} et s'arrête là. La règle « à partir de quand faut-il alerter »
 * appartient au contexte « Déchets ».
 */
@Service
@Transactional
public class MeasurementIngestionServiceImpl implements MeasurementIngestionService {

    /** Tolérance sur un horodatage dans le futur : dérive d'horloge d'un microcontrôleur. */
    private static final Duration CLOCK_SKEW_TOLERANCE = Duration.ofMinutes(15);

    private final SensorRepository sensorRepository;
    private final MeasurementRepository measurementRepository;
    private final ApplicationEventPublisher eventPublisher;

    public MeasurementIngestionServiceImpl(SensorRepository sensorRepository,
                                           MeasurementRepository measurementRepository,
                                           ApplicationEventPublisher eventPublisher) {
        this.sensorRepository = sensorRepository;
        this.measurementRepository = measurementRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public boolean ingest(String apiKey, Integer fillLevelPercent, Double temperatureCelsius,
                          Double humidityPercent, Instant measuredAt) {
        Sensor sensor = authenticate(apiKey);
        Instant now = Instant.now();
        Instant when = validateInstant(measuredAt, now);
        validatePayload(fillLevelPercent, temperatureCelsius, humidityPercent);

        if (measurementRepository.existsBySensorIdAndMeasuredAt(sensor.getSensorId(), when)) {
            return false; // réémission : déjà connue, rien à faire
        }

        Measurement measurement = new Measurement();
        measurement.setSensorId(sensor.getSensorId());
        measurement.setDepotoirId(sensor.getDepotoirId());
        measurement.setFillLevelPercent(fillLevelPercent);
        measurement.setTemperatureCelsius(temperatureCelsius);
        measurement.setHumidityPercent(humidityPercent);
        measurement.setMeasuredAt(when);
        measurement.setReceivedAt(now);
        measurement.setSource(MeasurementSource.IOT);
        measurementRepository.save(measurement);

        sensor.setLastSeenAt(now);
        sensorRepository.save(sensor);

        eventPublisher.publishEvent(new MeasurementRecorded(
                sensor.getSensorId(), sensor.getDepotoirId(),
                fillLevelPercent, temperatureCelsius, humidityPercent, when));
        return true;
    }

    private Sensor authenticate(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Clé de capteur absente");
        }
        // Message identique quelle que soit la cause : distinguer « clé inconnue » de « capteur
        // désactivé » renseignerait un attaquant sur les clés en sa possession.
        Sensor sensor = sensorRepository.findByApiKeyHash(hash(apiKey))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Capteur non reconnu"));
        if (!sensor.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Capteur non reconnu");
        }
        return sensor;
    }

    private Instant validateInstant(Instant measuredAt, Instant now) {
        // Horodatage absent : on prend l'heure de réception plutôt que de rejeter — perdre une
        // mesure parce que le firmware n'a pas d'horloge serait absurde.
        Instant when = (measuredAt == null) ? now : measuredAt;
        if (when.isAfter(now.plus(CLOCK_SKEW_TOLERANCE))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Horodatage de mesure dans le futur");
        }
        return when;
    }

    private void validatePayload(Integer fill, Double temperature, Double humidity) {
        if (fill == null && temperature == null && humidity == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mesure vide");
        }
        if (fill != null && (fill < 0 || fill > 100)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Niveau de remplissage hors bornes (0-100) : " + fill);
        }
        if (humidity != null && (humidity < 0 || humidity > 100)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Humidité hors bornes (0-100) : " + humidity);
        }
        // Bornes du DHT11, larges à dessein : un capteur défaillant renvoie souvent des valeurs
        // aberrantes, qu'il vaut mieux refuser que voir polluer les moyennes.
        if (temperature != null && (temperature < -40 || temperature > 125)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Température hors bornes (-40..125) : " + temperature);
        }
    }

    /** SHA-256 : la clé est un secret à forte entropie, un hachage lent n'apporterait rien. */
    public static String hash(String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(apiKey.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible sur cette JVM", e);
        }
    }
}
