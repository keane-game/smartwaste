package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sonaged.collecte.master.model.Avis;


public interface AvisRepository extends JpaRepository<Avis, Integer> {
}
