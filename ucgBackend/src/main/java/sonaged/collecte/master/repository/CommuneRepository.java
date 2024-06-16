package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sonaged.collecte.master.model.CommuneEntity;

public interface CommuneRepository extends JpaRepository<CommuneEntity, Long> {
    CommuneEntity findByName(String name);
}

