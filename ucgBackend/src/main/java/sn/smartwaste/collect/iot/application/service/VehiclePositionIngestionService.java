package sn.smartwaste.collect.iot.application.service;

import java.time.Instant;

/** Réception des positions transmises par les traceurs embarqués (ADR-0004, même chaîne). */
public interface VehiclePositionIngestionService {

    /**
     * Enregistre une position au nom du traceur identifié par sa clé d'API.
     *
     * @throws org.springframework.web.server.ResponseStatusException 401 si la clé est inconnue ou
     *         le traceur désactivé, 400 si les coordonnées sont hors bornes
     */
    void ingest(String apiKey, double latitude, double longitude, Instant recordedAt);
}
