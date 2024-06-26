package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sonaged.collecte.master.model.CoordinateEntity;
import sonaged.collecte.master.model.GeometryEntity;

import java.util.List;

@Repository
public interface GeometryRepository extends JpaRepository<GeometryEntity, Long> {

    //List<CoordinateEntity> findByCoordinates(Long geometryId);
}
