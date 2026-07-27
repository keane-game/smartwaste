package sonaged.collecte.master.model;

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
import sonaged.collecte.master.enums.CircuitShift;

import java.util.Objects;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "CIRCUITBALAYAGE")
public class CircuitBalayageEntity extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CircuitbalayageId")
    Long circuitbalayageId;

    @Column(name = "Name")
    String name;

    @Column(name = "Code")
    String code;

    @Column(name = "Shift")
    @Enumerated(EnumType.STRING)
    CircuitShift shift;

    @Column(name = "Length")
    String length;

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
        CircuitBalayageEntity that = (CircuitBalayageEntity) o;
        return getCircuitbalayageId() != null && Objects.equals(getCircuitbalayageId(), that.getCircuitbalayageId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
