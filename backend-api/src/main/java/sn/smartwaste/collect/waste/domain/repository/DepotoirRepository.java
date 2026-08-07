package sn.smartwaste.collect.waste.domain.repository;

import sn.smartwaste.collect.shared.domain.repository.SoftDeleteRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.smartwaste.collect.territory.domain.model.CommuneEntity;
import sn.smartwaste.collect.waste.domain.model.DepotoirEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface DepotoirRepository extends SoftDeleteRepository<DepotoirEntity, UUID> {

    List<DepotoirEntity> findByTypeDepotoir_NameContainingIgnoreCase(String name);

    /** Points de collecte actifs d'une commune — perimetre d'une tournee. */
    java.util.List<DepotoirEntity> findByCommuneIdAndDeletionStatus(
            java.util.UUID communeId,
            sn.smartwaste.collect.shared.domain.model.DeletionStatus deletionStatus);
}
