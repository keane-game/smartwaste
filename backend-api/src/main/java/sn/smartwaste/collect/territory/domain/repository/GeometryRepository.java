package sn.smartwaste.collect.territory.domain.repository;

import java.util.UUID;

// Le socle soft-delete reste dans le code hérité : import explicite le temps de la transition.
import sn.smartwaste.collect.shared.domain.repository.SoftDeleteRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.smartwaste.collect.territory.domain.model.CoordinateEntity;
import sn.smartwaste.collect.territory.domain.model.GeometryEntity;

import java.util.List;

@Repository
public interface GeometryRepository extends SoftDeleteRepository<GeometryEntity, UUID> {

    //List<CoordinateEntity> findByCoordinates(UUID geometryId);
}
