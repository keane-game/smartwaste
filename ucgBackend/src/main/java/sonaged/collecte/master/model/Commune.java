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
@Table(name = "SND_COMMUNE")
public class Commune {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "communeId")
    private  Long communeId;

    @Column(name = "communeName")
    private String communeName;

    @Column(name = "communeCode")
    private String communeCode;

    @Column(name = "totalResident")
    private String residentTotal;

    @Column(name = "womanResident")
    private String womanResident;

    @Column(name = "manResident")
    private String manResident;

    @Column(name = "communeLength")
    private String communeLength;

    @Column(name = "communeArea")
    private String communeArea;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "geometryId", nullable = false)
    @EqualsAndHashCode.Include
    Geometry geometry;

    @ManyToOne(cascade = { CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(name = "departmentId")
    private Department department;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Commune commune = (Commune) o;
        return getCommuneId() != null && Objects.equals(getCommuneId(), commune.getCommuneId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
