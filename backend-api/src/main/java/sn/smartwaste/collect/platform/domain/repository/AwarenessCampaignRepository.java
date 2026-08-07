package sn.smartwaste.collect.platform.domain.repository;

import java.util.List;
import java.util.UUID;

import sn.smartwaste.collect.platform.domain.model.AwarenessCampaign;
import sn.smartwaste.collect.shared.domain.repository.SoftDeleteRepository;

public interface AwarenessCampaignRepository extends SoftDeleteRepository<AwarenessCampaign, UUID> {

    List<AwarenessCampaign> findByActiveTrueOrderByStartDateDesc();
}
