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
@Table(name = "SND_DEPOTOIR")
public class Depotoir {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "depotoirId")
    private  Long depotoirId;

    @Column(name = "depotoirAddress")
    private String depotoirAddress;

    @ManyToOne(cascade = { CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(name = "typeDepotoirId")
    private TypeDepotoir typeDepotoir;


    @ManyToOne(cascade = { CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(name = "quartierId")
    private Quartier quartier;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Depotoir depotoir = (Depotoir) o;
        return getDepotoirId() != null && Objects.equals(getDepotoirId(), depotoir.getDepotoirId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
