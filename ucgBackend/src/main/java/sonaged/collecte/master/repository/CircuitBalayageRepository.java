package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sonaged.collecte.master.model.CircuitBalayageEntity;

@Repository
public interface CircuitBalayageRepository extends JpaRepository<CircuitBalayageEntity, Long> {
}
