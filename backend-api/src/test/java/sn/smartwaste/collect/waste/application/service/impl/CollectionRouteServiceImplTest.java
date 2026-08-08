package sn.smartwaste.collect.waste.application.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.smartwaste.collect.shared.domain.model.DeletionStatus;
import sn.smartwaste.collect.waste.application.service.CollectionRouteService.RouteStop;
import sn.smartwaste.collect.waste.application.service.CollectionRouteService.StopPriority;
import sn.smartwaste.collect.waste.domain.model.DepotoirEntity;
import sn.smartwaste.collect.waste.domain.repository.DepotoirRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Priorisation des tournées.
 *
 * <p>Les trois règles testées ici corrigent chacune une erreur qu'on commet naturellement en
 * écrivant ce tri, et dont la conséquence est un outil qu'on finit par ne plus suivre.
 */
@ExtendWith(MockitoExtension.class)
class CollectionRouteServiceImplTest {

    private static final UUID COMMUNE = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-07-29T09:00:00Z");
    private static final int THRESHOLD = 80;

    @Mock
    private DepotoirRepository depotoirRepository;

    @Mock
    private sn.smartwaste.collect.waste.domain.repository.CollectionPassageRepository passageRepository;

    /** Ces cas portent sur le tri, pas sur l'autorisation : le garde laisse passer. */
    private final TerritorialAccessGuard accessGuard = new TerritorialAccessGuard(null, null) {
        @Override public void requireAccessTo(java.util.UUID communeId) { }
        @Override public boolean isAdministration() { return true; }
    };

    private CollectionRouteServiceImpl service() {
        // Anti-famine desactive (age maximal tres eleve) : ces cas-ci verifient le tri par urgence
        // et par anciennete, sans que le rattrapage des delaisses ne s'en mele.
        // Bonus de remplissage a 0 : ces cas testent le tri par urgence/anciennete, pas le
        // departage geographique pondere (couvert par CollectionRouteGeographyTest).
        return new CollectionRouteServiceImpl(depotoirRepository, passageRepository, accessGuard,
                Clock.fixed(NOW, ZoneId.of("UTC")), THRESHOLD, 24, 24 * 3650, 0);
    }

    /** UUID stable dérivé d'un petit entier : les cas restent lisibles (`point(1, ...)`), l'entité est en UUID. */
    private static UUID uuid(int n) {
        return UUID.fromString(String.format("00000000-0000-0000-0000-%012d", n));
    }

    private static DepotoirEntity point(int id, String address, Integer fill, Instant measuredAt) {
        var d = new DepotoirEntity();
        d.setDepotoirId(uuid(id));
        d.setAddress(address);
        d.setFillLevelPercent(fill);
        d.setLastMeasuredAt(measuredAt);
        return d;
    }

    private void given(DepotoirEntity... points) {
        when(depotoirRepository.findByCommuneIdAndDeletionStatus(COMMUNE, DeletionStatus.ACTIVE))
                .thenReturn(List.of(points));
    }

    private static Instant hoursAgo(long h) {
        return NOW.minus(h, ChronoUnit.HOURS);
    }

    @Test
    @DisplayName("un point jamais mesuré n'est pas traité comme vide : il passe avant ceux qui se remplissent")
    void neverMeasuredIsNotTreatedAsEmpty() {
        given(point(1, "Plein", 95, hoursAgo(1)),
              point(2, "Jamais mesure", null, null),
              point(3, "A moitie", 50, hoursAgo(1)),
              point(4, "Vide", 5, hoursAgo(1)));

        List<RouteStop> plan = service().planForCommune(COMMUNE);

        // L'erreur naturelle serait de trier sur fillLevel en lisant null comme 0 : le point sans
        // capteur finirait dernier et ne serait JAMAIS collecte. L'angle mort grandirait tout seul.
        assertThat(plan).extracting(RouteStop::depotoirId).containsExactly(uuid(1), uuid(2), uuid(3), uuid(4));
        assertThat(plan.get(1).priority()).isEqualTo(StopPriority.ETAT_INCONNU);
        assertThat(plan.get(1).reason()).contains("Jamais mesure");
    }

    @Test
    @DisplayName("une mesure périmée vaut une absence de mesure")
    void staleMeasurementCountsAsUnknown() {
        given(point(1, "Mesure d'hier", 10, hoursAgo(30)),
              point(2, "Mesure fraiche", 50, hoursAgo(1)));

        List<RouteStop> plan = service().planForCommune(COMMUNE);

        // Un niveau de 10 % date de 30 h ne dit rien de l'etat d'aujourd'hui : le croire reviendrait
        // a ignorer le point alors qu'il a pu deborder entre-temps.
        assertThat(plan.get(0).depotoirId()).isEqualTo(uuid(1));
        assertThat(plan.get(0).priority()).isEqualTo(StopPriority.ETAT_INCONNU);
        assertThat(plan.get(1).priority()).isEqualTo(StopPriority.A_SURVEILLER);
    }

    @Test
    @DisplayName("à urgence égale, le point qui attend depuis le plus longtemps passe devant")
    void oldestWaitsGoFirstAmongEquals() {
        given(point(1, "Plein a l'instant", 95, hoursAgo(1)),
              point(2, "Plein depuis hier", 82, hoursAgo(20)));

        List<RouteStop> plan = service().planForCommune(COMMUNE);

        // Sans ce departage, le point a 95 % remesure en continu repasserait indefiniment devant
        // celui a 82 % qui attend depuis 20 h : famine, et outil abandonne.
        assertThat(plan).extracting(RouteStop::depotoirId).containsExactly(uuid(2), uuid(1));
        assertThat(plan).allSatisfy(s -> assertThat(s.priority()).isEqualTo(StopPriority.DEBORDEMENT));
    }

    @Test
    @DisplayName("un point au-dessus du seuil mais périmé bascule en état inconnu, pas en débordement")
    void staleAboveThresholdIsUnknownNotOverflow() {
        given(point(1, "Plein mais vieux", 95, hoursAgo(48)));

        // On ne peut pas affirmer qu'il deborde : la mesure ne prouve plus rien. La distinction est
        // honnete et evite d'annoncer une urgence qu'on ne sait pas reelle.
        assertThat(service().planForCommune(COMMUNE).get(0).priority()).isEqualTo(StopPriority.ETAT_INCONNU);
    }

    @Test
    @DisplayName("chaque arrêt porte sa raison — un ordre inexplicable n'est pas suivi")
    void everyStopExplainsItself() {
        given(point(1, "Plein", 95, hoursAgo(1)),
              point(2, "Vide", 5, hoursAgo(1)));

        assertThat(service().planForCommune(COMMUNE))
                .allSatisfy(s -> assertThat(s.reason()).isNotBlank());
    }

    @Test
    @DisplayName("une commune sans point de collecte rend une liste vide, pas une erreur")
    void emptyCommuneYieldsEmptyPlan() {
        given();

        assertThat(service().planForCommune(COMMUNE)).isEmpty();
    }
}
