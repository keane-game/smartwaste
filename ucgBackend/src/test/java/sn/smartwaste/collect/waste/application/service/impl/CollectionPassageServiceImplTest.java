package sn.smartwaste.collect.waste.application.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.waste.domain.model.AlertEntity;
import sn.smartwaste.collect.waste.domain.model.CollectionPassage;
import sn.smartwaste.collect.waste.domain.model.DepotoirEntity;
import sn.smartwaste.collect.waste.domain.model.PassageOutcome;
import sn.smartwaste.collect.waste.domain.repository.AlertRepository;
import sn.smartwaste.collect.waste.domain.repository.CollectionPassageRepository;
import sn.smartwaste.collect.waste.domain.repository.DepotoirRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Le passage d'un agent sur un point de collecte (G1 du backlog).
 *
 * <p><b>Le maillon qui manquait.</b> La boucle « détecter → alerter → collecter → constater »
 * s'arrêtait au troisième temps : rien ne permettait d'enregistrer qu'un point avait été vidé. Le
 * niveau ne retombait que si un capteur le disait — or 71 points sur 71 n'en ont pas. L'alerte
 * restait ouverte indéfiniment, la tournée reproposait le point, et aucun indicateur d'efficacité
 * n'était calculable.
 *
 * <p><b>Deux décisions se lisent dans ces cas, et méritent d'être explicites :</b>
 * <ul>
 *   <li><b>Un passage vaut information sur l'état</b>, au même titre qu'une mesure : il rafraîchit
 *       {@code lastMeasuredAt}. Sans cela, un point vidé le matin retomberait en
 *       {@code ETAT_INCONNU} au bout de 24 h et reviendrait en tête de tournée — les agents
 *       reverraient chaque jour les points qu'ils viennent de vider. Le capteur garde le dernier
 *       mot : une mesure postérieure écrase la déclaration, le projecteur ignorant déjà toute
 *       mesure antérieure au dernier état connu.</li>
 *   <li><b>Une alerte de maintenance n'est pas refermée par une collecte.</b> Vider un bac ne
 *       répare pas le capteur qui l'observe.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class CollectionPassageServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-07-30T12:00:00Z");
    private static final Long POINT = 73L;
    private static final UUID AGENT = UUID.randomUUID();

    @Mock private DepotoirRepository depotoirRepository;
    @Mock private AlertRepository alertRepository;
    @Mock private CollectionPassageRepository passageRepository;
    @Mock private CurrentUserProvider currentUserProvider;
    /** Ces cas portent sur le comportement metier ; l'autorisation est verifiee separement
     *  (CollectionPassageAuthorizationTest). */
    private final TerritorialAccessGuard accessGuard = new TerritorialAccessGuard(null, null) {
        @Override public void requireAccessTo(java.util.UUID communeId) { }
        @Override public boolean isAdministration() { return true; }
    };

    private CollectionPassageServiceImpl service() {
        return new CollectionPassageServiceImpl(depotoirRepository, alertRepository,
                passageRepository, currentUserProvider, accessGuard,
                Clock.fixed(NOW, ZoneId.of("UTC")));
    }

    private DepotoirEntity point(Integer fill) {
        var d = new DepotoirEntity();
        d.setDepotoirId(POINT);
        d.setAddress("Ecole dalifort");
        d.setFillLevelPercent(fill);
        d.setLastMeasuredAt(NOW.minusSeconds(7200));
        lenient().when(depotoirRepository.findById(POINT)).thenReturn(Optional.of(d));
        lenient().when(currentUserProvider.requireCurrentUserId()).thenReturn(AGENT);
        return d;
    }

    private AlertEntity openAlert(String object) {
        var a = new AlertEntity();
        a.setObject(object);
        a.setDepotoirId(POINT);
        return a;
    }

    @Test
    @DisplayName("un point collecté retombe à zéro et le passage est horodaté")
    void collectingEmptiesThePoint() {
        var depotoir = point(92);
        when(alertRepository.findByDepotoirIdAndResolvedAtIsNull(POINT)).thenReturn(List.of());

        service().markCollected(POINT);

        assertThat(depotoir.getFillLevelPercent()).isZero();
        assertThat(depotoir.getLastCollectedAt()).isEqualTo(NOW);
        verify(depotoirRepository).save(depotoir);
    }

    @Test
    @DisplayName("un passage rafraîchit l'information au même titre qu'une mesure")
    void aPassageRefreshesTheInformation() {
        // Sans cela, un point vide le matin retomberait en ETAT_INCONNU au bout de 24 h et
        // reviendrait en tete de tournee : les agents reverraient chaque jour ce qu'ils ont vide.
        var depotoir = point(92);
        when(alertRepository.findByDepotoirIdAndResolvedAtIsNull(POINT)).thenReturn(List.of());

        service().markCollected(POINT);

        assertThat(depotoir.getLastMeasuredAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("les alertes de collecte du point sont refermées")
    void collectingResolvesOverflowAlerts() {
        point(92);
        var debordement = openAlert("Point de collecte plein");
        when(alertRepository.findByDepotoirIdAndResolvedAtIsNull(POINT))
                .thenReturn(List.of(debordement));

        service().markCollected(POINT);

        assertThat(debordement.getResolvedAt()).isNotNull();
        verify(alertRepository).save(debordement);
    }

    @Test
    @DisplayName("une alerte de capteur muet survit à la collecte")
    void collectingDoesNotResolveMaintenanceAlerts() {
        // Vider un bac ne repare pas le capteur qui l'observe. Refermer cette alerte ferait
        // disparaitre un probleme non resolu, et le point resterait un angle mort silencieux.
        point(92);
        var muet = openAlert(SensorSilenceProjector.OBJET_SILENCE);
        when(alertRepository.findByDepotoirIdAndResolvedAtIsNull(POINT)).thenReturn(List.of(muet));

        service().markCollected(POINT);

        assertThat(muet.getResolvedAt()).isNull();
        verify(alertRepository, never()).save(muet);
    }

    @Test
    @DisplayName("un point inaccessible n'est pas vidé et garde son alerte")
    void inaccessibleKeepsEverything() {
        // Un obstacle n'est pas une collecte : confondre les deux ferait disparaitre de la tournee
        // du lendemain un point qu'on n'a justement pas pu desservir.
        var depotoir = point(92);

        service().markInaccessible(POINT, "voie barree");

        assertThat(depotoir.getFillLevelPercent()).isEqualTo(92);
        assertThat(depotoir.getLastCollectedAt()).isNull();
        verify(alertRepository, never()).save(any());
    }

    @Test
    @DisplayName("le passage enregistre son issue, son motif et son agent")
    void recordsThePassage() {
        point(92);

        service().markInaccessible(POINT, "voie barree");

        var saved = ArgumentCaptor.forClass(CollectionPassage.class);
        verify(passageRepository).save(saved.capture());
        assertThat(saved.getValue().getOutcome()).isEqualTo(PassageOutcome.INACCESSIBLE);
        assertThat(saved.getValue().getReason()).isEqualTo("voie barree");
        assertThat(saved.getValue().getAgentId()).isEqualTo(AGENT);
        assertThat(saved.getValue().getDepotoirId()).isEqualTo(POINT);
    }

    @Test
    @DisplayName("un point inconnu est refusé plutôt qu'ignoré")
    void unknownPointIsRejected() {
        when(depotoirRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().markCollected(999L))
                .isInstanceOf(RuntimeException.class);

        verify(passageRepository, never()).save(any());
    }
}
