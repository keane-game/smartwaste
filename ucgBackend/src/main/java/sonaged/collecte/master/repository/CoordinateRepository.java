package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sonaged.collecte.master.model.Coordinate;

@Repository
public interface CoordinateRepository extends JpaRepository<Coordinate, Long> {
}
