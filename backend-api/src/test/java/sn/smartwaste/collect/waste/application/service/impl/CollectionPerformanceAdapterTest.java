package sn.smartwaste.collect.waste.application.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.smartwaste.collect.shared.domain.model.DeletionStatus;
import sn.smartwaste.collect.waste.domain.model.AlertEntity;
import sn.smartwaste.collect.waste.domain.model.CollectionPassage;
import sn.smartwaste.collect.waste.domain.model.DepotoirEntity;
import sn.smartwaste.collect.waste.domain.model.PassageOutcome;
import sn.smartwaste.collect.waste.domain.repository.AlertRepository;
import sn.smartwaste.collect.waste.domain.repository.CollectionPassageRepository;
import sn.smartwaste.collect.waste.domain.repository.DepotoirRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Rapport d'efficacité de la collecte (G5 du backlog).
 *
 * <p><b>Ce que ce rapport rend enfin possible.</b> {@code /v1/supervision/stats} ne donnait que des
 * compteurs <i>instantanés</i> : combien d'alertes ouvertes à cet instant, combien de points. Rien
 * ne disait si la situation s'améliorait. Le cas d'utilisation « générer des rapports de
 * performance pour surveiller l'efficacité du système » (mémoire §3.1.6.1) restait sans réponse.
 *
 * <p>Il était de toute façon incalculable avant le lot 2 : sans passage enregistré, il n'existe ni
 * délai entre l'alerte et le vidage, ni numérateur au taux de réalisation.
 *
 * <p><b>Trois choix se lisent dans ces cas</b>, et chacun évite un chiffre qui mentirait :
 * <ul>
 *   <li>aucune alerte résolue ne donne <b>pas</b> un délai de zéro — qui se lirait comme une
 *       réactivité parfaite — mais l'absence de valeur ;</li>
 *   <li>les alertes de capteur muet sont exclues du délai : elles mesurent la maintenance, pas la
 *       collecte, et les mélanger produirait une moyenne qui ne décrit rien ;</li>
 *   <li>un point visité deux fois ne compte qu'une : c'est une couverture de territoire, pas un
 *       compteur d'actes.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class CollectionPerformanceAdapterTest {

    private static final UUID COMMUNE = UUID.randomUUID();
    private static final Instant DEBUT = Instant.parse("2026-07-01T00:00:00Z");
    private static final Instant FIN = Instant.parse("2026-08-01T00:00:00Z");

    @Mock private DepotoirRepository depotoirRepository;
    @Mock private AlertRepository alertRepository;
    @Mock private CollectionPassageRepository passageRepository;

    private CollectionPerformanceAdapter adapter() {
        return new CollectionPerformanceAdapter(depotoirRepository, alertRepository,
                passageRepository);
    }

    /** UUID stable dérivé d'un petit entier : les cas restent lisibles, les entités sont en UUID. */
    private static UUID uuid(long n) {
        return UUID.fromString(String.format("00000000-0000-0000-0000-%012d", n));
    }

    private DepotoirEntity point(long id, String address) {
        var d = new DepotoirEntity();
        d.setDepotoirId(uuid(id));
        d.setAddress(address);
        return d;
    }

    private AlertEntity alert(long depotoirId, String object, String raisedAt, String resolvedAt) {
        var a = new AlertEntity();
        a.setDepotoirId(uuid(depotoirId));
        a.setObject(object);
        a.setCreatedDate(LocalDateTime.parse(raisedAt));
        if (resolvedAt != null) {
            a.setResolvedAt(LocalDateTime.parse(resolvedAt));
        }
        return a;
    }

    private CollectionPassage passage(long depotoirId, PassageOutcome outcome) {
        var p = new CollectionPassage();
        p.setDepotoirId(uuid(depotoirId));
        p.setOutcome(outcome);
        p.setOccurredAt(DEBUT.plusSeconds(3600));
        return p;
    }

    private void given(List<DepotoirEntity> points, List<AlertEntity> alerts,
                       List<CollectionPassage> passages) {
        lenient().when(depotoirRepository.findByCommuneIdAndDeletionStatus(COMMUNE,
                DeletionStatus.ACTIVE)).thenReturn(points);
        lenient().when(alertRepository.findByDepotoirIdInAndCreatedDateBetween(anyList(), any(), any()))
                .thenReturn(alerts);
        lenient().when(passageRepository.findByDepotoirIdInAndOccurredAtBetween(anyList(), any(), any()))
                .thenReturn(passages);
    }

    @Test
    @DisplayName("le delai moyen de resolution est calcule sur les alertes de collecte")
    void averagesResolutionDelay() {
        given(List.of(point(1, "Ecole")),
              List.of(alert(1, "Point de collecte plein", "2026-07-10T08:00:00", "2026-07-10T12:00:00"),
                      alert(1, "Point de collecte plein", "2026-07-11T08:00:00", "2026-07-11T10:00:00")),
              List.of());

        var report = adapter().reportFor(COMMUNE, DEBUT, FIN);

        // 4 h et 2 h -> 3 h.
        assertThat(report.averageResolutionHours()).isCloseTo(3.0, within(0.001));
        assertThat(report.alertsRaised()).isEqualTo(2);
        assertThat(report.alertsResolved()).isEqualTo(2);
    }

    @Test
    @DisplayName("aucune alerte resolue ne donne pas un delai de zero")
    void noResolutionMeansNoFigure() {
        // Zero se lirait comme une reactivite parfaite, alors qu'il signifie « on ne sait pas ».
        given(List.of(point(1, "Ecole")),
              List.of(alert(1, "Point de collecte plein", "2026-07-10T08:00:00", null)),
              List.of());

        var report = adapter().reportFor(COMMUNE, DEBUT, FIN);

        assertThat(report.averageResolutionHours()).isNull();
        assertThat(report.alertsRaised()).isEqualTo(1);
        assertThat(report.alertsResolved()).isZero();
    }

    @Test
    @DisplayName("une alerte de capteur muet n'entre pas dans le delai de collecte")
    void maintenanceAlertsStayOutOfTheDelay() {
        // Elle mesure la reactivite de la maintenance : la moyenner avec les vidages produirait un
        // chiffre qui ne decrit ni l'une ni l'autre.
        given(List.of(point(1, "Ecole")),
              List.of(alert(1, "Point de collecte plein", "2026-07-10T08:00:00", "2026-07-10T12:00:00"),
                      alert(1, SensorSilenceProjector.OBJET_SILENCE,
                              "2026-07-10T08:00:00", "2026-07-15T08:00:00")),
              List.of());

        var report = adapter().reportFor(COMMUNE, DEBUT, FIN);

        assertThat(report.averageResolutionHours()).isCloseTo(4.0, within(0.001));
    }

    @Test
    @DisplayName("le taux de realisation compte les points, pas les actes")
    void completionCountsPointsNotActs() {
        given(List.of(point(1, "Ecole"), point(2, "Marche"), point(3, "Gare"), point(4, "Port")),
              List.of(),
              List.of(passage(1, PassageOutcome.COLLECTED),
                      passage(1, PassageOutcome.COLLECTED),
                      passage(2, PassageOutcome.INACCESSIBLE)));

        var report = adapter().reportFor(COMMUNE, DEBUT, FIN);

        assertThat(report.stops()).isEqualTo(4);
        assertThat(report.served()).isEqualTo(2);      // points 1 et 2
        assertThat(report.collected()).isEqualTo(1);   // seul le point 1 a ete vide
        assertThat(report.inaccessible()).isEqualTo(1);
        assertThat(report.completionRate()).isCloseTo(0.5, within(0.001));
    }

    @Test
    @DisplayName("les points chroniques sont classes par nombre de debordements")
    void ranksChronicPoints() {
        // C'est le seul chiffre du rapport qui designe une ACTION : renforcer la tournee ici.
        given(List.of(point(1, "Ecole"), point(2, "Marche")),
              List.of(alert(2, "Point de collecte plein", "2026-07-01T08:00:00", null),
                      alert(2, "Point de collecte plein", "2026-07-05T08:00:00", null),
                      alert(2, "Point de collecte plein", "2026-07-09T08:00:00", null),
                      alert(1, "Point de collecte plein", "2026-07-03T08:00:00", null)),
              List.of());

        var report = adapter().reportFor(COMMUNE, DEBUT, FIN);

        assertThat(report.chronicPoints()).hasSize(2);
        assertThat(report.chronicPoints().getFirst().depotoirId()).isEqualTo(uuid(2));
        assertThat(report.chronicPoints().getFirst().address()).isEqualTo("Marche");
        assertThat(report.chronicPoints().getFirst().overflowCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("un territoire sans point rend un rapport vide, pas une erreur")
    void emptyTerritoryYieldsAnEmptyReport() {
        // Une commune peut n'avoir aucun point importe : 15 des 71 n'ont d'ailleurs pas de commune.
        given(List.of(), List.of(), List.of());

        var report = adapter().reportFor(COMMUNE, DEBUT, FIN);

        assertThat(report.stops()).isZero();
        assertThat(report.completionRate()).isZero();
        assertThat(report.chronicPoints()).isEmpty();
        assertThat(report.averageResolutionHours()).isNull();
    }

    @Test
    @DisplayName("sans commune precisee, le rapport porte sur tout le referentiel")
    void nullCommuneCoversEverything() {
        when(depotoirRepository.findByDeletionStatus(DeletionStatus.ACTIVE))
                .thenReturn(List.of(point(1, "Ecole"), point(2, "Marche")));
        lenient().when(alertRepository.findByDepotoirIdInAndCreatedDateBetween(anyList(), any(), any()))
                .thenReturn(List.of());
        lenient().when(passageRepository.findByDepotoirIdInAndOccurredAtBetween(anyList(), any(), any()))
                .thenReturn(List.of());

        var report = adapter().reportFor(null, DEBUT, FIN);

        assertThat(report.stops()).isEqualTo(2);
    }
}
