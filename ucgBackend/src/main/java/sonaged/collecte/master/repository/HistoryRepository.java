package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sonaged.collecte.master.model.History;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
}
