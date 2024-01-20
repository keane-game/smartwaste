package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sonaged.collecte.master.model.CircuitCollect;

@Repository
public interface CircuitCollectRepository extends JpaRepository<CircuitCollect, Long> {
}
