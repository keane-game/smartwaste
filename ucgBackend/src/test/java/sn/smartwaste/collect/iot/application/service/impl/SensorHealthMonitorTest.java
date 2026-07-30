package sn.smartwaste.collect.iot.application.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import sn.smartwaste.collect.iot.domain.model.Sensor;
import sn.smartwaste.collect.iot.domain.repository.SensorRepository;
import sn.smartwaste.collect.shared.domain.event.SensorSilenceDetected;
import sn.smartwaste.collect.shared.domain.event.SensorBackOnline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Détection des capteurs muets (G7 du backlog).
 *
 * <p><b>Le trou que cela ferme.</b> {@code Sensor} portait déjà {@code lastSeenAt} et
 * {@code active}, et l'ingestion publiait ses métriques — mais <b>rien ne se déclenchait</b> quand
 * un capteur cessait d'émettre. Le point de collecte retombait silencieusement en
 * {@code ETAT_INCONNU} : la tournée le traite correctement, ce qui est le mérite de cette priorité,
 * mais personne n'apprenait que le capteur était mort. Un parc se dégrade alors sans que quiconque
 * le sache, et l'angle mort grandit tout seul.
 *
 * <p><b>Ce qui compte autant que la détection : ne pas répéter.</b> Un capteur mort depuis une
 * semaine ne doit pas produire une alerte à chaque passage du planificateur. C'est le défaut le
 * plus courant de ce genre de surveillance, et celui qui la fait désactiver.
 */
@ExtendWith(MockitoExtension.class)
class SensorHealthMonitorTest {

    private static final Instant NOW = Instant.parse("2026-07-30T12:00:00Z");
    private static final int SEUIL_HEURES = 6;

    @Mock private SensorRepository sensorRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    private SensorHealthMonitor monitor() {
        return new SensorHealthMonitor(sensorRepository, eventPublisher,
                Clock.fixed(NOW, ZoneId.of("UTC")), SEUIL_HEURES);
    }

    private static Sensor sensor(Instant lastSeenAt, Instant silenceReportedAt, boolean active) {
        var s = new Sensor();
        s.setSensorId(UUID.randomUUID());
        s.setDeviceCode("CAPTEUR-TEST");
        s.setDepotoirId(42L);
        s.setActive(active);
        s.setLastSeenAt(lastSeenAt);
        s.setSilenceReportedAt(silenceReportedAt);
        return s;
    }

    private static Instant hoursAgo(long h) {
        return NOW.minusSeconds(h * 3600);
    }

    @Test
    @DisplayName("un capteur silencieux au-delà du seuil est signalé")
    void reportsASilentSensor() {
        var muet = sensor(hoursAgo(10), null, true);
        when(sensorRepository.findAll()).thenReturn(List.of(muet));

        monitor().checkSilentSensors();

        verify(eventPublisher).publishEvent(any(SensorSilenceDetected.class));
        assertThat(muet.getSilenceReportedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("un capteur qui émet encore n'est pas signalé")
    void ignoresALiveSensor() {
        when(sensorRepository.findAll()).thenReturn(List.of(sensor(hoursAgo(1), null, true)));

        monitor().checkSilentSensors();

        verify(eventPublisher, never()).publishEvent(any(SensorSilenceDetected.class));
    }

    @Test
    @DisplayName("un silence déjà signalé ne l'est pas une seconde fois")
    void doesNotReportTheSameSilenceTwice() {
        // Le cas qui decide de l'utilisabilite : sans cette garde, un capteur mort depuis une
        // semaine produirait une alerte a chaque passage du planificateur.
        when(sensorRepository.findAll())
                .thenReturn(List.of(sensor(hoursAgo(200), hoursAgo(150), true)));

        monitor().checkSilentSensors();

        verify(eventPublisher, never()).publishEvent(any(SensorSilenceDetected.class));
    }

    @Test
    @DisplayName("un capteur désactivé n'est pas surveillé")
    void ignoresADeactivatedSensor() {
        // Un capteur retire du service est muet par construction : le signaler serait du bruit.
        when(sensorRepository.findAll()).thenReturn(List.of(sensor(hoursAgo(500), null, false)));

        monitor().checkSilentSensors();

        verify(eventPublisher, never()).publishEvent(any(SensorSilenceDetected.class));
    }

    @Test
    @DisplayName("un capteur qui n'a jamais émis n'est pas signalé comme muet")
    void ignoresANeverSeenSensor() {
        // Un capteur enrole a l'instant n'a pas encore emis : le declarer muet accueillerait chaque
        // installation par une alerte. C'est le meme piege que « jamais mesure n'est pas vide ».
        when(sensorRepository.findAll()).thenReturn(List.of(sensor(null, null, true)));

        monitor().checkSilentSensors();

        verify(eventPublisher, never()).publishEvent(any(SensorSilenceDetected.class));
    }

    @Test
    @DisplayName("le retour d'un capteur signalé est constaté et le signalement levé")
    void clearsTheSilenceOnReturn() {
        var revenu = sensor(hoursAgo(200), hoursAgo(150), true);

        monitor().noteActivity(revenu);

        assertThat(revenu.getSilenceReportedAt()).isNull();
        verify(eventPublisher).publishEvent(any(SensorBackOnline.class));
    }

    @Test
    @DisplayName("l'activité d'un capteur jamais signalé ne produit aucun événement")
    void staysQuietWhenNothingWasReported() {
        monitor().noteActivity(sensor(hoursAgo(1), null, true));

        verify(eventPublisher, never()).publishEvent(any(SensorBackOnline.class));
    }
}
