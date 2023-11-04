package ucg.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ucg.collecte.master.model.Circuit;

@Repository
public interface CircuitRepository extends JpaRepository<Circuit, Long> {
}
