package sn.smartwaste.collect.territory.domain.model;

import org.hibernate.annotations.UuidGenerator;
import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;

import java.util.UUID;

import sn.smartwaste.collect.shared.domain.model.AbstractAuditingEntity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.hibernate.proxy.HibernateProxy;

import java.util.List;
import java.util.Objects;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "DEPARTEMENT")
public class DepartmentEntity extends AbstractAuditingEntity<UUID> {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "DepartmentId")
     UUID departmentId;

    @Column(name = "Name")
    String name;

    @Column(name = "Area")
    double area;

    @Column(name = "Effectif")
    Integer effectif;

    @Column(name = "Code")
    String code;

    @OneToOne(fetch = FetchType.LAZY,  cascade = CascadeType.ALL)
    @JoinColumn(name = "geometryId", nullable = true)
    @JsonIgnore
    @ToString.Exclude
    GeometryEntity geometry;

    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "department")
    @JsonIgnore
    @ToString.Exclude
    List<CommuneEntity> communes;

    // P1-2 : @ManyToOne est EAGER par défaut → LAZY explicite (chaîne N+1, R3).
    @ManyToOne(cascade = { CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinColumn(name = "regionId")
    @JsonIgnore
    RegionEntity region;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        DepartmentEntity that = (DepartmentEntity) o;
        return getDepartmentId() != null && Objects.equals(getDepartmentId(), that.getDepartmentId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
