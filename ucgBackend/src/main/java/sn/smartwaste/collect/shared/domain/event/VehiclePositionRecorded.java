package sn.smartwaste.collect.shared.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Événement de domaine : un véhicule de collecte a transmis sa position.
 *
 * <p>Publié par « Ingestion IoT », consommé par « Déchets » qui tient à jour la dernière position
 * connue de sa flotte. Même découplage que pour les mesures de remplissage : l'ingestion ne sait pas
 * ce qu'est une flotte, et le contexte métier ne sait pas comment la position est arrivée.
 *
 * @param vehicleId  véhicule concerné, référence par identifiant (ADR-0012)
 * @param latitude   latitude en degrés décimaux
 * @param longitude  longitude en degrés décimaux
 * @param recordedAt horodatage <b>de la position</b>, pas de sa réception
 */
public record VehiclePositionRecorded(UUID vehicleId,
                                      double latitude,
                                      double longitude,
                                      Instant recordedAt) { }
