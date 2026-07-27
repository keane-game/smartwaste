package sonaged.collecte.master.model;

import java.util.UUID;

// Le référentiel territorial a migré vers son module dédié : les types autrefois résolus
// par appartenance au même package requièrent maintenant un import explicite.
import sn.smartwaste.collect.territory.domain.model.GeometryEntity;


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
@Table(name = "DEPOTOIR")
public class DepotoirEntity extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DepotoirId")
    Long depotoirId;

    @Column(name = "Address")
    String address;

    @OneToOne(fetch = FetchType.LAZY,  cascade = CascadeType.ALL)
    @JoinColumn(name = "geometryId", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    GeometryEntity geometry;

    // P1-2 : TypeDepotoir est un référentiel PARTAGÉ. CascadeType.ALL permettait de
    // supprimer/écraser une valeur de référence en cascade depuis un Depotoir (R4) →
    // limité à REFRESH/MERGE. EAGER → LAZY pour couper la chaîne N+1 (R3).
    @ManyToOne(cascade = { CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinColumn(name = "typeDepotoirId")
    @JsonIgnore
    TypeDepotoirEntity typeDepotoir;

    // P1-6 / ADR-0012 : lien Depotoir → Quartier par IDENTIFIANT. Depotoir (contexte « Point de
    // collecte ») et Quartier (contexte « Référentiel territorial ») sont des contextes distincts
    // → pas d'association objet, pas de cascade. FK physique retirée en P1-7 (1.5.0).
    @Column(name = "quartierId")
    UUID quartierId;

    // P1-7 / ADR-0012 : `@ManyToOne CommuneEntity` → référence par IDENTIFIANT.
    // Commune appartient au contexte « Référentiel territorial », Depotoir au contexte
    // « Point de collecte » : aucune association objet, aucune FK physique, aucune cascade
    // ne doit traverser cette frontière. L'existence de la commune est vérifiée par
    // validation APPLICATIVE (ContexteReferentielValidator), plus par contrainte SQL.
    @Column(name = "communeId")
    UUID communeId;



    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        DepotoirEntity depotoir = (DepotoirEntity) o;
        return getDepotoirId() != null && Objects.equals(getDepotoirId(), depotoir.getDepotoirId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
