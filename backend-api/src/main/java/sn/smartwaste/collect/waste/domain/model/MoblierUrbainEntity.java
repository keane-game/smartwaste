package sn.smartwaste.collect.waste.domain.model;

import sn.smartwaste.collect.shared.domain.model.AbstractAuditingEntity;
import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.proxy.HibernateProxy;

import java.util.List;
import java.util.Objects;
import java.util.UUID;


/**
 * Cloisonnement multi-tenant (ADR-0020) : filtre inerte tant qu'aucune session ne l'active.
 * {@code @FilterDef} n'est déclaré qu'une fois, sur {@code CommuneEntity}.
 */
@Filter(name = "organizationFilter", condition = "organizationid = :organizationId")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "MOBLIERURBAIN")
public class MoblierUrbainEntity extends AbstractAuditingEntity<UUID> {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "MoblierUrbainId")
    UUID moblierUrbainId;

    /** Collectivité propriétaire (ADR-0020). */
    @Column(name = "organizationId", nullable = false)
    UUID organizationId;

    @Column(name = "Name")
    String name;

    @Column(name = "Code")
    String code;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        MoblierUrbainEntity that = (MoblierUrbainEntity) o;
        return getMoblierUrbainId() != null && Objects.equals(getMoblierUrbainId(), that.getMoblierUrbainId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
