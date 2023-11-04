package ucg.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ucg.collecte.master.model.Quartier;

public interface QuartierRepository extends JpaRepository<Quartier, Long> {
}
