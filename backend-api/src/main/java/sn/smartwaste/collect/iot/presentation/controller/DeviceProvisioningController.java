package sn.smartwaste.collect.iot.presentation.controller;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import sn.smartwaste.collect.iot.application.service.DeviceProvisioningService;

/**
 * Enrôlement des équipements de terrain (`/v1/devices/**`).
 *
 * <p>Sans ces endpoints, la chaîne d'ingestion n'avait aucun émetteur possible : capteurs et
 * traceurs pouvaient être reçus, jamais déclarés.
 *
 * <p><b>Réservé à l'encadrement.</b> Enrôler un équipement, c'est créer une identité capable
 * d'écrire en base sans compte utilisateur — c'est au moins aussi sensible que gérer un compte.
 *
 * <p>⚠️ La réponse d'enrôlement et de rotation contient la clé <b>en clair</b>, unique occasion de
 * la lire. Elle n'est pas relisible ensuite : seule son empreinte est conservée.
 */
@RestController
@RequestMapping("/v1/devices")
// Enroler un equipement est un ACTE, pas un statut : la permission le nomme, et l'administration
// peut la confier a un profil technique sans qu'on livre du code.
@PreAuthorize("hasAuthority('MANAGE_DEVICES')")
public class DeviceProvisioningController {

    private final DeviceProvisioningService provisioningService;

    public DeviceProvisioningController(DeviceProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    @Operation(summary = "Enroler un capteur sur un point de collecte",
               description = "La cle d'API rendue ici ne sera plus jamais lisible.")
    @PostMapping("/sensors")
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceProvisioningService.ProvisionedDevice enrollSensor(@RequestBody SensorEnrollment body) {
        return provisioningService.enrollSensor(body.deviceCode(), body.depotoirId());
    }

    @Operation(summary = "Enroler un traceur sur un vehicule",
               description = "La cle d'API rendue ici ne sera plus jamais lisible.")
    @PostMapping("/vehicle-trackers")
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceProvisioningService.ProvisionedDevice enrollTracker(@RequestBody TrackerEnrollment body) {
        return provisioningService.enrollVehicleTracker(body.deviceCode(), body.vehicleId());
    }

    @Operation(summary = "Remplacer la cle d'un capteur (l'ancienne cesse immediatement)")
    @PostMapping("/sensors/{sensorId}/key")
    public DeviceProvisioningService.ProvisionedDevice rotateSensorKey(@PathVariable("sensorId") UUID sensorId) {
        return provisioningService.rotateSensorKey(sensorId);
    }

    @Operation(summary = "Remplacer la cle d'un traceur (l'ancienne cesse immediatement)")
    @PostMapping("/vehicle-trackers/{trackerId}/key")
    public DeviceProvisioningService.ProvisionedDevice rotateTrackerKey(@PathVariable("trackerId") UUID trackerId) {
        return provisioningService.rotateVehicleTrackerKey(trackerId);
    }

    @Operation(summary = "Lister les capteurs (sans les cles)")
    @GetMapping("/sensors")
    public List<DeviceProvisioningService.DeviceSummary> sensors() {
        return provisioningService.listSensors();
    }

    @Operation(summary = "Lister les traceurs (sans les cles)")
    @GetMapping("/vehicle-trackers")
    public List<DeviceProvisioningService.DeviceSummary> trackers() {
        return provisioningService.listVehicleTrackers();
    }

    @Operation(summary = "Desactiver un capteur (refuse a l'ingestion, historique conserve)")
    @DeleteMapping("/sensors/{sensorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateSensor(@PathVariable("sensorId") UUID sensorId) {
        provisioningService.deactivateSensor(sensorId);
    }

    @Operation(summary = "Desactiver un traceur")
    @DeleteMapping("/vehicle-trackers/{trackerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateTracker(@PathVariable("trackerId") UUID trackerId) {
        provisioningService.deactivateVehicleTracker(trackerId);
    }

    public record SensorEnrollment(String deviceCode, UUID depotoirId) { }

    public record TrackerEnrollment(String deviceCode, UUID vehicleId) { }
}
