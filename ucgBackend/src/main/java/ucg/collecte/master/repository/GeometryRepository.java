package ucg.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ucg.collecte.master.model.Geometry;

@Repository
public interface GeometryRepository extends JpaRepository<Geometry, Long> {
}
