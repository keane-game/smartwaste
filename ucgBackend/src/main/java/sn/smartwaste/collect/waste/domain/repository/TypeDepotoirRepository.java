package sn.smartwaste.collect.waste.domain.repository;

import sn.smartwaste.collect.shared.domain.repository.SoftDeleteRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.smartwaste.collect.waste.domain.model.TypeDepotoirEntity;

@Repository
public interface TypeDepotoirRepository extends SoftDeleteRepository<TypeDepotoirEntity, Long> {

    TypeDepotoirEntity findByNameIgnoreCase(String name);
}
