package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sonaged.collecte.master.model.CircuitEntity;

@Repository
public interface CircuitRepository extends SoftDeleteRepository<CircuitEntity, Long> {
}
