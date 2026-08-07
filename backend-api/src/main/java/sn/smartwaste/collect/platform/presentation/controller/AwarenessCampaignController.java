package sn.smartwaste.collect.platform.presentation.controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import sn.smartwaste.collect.platform.application.service.AwarenessCampaignService;
import sn.smartwaste.collect.platform.domain.model.AwarenessCampaign;

/**
 * Campagnes de sensibilisation (`/v1/awareness/campaigns`, G3 suite).
 *
 * <p>Rédiger reste réservé à l'administration, comme pour un message isolé. Lire la liste des
 * campagnes en cours est ouvert à tout compte authentifié : c'est le même contenu, à la même
 * échelle de sensibilité, qu'un message de sensibilisation reçu.
 */
@RestController
@RequestMapping("/v1/awareness/campaigns")
public class AwarenessCampaignController {

    private final AwarenessCampaignService campaignService;

    public AwarenessCampaignController(AwarenessCampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @Operation(summary = "Creer une campagne de sensibilisation",
               description = "Regroupe des messages et des quiz sous un theme commun. Sans date de "
                       + "cloture, la campagne reste ouverte.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SEND_AWARENESS')")
    public CampagneCreee create(@RequestBody CampagneRequest body) {
        var campaign = campaignService.create(body.name(), body.description(),
                body.startDate(), body.endDate());
        return new CampagneCreee(campaign.getCampaignId(), campaign.getStartDate());
    }

    @Operation(summary = "Campagnes en cours")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<CampagneVue> active() {
        return campaignService.activeCampaigns().stream()
                .map(c -> new CampagneVue(c.getCampaignId(), c.getName(), c.getDescription(),
                        c.getStartDate(), c.getEndDate()))
                .toList();
    }

    @Operation(summary = "Cloturer une campagne",
               description = "Ne supprime rien : l'historique des messages et quiz rattaches reste lisible.")
    @PostMapping("/{campaignId}/close")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('SEND_AWARENESS')")
    public void close(@PathVariable("campaignId") UUID campaignId) {
        campaignService.close(campaignId);
    }

    public record CampagneRequest(String name, String description, Instant startDate, Instant endDate) { }

    public record CampagneCreee(UUID campaignId, Instant startDate) { }

    public record CampagneVue(UUID campaignId, String name, String description,
                              Instant startDate, Instant endDate) { }
}
