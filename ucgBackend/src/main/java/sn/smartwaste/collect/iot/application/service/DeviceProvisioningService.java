package sn.smartwaste.collect.iot.application.service;

import java.util.List;
import java.util.UUID;

/**
 * Enrôlement des équipements de terrain (capteurs, traceurs).
 *
 * <p>Sans lui, la chaîne d'ingestion n'avait aucun émetteur possible : les tables existaient, rien
 * ne pouvait les remplir.
 *
 * <p><b>La clé d'API n'est rendue qu'une fois</b>, à l'enrôlement. Seule son empreinte est
 * conservée, donc elle est irrécupérable ensuite — c'est ce qui garantit qu'une base exfiltrée ne
 * livre pas les clés. Un équipement dont la clé est perdue se fait réenrôler, pas relire.
 */
public interface DeviceProvisioningService {

    /** Enrôle un capteur sur un point de collecte. La clé rendue ne sera plus jamais lisible. */
    ProvisionedDevice enrollSensor(String deviceCode, Long depotoirId);

    /** Enrôle un traceur sur un véhicule. La clé rendue ne sera plus jamais lisible. */
    ProvisionedDevice enrollVehicleTracker(String deviceCode, UUID vehicleId);

    /** Remplace la clé d'un capteur — l'ancienne cesse immédiatement de fonctionner. */
    ProvisionedDevice rotateSensorKey(UUID sensorId);

    /** Remplace la clé d'un traceur — l'ancienne cesse immédiatement de fonctionner. */
    ProvisionedDevice rotateVehicleTrackerKey(UUID trackerId);

    /** Désactive un équipement sans le supprimer : il est refusé à l'ingestion, l'historique reste. */
    void deactivateSensor(UUID sensorId);

    void deactivateVehicleTracker(UUID trackerId);

    List<DeviceSummary> listSensors();

    List<DeviceSummary> listVehicleTrackers();

    /**
     * Résultat d'un enrôlement ou d'une rotation.
     *
     * @param apiKey clé en clair — <b>seule occasion de la lire</b>, à transmettre à l'installateur
     */
    record ProvisionedDevice(UUID deviceId, String deviceCode, String apiKey) { }

    /** Vue d'administration : jamais la clé, ni son empreinte. */
    record DeviceSummary(UUID deviceId, String deviceCode, String target,
                         boolean active, java.time.Instant lastSeenAt) { }
}
