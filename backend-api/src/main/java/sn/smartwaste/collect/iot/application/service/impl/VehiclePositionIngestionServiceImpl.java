package sn.smartwaste.collect.iot.application.service.impl;

import java.time.Duration;
import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import sn.smartwaste.collect.iot.application.service.VehiclePositionIngestionService;
import sn.smartwaste.collect.iot.domain.model.VehicleTracker;
import sn.smartwaste.collect.iot.domain.repository.VehicleTrackerRepository;
import sn.smartwaste.collect.iot.infrastructure.security.DeviceApiKeys;
import sn.smartwaste.collect.shared.domain.event.VehiclePositionRecorded;

/**
 * Ingestion des positions de véhicules.
 *
 * <p><b>Aucune position n'est persistée ici</b>, contrairement aux mesures de remplissage. Un camion
 * émet plusieurs fois par minute : conserver la trace complète serait un tout autre volume, et un
 * tout autre besoin (reconstitution de tournée, à traiter pour lui-même). Le service publie
 * l'événement ; le contexte « Déchets » ne garde que le dernier point connu, qui est ce dont la
 * carte a besoin.
 *
 * <p>Même authentification par clé de device que les capteurs, et même message d'échec quelle que
 * soit la cause : distinguer « clé inconnue » de « traceur désactivé » renseignerait un attaquant.
 */
@Service
@Transactional
public class VehiclePositionIngestionServiceImpl implements VehiclePositionIngestionService {

    /** Tolérance sur un horodatage dans le futur : dérive d'horloge d'un traceur. */
    private static final Duration CLOCK_SKEW_TOLERANCE = Duration.ofMinutes(15);

    private final VehicleTrackerRepository trackerRepository;
    private final ApplicationEventPublisher eventPublisher;

    public VehiclePositionIngestionServiceImpl(VehicleTrackerRepository trackerRepository,
                                                ApplicationEventPublisher eventPublisher) {
        this.trackerRepository = trackerRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void ingest(String apiKey, double latitude, double longitude, Instant recordedAt) {
        VehicleTracker tracker = authenticate(apiKey);
        Instant now = Instant.now();
        Instant when = (recordedAt == null) ? now : recordedAt;

        if (when.isAfter(now.plus(CLOCK_SKEW_TOLERANCE))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Horodatage dans le futur");
        }
        // Un traceur qui perd le signal renvoie volontiers (0, 0) — un point au large du golfe de
        // Guinée. L'accepter placerait le camion en pleine mer sur la carte de supervision.
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coordonnees hors bornes");
        }
        if (latitude == 0 && longitude == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Position nulle (perte de signal)");
        }

        tracker.setLastSeenAt(now);
        trackerRepository.save(tracker);

        eventPublisher.publishEvent(
                new VehiclePositionRecorded(tracker.getVehicleId(), latitude, longitude, when));
    }

    private VehicleTracker authenticate(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cle de traceur absente");
        }
        VehicleTracker tracker = trackerRepository.findByApiKeyHash(DeviceApiKeys.hash(apiKey))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Traceur non reconnu"));
        if (!tracker.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Traceur non reconnu");
        }
        return tracker;
    }
}
