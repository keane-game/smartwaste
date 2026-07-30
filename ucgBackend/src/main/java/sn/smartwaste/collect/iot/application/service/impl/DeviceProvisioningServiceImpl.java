package sn.smartwaste.collect.iot.application.service.impl;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import sn.smartwaste.collect.iot.application.service.DeviceProvisioningService;
import sn.smartwaste.collect.iot.domain.model.Sensor;
import sn.smartwaste.collect.iot.domain.model.VehicleTracker;
import sn.smartwaste.collect.iot.domain.repository.SensorRepository;
import sn.smartwaste.collect.iot.domain.repository.VehicleTrackerRepository;
import sn.smartwaste.collect.iot.infrastructure.security.DeviceApiKeys;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;

/**
 * Enrôlement des équipements.
 *
 * <p><b>La clé est générée ici, jamais fournie par l'appelant.</b> Laisser l'administrateur choisir
 * la clé d'un équipement reviendrait à accepter « 1234 » sur un objet posé dans la rue, qui peut
 * ensuite injecter des mesures pendant des années.
 *
 * <p>256 bits de {@link SecureRandom}, rendus une seule fois. Seule l'empreinte est persistée : la
 * clé est donc irrécupérable, y compris pour un administrateur — c'est ce qui donne sa valeur au
 * stockage haché.
 */
@Service
@Transactional
public class DeviceProvisioningServiceImpl implements DeviceProvisioningService {

    private static final int API_KEY_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SensorRepository sensorRepository;
    private final VehicleTrackerRepository trackerRepository;

    public DeviceProvisioningServiceImpl(SensorRepository sensorRepository,
                                         VehicleTrackerRepository trackerRepository) {
        this.sensorRepository = sensorRepository;
        this.trackerRepository = trackerRepository;
    }

    @Override
    public ProvisionedDevice enrollSensor(String deviceCode, Long depotoirId) {
        requireCode(deviceCode);
        if (depotoirId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le point de collecte est obligatoire");
        }
        sensorRepository.findByDeviceCode(deviceCode).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Un capteur porte deja le code " + deviceCode);
        });
        String apiKey = newApiKey();
        var sensor = new Sensor();
        sensor.setDeviceCode(deviceCode);
        sensor.setDepotoirId(depotoirId);
        sensor.setApiKeyHash(DeviceApiKeys.hash(apiKey));
        sensor.setActive(true);
        var saved = sensorRepository.save(sensor);
        return new ProvisionedDevice(saved.getSensorId(), saved.getDeviceCode(), apiKey);
    }

    @Override
    public ProvisionedDevice enrollVehicleTracker(String deviceCode, UUID vehicleId) {
        requireCode(deviceCode);
        if (vehicleId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le vehicule est obligatoire");
        }
        String apiKey = newApiKey();
        var tracker = new VehicleTracker();
        tracker.setDeviceCode(deviceCode);
        tracker.setVehicleId(vehicleId);
        tracker.setApiKeyHash(DeviceApiKeys.hash(apiKey));
        tracker.setActive(true);
        var saved = trackerRepository.save(tracker);
        return new ProvisionedDevice(saved.getTrackerId(), saved.getDeviceCode(), apiKey);
    }

    @Override
    public ProvisionedDevice rotateSensorKey(UUID sensorId) {
        var sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new ResourceNotFoundException("Capteur [%s] introuvable".formatted(sensorId)));
        String apiKey = newApiKey();
        // L'ancienne empreinte est ecrasee : la cle precedente cesse de fonctionner immediatement,
        // ce qui est le but d'une rotation apres suspicion de compromission.
        sensor.setApiKeyHash(DeviceApiKeys.hash(apiKey));
        sensorRepository.save(sensor);
        return new ProvisionedDevice(sensor.getSensorId(), sensor.getDeviceCode(), apiKey);
    }

    @Override
    public ProvisionedDevice rotateVehicleTrackerKey(UUID trackerId) {
        var tracker = trackerRepository.findById(trackerId)
                .orElseThrow(() -> new ResourceNotFoundException("Traceur [%s] introuvable".formatted(trackerId)));
        String apiKey = newApiKey();
        tracker.setApiKeyHash(DeviceApiKeys.hash(apiKey));
        trackerRepository.save(tracker);
        return new ProvisionedDevice(tracker.getTrackerId(), tracker.getDeviceCode(), apiKey);
    }

    @Override
    public void deactivateSensor(UUID sensorId) {
        sensorRepository.findById(sensorId).ifPresent(s -> {
            s.setActive(false);
            sensorRepository.save(s);
        });
    }

    @Override
    public void deactivateVehicleTracker(UUID trackerId) {
        trackerRepository.findById(trackerId).ifPresent(t -> {
            t.setActive(false);
            trackerRepository.save(t);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceSummary> listSensors() {
        // Trie du plus ancien contact au plus recent : le parc se lit par ce qui ne va pas.
        // `nullsFirst` place en tete les capteurs qui n'ont JAMAIS emis — une installation qui n'a
        // jamais parle est le premier cas a regarder, et il ne declenche aucune alerte (G7).
        return sensorRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(Sensor::getLastSeenAt,
                        java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder())))
                .map(s -> new DeviceSummary(s.getSensorId(), s.getDeviceCode(),
                        String.valueOf(s.getDepotoirId()), s.isActive(), s.getLastSeenAt(),
                        s.getSilenceReportedAt()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceSummary> listVehicleTrackers() {
        return trackerRepository.findAll().stream()
                .map(t -> new DeviceSummary(t.getTrackerId(), t.getDeviceCode(),
                        String.valueOf(t.getVehicleId()), t.isActive(), t.getLastSeenAt(), null))
                .toList();
    }

    private void requireCode(String deviceCode) {
        if (deviceCode == null || deviceCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le code de l'equipement est obligatoire");
        }
    }

    /** Clé jamais fournie par l'appelant : sur un objet posé dans la rue, « 1234 » vivrait des années. */
    private static String newApiKey() {
        byte[] bytes = new byte[API_KEY_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
