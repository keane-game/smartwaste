package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.smartwaste.collect.territory.domain.model.CommuneEntity;
import sonaged.collecte.master.model.DepotoirEntity;

import java.util.List;

@Repository
public interface DepotoirRepository extends SoftDeleteRepository<DepotoirEntity, Long> {

    List<DepotoirEntity> findByTypeDepotoir_NameContainingIgnoreCase(String name);
}
