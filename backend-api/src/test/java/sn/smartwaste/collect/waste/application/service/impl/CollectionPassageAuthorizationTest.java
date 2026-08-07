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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import sn.smartwaste.collect.identity.application.api.AgentDirectory;
import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.waste.domain.model.DepotoirEntity;
import sn.smartwaste.collect.waste.domain.repository.AlertRepository;
import sn.smartwaste.collect.waste.domain.repository.CollectionPassageRepository;
import sn.smartwaste.collect.waste.domain.repository.DepotoirRepository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Un agent ne peut déclarer un passage que sur son territoire.
 *
 * <p><b>Le défaut fermé ici, et il était de mon fait.</b> L'affectation territoriale
 * ({@code AgentAssignment}, {@code AgentDirectory}) a été créée avec le lot 2 précisément pour
 * borner un agent à ses communes — puis <b>jamais appliquée</b> : les endpoints se contentaient de
 * {@code hasAnyRole('AGENT','ADMIN','SUPER_ADMIN')}, si bien que n'importe quel agent pouvait
 * déclarer collecté n'importe lequel des 71 points, dans n'importe quelle commune. Une remise à
 * zéro du niveau ferme l'alerte et sort le point de la tournée : de quoi faire disparaître un
 * débordement réel depuis un compte sans rapport avec le terrain concerné.
 *
 * <p>C'est exactement le travers que ce dépôt connaît déjà — treize règles d'autorisation déclarées
 * que rien n'applique. Une règle qui n'est pas appliquée ne proteste pas : seul un test le fait.
 */
@ExtendWith(MockitoExtension.class)
class CollectionPassageAuthorizationTest {

    private static final Instant NOW = Instant.parse("2026-07-30T12:00:00Z");
    private static final UUID POINT = UUID.fromString("00000000-0000-0000-0000-000000000073");
    private static final UUID AGENT = UUID.randomUUID();
    private static final UUID COMMUNE = UUID.randomUUID();

    @Mock private DepotoirRepository depotoirRepository;
    @Mock private AlertRepository alertRepository;
    @Mock private CollectionPassageRepository passageRepository;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private AgentDirectory agentDirectory;

    private CollectionPassageServiceImpl service() {
        return new CollectionPassageServiceImpl(depotoirRepository, alertRepository,
                passageRepository, currentUserProvider,
                org.mockito.Mockito.mock(sn.smartwaste.collect.identity.application.api.UserDirectory.class),
                new TerritorialAccessGuard(currentUserProvider, agentDirectory),
                Clock.fixed(NOW, ZoneId.of("UTC")));
    }

    private DepotoirEntity point(UUID communeId) {
        var d = new DepotoirEntity();
        d.setDepotoirId(POINT);
        d.setFillLevelPercent(92);
        d.setCommuneId(communeId);
        lenient().when(depotoirRepository.findById(POINT)).thenReturn(Optional.of(d));
        lenient().when(currentUserProvider.requireCurrentUserId()).thenReturn(AGENT);
        lenient().when(alertRepository.findByDepotoirIdAndResolvedAtIsNull(POINT))
                .thenReturn(List.of());
        return d;
    }

    @Test
    @DisplayName("un agent hors de son territoire est refusé")
    void agentOutsideItsTerritoryIsRefused() {
        var depotoir = point(COMMUNE);
        when(currentUserProvider.currentUserHasAnyRole("ADMIN", "SUPER_ADMIN")).thenReturn(false);
        when(agentDirectory.covers(AGENT, COMMUNE)).thenReturn(false);

        assertThatThrownBy(() -> service().markCollected(POINT))
                .isInstanceOf(AccessDeniedException.class);

        verify(passageRepository, never()).save(any());
        // Et surtout : le niveau n'a pas bouge. Une remise a zero non autorisee effacerait un
        // debordement reel et sortirait le point de la tournee.
        org.assertj.core.api.Assertions.assertThat(depotoir.getFillLevelPercent()).isEqualTo(92);
    }

    @Test
    @DisplayName("un agent sur son territoire est accepté")
    void agentOnItsTerritoryIsAccepted() {
        point(COMMUNE);
        when(currentUserProvider.currentUserHasAnyRole("ADMIN", "SUPER_ADMIN")).thenReturn(false);
        when(agentDirectory.covers(AGENT, COMMUNE)).thenReturn(true);

        service().markCollected(POINT);

        verify(passageRepository).save(any());
    }

    @Test
    @DisplayName("l'administration n'est pas bornee par les affectations")
    void administrationIsNotTerritoriallyBound() {
        // L'administration supervise les 12 communes ; lui imposer une affectation la bloquerait
        // sur son propre outil.
        point(COMMUNE);
        when(currentUserProvider.currentUserHasAnyRole("ADMIN", "SUPER_ADMIN")).thenReturn(true);

        service().markCollected(POINT);

        verify(passageRepository).save(any());
        verify(agentDirectory, never()).covers(any(), any());
    }

    @Test
    @DisplayName("un point sans commune n'est declarable que par l'administration")
    void pointWithoutCommuneIsAdminOnly() {
        // 15 des 71 points importes n'ont pas de commune (libelles source divergents). Aucun agent
        // ne peut « couvrir » un territoire inexistant : les laisser ouverts a tous rouvrirait le
        // trou par la porte de derriere.
        point(null);
        when(currentUserProvider.currentUserHasAnyRole("ADMIN", "SUPER_ADMIN")).thenReturn(false);

        assertThatThrownBy(() -> service().markCollected(POINT))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("la meme regle s'applique a un point declare inaccessible")
    void inaccessibleIsGuardedToo() {
        // Sans cette symetrie, un agent non habilite pourrait polluer la trace d'exploitation d'un
        // territoire qui ne le concerne pas.
        point(COMMUNE);
        when(currentUserProvider.currentUserHasAnyRole("ADMIN", "SUPER_ADMIN")).thenReturn(false);
        when(agentDirectory.covers(AGENT, COMMUNE)).thenReturn(false);

        assertThatThrownBy(() -> service().markInaccessible(POINT, "voie barree"))
                .isInstanceOf(AccessDeniedException.class);

        verify(passageRepository, never()).save(any());
    }
}
