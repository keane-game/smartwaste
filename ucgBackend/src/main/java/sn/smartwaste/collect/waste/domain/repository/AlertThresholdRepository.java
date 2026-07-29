package sn.smartwaste.collect.waste.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sn.smartwaste.collect.waste.domain.model.AlertThreshold;

@Repository
public interface AlertThresholdRepository extends JpaRepository<AlertThreshold, UUID> {

    Optional<AlertThreshold> findByTypeDepotoirIdAndActiveTrue(Long typeDepotoirId);

    /** Seuil par défaut : celui qui ne vise aucun type en particulier. */
    Optional<AlertThreshold> findByTypeDepotoirIdIsNullAndActiveTrue();

    List<AlertThreshold> findAllByOrderByTypeDepotoirIdAsc();
}
