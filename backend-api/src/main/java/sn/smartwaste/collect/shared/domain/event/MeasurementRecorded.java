package sn.smartwaste.collect.shared.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Événement de domaine : un capteur vient de transmettre une mesure (ADR-0004).
 *
 * <p>Publié par le contexte « Ingestion IoT » après persistance, consommé par le contexte
 * « Déchets » qui met à jour le niveau du point de collecte et évalue le seuil d'alerte.
 *
 * <p><b>Pourquoi l'évaluation du seuil n'est pas ici.</b> Savoir qu'un bac est plein à 87 % est une
 * mesure ; décider que 87 % justifie une alerte est une <b>règle métier déchets</b>. L'ingestion
 * n'a pas à la connaître — sans quoi changer le seuil imposerait de toucher la chaîne IoT.
 *
 * <p>Charge utile autonome, en types primitifs : le jour où l'ingestion passe sur MQTT ou Kafka
 * (ADR-0004, anticipé), cet enregistrement est sérialisable tel quel.
 *
 * @param sensorId           capteur émetteur
 * @param depotoirId         point de collecte instrumenté, référence par identifiant (ADR-0012)
 * @param fillLevelPercent   niveau de remplissage en %, {@code null} si le capteur ne le mesure pas
 * @param temperatureCelsius température interne, {@code null} si non mesurée (capteur DHT11)
 * @param humidityPercent    humidité interne, {@code null} si non mesurée (capteur DHT11)
 * @param measuredAt         horodatage <b>de la mesure</b>, pas de sa réception
 */
public record MeasurementRecorded(UUID sensorId,
                                  UUID depotoirId,
                                  Integer fillLevelPercent,
                                  Double temperatureCelsius,
                                  Double humidityPercent,
                                  Instant measuredAt) { }
