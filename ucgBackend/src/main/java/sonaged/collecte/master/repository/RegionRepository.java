package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sonaged.collecte.master.model.RegionEntity;

@Repository
public interface RegionRepository extends JpaRepository<RegionEntity, Long> {
}
