package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sonaged.collecte.master.model.GeometryEntity;

@Repository
public interface GeometryRepository extends JpaRepository<GeometryEntity, Long> {
}
