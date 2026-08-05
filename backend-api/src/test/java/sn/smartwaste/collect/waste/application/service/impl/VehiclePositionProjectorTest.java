package sn.smartwaste.collect.waste.application.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.smartwaste.collect.shared.domain.event.VehiclePositionRecorded;
import sn.smartwaste.collect.waste.domain.model.Vehicle;
import sn.smartwaste.collect.waste.domain.repository.VehicleRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Mise à jour de la dernière position connue de la flotte.
 *
 * <p>Le comportement décisif est le rejet des positions arriérées. Un traceur qui perd le réseau
 * accumule et rejoue à la reconnexion : sans garde, le camion <b>reculerait</b> sur la carte. Une
 * position figée se remarque ; une position qui recule se croit.
 */
@ExtendWith(MockitoExtension.class)
class VehiclePositionProjectorTest {

    private static final UUID VEHICLE = UUID.randomUUID();

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehiclePositionProjector projector;

    private Vehicle vehicle(Double lat, Instant at) {
        var v = new Vehicle();
        v.setVehicleId(VEHICLE);
        v.setLastLatitude(lat);
        v.setLastPositionAt(at);
        when(vehicleRepository.findById(VEHICLE)).thenReturn(Optional.of(v));
        return v;
    }

    @Test
    @DisplayName("une position plus récente met à jour le véhicule")
    void newerPositionIsApplied() {
        var v = vehicle(14.70, Instant.now().minus(10, ChronoUnit.MINUTES));
        Instant now = Instant.now();

        projector.on(new VehiclePositionRecorded(VEHICLE, 14.76, -17.36, now));

        assertThat(v.getLastLatitude()).isEqualTo(14.76);
        assertThat(v.getLastPositionAt()).isEqualTo(now);
        verify(vehicleRepository).save(v);
    }

    @Test
    @DisplayName("une position arriérée n'écrase pas la plus récente : le camion ne recule pas")
    void stalePositionIsIgnored() {
        Instant recent = Instant.now();
        var v = vehicle(14.76, recent);

        projector.on(new VehiclePositionRecorded(VEHICLE, 14.10, -17.90, recent.minus(30, ChronoUnit.MINUTES)));

        assertThat(v.getLastLatitude()).isEqualTo(14.76);
        assertThat(v.getLastPositionAt()).isEqualTo(recent);
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    @DisplayName("une première position est acceptée même sans historique")
    void firstPositionIsAccepted() {
        var v = vehicle(null, null);

        projector.on(new VehiclePositionRecorded(VEHICLE, 14.76, -17.36, Instant.now()));

        assertThat(v.getLastLatitude()).isEqualTo(14.76);
        verify(vehicleRepository).save(v);
    }

    @Test
    @DisplayName("un véhicule retiré de la flotte n'interrompt pas l'ingestion")
    void unknownVehicleIsTolerated() {
        when(vehicleRepository.findById(VEHICLE)).thenReturn(Optional.empty());

        projector.on(new VehiclePositionRecorded(VEHICLE, 14.76, -17.36, Instant.now()));

        verify(vehicleRepository, never()).save(any());
    }
}
