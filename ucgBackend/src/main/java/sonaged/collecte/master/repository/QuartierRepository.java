package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sonaged.collecte.master.model.QuartierEntity;

public interface QuartierRepository extends JpaRepository<QuartierEntity, Long> {
}
