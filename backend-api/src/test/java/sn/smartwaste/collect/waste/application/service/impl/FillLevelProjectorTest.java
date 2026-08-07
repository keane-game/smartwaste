package sn.smartwaste.collect.waste.application.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import sn.smartwaste.collect.shared.domain.event.AlertRaisedEvent;
import sn.smartwaste.collect.shared.domain.event.MeasurementRecorded;
import sn.smartwaste.collect.waste.domain.model.AlertCode;
import sn.smartwaste.collect.waste.domain.model.AlertEntity;
import sn.smartwaste.collect.waste.domain.model.DepotoirEntity;
import sn.smartwaste.collect.waste.domain.repository.AlertRepository;
import sn.smartwaste.collect.waste.application.service.ThresholdResolver;
import sn.smartwaste.collect.waste.domain.repository.DepotoirRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Moteur de seuils — la regle metier centrale du produit.
 *
 * <p>C'est ici que « detecter le niveau de remplissage » devient « declencher une alerte ». Les
 * comportements verrouilles sont ceux qui font la difference entre un moteur exploitable et un
 * moteur qu'on finit par desactiver :
 *
 * <ol>
 *   <li>l'alerte se leve au <b>franchissement</b>, pas a chaque mesure au-dessus du seuil ;</li>
 *   <li>une mesure <b>arrieree</b> n'ecrase pas un etat plus recent ;</li>
 *   <li>l'alerte automatique est <b>rattachee a son point de collecte</b> (ADR-0005) ;</li>
 *   <li>elle emprunte le <b>meme evenement</b> que les alertes manuelles, donc la diffusion SSE
 *       fonctionne sans modification.</li>
 * </ol>
 */
@ExtendWith(MockitoExtension.class)
class FillLevelProjectorTest {

    private static final UUID DEPOTOIR_ID = UUID.fromString("00000000-0000-0000-0000-000000000042");
    private static final UUID ALERT_ID = UUID.fromString("00000000-0000-0000-0000-000000000007");
    private static final UUID SENSOR_ID = UUID.randomUUID();
    private static final int THRESHOLD = 80;

    @Mock
    private DepotoirRepository depotoirRepository;
    @Mock
    private AlertRepository alertRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private ThresholdResolver thresholdResolver;

    private FillLevelProjector projector() {
        return new FillLevelProjector(depotoirRepository, alertRepository, eventPublisher, thresholdResolver);
    }

    /** Seuils applicables : remplissage seul, sauf mention contraire. */
    private void thresholds(Double temperature, Double humidity) {
        lenient().when(thresholdResolver.resolve(any(DepotoirEntity.class)))
                .thenReturn(new ThresholdResolver.EffectiveThresholds(THRESHOLD, temperature, humidity));
    }

    private static DepotoirEntity depotoir(Integer fill, Instant lastMeasuredAt) {
        DepotoirEntity d = new DepotoirEntity();
        d.setDepotoirId(DEPOTOIR_ID);
        d.setAddress("Pikine Nord");
        d.setFillLevelPercent(fill);
        d.setLastMeasuredAt(lastMeasuredAt);
        return d;
    }

    private static MeasurementRecorded measure(Integer fill, Instant at) {
        return new MeasurementRecorded(SENSOR_ID, DEPOTOIR_ID, fill, null, null, at);
    }

    private void givenDepotoir(DepotoirEntity d) {
        when(depotoirRepository.findById(DEPOTOIR_ID)).thenReturn(Optional.of(d));
        lenient().when(depotoirRepository.save(any(DepotoirEntity.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(alertRepository.save(any(AlertEntity.class))).thenAnswer(i -> {
            AlertEntity a = i.getArgument(0);
            a.setAlertId(ALERT_ID);
            return a;
        });
    }

    private AlertRaisedEvent captureAlertEvent() {
        ArgumentCaptor<Object> published = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(published.capture());
        assertThat(published.getValue()).isInstanceOf(AlertRaisedEvent.class);
        return (AlertRaisedEvent) published.getValue();
    }

    @Test
    @DisplayName("une mesure applique le niveau et l'horodatage au point de collecte")
    void measurementUpdatesCollectionPoint() {
        DepotoirEntity d = depotoir(null, null);
        thresholds(null, null);
        givenDepotoir(d);
        Instant at = Instant.now();

        projector().on(measure(30, at));

        assertThat(d.getFillLevelPercent()).isEqualTo(30);
        assertThat(d.getLastMeasuredAt()).isEqualTo(at);
        verify(alertRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("franchir le seuil leve une alerte rattachee au point de collecte")
    void crossingThresholdRaisesAlertLinkedToCollectionPoint() {
        thresholds(null, null);
        givenDepotoir(depotoir(50, Instant.now().minus(1, ChronoUnit.HOURS)));

        projector().on(measure(87, Instant.now()));

        ArgumentCaptor<AlertEntity> saved = ArgumentCaptor.forClass(AlertEntity.class);
        verify(alertRepository).save(saved.capture());
        // ADR-0005 : l'alerte AUTOMATIQUE est toujours rattachee a son point de collecte —
        // c'est ce qui la rend exploitable sur la carte et dans les tournees.
        assertThat(saved.getValue().getDepotoirId()).isEqualTo(DEPOTOIR_ID);
        assertThat(saved.getValue().getAddress()).isEqualTo("Pikine Nord");
        assertThat(saved.getValue().getMessage()).contains("87").contains("80");

        AlertRaisedEvent event = captureAlertEvent();
        assertThat(event.source()).isEqualTo(AlertRaisedEvent.Source.THRESHOLD);
        assertThat(event.alert().alertId()).isEqualTo(ALERT_ID);
    }

    @Test
    @DisplayName("rester au-dessus du seuil ne releve PAS d'alerte a chaque mesure")
    void stayingAboveThresholdDoesNotSpam() {
        thresholds(null, null);
        givenDepotoir(depotoir(85, Instant.now().minus(1, ChronoUnit.HOURS)));

        projector().on(measure(92, Instant.now()));

        // Sans cette garde, un bac reste plein produirait une alerte par mesure — des centaines
        // par jour. C'est le defaut qui fait desactiver ce genre de moteur en exploitation.
        verify(alertRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("repasser sous le seuil puis au-dessus leve une nouvelle alerte")
    void newCrossingAfterCollectionRaisesAgain() {
        // Le bac a ete vide (retour a 10 %) : le prochain remplissage est un evenement neuf.
        thresholds(null, null);
        givenDepotoir(depotoir(10, Instant.now().minus(1, ChronoUnit.HOURS)));

        projector().on(measure(81, Instant.now()));

        verify(alertRepository).save(any(AlertEntity.class));
    }

    @Test
    @DisplayName("une mesure arrieree n'ecrase pas un etat plus recent")
    void staleMeasurementIsIgnored() {
        Instant recent = Instant.now();
        DepotoirEntity d = depotoir(20, recent);
        when(depotoirRepository.findById(DEPOTOIR_ID)).thenReturn(Optional.of(d));

        // Capteur hors ligne qui poste en differe : le plus recent doit gagner.
        projector().on(measure(95, recent.minus(2, ChronoUnit.HOURS)));

        assertThat(d.getFillLevelPercent()).isEqualTo(20);
        verify(depotoirRepository, never()).save(any());
        verify(alertRepository, never()).save(any());
    }

    @Test
    @DisplayName("une mesure sans niveau (climat seul) ne touche pas au remplissage")
    void climateOnlyMeasurementIsIgnoredForFillLevel() {
        thresholds(null, null);
        givenDepotoir(depotoir(50, Instant.now().minus(1, ChronoUnit.HOURS)));

        projector().on(new MeasurementRecorded(SENSOR_ID, DEPOTOIR_ID, null, 34.5, 70.0, Instant.now()));

        // Le climat est enregistre, mais aucune alerte de remplissage n'est levee et le niveau
        // ne bouge pas : les grandeurs sont independantes.
        verify(alertRepository, never()).save(any());
    }

    @Test
    @DisplayName("franchir le seuil de temperature leve une alerte distincte du remplissage")
    void temperatureThresholdRaisesItsOwnAlert() {
        thresholds(40.0, null);
        DepotoirEntity d = depotoir(20, Instant.now().minus(1, ChronoUnit.HOURS));
        d.setLastTemperatureCelsius(30.0);
        givenDepotoir(d);

        projector().on(new MeasurementRecorded(SENSOR_ID, DEPOTOIR_ID, 20, 45.0, null, Instant.now()));

        ArgumentCaptor<AlertEntity> saved = ArgumentCaptor.forClass(AlertEntity.class);
        verify(alertRepository).save(saved.capture());
        // Le capteur DHT11 est prevu par la specification precisement pour ca : odeurs et
        // proliferation bacterienne. Rien a voir avec un bac plein.
        assertThat(saved.getValue().getObject()).isEqualTo("Temperature anormale");
        assertThat(saved.getValue().getCode()).isEqualTo(AlertCode.DANGER);
    }

    @Test
    @DisplayName("un bac peut deborder ET fermenter : deux alertes, deux problemes")
    void fillAndClimateAreEvaluatedIndependently() {
        thresholds(40.0, 90.0);
        DepotoirEntity d = depotoir(10, Instant.now().minus(1, ChronoUnit.HOURS));
        d.setLastTemperatureCelsius(20.0);
        d.setLastHumidityPercent(50.0);
        givenDepotoir(d);

        projector().on(new MeasurementRecorded(SENSOR_ID, DEPOTOIR_ID, 95, 45.0, 95.0, Instant.now()));

        // Trois franchissements simultanes : ce sont trois interventions distinctes, pas une seule.
        verify(alertRepository, org.mockito.Mockito.times(3)).save(any(AlertEntity.class));
    }

    @Test
    @DisplayName("une grandeur non surveillee (seuil nul) ne declenche jamais")
    void unmonitoredMetricNeverTriggers() {
        thresholds(null, null);
        DepotoirEntity d = depotoir(10, Instant.now().minus(1, ChronoUnit.HOURS));
        givenDepotoir(d);

        projector().on(new MeasurementRecorded(SENSOR_ID, DEPOTOIR_ID, 10, 200.0, 100.0, Instant.now()));

        // Seuil nul = « ne pas surveiller », ce qui differe d'un seuil a zero qui alerterait
        // en permanence.
        verify(alertRepository, never()).save(any());
    }

    @Test
    @DisplayName("un point de collecte inconnu n'interrompt pas l'ingestion")
    void unknownCollectionPointIsToleratedy() {
        when(depotoirRepository.findById(DEPOTOIR_ID)).thenReturn(Optional.empty());

        // La mesure est deja persistee cote iot : echouer ici la perdrait sans rien reparer.
        projector().on(measure(95, Instant.now()));

        verify(alertRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
