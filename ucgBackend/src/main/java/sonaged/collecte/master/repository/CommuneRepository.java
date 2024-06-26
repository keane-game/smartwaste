package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sonaged.collecte.master.model.CommuneEntity;

import java.util.List;

public interface CommuneRepository extends JpaRepository<CommuneEntity, Long> {
    CommuneEntity findByNameIgnoreCase(String name);
    List<CommuneEntity> findByNameContainingIgnoreCase(String name);
}

