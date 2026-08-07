package sn.smartwaste.collect.waste.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import sn.smartwaste.collect.shared.domain.repository.SoftDeleteRepository;
import sn.smartwaste.collect.waste.domain.model.CollectionPassage;

public interface CollectionPassageRepository extends SoftDeleteRepository<CollectionPassage, UUID> {

    List<CollectionPassage> findByDepotoirIdInAndOccurredAtBetween(List<UUID> depotoirIds,
                                                                   Instant from, Instant to);

    List<CollectionPassage> findByDepotoirIdOrderByOccurredAtDesc(UUID depotoirId);
}
