package ucg.collecte.master.model;

import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

public abstract class AuditModel {

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    @CreatedDate
    private Instant createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    @LastModifiedDate
    private Instant updatedAt;

    @Column
    @CreatedBy
    private User createdBy;
}
