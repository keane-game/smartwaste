package sn.smartwaste.collect.territory.domain.model;

import org.hibernate.annotations.UuidGenerator;
import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;

import java.util.UUID;

import sn.smartwaste.collect.shared.domain.model.AbstractAuditingEntity;
// Plus aucun import vers le contexte « Déchets » : les trois associations sortantes ont été
// retirées (voir le commentaire sur les collections, plus bas). Le référentiel territorial
// redevient ce que l'ADR-0013 prescrit — une source de vérité qui ne dépend de personne.


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
@Table(name = "COMMUNE")
public class CommuneEntity extends AbstractAuditingEntity<UUID> {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "CommuneId")
     UUID communeId;

    @Column(name = "Name")
    String name;

    @Column(name = "Code")
    String code;

    @Column(name = "ST")
    String st;

    @Column(name = "totalResident")
    String total;

    @Column(name = "WomanResident")
    String women;

    @Column(name = "ManResident")
    String men;

    @Column(name = "Length")
    String length;

    @Column(name = "Area")
    String area;

    @OneToOne(fetch = FetchType.LAZY,  cascade = CascadeType.ALL)
    @JoinColumn(name = "geometryId", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    GeometryEntity geometry;

    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "commune")
    @JsonIgnore
    @ToString.Exclude
    List<QuartierEntity> quartiers;

    // ---------------------------------------------------------------------------------
    // 🔴 Trois collections inverses RETIRÉES ici — elles empêchaient le démarrage.
    //
    // `circuitBalayage`, `circuitCollect` et `depotoirs` étaient des
    // `@OneToMany(mappedBy = "commune")` vers le contexte « Déchets ». Or P1-7a (ADR-0012) a
    // converti le côté propriétaire en simple identifiant : `DepotoirEntity`,
    // `CircuitCollectEntity` et `CircuitBalayageEntity` portent désormais `UUID communeId` et
    // n'ont plus AUCUN champ `commune`. Le `mappedBy` pointait donc dans le vide.
    //
    // Pourquoi personne ne l'avait vu : `mappedBy` est une chaîne de caractères, le compilateur
    // ne la vérifie pas. La rupture n'apparaît qu'à la construction du métamodèle Hibernate
    // (`AnnotationException: ... 'mappedBy' a property named 'commune' which does not exist`),
    // c'est-à-dire au démarrage — et l'application n'a jamais été lancée contre une base.
    // Le pendant sur `QuartierEntity.depotoirs` avait bien été commenté à l'époque ; ces
    // trois-là ont été oubliés. Aucun code ne les lisait (vérifié).
    //
    // Il ne s'agit donc pas d'un choix de modélisation à refaire : une association inverse ne
    // PEUT PAS exister quand le côté propriétaire est une référence par identifiant. Les
    // dépotoirs et circuits d'une commune se lisent via le contexte propriétaire
    // (`findByCommuneId`), comme le prévoit l'ADR-0012. `CommuneServiceImpl` faisait déjà de
    // même après le retrait du champ équivalent sur `dto/Commune`.
    //
    // Bonus : ces `CascadeType.ALL` cross-contexte auraient supprimé en cascade les dépotoirs
    // et circuits d'une commune effacée — exactement le risque signalé en P1-2 (R4).
    // Couvert désormais par `JpaMappingBootstrapTest`.
    // ---------------------------------------------------------------------------------

    // P1-2 : @ManyToOne est EAGER par défaut → LAZY explicite (chaîne N+1, R3).
    @ManyToOne(cascade = { CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinColumn(name = "departmentId")
    @JsonIgnore
    DepartmentEntity department;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        CommuneEntity commune = (CommuneEntity) o;
        return getCommuneId() != null && Objects.equals(getCommuneId(), commune.getCommuneId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
