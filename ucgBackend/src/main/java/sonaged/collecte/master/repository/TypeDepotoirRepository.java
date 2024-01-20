package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sonaged.collecte.master.model.TypeDepotoir;

@Repository
public interface TypeDepotoirRepository extends JpaRepository<TypeDepotoir, Long> {
}
