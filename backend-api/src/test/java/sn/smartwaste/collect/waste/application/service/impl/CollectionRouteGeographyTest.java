package sn.smartwaste.collect.waste.application.service.impl;

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

import sn.smartwaste.collect.shared.domain.model.DeletionStatus;
import sn.smartwaste.collect.territory.domain.model.CoordinateEntity;
import sn.smartwaste.collect.territory.domain.model.GeometryEntity;
import sn.smartwaste.collect.waste.application.service.CollectionRouteService.RouteStop;
import sn.smartwaste.collect.waste.domain.model.DepotoirEntity;
import sn.smartwaste.collect.waste.domain.repository.DepotoirRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Ordre de passage <b>géographique</b> à l'intérieur d'un même niveau d'urgence (ADR-0017).
 *
 * <p><b>Ce qui manquait.</b> Le service triait sur l'urgence puis l'ancienneté de l'information, et
 * <i>rien d'autre</i> : aucune position n'entrait dans le calcul — les points n'en avaient d'ailleurs
 * aucune avant l'ADR-0016. Le résultat était une liste de priorités, pas une tournée : deux points
 * voisins pouvaient se retrouver aux deux extrémités du parcours, et le camion traverser la commune
 * en zigzag.
 *
 * <p><b>Ce qu'il ne faut surtout pas casser en l'ajoutant.</b> Le service protège deux garanties que
 * le plus proche voisin, appliqué naïvement, détruit :
 * <ul>
 *   <li>l'urgence prime — un débordement à l'autre bout de la commune passe avant un point tiède
 *       qu'on a sous la main ;</li>
 *   <li>pas de famine — un point isolé ne doit pas être repoussé indéfiniment parce qu'un autre est
 *       toujours plus près. Au-delà d'un âge d'information, il repasse devant.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class CollectionRouteGeographyTest {

    private static final UUID COMMUNE = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-07-30T12:00:00Z");
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
        return new CollectionRouteServiceImpl(depotoirRepository, passageRepository, accessGuard,
                Clock.fixed(NOW, ZoneId.of("UTC")), THRESHOLD, 24, 72);
    }

    @Test
    @DisplayName("à urgence égale, le point le plus proche est desservi d'abord")
    void chainsByProximityWithinAPriority() {
        // Trois debordements egalement recents : seule la geographie peut les departager.
        // Depuis le premier point, l'ordre naturel est 1 -> 3 (proche) -> 2 (loin).
        given(point(1L, "depart", 95, hoursAgo(1), 14.7400, -17.4200),
              point(2L, "loin", 95, hoursAgo(1), 14.7800, -17.3000),
              point(3L, "proche", 95, hoursAgo(1), 14.7410, -17.4190));

        var plan = service().planForCommune(COMMUNE);

        assertThat(plan).extracting(RouteStop::depotoirId).containsExactly(uuid(1), uuid(3), uuid(2));
    }

    @Test
    @DisplayName("l'urgence prime toujours sur la proximité")
    void priorityStillDominatesDistance() {
        // Le point tiede est a quelques metres, le debordement a l'autre bout de la commune.
        // Le camion va au debordement : une tournee optimisee qui laisse deborder n'a aucun sens.
        given(point(1L, "tiede tout pres", 50, hoursAgo(1), 14.7400, -17.4200),
              point(2L, "debordement loin", 95, hoursAgo(1), 14.7800, -17.3000));

        var plan = service().planForCommune(COMMUNE);

        assertThat(plan).extracting(RouteStop::depotoirId).containsExactly(uuid(2), uuid(1));
    }

    @Test
    @DisplayName("parmi les points à l'état inconnu, le plus délaissé repasse devant malgré la distance")
    void staleStopIsRescuedFromStarvation() {
        // C'est DANS cette tranche que la famine peut s'installer, et nulle part ailleurs : au-dela
        // de 24 h une mesure est perimee, donc un point longtemps ignore quitte de toute facon
        // DEBORDEMENT pour ETAT_INCONNU. Restent alors des points tous « inconnus », que seul le
        // plus proche voisin departagerait — et un point isole y serait repousse indefiniment,
        // puisqu'il y a toujours quelqu'un de plus pres. Au-dela de l'age maximal (72 h), il repasse
        // devant.
        given(point(1L, "proche, inconnu depuis 30 h", 40, hoursAgo(30), 14.7400, -17.4200),
              point(2L, "loin, inconnu depuis 100 h", 40, hoursAgo(100), 14.7800, -17.3000));

        var plan = service().planForCommune(COMMUNE);

        assertThat(plan).extracting(RouteStop::depotoirId).containsExactly(uuid(2), uuid(1));
    }

    @Test
    @DisplayName("des points jamais mesurés sont chaînés géographiquement, pas tous « rattrapés »")
    void neverMeasuredStopsAreStillChained() {
        // Defaut trouve en exploitant les vraies donnees de Mbao : les 24 points n'ayant jamais ete
        // mesures, leur anciennete valait « depuis toujours » — donc au-dela de l'age maximal — et
        // TOUS basculaient dans le rattrapage des delaisses. Le chainage geographique ne s'executait
        // jamais : la tournee sortait dans l'ordre du depot, pour 35,10 km, exactement comme sans
        // tri. Un point jamais mesure n'est pas un point delaisse : il est inconnu, ce que sa
        // priorite dit deja. Le rattrapage ne concerne que les points reellement mesures, il y a
        // longtemps.
        given(point(1L, "depart", null, null, 14.7400, -17.4200),
              point(2L, "loin", null, null, 14.7800, -17.3000),
              point(3L, "proche", null, null, 14.7410, -17.4190));

        var plan = service().planForCommune(COMMUNE);

        assertThat(plan).extracting(RouteStop::depotoirId).containsExactly(uuid(1), uuid(3), uuid(2));
    }

    @Test
    @DisplayName("un point sans position reste dans la tournée")
    void stopWithoutCoordinatesIsKept() {
        // Il ne peut pas etre chaine, mais l'exclure le rendrait invisible : c'est exactement le
        // genre de point qu'on finit par ne jamais collecter.
        given(point(1L, "avec position", 95, hoursAgo(1), 14.7400, -17.4200),
              sansPosition(2L, "sans position", 95, hoursAgo(1)));

        var plan = service().planForCommune(COMMUNE);

        assertThat(plan).extracting(RouteStop::depotoirId).containsExactlyInAnyOrder(uuid(1), uuid(2));
    }

    @Test
    @DisplayName("la position est exposée pour que le client puisse tracer le parcours")
    void exposesCoordinates() {
        given(point(1L, "quelque part", 95, hoursAgo(1), 14.7400, -17.4200));

        var stop = service().planForCommune(COMMUNE).getFirst();

        assertThat(stop.latitude()).isEqualTo(14.7400);
        assertThat(stop.longitude()).isEqualTo(-17.4200);
    }

    // ---------------------------------------------------------------- fixtures

    private static DepotoirEntity point(long id, String address, Integer fill,
                                        Instant measuredAt, double lat, double lon) {
        var d = sansPosition(id, address, fill, measuredAt);
        var coordinate = new CoordinateEntity();
        coordinate.setLatitude(String.valueOf(lat));
        coordinate.setLongitude(String.valueOf(lon));
        var geometry = new GeometryEntity();
        geometry.setCoordinates(List.of(coordinate));
        d.setGeometry(geometry);
        return d;
    }

    /** UUID stable dérivé d'un petit entier : les cas restent lisibles (`point(1L, ...)`), l'entité est en UUID. */
    private static UUID uuid(long n) {
        return UUID.fromString(String.format("00000000-0000-0000-0000-%012d", n));
    }

    private static DepotoirEntity sansPosition(long id, String address, Integer fill,
                                               Instant measuredAt) {
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
        return NOW.minusSeconds(h * 3600);
    }
}
