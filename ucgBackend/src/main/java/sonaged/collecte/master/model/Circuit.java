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
@Table(name = "SND_CIRCUIT")
public class Circuit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "circuitId")
    private  Long circuitId;

    @Column(name = "circuitName")
    private String circuitName;

    @Column(name = "circuitbalayageCode")
    private String circuitCode;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "geometryId", nullable = false)
    @EqualsAndHashCode.Include
    Geometry geometry;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Circuit circuit = (Circuit) o;
        return getCircuitId() != null && Objects.equals(getCircuitId(), circuit.getCircuitId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
