package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sonaged.collecte.master.model.CircuitCollectEntity;

@Repository
public interface CircuitCollectRepository extends SoftDeleteRepository<CircuitCollectEntity, Long> {
}
