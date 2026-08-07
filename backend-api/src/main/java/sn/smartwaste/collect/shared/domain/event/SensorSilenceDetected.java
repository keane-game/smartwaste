package sn.smartwaste.collect.shared.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Un capteur a cessé d'émettre au-delà du seuil de silence toléré (G7 du backlog).
 *
 * <p>Publié par le contexte {@code iot}, qui sait qu'un équipement s'est tu ; consommé par
 * {@code waste}, seul à savoir qu'un point de collecte privé de capteur mérite une alerte. La
 * frontière est la même que pour {@link MeasurementRecorded} : « le capteur n'a rien dit depuis
 * 10 h » est un fait, « cela justifie une alerte » est une règle métier déchets.
 *
 * @param silentSince dernière émission connue, jamais {@code null} — un capteur qui n'a jamais
 *                    émis n'est pas muet, il n'a pas encore commencé
 */
public record SensorSilenceDetected(UUID sensorId,
                                    String deviceCode,
                                    UUID depotoirId,
                                    Instant silentSince,
                                    Instant detectedAt) { }
