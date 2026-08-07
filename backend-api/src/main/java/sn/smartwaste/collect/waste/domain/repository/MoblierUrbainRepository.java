package sn.smartwaste.collect.waste.domain.repository;

import sn.smartwaste.collect.shared.domain.repository.SoftDeleteRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.smartwaste.collect.waste.domain.model.MoblierUrbainEntity;

import java.util.UUID;

@Repository
public interface MoblierUrbainRepository extends SoftDeleteRepository<MoblierUrbainEntity, UUID> {
}
