package sn.smartwaste.collect.platform.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import sn.smartwaste.collect.platform.domain.model.AwarenessMessage;
import sn.smartwaste.collect.shared.domain.repository.SoftDeleteRepository;

public interface AwarenessMessageRepository extends SoftDeleteRepository<AwarenessMessage, UUID> {

    /** Messages dont l'heure est venue et qui ne sont pas encore partis. */
    List<AwarenessMessage> findBySentAtIsNullAndScheduledAtLessThanEqual(Instant now);

    /** Messages déjà diffusés vers ce quartier ou vers tout le monde, du plus récent au plus ancien. */
    List<AwarenessMessage> findBySentAtIsNotNullAndQuartierIdInOrderBySentAtDesc(List<UUID> quartierIds);

    /** Dernier message programmé pour ce quartier — sert au plafond de fréquence. */
    List<AwarenessMessage> findByQuartierIdAndScheduledAtGreaterThanEqual(UUID quartierId, Instant since);
}
