package sn.smartwaste.collect.analytics.application.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import sn.smartwaste.collect.analytics.application.dto.SupervisionStats;
import sn.smartwaste.collect.iot.application.api.IngestionMetrics;
import sn.smartwaste.collect.platform.application.api.AlertStreamMetrics;
import sn.smartwaste.collect.platform.application.api.CitizenReportMetrics;
import sn.smartwaste.collect.waste.application.api.WasteReadModel;
import sn.smartwaste.collect.waste.application.api.WasteReadModel.ActiveCollectionPoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Assemblage du rapport de supervision.
 *
 * <p>Ce service ne calcule presque rien : il assemble ce que quatre contextes publient. Les tests
 * portent donc sur les deux endroits où un assemblage peut mentir — une répartition dont les
 * tranches vides disparaissent, et un parc sans capteurs présenté comme un parc vide.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SupervisionStatsServiceTest {

    @Mock
    private WasteReadModel wasteReadModel;
    @Mock
    private AlertStreamMetrics alertStreamMetrics;
    @Mock
    private IngestionMetrics ingestionMetrics;
    @Mock
    private CitizenReportMetrics citizenReportMetrics;

    private SupervisionStatsService service;

    @BeforeEach
    void setUp() {
        when(wasteReadModel.activeAlerts()).thenReturn(List.of());
        when(wasteReadModel.activeCollectionPoints()).thenReturn(List.of());
        when(wasteReadModel.activeCircuits()).thenReturn(List.of());
        when(ingestionMetrics.silentSensors(any())).thenReturn(List.of());
        when(citizenReportMetrics.countByStatus()).thenReturn(Map.of());
        when(citizenReportMetrics.medianResolutionTime()).thenReturn(Optional.empty());

        service = new SupervisionStatsService(wasteReadModel, alertStreamMetrics,
                ingestionMetrics, citizenReportMetrics, Map.of());
    }

    private void givenCollectionPoints(Integer... fillLevels) {
        when(wasteReadModel.activeCollectionPoints()).thenReturn(
                java.util.Arrays.stream(fillLevels)
                        .map(f -> new ActiveCollectionPoint("Bac", f))
                        .toList());
    }

    @Test
    @DisplayName("un point sans capteur est compté à part, jamais dans la tranche basse")
    void uninstrumentedPointsAreNotCountedAsEmpty() {
        // Les ranger dans « 0-49 » ferait passer un parc non équipé pour un parc vide —
        // exactement l'illusion que ce tableau de bord doit empêcher.
        givenCollectionPoints(null, null, 10);

        assertThat(service.compute(null).depotoirsByFillLevel())
                .containsEntry("NON_INSTRUMENTE", 2L)
                .containsEntry("0-49", 1L);
    }

    @Test
    @DisplayName("toutes les tranches sont présentes, y compris celles restées à zéro")
    void everyBucketIsPresentEvenAtZero() {
        givenCollectionPoints(95);

        assertThat(service.compute(null).depotoirsByFillLevel())
                .containsExactly(
                        java.util.Map.entry("0-49", 0L),
                        java.util.Map.entry("50-74", 0L),
                        java.util.Map.entry("75-89", 0L),
                        java.util.Map.entry("90-100", 1L),
                        java.util.Map.entry("NON_INSTRUMENTE", 0L));
    }

    @Test
    @DisplayName("les bornes de tranche sont inclusives en bas : 50, 75 et 90 basculent")
    void bucketBoundariesAreLowerInclusive() {
        givenCollectionPoints(49, 50, 74, 75, 89, 90, 100);

        assertThat(service.compute(null).depotoirsByFillLevel())
                .containsEntry("0-49", 1L)
                .containsEntry("50-74", 2L)
                .containsEntry("75-89", 2L)
                .containsEntry("90-100", 2L);
    }

    @Test
    @DisplayName("le seuil de silence est publié avec la liste, pour que le chiffre soit interprétable")
    void silenceThresholdIsPublishedAlongsideTheList() {
        // « 3 capteurs muets » ne veut rien dire sans savoir depuis quand. Le seuil voyage donc
        // avec la donnée plutôt que d'être une convention implicite côté frontend.
        Instant lastSeen = Instant.parse("2026-07-28T08:00:00Z");
        when(ingestionMetrics.silentSensors(any())).thenReturn(
                List.of(new IngestionMetrics.SilentSensor("ESP-001", 7L, lastSeen),
                        new IngestionMetrics.SilentSensor("ESP-002", 8L, null)));

        SupervisionStats.IngestionHealth ingestion = service.compute(null).ingestion();

        assertThat(ingestion.silenceThresholdHours()).isEqualTo(24);
        assertThat(ingestion.silentSensors())
                .extracting(SupervisionStats.IngestionHealth.SilentSensor::deviceCode)
                .containsExactly("ESP-001", "ESP-002");
    }

    @Test
    @DisplayName("un délai médian absent reste absent : aucune valeur n'est substituée")
    void missingMedianStaysNull() {
        // Rendre 0 ferait passer un service qui n'a jamais rien clos pour un service instantané.
        assertThat(service.compute(null).citizenReports().medianResolutionMinutes()).isNull();
    }

    @Test
    @DisplayName("le délai médian est publié en minutes")
    void medianIsExposedInMinutes() {
        when(citizenReportMetrics.medianResolutionTime()).thenReturn(Optional.of(Duration.ofHours(3)));

        assertThat(service.compute(null).citizenReports().medianResolutionMinutes()).isEqualTo(180L);
    }

    @Test
    @DisplayName("les mesures sont comptées sur la fenêtre demandée, pas sur une période fixe")
    void measurementsFollowTheRequestedWindow() {
        // Sinon la volumétrie de mesures et la série d'alertes ne couvriraient pas la même période,
        // et les lire l'une contre l'autre — ce pour quoi elles sont dans le même rapport —
        // induirait en erreur.
        Instant before = Instant.now().minus(Duration.ofDays(7));
        service.compute(7);

        var captor = org.mockito.ArgumentCaptor.forClass(Instant.class);
        org.mockito.Mockito.verify(ingestionMetrics).countMeasurementsSince(captor.capture());
        assertThat(captor.getValue()).isBetween(before.minusSeconds(60), before.plusSeconds(60));
    }
}
