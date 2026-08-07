package sn.smartwaste.collect.platform.application.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.platform.application.service.AwarenessCampaignService;
import sn.smartwaste.collect.platform.domain.model.AwarenessCampaign;
import sn.smartwaste.collect.platform.domain.repository.AwarenessCampaignRepository;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;

@Service
public class AwarenessCampaignServiceImpl implements AwarenessCampaignService {

    private final AwarenessCampaignRepository campaignRepository;
    private final CurrentUserProvider currentUserProvider;
    private final Clock clock;

    public AwarenessCampaignServiceImpl(AwarenessCampaignRepository campaignRepository,
                                        CurrentUserProvider currentUserProvider,
                                        Clock clock) {
        this.campaignRepository = campaignRepository;
        this.currentUserProvider = currentUserProvider;
        this.clock = clock;
    }

    @Override
    @Transactional
    public AwarenessCampaign create(String name, String description, Instant startDate, Instant endDate) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nom de la campagne est obligatoire");
        }
        if (endDate != null && startDate != null && endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La date de cloture ne peut pas preceder la date de debut");
        }
        var campaign = new AwarenessCampaign();
        campaign.setName(name);
        campaign.setDescription(description);
        campaign.setStartDate(startDate == null ? Instant.now(clock) : startDate);
        campaign.setEndDate(endDate);
        campaign.setAuthorId(currentUserProvider.requireCurrentUserId());
        return campaignRepository.save(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AwarenessCampaign> activeCampaigns() {
        return campaignRepository.findByActiveTrueOrderByStartDateDesc();
    }

    @Override
    @Transactional
    public void close(UUID campaignId) {
        var campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Campagne [%s] introuvable".formatted(campaignId)));
        campaign.setActive(false);
        campaignRepository.save(campaign);
    }
}
