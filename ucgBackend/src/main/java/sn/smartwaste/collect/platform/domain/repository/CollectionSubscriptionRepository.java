package sn.smartwaste.collect.platform.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sn.smartwaste.collect.platform.domain.model.CollectionSubscription;

@Repository
public interface CollectionSubscriptionRepository extends JpaRepository<CollectionSubscription, UUID> {

    List<CollectionSubscription> findByQuartierIdAndActiveTrue(UUID quartierId);

    List<CollectionSubscription> findByUserIdAndActiveTrue(UUID userId);

    Optional<CollectionSubscription> findByUserIdAndQuartierId(UUID userId, UUID quartierId);
}
