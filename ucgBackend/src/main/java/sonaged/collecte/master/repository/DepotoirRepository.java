package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sonaged.collecte.master.model.Depotoir;

@Repository
public interface DepotoirRepository extends JpaRepository<Depotoir, Long> {
}
