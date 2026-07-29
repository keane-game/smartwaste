package sn.smartwaste.collect.platform.application.service;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.platform.domain.model.Avis;
import sn.smartwaste.collect.platform.domain.model.AvisStatus;
import sn.smartwaste.collect.platform.domain.repository.AvisRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Signalements citoyens : dépôt et cycle de vie.
 *
 * <p>Deux familles de garanties, pour deux risques distincts.
 *
 * <p><b>À la création</b> — l'entité JPA est liée directement au corps de la requête. Sans
 * neutralisation, un habitant pourrait déposer un signalement déjà clos, l'attribuer à un autre, ou
 * écraser celui d'un tiers en fournissant son identifiant (IDOR).
 *
 * <p><b>Aux transitions</b> — sans contrôle, un signalement clos pourrait être rouvert, ou passer
 * directement de « signalé » à « traité » sans qu'aucune intervention n'ait eu lieu. La file de
 * traitement ne voudrait alors plus rien dire.
 */
@ExtendWith(MockitoExtension.class)
class AvisServiceTest {

    private static final UUID AUTEUR = UUID.randomUUID();
    private static final UUID AGENT = UUID.randomUUID();

    @Mock
    private AvisRepository avisRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private AvisService avisService;

    private Avis captureSaved() {
        ArgumentCaptor<Avis> saved = ArgumentCaptor.forClass(Avis.class);
        verify(avisRepository).save(saved.capture());
        return saved.getValue();
    }

    private Avis existing(AvisStatus statut) {
        var avis = new Avis();
        avis.setId(12);
        avis.setStatut(statut);
        lenient().when(avisRepository.findById(12)).thenReturn(Optional.of(avis));
        lenient().when(avisRepository.save(any(Avis.class))).thenAnswer(i -> i.getArgument(0));
        return avis;
    }

    @Test
    @DisplayName("à la création, le serveur impose l'auteur, l'état initial et efface le traitement")
    void creationIsServerControlled() {
        when(currentUserProvider.requireCurrentUserId()).thenReturn(AUTEUR);

        var forged = new Avis();
        forged.setId(999);                                  // tentative d'écrasement (IDOR)
        forged.setStatut(AvisStatus.TRAITE);                // tentative d'auto-clôture
        forged.setUserId(UUID.randomUUID());                // tentative d'attribution à un tiers
        forged.setProcessedByUserId(UUID.randomUUID());
        forged.setMessage("Depot sauvage rue 10");
        forged.setLatitude("14.75");
        forged.setLongitude("-17.39");

        avisService.create(forged);

        Avis saved = captureSaved();
        assertThat(saved.getId()).isZero();                 // INSERT garanti
        assertThat(saved.getStatut()).isEqualTo(AvisStatus.SIGNALE);
        assertThat(saved.getUserId()).isEqualTo(AUTEUR);
        assertThat(saved.getProcessedByUserId()).isNull();
        assertThat(saved.getProcessedAt()).isNull();
        // Ce que l'habitant fournit légitimement est conservé.
        assertThat(saved.getMessage()).isEqualTo("Depot sauvage rue 10");
        assertThat(saved.getLatitude()).isEqualTo("14.75");
    }

    @Test
    @DisplayName("prise en charge puis clôture : l'agent et la date sont tracés")
    void closingRecordsWhoAndWhen() {
        var avis = existing(AvisStatus.SIGNALE);
        when(currentUserProvider.requireCurrentUserId()).thenReturn(AGENT);

        avisService.changeStatus(12, AvisStatus.EN_COURS);
        assertThat(avis.getStatut()).isEqualTo(AvisStatus.EN_COURS);
        // La prise en charge n'est pas une clôture : rien n'est encore tracé.
        assertThat(avis.getProcessedAt()).isNull();

        avisService.changeStatus(12, AvisStatus.TRAITE);
        assertThat(avis.getStatut()).isEqualTo(AvisStatus.TRAITE);
        assertThat(avis.getProcessedByUserId()).isEqualTo(AGENT);
        assertThat(avis.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("sauter la prise en charge est refusé (409)")
    void skippingInProgressIsRejected() {
        existing(AvisStatus.SIGNALE);

        assertThatThrownBy(() -> avisService.changeStatus(12, AvisStatus.TRAITE))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("interdite");

        verify(avisRepository, never()).save(any());
    }

    @Test
    @DisplayName("un signalement clos ne peut pas être rouvert")
    void terminalStatusCannotBeReopened() {
        existing(AvisStatus.TRAITE);

        assertThatThrownBy(() -> avisService.changeStatus(12, AvisStatus.EN_COURS))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> avisService.changeStatus(12, AvisStatus.SIGNALE))
                .isInstanceOf(ResponseStatusException.class);

        verify(avisRepository, never()).save(any());
    }

    @Test
    @DisplayName("rejeter reste possible après prise en charge — on le découvre souvent sur place")
    void rejectingAfterVisitIsAllowed() {
        var avis = existing(AvisStatus.EN_COURS);
        when(currentUserProvider.requireCurrentUserId()).thenReturn(AGENT);

        avisService.changeStatus(12, AvisStatus.REJETE);

        assertThat(avis.getStatut()).isEqualTo(AvisStatus.REJETE);
        assertThat(avis.getProcessedByUserId()).isEqualTo(AGENT);
    }

    @Test
    @DisplayName("reclasser dans le même état est idempotent, sans réécrire la date de clôture")
    void sameStatusIsIdempotent() {
        var avis = existing(AvisStatus.TRAITE);
        var closedAt = java.time.Instant.parse("2026-07-01T10:00:00Z");
        avis.setProcessedAt(closedAt);

        avisService.changeStatus(12, AvisStatus.TRAITE);

        assertThat(avis.getProcessedAt()).isEqualTo(closedAt);
        verify(avisRepository, never()).save(any());
    }
}
