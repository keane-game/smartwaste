package sn.smartwaste.collect.shared.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Un capteur précédemment signalé muet a recommencé à émettre (G7 du backlog).
 *
 * <p>Le pendant de {@link SensorSilenceDetected} : sans lui, une alerte de silence resterait
 * ouverte indéfiniment et le tableau de bord accumulerait des problèmes déjà résolus.
 */
public record SensorBackOnline(UUID sensorId,
                               String deviceCode,
                               UUID depotoirId,
                               Instant backAt) { }
