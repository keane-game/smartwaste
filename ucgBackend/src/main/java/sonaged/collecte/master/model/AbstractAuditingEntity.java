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

}