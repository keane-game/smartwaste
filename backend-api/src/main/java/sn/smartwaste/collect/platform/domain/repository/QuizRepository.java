package sn.smartwaste.collect.platform.domain.repository;

import java.util.List;
import java.util.UUID;

import sn.smartwaste.collect.platform.domain.model.Quiz;
import sn.smartwaste.collect.shared.domain.repository.SoftDeleteRepository;

public interface QuizRepository extends SoftDeleteRepository<Quiz, UUID> {

    List<Quiz> findByActiveTrueOrderByCreatedDateDesc();

    List<Quiz> findByCampaignIdOrderByCreatedDateDesc(UUID campaignId);
}
