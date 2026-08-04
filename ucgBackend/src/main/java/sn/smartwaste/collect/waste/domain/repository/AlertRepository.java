package sn.smartwaste.collect.waste.domain.repository;

import sn.smartwaste.collect.shared.domain.repository.SoftDeleteRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.smartwaste.collect.waste.domain.model.AlertEntity;

@Repository
public interface AlertRepository extends SoftDeleteRepository<AlertEntity, Long> {

    /**
     * Alertes <b>ouvertes</b> d'un point pour un objet donné — « ouverte » se lisant
     * {@code resolvedAt IS NULL} depuis le changelog 2.12.0.
     *
     * <p>Sert à refermer ce qui a cessé : un capteur qui réémet clôt son alerte de silence, un
     * point vidé clôt son alerte de débordement. Sans cela le tableau de bord accumulerait des
     * problèmes déjà résolus, jusqu'à ce qu'on cesse de le regarder.
     */
    java.util.List<AlertEntity> findByDepotoirIdAndObjectAndResolvedAtIsNull(Long depotoirId,
                                                                            String object);

    /** Toutes les alertes ouvertes d'un point, quel qu'en soit l'objet. */
    java.util.List<AlertEntity> findByDepotoirIdAndResolvedAtIsNull(Long depotoirId);

    /**
     * Alertes levées sur un ensemble de points pendant une période (G5).
     *
     * <p>Le filtre porte sur la <b>date de levée</b> et non sur la résolution : un rapport
     * mensuel doit compter ce qui s'est produit ce mois-là, y compris ce qui n'est pas encore
     * refermé — sinon les périodes difficiles paraîtraient les plus calmes.
     */
    java.util.List<AlertEntity> findByDepotoirIdInAndCreatedDateBetween(
            java.util.List<Long> depotoirIds,
            java.time.LocalDateTime from,
            java.time.LocalDateTime to);
}
