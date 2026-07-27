package sn.smartwaste.collect.territory.domain.model;

import org.hibernate.annotations.UuidGenerator;
import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;

import java.util.UUID;

import sonaged.collecte.master.model.AbstractAuditingEntity;
// Associations sortantes vers le contexte « Déchets », encore dans le code hérité : ces types
// étaient résolus par appartenance au même package avant la migration, ils exigent désormais
// des imports explicites.
import sonaged.collecte.master.model.CircuitBalayageEntity;
import sonaged.collecte.master.model.CircuitCollectEntity;
import sonaged.collecte.master.model.DepotoirEntity;


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
@Table(name = "COMMUNE")
public class CommuneEntity extends AbstractAuditingEntity<UUID> {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "CommuneId")
     UUID communeId;

    @Column(name = "Name")
    String name;

    @Column(name = "Code")
    String code;

    @Column(name = "ST")
    String st;

    @Column(name = "totalResident")
    String total;

    @Column(name = "WomanResident")
    String women;

    @Column(name = "ManResident")
    String men;

    @Column(name = "Length")
    String length;

    @Column(name = "Area")
    String area;

    @OneToOne(fetch = FetchType.LAZY,  cascade = CascadeType.ALL)
    @JoinColumn(name = "geometryId", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    GeometryEntity geometry;

    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "commune")
    @JsonIgnore
    @ToString.Exclude
    List<QuartierEntity> quartiers;

    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "commune")
    @JsonIgnore
    @ToString.Exclude
    List<CircuitBalayageEntity> circuitBalayage;

    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "commune")
    @JsonIgnore
    @ToString.Exclude
    List<CircuitCollectEntity> circuitCollect;

    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "commune")
    @JsonIgnore
    @ToString.Exclude
    List<DepotoirEntity> depotoirs;

    // P1-2 : @ManyToOne est EAGER par défaut → LAZY explicite (chaîne N+1, R3).
    @ManyToOne(cascade = { CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinColumn(name = "departmentId")
    @JsonIgnore
    DepartmentEntity department;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        CommuneEntity commune = (CommuneEntity) o;
        return getCommuneId() != null && Objects.equals(getCommuneId(), commune.getCommuneId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
