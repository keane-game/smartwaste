package sn.smartwaste.collect.territory.domain.repository;

import java.util.UUID;

// Le socle soft-delete reste dans le code hérité : import explicite le temps de la transition.
import sonaged.collecte.master.repository.SoftDeleteRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.smartwaste.collect.territory.domain.model.QuartierEntity;

public interface QuartierRepository extends SoftDeleteRepository<QuartierEntity, UUID> {
}
