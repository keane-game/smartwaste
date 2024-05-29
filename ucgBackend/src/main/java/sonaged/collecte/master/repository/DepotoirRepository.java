package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sonaged.collecte.master.model.DepotoirEntity;

@Repository
public interface DepotoirRepository extends JpaRepository<DepotoirEntity, Long> {
}
