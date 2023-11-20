package ucg.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ucg.collecte.master.model.CircuitBalayage;

@Repository
public interface CircuitBalayageRepository extends JpaRepository<CircuitBalayage, Long> {
}
