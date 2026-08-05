package sn.smartwaste.collect.territory.domain.repository;

import java.util.UUID;

// Le socle soft-delete reste dans le code hérité : import explicite le temps de la transition.
import sn.smartwaste.collect.shared.domain.repository.SoftDeleteRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sn.smartwaste.collect.territory.domain.model.CommuneEntity;

import java.util.List;

public interface CommuneRepository extends SoftDeleteRepository<CommuneEntity, UUID> {
    CommuneEntity findByNameIgnoreCase(String name);
    List<CommuneEntity> findByNameContainingIgnoreCase(String name);


    @Query("SELECT SUM(CAST(c.total AS long)) FROM CommuneEntity c")
    Long countTotalHabitants();
}

