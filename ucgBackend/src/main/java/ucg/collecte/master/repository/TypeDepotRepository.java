package ucg.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ucg.collecte.master.model.TypeDepot;

@Repository
public interface TypeDepotRepository extends JpaRepository<TypeDepot, Long> {
}
