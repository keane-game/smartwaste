package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sonaged.collecte.master.model.Region;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
}
