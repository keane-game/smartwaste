package sn.smartwaste.collect.platform.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import sn.smartwaste.collect.platform.domain.model.DeviceToken;
import sn.smartwaste.collect.shared.domain.model.DeletionStatus;
import sn.smartwaste.collect.shared.domain.repository.SoftDeleteRepository;

public interface DeviceTokenRepository extends SoftDeleteRepository<DeviceToken, UUID> {

    Optional<DeviceToken> findByToken(String token);

    List<DeviceToken> findByUserIdInAndDeletionStatus(List<UUID> userIds, DeletionStatus status);

    List<DeviceToken> findByUserIdAndDeletionStatus(UUID userId, DeletionStatus status);
}
