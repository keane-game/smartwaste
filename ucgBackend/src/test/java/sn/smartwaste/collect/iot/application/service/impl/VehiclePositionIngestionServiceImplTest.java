package sn.smartwaste.collect.iot.application.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.server.ResponseStatusException;

import sn.smartwaste.collect.iot.domain.model.VehicleTracker;
import sn.smartwaste.collect.iot.domain.repository.VehicleTrackerRepository;
import sn.smartwaste.collect.iot.infrastructure.security.DeviceApiKeys;
import sn.smartwaste.collect.shared.domain.event.VehiclePositionRecorded;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Ingestion des positions de véhicules.
 *
 * <p>Les rejets testés ici viennent tous du terrain, pas de la théorie : un traceur bon marché perd
 * le signal, renvoie des coordonnées nulles, ou repart avec une horloge fausse. Accepter ces
 * messages placerait des camions au mauvais endroit sur la carte de supervision — et un superviseur
 * agirait dessus.
 */
@ExtendWith(MockitoExtension.class)
class VehiclePositionIngestionServiceImplTest {

    private static final String KEY = "cle-de-traceur";
    private static final UUID VEHICLE = UUID.randomUUID();

    @Mock
    private VehicleTrackerRepository trackerRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private VehiclePositionIngestionServiceImpl service;

    private VehicleTracker tracker(boolean active) {
        var tracker = new VehicleTracker();
        tracker.setVehicleId(VEHICLE);
        tracker.setApiKeyHash(DeviceApiKeys.hash(KEY));
        tracker.setActive(active);
        lenient().when(trackerRepository.findByApiKeyHash(DeviceApiKeys.hash(KEY)))
                .thenReturn(Optional.of(tracker));
        lenient().when(trackerRepository.save(any(VehicleTracker.class))).thenAnswer(i -> i.getArgument(0));
        return tracker;
    }

    private VehiclePositionRecorded capturePublished() {
        ArgumentCaptor<Object> published = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(published.capture());
        return (VehiclePositionRecorded) published.getValue();
    }

    @Test
    @DisplayName("une position valide est publiée pour le véhicule du traceur")
    void validPositionIsPublished() {
        tracker(true);
        Instant at = Instant.now().minus(1, ChronoUnit.MINUTES);

        service.ingest(KEY, 14.7645, -17.3660, at);

        VehiclePositionRecorded event = capturePublished();
        assertThat(event.vehicleId()).isEqualTo(VEHICLE);
        assertThat(event.latitude()).isEqualTo(14.7645);
        assertThat(event.recordedAt()).isEqualTo(at);
    }

    @Test
    @DisplayName("la position (0,0) d'un traceur ayant perdu le signal est refusée")
    void nullIslandIsRejected() {
        tracker(true);

        // Un traceur sans fix renvoie volontiers (0,0) — un point au large du golfe de Guinée.
        // L'accepter placerait le camion en pleine mer.
        assertThatThrownBy(() -> service.ingest(KEY, 0, 0, Instant.now()))
                .isInstanceOf(ResponseStatusException.class);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("des coordonnées hors bornes sont refusées")
    void outOfRangeCoordinatesAreRejected() {
        tracker(true);

        assertThatThrownBy(() -> service.ingest(KEY, 91, 10, Instant.now()))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> service.ingest(KEY, 10, 181, Instant.now()))
                .isInstanceOf(ResponseStatusException.class);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("un horodatage lointain dans le futur est refusé")
    void futureTimestampIsRejected() {
        tracker(true);

        assertThatThrownBy(() -> service.ingest(KEY, 14.7, -17.3, Instant.now().plus(2, ChronoUnit.HOURS)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    @DisplayName("un horodatage absent est remplacé par l'heure de réception plutôt que rejeté")
    void missingTimestampFallsBackToNow() {
        tracker(true);

        service.ingest(KEY, 14.7, -17.3, null);

        // Perdre une position parce que le firmware n'a pas d'horloge serait absurde.
        assertThat(capturePublished().recordedAt()).isNotNull();
    }

    @Test
    @DisplayName("clé inconnue ou traceur désactivé : 401, sans distinguer les deux cas")
    void unknownOrInactiveTrackerIsRejected() {
        when(trackerRepository.findByApiKeyHash(DeviceApiKeys.hash("inconnue"))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.ingest("inconnue", 14.7, -17.3, Instant.now()))
                .isInstanceOf(ResponseStatusException.class);

        tracker(false);
        assertThatThrownBy(() -> service.ingest(KEY, 14.7, -17.3, Instant.now()))
                .isInstanceOf(ResponseStatusException.class);

        assertThatThrownBy(() -> service.ingest(null, 14.7, -17.3, Instant.now()))
                .isInstanceOf(ResponseStatusException.class);
        verify(eventPublisher, never()).publishEvent(any());
    }
}
