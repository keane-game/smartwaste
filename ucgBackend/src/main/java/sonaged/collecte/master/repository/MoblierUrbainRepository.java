package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sonaged.collecte.master.model.MoblierUrbain;

@Repository
public interface MoblierUrbainRepository extends JpaRepository<MoblierUrbain, Long> {
}
