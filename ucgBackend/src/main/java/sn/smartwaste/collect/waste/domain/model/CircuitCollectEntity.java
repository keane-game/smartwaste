package sn.smartwaste.collect.waste.domain.model;

import sn.smartwaste.collect.shared.domain.model.AbstractAuditingEntity;

import java.util.UUID;

// Le référentiel territorial a migré vers son module dédié : les types autrefois résolus
// par appartenance au même package requièrent maintenant un import explicite.
import sn.smartwaste.collect.territory.domain.model.GeometryEntity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import lombok.EqualsAndHashCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "CIRCUITCOLLECT")
public class CircuitCollectEntity extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CircuitcollectId")
    Long circuitcollectId;

    @Column(name = "Name")
    String Name;

    @Column(name = "Code")
    String code;

    @Column(name = "Length")
    String length;

    @Column(name = "Frequence")
    String frequence;

    @Column(name = "LatiPointA")
    String latiPointA;

    @Column(name = "LatiPointD")
    String latiPointD;

    @Column(name = "LongPointA")
    String longPointA;

    @Column(name = "LongPointD")
    String longPointD;

    @Column(name = "Type")
    String type;

    @Column(name = "Cat")
    String cat;

    @Column(name = "Rotattion")
    String rotation;

    @Column(name = "Section")
    String section;

    @Column(name = "Sectection")
    String sectection;

    @OneToOne(fetch = FetchType.LAZY,  cascade = CascadeType.ALL)
    @JoinColumn(name = "geometryId", nullable = true)
    @JsonIgnore
    @ToString.Exclude
    GeometryEntity geometry;

    // P1-7 / ADR-0012 : référence par IDENTIFIANT vers le contexte « Référentiel territorial »
    // (plus d'association objet ni de FK physique cross-contexte).
    @Column(name = "communeId")
    UUID communeId;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        CircuitCollectEntity that = (CircuitCollectEntity) o;
        return getCircuitcollectId() != null && Objects.equals(getCircuitcollectId(), that.getCircuitcollectId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
