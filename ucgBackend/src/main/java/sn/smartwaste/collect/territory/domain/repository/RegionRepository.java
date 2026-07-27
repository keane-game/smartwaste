package sn.smartwaste.collect.territory.domain.repository;

import java.util.UUID;

// Le socle soft-delete reste dans le code hérité : import explicite le temps de la transition.
import sonaged.collecte.master.repository.SoftDeleteRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.smartwaste.collect.territory.domain.model.RegionEntity;

@Repository
public interface RegionRepository extends SoftDeleteRepository<RegionEntity, UUID> {
}
