package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sonaged.collecte.master.model.Commune;

public interface CommuneRepository extends JpaRepository<Commune, Long> {
}
