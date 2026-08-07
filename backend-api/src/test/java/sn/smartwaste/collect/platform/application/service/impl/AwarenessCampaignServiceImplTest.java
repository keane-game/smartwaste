package sn.smartwaste.collect.platform.application.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.platform.domain.model.AwarenessCampaign;
import sn.smartwaste.collect.platform.domain.repository.AwarenessCampaignRepository;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Campagnes de sensibilisation (G3, suite).
 *
 * <p>Une campagne ne diffuse rien par elle-même — elle nomme un groupe. Ce que ces cas protègent :
 * un nom obligatoire (sans quoi la liste ne se lirait pas), une clôture qui ne précède jamais le
 * début, et une clôture qui désactive sans supprimer.
 */
@ExtendWith(MockitoExtension.class)
class AwarenessCampaignServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-08-07T10:00:00Z");
    private static final UUID AUTEUR = UUID.randomUUID();

    @Mock private AwarenessCampaignRepository campaignRepository;
    @Mock private CurrentUserProvider currentUserProvider;

    private AwarenessCampaignServiceImpl service() {
        return new AwarenessCampaignServiceImpl(campaignRepository, currentUserProvider,
                Clock.fixed(NOW, ZoneId.of("UTC")));
    }

    @Test
    @DisplayName("une campagne sans nom est refusee")
    void nameIsRequired() {
        assertThatThrownBy(() -> service().create("", "desc", NOW, null))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> service().create(null, "desc", NOW, null))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    @DisplayName("une cloture avant le debut est refusee")
    void endDateCannotPrecedeStartDate() {
        assertThatThrownBy(() -> service().create("Aout proprete", "desc",
                NOW.plusSeconds(3600), NOW))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    @DisplayName("une campagne valide est enregistree, active, avec son auteur")
    void validCampaignIsCreatedActive() {
        when(currentUserProvider.requireCurrentUserId()).thenReturn(AUTEUR);
        when(campaignRepository.save(any(AwarenessCampaign.class))).thenAnswer(i -> i.getArgument(0));

        var campaign = service().create("Aout proprete", "Tri des dechets", NOW, null);

        assertThat(campaign.isActive()).isTrue();
        assertThat(campaign.getAuthorId()).isEqualTo(AUTEUR);
        assertThat(campaign.getStartDate()).isEqualTo(NOW);
        assertThat(campaign.getEndDate()).isNull();
    }

    @Test
    @DisplayName("sans date de debut, la campagne commence maintenant")
    void missingStartDateDefaultsToNow() {
        when(currentUserProvider.requireCurrentUserId()).thenReturn(AUTEUR);
        when(campaignRepository.save(any(AwarenessCampaign.class))).thenAnswer(i -> i.getArgument(0));

        var campaign = service().create("Aout proprete", "desc", null, null);

        assertThat(campaign.getStartDate()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("cloturer desactive sans supprimer")
    void closeDeactivatesWithoutDeleting() {
        var campaignId = UUID.randomUUID();
        var campaign = new AwarenessCampaign();
        campaign.setCampaignId(campaignId);
        campaign.setActive(true);
        when(campaignRepository.findById(campaignId)).thenReturn(java.util.Optional.of(campaign));

        service().close(campaignId);

        assertThat(campaign.isActive()).isFalse();
        var saved = ArgumentCaptor.forClass(AwarenessCampaign.class);
        verify(campaignRepository).save(saved.capture());
        assertThat(saved.getValue()).isSameAs(campaign);
    }

    @Test
    @DisplayName("cloturer une campagne inconnue leve ResourceNotFoundException")
    void closingUnknownCampaignThrows() {
        var campaignId = UUID.randomUUID();
        when(campaignRepository.findById(campaignId)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service().close(campaignId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("la liste ne rend que les campagnes actives")
    void activeCampaignsDelegatesToRepository() {
        var active = new AwarenessCampaign();
        when(campaignRepository.findByActiveTrueOrderByStartDateDesc()).thenReturn(List.of(active));

        assertThat(service().activeCampaigns()).containsExactly(active);
    }
}
