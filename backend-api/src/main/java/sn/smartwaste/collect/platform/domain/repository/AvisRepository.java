package sn.smartwaste.collect.platform.domain.repository;

import sn.smartwaste.collect.platform.domain.model.Avis;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface AvisRepository extends JpaRepository<Avis, UUID> {

    java.util.List<Avis> findByStatutOrderByIdDesc(sn.smartwaste.collect.platform.domain.model.AvisStatus statut);

    java.util.List<Avis> findByUserIdOrderByIdDesc(java.util.UUID userId);

    /** Signalements encore ouverts, pour la carte de supervision. */
    java.util.List<Avis> findByStatutInAndLatitudeIsNotNull(
            java.util.Collection<sn.smartwaste.collect.platform.domain.model.AvisStatus> statuts);
}
