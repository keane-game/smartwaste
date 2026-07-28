package sn.smartwaste.collect.iot.application.service;

import java.time.Instant;

/** Réception des mesures transmises par les capteurs (ADR-0004). */
public interface MeasurementIngestionService {

    /**
     * Enregistre une mesure au nom du capteur identifié par sa clé d'API.
     *
     * @return {@code true} si la mesure a été enregistrée, {@code false} si elle était un doublon
     * @throws org.springframework.web.server.ResponseStatusException 401 si la clé est inconnue
     *         ou le capteur désactivé, 400 si la charge utile est incohérente
     */
    boolean ingest(String apiKey, Integer fillLevelPercent, Double temperatureCelsius,
                   Double humidityPercent, Instant measuredAt);
}
