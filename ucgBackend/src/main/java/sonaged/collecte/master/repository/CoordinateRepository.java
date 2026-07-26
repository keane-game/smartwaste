package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sonaged.collecte.master.model.CoordinateEntity;

@Repository
public interface CoordinateRepository extends SoftDeleteRepository<CoordinateEntity, Long> {
}
