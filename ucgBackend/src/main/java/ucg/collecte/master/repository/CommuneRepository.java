package ucg.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ucg.collecte.master.model.Commune;

public interface CommuneRepository extends JpaRepository<Commune, Long> {
}
