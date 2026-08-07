package sn.smartwaste.collect.platform.application.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import sn.smartwaste.collect.platform.domain.model.AwarenessCampaign;

/**
 * Campagnes de sensibilisation — regroupent messages et quiz sous un thème commun (G3, suite).
 */
public interface AwarenessCampaignService {

    /** @param endDate {@code null} = campagne ouverte, sans clôture prévue */
    AwarenessCampaign create(String name, String description, Instant startDate, Instant endDate);

    /** Campagnes en cours, la plus récente d'abord. */
    List<AwarenessCampaign> activeCampaigns();

    /** Clôt une campagne sans la supprimer : son historique (messages, quiz) reste lisible. */
    void close(UUID campaignId);
}
