package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sonaged.collecte.master.model.MoblierUrbainEntity;

@Repository
public interface MoblierUrbainRepository extends SoftDeleteRepository<MoblierUrbainEntity, Long> {
}
