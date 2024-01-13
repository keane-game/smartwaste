package ucg.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ucg.collecte.master.model.Departement;

@Repository
public interface DepartementRepository extends JpaRepository<Departement, Long> {
}
