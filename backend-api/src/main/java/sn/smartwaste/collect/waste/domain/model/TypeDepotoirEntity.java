package sn.smartwaste.collect.waste.domain.model;

import sn.smartwaste.collect.shared.domain.model.AbstractAuditingEntity;
import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.proxy.HibernateProxy;

import java.util.List;
import java.util.Objects;
import java.util.UUID;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "TYPEDEPOTOIR")
public class TypeDepotoirEntity  extends AbstractAuditingEntity<UUID> {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "TypeDepotoirId")
    UUID typeDepotoirId;

    @Column(name = "Name")
    String name;

    // P1-2 : cascade ALL retiré — TypeDepotoir est un référentiel partagé (R4) ; le supprimer
    // ne doit jamais cascade-supprimer tous les Depotoir qui le référencent. Rien ne lit
    // aujourd'hui cette collection (aucun champ `depotoirs` sur le DTO TypeDepotoir, aucun
    // appelant), elle reste néanmoins déclarée côté entité pour la navigation JPA inverse.
    @JsonIgnore
    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "typeDepotoir")
    @ToString.Exclude
    List<DepotoirEntity> depotoirs;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        TypeDepotoirEntity that = (TypeDepotoirEntity) o;
        return getTypeDepotoirId() != null && Objects.equals(getTypeDepotoirId(), that.getTypeDepotoirId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
