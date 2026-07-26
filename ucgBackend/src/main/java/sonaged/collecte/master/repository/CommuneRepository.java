package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sonaged.collecte.master.model.CommuneEntity;

import java.util.List;

public interface CommuneRepository extends SoftDeleteRepository<CommuneEntity, Long> {
    CommuneEntity findByNameIgnoreCase(String name);
    List<CommuneEntity> findByNameContainingIgnoreCase(String name);


    @Query("SELECT SUM(CAST(c.total AS Long)) FROM CommuneEntity c")
    Long countTotalHabitants();
}

