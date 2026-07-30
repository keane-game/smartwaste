package sn.smartwaste.collect.identity.domain.repository;

import java.util.List;
import java.util.UUID;

import sn.smartwaste.collect.identity.domain.model.AgentAssignment;
import sn.smartwaste.collect.shared.domain.model.DeletionStatus;
import sn.smartwaste.collect.shared.domain.repository.SoftDeleteRepository;

public interface AgentAssignmentRepository extends SoftDeleteRepository<AgentAssignment, UUID> {

    List<AgentAssignment> findByUserIdAndDeletionStatus(UUID userId, DeletionStatus status);

    boolean existsByUserIdAndCommuneIdAndDeletionStatus(UUID userId, UUID communeId,
                                                        DeletionStatus status);
}
