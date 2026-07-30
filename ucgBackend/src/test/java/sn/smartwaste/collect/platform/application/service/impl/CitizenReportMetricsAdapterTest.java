package sn.smartwaste.collect.platform.application.service.impl;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.smartwaste.collect.platform.domain.model.Avis;
import sn.smartwaste.collect.platform.domain.model.AvisStatus;
import sn.smartwaste.collect.platform.domain.repository.AvisRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Traitement des signalements citoyens.
 *
 * <p>Ces indicateurs disent ce que l'habitant a vécu : combien de temps il a attendu, et combien
 * de signalements n'ont jamais reçu de réponse. Chaque test ci-dessous ferme une manière d'obtenir
 * un chiffre flatteur à partir des mêmes données.
 */
@ExtendWith(MockitoExtension.class)
class CitizenReportMetricsAdapterTest {

    private static final Instant NOW = Instant.parse("2026-07-30T12:00:00Z");

    @Mock
    private AvisRepository avisRepository;

    private CitizenReportMetricsAdapter adapter() {
        return new CitizenReportMetricsAdapter(avisRepository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static Avis avis(AvisStatus statut, Instant submittedAt, Instant processedAt) {
        var a = new Avis();
        a.setStatut(statut);
        a.setSubmittedAt(submittedAt);
        a.setProcessedAt(processedAt);
        return a;
    }

    private void given(Avis... avis) {
        when(avisRepository.findAll()).thenReturn(List.of(avis));
    }

    private static Instant hoursAgo(long h) {
        return NOW.minus(h, ChronoUnit.HOURS);
    }

    @Test
    @DisplayName("les états sans aucun signalement sont rendus à zéro, pas omis")
    void everyStatusIsPresentEvenAtZero() {
        // Un tableau où « TRAITE » disparaît quand rien n'a été traité se lit comme une donnée
        // manquante — ou pire, ne se remarque pas du tout.
        given(avis(AvisStatus.SIGNALE, hoursAgo(3), null));

        assertThat(adapter().countByStatus())
                .containsEntry("SIGNALE", 1L)
                .containsEntry("EN_COURS", 0L)
                .containsEntry("TRAITE", 0L)
                .containsEntry("REJETE", 0L);
    }

    @Test
    @DisplayName("la médiane décrit le cas courant, là où la moyenne serait emportée par un oubli")
    void medianIsNotDraggedByAnOutlier() {
        // Trois clôtures en 1 h et une oubliée six mois : la moyenne annoncerait ~1 100 h, un
        // chiffre que personne ne reconnaît. La médiane rend l'heure réellement vécue.
        given(closedAfter(Duration.ofHours(1)),
              closedAfter(Duration.ofHours(1)),
              closedAfter(Duration.ofHours(1)),
              closedAfter(Duration.ofDays(180)));

        assertThat(adapter().medianResolutionTime())
                .contains(Duration.ofHours(1));
    }

    @Test
    @DisplayName("sur un nombre pair de clôtures, la médiane est la moyenne des deux valeurs centrales")
    void evenSampleAveragesTheTwoMiddleValues() {
        given(closedAfter(Duration.ofHours(2)),
              closedAfter(Duration.ofHours(4)));

        assertThat(adapter().medianResolutionTime())
                .contains(Duration.ofHours(3));
    }

    @Test
    @DisplayName("sans aucune clôture, aucun délai n'est inventé")
    void noClosureMeansNoValue() {
        // Rendre zéro ferait passer un service qui n'a jamais rien traité pour un service
        // instantané. `empty` oblige l'appelant à afficher « non disponible ».
        given(avis(AvisStatus.SIGNALE, hoursAgo(50), null));

        assertThat(adapter().medianResolutionTime()).isEmpty();
    }

    @Test
    @DisplayName("une clôture antérieure au dépôt est écartée du calcul")
    void negativeDelaysAreIgnored() {
        // Cas réel : les signalements antérieurs à l'ajout de `submittedAt`, dont la date de dépôt
        // a été reprise après coup. Un délai négatif tirerait la médiane vers le bas sans qu'on
        // puisse le voir dans le résultat.
        given(avis(AvisStatus.TRAITE, hoursAgo(1), hoursAgo(5)),
              closedAfter(Duration.ofHours(6)));

        assertThat(adapter().medianResolutionTime())
                .contains(Duration.ofHours(6));
    }

    @Test
    @DisplayName("seuls les signalements encore ouverts comptent comme oubliés")
    void onlyOpenReportsCountAsStale() {
        given(avis(AvisStatus.SIGNALE, hoursAgo(72), null),   // ouvert, ancien -> oublié
              avis(AvisStatus.EN_COURS, hoursAgo(72), null),  // pris en charge mais toujours ouvert
              avis(AvisStatus.SIGNALE, hoursAgo(3), null),    // ouvert, récent
              avis(AvisStatus.TRAITE, hoursAgo(72), hoursAgo(70)),
              avis(AvisStatus.REJETE, hoursAgo(72), hoursAgo(70)));

        assertThat(adapter().countOpenOlderThan(Duration.ofHours(48))).isEqualTo(2);
    }

    /** Signalement clos, déposé {@code delay} avant sa clôture. */
    private static Avis closedAfter(Duration delay) {
        Instant processedAt = hoursAgo(1);
        return avis(AvisStatus.TRAITE, processedAt.minus(delay), processedAt);
    }
}
