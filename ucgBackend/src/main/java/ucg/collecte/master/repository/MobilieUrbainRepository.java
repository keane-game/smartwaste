package ucg.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ucg.collecte.master.model.MobilieUrbain;

@Repository
public interface MobilieUrbainRepository extends JpaRepository<MobilieUrbain, Long> {
}
