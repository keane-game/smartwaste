package sonaged.collecte.master.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

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


@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "SND_CIRCUITCOLLECT")
public class CircuitCollectEntity extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "circuitcollectId")
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
