package sn.smartwaste.collect.waste.domain.repository;

import sn.smartwaste.collect.shared.domain.repository.SoftDeleteRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.smartwaste.collect.waste.domain.model.CircuitBalayageEntity;

import java.util.UUID;

@Repository
public interface CircuitBalayageRepository extends SoftDeleteRepository<CircuitBalayageEntity, UUID> {
}
