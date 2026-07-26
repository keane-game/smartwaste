package sonaged.collecte.master.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sonaged.collecte.master.enums.DeletionStatus;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.model.AbstractAuditingEntity;
import sonaged.collecte.master.repository.SoftDeleteRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service transverse de suppression logique (soft-delete) — réutilisable par toutes les entités.
 *
 * <p>Suppression = passage en {@link DeletionStatus#PENDING_DELETION} + horodatage. Purge
 * définitive après la période de rétention ({@code sonaged.deletion.retention-days}, 30 j par
 * défaut). Restauration possible tant que le délai n'est pas dépassé.
 */
@Service
@Slf4j
public class SoftDeleteService {

    private final long retentionDays;

    public SoftDeleteService(@Value("${sonaged.deletion.retention-days:30}") long retentionDays) {
        this.retentionDays = retentionDays;
    }

    public long getRetentionDays() {
        return retentionDays;
    }

    /** Date de purge définitive prévue (ou {@code null} si la ressource n'est pas supprimée). */
    public LocalDateTime purgeDueAt(AbstractAuditingEntity<?> entity) {
        return entity.getDeletionRequestedAt() == null
                ? null
                : entity.getDeletionRequestedAt().plusDays(retentionDays);
    }

    /** Suppression logique d'une ressource. */
    @Transactional
    public <E extends AbstractAuditingEntity<?>, ID> E softDelete(SoftDeleteRepository<E, ID> repository, ID id) {
        E entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ressource [%s] introuvable".formatted(id)));
        if (!entity.isPendingDeletion()) {
            entity.markForDeletion(LocalDateTime.now());
            entity = repository.save(entity);
            log.info("Soft-delete : {} (purge prévue le {})", id, purgeDueAt(entity));
        }
        return entity;
    }

    /** Restauration d'une ressource en attente de suppression, si le délai n'est pas dépassé. */
    @Transactional
    public <E extends AbstractAuditingEntity<?>, ID> E restore(SoftDeleteRepository<E, ID> repository, ID id) {
        E entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ressource [%s] introuvable".formatted(id)));
        if (!entity.isPendingDeletion()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La ressource n'est pas en attente de suppression.");
        }
        LocalDateTime due = purgeDueAt(entity);
        if (due != null && due.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Délai de restauration dépassé : la ressource est bonne pour la purge.");
        }
        entity.restore();
        log.info("Restore : {}", id);
        return repository.save(entity);
    }

    // ---- Variantes « génériques » (repository non typé) pour l'exposition transverse ----
    // Utilisées par DeletionController, qui résout un repository par nom de ressource. Le cast
    // non vérifié est confiné à `erase(...)` : à l'exécution l'id est un Long et l'entité
    // correspond bien au repository.

    /**
     * Réinterprète un repository de type inconnu en repository d'entités auditées.
     *
     * <p>Volontairement paramétré plutôt que <em>brut</em> : un type brut efface aussi les
     * génériques des méthodes héritées, si bien que {@code Optional.orElseThrow(Supplier)}
     * retombe sur sa signature effacée {@code throws Throwable} et ne compile plus.
     * Le cast lui-même s'efface en simple cast vers {@code SoftDeleteRepository} (sûr à l'exécution).
     */
    @SuppressWarnings("unchecked")
    private static SoftDeleteRepository<AbstractAuditingEntity<?>, Object> erase(
            SoftDeleteRepository<?, ?> repository) {
        return (SoftDeleteRepository<AbstractAuditingEntity<?>, Object>) repository;
    }

    /** Liste des entités en attente de suppression pour un repository quelconque. */
    public List<? extends AbstractAuditingEntity<?>> pendingDeletion(SoftDeleteRepository<?, ?> repository) {
        return erase(repository).findByDeletionStatus(DeletionStatus.PENDING_DELETION);
    }

    /** Restaure une entité (repository quelconque) par identifiant. */
    @Transactional
    public AbstractAuditingEntity<?> restoreById(SoftDeleteRepository<?, ?> repository, Object id) {
        SoftDeleteRepository<AbstractAuditingEntity<?>, Object> raw = erase(repository);
        AbstractAuditingEntity<?> entity = raw.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ressource [%s] introuvable".formatted(id)));
        if (!entity.isPendingDeletion()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La ressource n'est pas en attente de suppression.");
        }
        LocalDateTime due = purgeDueAt(entity);
        if (due != null && due.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Délai de restauration dépassé : la ressource est bonne pour la purge.");
        }
        entity.restore();
        return raw.save(entity);
    }

    /** Purge définitive des ressources dont le délai de rétention est dépassé. */
    @Transactional
    public <E extends AbstractAuditingEntity<?>, ID> int purgeExpired(SoftDeleteRepository<E, ID> repository) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        List<E> expired = repository.findByDeletionStatusAndDeletionRequestedAtBefore(
                DeletionStatus.PENDING_DELETION, cutoff);
        if (!expired.isEmpty()) {
            repository.deleteAll(expired);
            log.info("Purge définitive : {} élément(s) de {}", expired.size(),
                    repository.getClass().getSimpleName());
        }
        return expired.size();
    }
}
