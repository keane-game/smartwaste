package sn.smartwaste.collect.identity.application.service.impl;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.identity.application.api.AgentDirectory;
import sn.smartwaste.collect.identity.domain.model.AgentAssignment;
import sn.smartwaste.collect.identity.domain.repository.AgentAssignmentRepository;
import sn.smartwaste.collect.shared.domain.model.DeletionStatus;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AgentDirectoryImpl implements AgentDirectory {

    private final AgentAssignmentRepository assignmentRepository;

    @Override
    public List<UUID> communesOf(UUID agentId) {
        return assignmentRepository.findByUserIdAndDeletionStatus(agentId, DeletionStatus.ACTIVE)
                .stream().map(AgentAssignment::getCommuneId).toList();
    }

    @Override
    public boolean covers(UUID agentId, UUID communeId) {
        return assignmentRepository.existsByUserIdAndCommuneIdAndDeletionStatus(
                agentId, communeId, DeletionStatus.ACTIVE);
    }
}
