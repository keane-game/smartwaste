package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sonaged.collecte.master.model.Alert;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
}
