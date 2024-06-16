package sonaged.collecte.master.model;

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
@Table(name = "QUARTIER")
public class QuartierEntity extends AbstractAuditingEntity<Long>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QuartierId")
    Long quartierId;

    @Column(name = "Name")
    String name;

    @Column(name = "Code")
    String code;

    @Column(name = "Cav")
    String cav;

    @Column(name = "CodeCav")
    String codeCav;

    @Column(name = "Ccrca")
    String cCrca;

    @Column(name = "CodeCcrca")
    String codeCcrca;


    @Column(name = "CodeEntity")
    String codeEntity;

    @Column(name = "Numerozr")
    String numerozr;

    @Column(name = "CodeSzr")
    String codeSzr;

    @Column(name = "ZoneCoron")
    String zoneCoron;

    @Column(name = "Poucentage")
    String poucentage;

    @Column(name = "Length")
    String length;

    @Column(name = "Area")
    String Area;

    @JsonIgnore
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "quartier")
    @ToString.Exclude
    List<DepotoirEntity> depotoirs;

    @ManyToOne(cascade = { CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.EAGER)
    @JoinColumn(name = "communeId")
    @JsonIgnore
    CommuneEntity commune;

    @OneToOne(fetch = FetchType.LAZY,  cascade = CascadeType.ALL)
    @JoinColumn(name = "geometryId", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    GeometryEntity geometry;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        QuartierEntity  quartier= (QuartierEntity) o;
        return getQuartierId() != null && Objects.equals(getQuartierId(), quartier.getQuartierId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
