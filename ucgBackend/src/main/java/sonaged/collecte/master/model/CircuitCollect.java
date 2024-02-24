package sonaged.collecte.master.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "SND_CIRCUITCOLLECT")
public class CircuitCollect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "circuitcollectId")
    private  Long circuitcollectId;

    @Column(name = "circuitcollectName")
    private String circuitcollectName;

    @Column(name = "circuitcollectCode")
    private String circuitcollectCode;

    @Column(name = "circuitcollectLength")
    private String circuitcollectLength;

    @Column(name = "circuitcollectFrequence")
    private String circuitcollectFrequence;

    @Column(name = "circuitcollectLatiPointA")
    private String circuitcollectLatiPointA;

    @Column(name = "circuitcollectLatiPointD")
    private String circuitcollectLatiPointD;

    @Column(name = "circuitcollectLongPointA")
    private String circuitcollectLongPointA;

    @Column(name = "circuitcollectLongPointD")
    private String circuitcollectLongPointD;

    @Column(name = "circuitcollectType")
    private String circuitcollectType;

    @Column(name = "circuitcollectCat")
    private String circuitcollectCat;

    @Column(name = "circuitcollectRotattion")
    private String circuitcollectRotation;

    @Column(name = "circuitcollectSection")
    private String circuitcollectSection;

    @Column(name = "circuitcollectSectection")
    private String circuitcollectSectection;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        CircuitCollect that = (CircuitCollect) o;
        return getCircuitcollectId() != null && Objects.equals(getCircuitcollectId(), that.getCircuitcollectId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
