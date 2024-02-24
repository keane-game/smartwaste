package sonaged.collecte.master.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "SND_QUARTIER")
public class Quartier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quartierId")
    private  Long quartierId;

    @Column(name = "quartierName")
    private String quartierName;

    @Column(name = "quartierCode")
    private String quartierCode;

    @Column(name = "quartierCav")
    private String quartierCav;

    @Column(name = "quartierCodeCav")
    private String quartierCodeCav;

    @Column(name = "quartierCcrca")
    private String quartierCcrca;

    @Column(name = "quartierCodeCcrca")
    private String quartierCodeCcrca;


    @Column(name = "quartierCodeEntity")
    private String quartierCodeEntity;

    @Column(name = "quartierNumerozr")
    private String quartierNumerozr;

    @Column(name = "quartierCodeSzr")
    private String quartierCodeSzr;

    @Column(name = "quartierZoneCoron")
    private String quartierZoneCoron;

    @Column(name = "quartierPoucentage")
    private String quartierPoucentage;

    @Column(name = "quartierLength")
    private String quartierLength;

    @Column(name = "quartierArea")
    private String quartierArea;

    @JsonIgnore
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "quartier")
    @ToString.Exclude
    List<Depotoir> depotoirs;


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
        Quartier quartier = (Quartier) o;
        return getQuartierId() != null && Objects.equals(getQuartierId(), quartier.getQuartierId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
