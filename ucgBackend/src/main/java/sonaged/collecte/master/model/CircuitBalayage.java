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
@Table(name = "SND_CIRCUITBALAYAGE")
public class CircuitBalayage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "circuitbalayageId")
    private  Long circuitbalayageId;

    @Column(name = "circuitbalayageName")
    private String circuitbalayageName;

    @Column(name = "circuitbalayageCode")
    private String circuitbalayageCode;

    @Column(name = "circuitbalayageShift")
    private String circuitbalayageShift;

    @Column(name = "circuitbalayageLength")
    private String circuitbalayageLength;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        CircuitBalayage that = (CircuitBalayage) o;
        return getCircuitbalayageId() != null && Objects.equals(getCircuitbalayageId(), that.getCircuitbalayageId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
