package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sonaged.collecte.master.model.Quartier;

public interface QuartierRepository extends JpaRepository<Quartier, Long> {
}
