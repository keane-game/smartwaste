package sonaged.collecte.master.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import sonaged.collecte.master.enums.DeletionStatus;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;


/**
 * Base abstract class for entities which will hold definitions for created, last modified, created by,
 * last modified by attributes.
 */

@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = { "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate" }, allowGetters = true)
public abstract class AbstractAuditingEntity<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @CreatedBy
    @Column(name = "createdBy", nullable = true, length = 50, updatable = false)
    private String createdBy;


    @Column(name = "createdDate", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdDate;

    @LastModifiedBy
    @Column(name = "lastModifiedBy", length = 50)
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "lastModifiedDate")
    @UpdateTimestamp
    private LocalDateTime lastModifiedDate;

    @Column( columnDefinition="boolean default false")
    boolean archived = false;

    // ---- Soft-delete (suppression logique) : commun à TOUTES les entités ----
    // Suppression = passage en PENDING_DELETION + horodatage ; purge définitive après la
    // période de rétention (30 j par défaut, cf. sonaged.deletion.retention-days) ;
    // restauration possible tant que le délai court. Voir SoftDeleteService / DeletionPurgeScheduler.

    @Enumerated(EnumType.STRING)
    @Column(name = "deletionStatus", length = 30)
    private DeletionStatus deletionStatus = DeletionStatus.ACTIVE;

    @Column(name = "deletionRequestedAt")
    private LocalDateTime deletionRequestedAt;

    /** Marque l'entité comme supprimée logiquement (à l'instant donné). */
    public void markForDeletion(LocalDateTime when) {
        this.deletionStatus = DeletionStatus.PENDING_DELETION;
        this.deletionRequestedAt = when;
    }

    /** Restaure l'entité (annule une suppression logique). */
    public void restore() {
        this.deletionStatus = DeletionStatus.ACTIVE;
        this.deletionRequestedAt = null;
    }

    public boolean isPendingDeletion() {
        return this.deletionStatus == DeletionStatus.PENDING_DELETION;
    }

}