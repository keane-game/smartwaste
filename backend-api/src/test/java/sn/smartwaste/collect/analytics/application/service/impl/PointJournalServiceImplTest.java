package sn.smartwaste.collect.analytics.application.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.smartwaste.collect.iot.application.api.IngestionMetrics;
import sn.smartwaste.collect.waste.application.api.WasteReadModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Journal d'un point de collecte (G8 du backlog).
 *
 * <p><b>Ce qui manquait.</b> La boucle produit désormais des faits — mesures, alertes, passages —
 * mais rien ne permettait de les lire <b>pour un point donné</b>. Un superviseur qui se demande
 * « pourquoi ce point déborde-t-il toutes les semaines ? » n'avait aucune vue d'ensemble : le
 * niveau courant, et rien de son histoire.
 *
 * <p><b>Pourquoi cela ne touche pas {@code HistoryEntity}.</b> Le journal est une <b>lecture</b> sur
 * des données déjà écrites par les lots 1 et 2, dans trois tables qui existent. Il n'exige ni
 * entité nouvelle ni suppression — la coquille vide {@code HistoryEntity} et son sort restent une
 * question ouverte, qu'il n'y avait pas lieu de trancher pour livrer ceci.
 *
 * <p><b>Ce qui se joue ici</b> est la fusion : deux contextes rendent leurs faits séparément, et
 * l'histoire d'un point est leur entrelacement chronologique.
 */
@ExtendWith(MockitoExtension.class)
class PointJournalServiceImplTest {

    private static final UUID POINT = UUID.fromString("00000000-0000-0000-0000-000000000215");
    private static final Instant DEBUT = Instant.parse("2026-08-01T00:00:00Z");
    private static final Instant FIN = Instant.parse("2026-08-05T00:00:00Z");

    @Mock private WasteReadModel waste;
    @Mock private IngestionMetrics iot;

    private PointJournalServiceImpl service() {
        return new PointJournalServiceImpl(waste, iot);
    }

    private void given(List<WasteReadModel.PointEvent> events,
                       List<IngestionMetrics.RecordedMeasurement> measurements) {
        lenient().when(waste.collectionPointExists(POINT)).thenReturn(true);
        lenient().when(waste.pointEvents(any(), any(), any())).thenReturn(events);
        lenient().when(iot.measurementsFor(any(), any(), any())).thenReturn(measurements);
    }

    private static Instant at(String iso) {
        return Instant.parse(iso);
    }

    @Test
    @DisplayName("les faits des deux contextes s'entrelacent par ordre chronologique")
    void interleavesBothSourcesChronologically() {
        // Le coeur du service : lus separement, ces faits ne racontent rien ; entrelaces, ils
        // racontent « le bac s'est rempli, l'alerte est partie, l'agent est passe ».
        given(List.of(
                        new WasteReadModel.PointEvent(at("2026-08-02T10:00:00Z"),
                                "ALERTE_LEVEE", "Point de collecte plein", "Niveau 92%"),
                        new WasteReadModel.PointEvent(at("2026-08-02T15:00:00Z"),
                                "COLLECTE", "Point collecte", null)),
                List.of(
                        new IngestionMetrics.RecordedMeasurement(at("2026-08-02T09:00:00Z"),
                                92, 34.0, 60.0, "IOT"),
                        new IngestionMetrics.RecordedMeasurement(at("2026-08-01T09:00:00Z"),
                                40, 30.0, 55.0, "IOT")));

        var journal = service().journalFor(POINT, DEBUT, FIN);

        assertThat(journal.entries()).extracting(e -> e.kind()).containsExactly(
                "MESURE", "MESURE", "ALERTE_LEVEE", "COLLECTE");
        assertThat(journal.entries()).extracting(e -> e.at()).isSorted();
    }

    @Test
    @DisplayName("un point sans histoire rend un journal vide, pas une erreur")
    void emptyHistoryIsNotAnError() {
        // Le cas majoritaire aujourd'hui : 69 des 71 points n'ont ni capteur ni passage.
        given(List.of(), List.of());

        assertThat(service().journalFor(POINT, DEBUT, FIN).entries()).isEmpty();
    }

    @Test
    @DisplayName("une mesure porte ses trois grandeurs dans son libelle")
    void measurementCarriesItsReadings() {
        given(List.of(), List.of(new IngestionMetrics.RecordedMeasurement(
                at("2026-08-02T09:00:00Z"), 92, 34.5, 61.0, "IOT")));

        var entree = service().journalFor(POINT, DEBUT, FIN).entries().getFirst();

        assertThat(entree.label()).contains("92");
        assertThat(entree.detail()).contains("34,5").contains("61");
    }

    @Test
    @DisplayName("une mesure partielle n'invente pas les grandeurs absentes")
    void partialMeasurementInventsNothing() {
        // Un capteur peut ne transmettre que le remplissage : afficher « 0 °C » serait une mesure
        // inventee, et un superviseur y lirait une anomalie thermique.
        given(List.of(), List.of(new IngestionMetrics.RecordedMeasurement(
                at("2026-08-02T09:00:00Z"), 92, null, null, "IOT")));

        var entree = service().journalFor(POINT, DEBUT, FIN).entries().getFirst();

        // Aucune condition n'est rendue du tout — plutot que « 0,0 °C », qu'un superviseur lirait
        // comme une anomalie thermique. Le niveau, lui, reste affiche.
        assertThat(entree.detail()).isNull();
        assertThat(entree.label()).contains("92");
    }

    @Test
    @DisplayName("un point inconnu est signale, pas rendu comme vide")
    void unknownPointIsReported() {
        // Un journal vide se lirait « ce point n'a pas d'histoire », alors qu'il n'existe pas.
        when(waste.collectionPointExists(POINT)).thenReturn(false);

        assertThat(service().journalFor(POINT, DEBUT, FIN)).isNull();
    }
}
