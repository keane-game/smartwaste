package sn.smartwaste.collect.iot.application.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.smartwaste.collect.iot.application.api.IngestionMetrics.SilentSensor;
import sn.smartwaste.collect.iot.domain.model.Sensor;
import sn.smartwaste.collect.iot.domain.repository.MeasurementRepository;
import sn.smartwaste.collect.iot.domain.repository.SensorRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Santé de la chaîne d'ingestion.
 *
 * <p><b>Ce que ces tests protègent.</b> Un capteur en panne ne transmet rien, donc ne déclenche
 * aucune alerte : dans tous les autres indicateurs, sa défaillance est <i>indiscernable</i> d'un
 * point de collecte qui se porte bien. Chaque cas ci-dessous correspond à une manière de rendre
 * cette panne invisible — et donc de rendre le tableau de bord d'autant plus rassurant que le parc
 * se dégrade.
 */
@ExtendWith(MockitoExtension.class)
class IngestionMetricsAdapterTest {

    private static final Instant NOW = Instant.parse("2026-07-30T12:00:00Z");
    private static final Instant CUTOFF = NOW.minus(24, ChronoUnit.HOURS);

    @Mock
    private SensorRepository sensorRepository;
    @Mock
    private MeasurementRepository measurementRepository;

    private IngestionMetricsAdapter adapter() {
        return new IngestionMetricsAdapter(sensorRepository, measurementRepository);
    }

    private static Sensor sensor(String code, long depotoirId, boolean active, Instant lastSeenAt) {
        var s = new Sensor();
        s.setDeviceCode(code);
        s.setDepotoirId(depotoirId);
        s.setActive(active);
        s.setLastSeenAt(lastSeenAt);
        return s;
    }

    private void given(Sensor... sensors) {
        when(sensorRepository.findAll()).thenReturn(List.of(sensors));
    }

    private static Instant hoursAgo(long h) {
        return NOW.minus(h, ChronoUnit.HOURS);
    }

    @Test
    @DisplayName("un capteur enrolé qui n'a jamais émis est signalé muet, pas ignoré")
    void neverEmittedIsSilent() {
        // Le cas le plus grave : l'installation n'a peut-être jamais fonctionné. Traiter
        // `lastSeenAt == null` comme « pas encore de données » le ferait disparaître du rapport
        // exactement au moment où il faut aller sur place.
        given(sensor("ESP-001", 1L, true, null),
              sensor("ESP-002", 2L, true, hoursAgo(1)));

        assertThat(adapter().silentSensors(CUTOFF))
                .extracting(SilentSensor::deviceCode)
                .containsExactly("ESP-001");
    }

    @Test
    @DisplayName("un capteur muet depuis plus longtemps que le seuil est signalé, pas celui qui vient d'émettre")
    void onlySensorsPastTheCutoffAreReported() {
        given(sensor("MUET", 1L, true, hoursAgo(30)),
              sensor("FRAIS", 2L, true, hoursAgo(2)));

        assertThat(adapter().silentSensors(CUTOFF))
                .extracting(SilentSensor::deviceCode)
                .containsExactly("MUET");
    }

    @Test
    @DisplayName("un capteur désactivé n'est ni compté ni signalé muet")
    void inactiveSensorsAreExcluded() {
        // Un capteur retiré du terrain est muet par construction. Le signaler enverrait
        // quelqu'un chercher un matériel qui n'y est plus, et noierait les vraies pannes.
        given(sensor("RETIRE", 1L, false, hoursAgo(500)),
              sensor("ACTIF", 2L, true, hoursAgo(1)));

        var adapter = adapter();
        assertThat(adapter.silentSensors(CUTOFF)).isEmpty();
        assertThat(adapter.countActiveSensors()).isEqualTo(1);
    }

    @Test
    @DisplayName("deux capteurs sur le même point ne comptent qu'un point instrumenté")
    void instrumentedPointsAreDistinct() {
        // Sinon la couverture du parc paraît meilleure qu'elle ne l'est dès qu'un point est
        // doublement équipé — le cas normal quand on remplace un capteur sans désenrôler l'ancien.
        given(sensor("A", 7L, true, hoursAgo(1)),
              sensor("B", 7L, true, hoursAgo(1)),
              sensor("C", 8L, true, hoursAgo(1)));

        assertThat(adapter().countInstrumentedCollectionPoints()).isEqualTo(2);
    }

    @Test
    @DisplayName("le comptage des mesures est délégué à la base, pas recalculé en mémoire")
    void measurementsAreCountedInDatabase() {
        // La table des mesures est la seule dont le volume interdit un chargement complet :
        // un `findAll().size()` y deviendrait un incident de production.
        when(measurementRepository.countByMeasuredAtAfter(CUTOFF)).thenReturn(4_200L);

        assertThat(adapter().countMeasurementsSince(CUTOFF)).isEqualTo(4_200L);
    }
}
