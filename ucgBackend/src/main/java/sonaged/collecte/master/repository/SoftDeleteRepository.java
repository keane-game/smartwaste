package sonaged.collecte.master.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.jpa.repository.JpaRepository;
import sonaged.collecte.master.enums.DeletionStatus;
import sonaged.collecte.master.model.AbstractAuditingEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository de base pour les entités soft-deletables (toutes héritent d'{@link AbstractAuditingEntity}).
 *
 * <p>Fournit les requêtes dérivées communes au soft-delete. Les repositories concrets étendent
 * ce type au lieu de {@link JpaRepository} pour bénéficier — et être couverts par la purge
 * automatique ({@code DeletionPurgeScheduler}) et le {@code SoftDeleteService}.
 *
 * @param <E>  type d'entité (héritant d'{@link AbstractAuditingEntity})
 * @param <ID> type de la clé
 */
@NoRepositoryBean
public interface SoftDeleteRepository<E extends AbstractAuditingEntity<?>, ID>
        extends JpaRepository<E, ID> {

    Page<E> findByDeletionStatus(DeletionStatus status, Pageable pageable);

    List<E> findByDeletionStatus(DeletionStatus status);

    long countByDeletionStatus(DeletionStatus status);

    /** Éléments en attente de purge dont le délai de rétention est dépassé. */
    List<E> findByDeletionStatusAndDeletionRequestedAtBefore(DeletionStatus status, LocalDateTime cutoff);
}
