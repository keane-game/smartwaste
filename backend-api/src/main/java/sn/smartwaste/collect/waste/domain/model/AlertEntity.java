
package sn.smartwaste.collect.waste.domain.model;

import sn.smartwaste.collect.shared.domain.model.AbstractAuditingEntity;
import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;

// Le référentiel territorial a migré vers son module dédié : les types autrefois résolus
// par appartenance au même package requièrent maintenant un import explicite.
import sn.smartwaste.collect.territory.domain.model.CoordinateEntity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.proxy.HibernateProxy;
import sn.smartwaste.collect.waste.domain.model.AlertCode;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Cloisonnement multi-tenant (ADR-0020) : filtre inerte tant qu'aucune session ne l'active.
 * {@code @FilterDef} n'est déclaré qu'une fois, sur {@code CommuneEntity}.
 */
@Filter(name = "organizationFilter", condition = "organizationid = :organizationId")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "ALERT")
public class AlertEntity extends AbstractAuditingEntity<UUID> {


    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "AlertId")
    UUID alertId;

    /** Collectivité propriétaire (ADR-0020). */
    @Column(name = "organizationId", nullable = false)
    UUID organizationId;

    @Column(name = "Object")
    String object;

    @Column(name = "Message", columnDefinition = "TEXT")
    String message;

    @Column(name = "Address")
    String address;

    @Enumerated(EnumType.STRING)
    @Column(name="Code")
    AlertCode code;

    // P1-7 / ADR-0012 : point de collecte concerné, référencé par IDENTIFIANT.
    // Le contexte « Alertes » et le contexte « Point de collecte » sont distincts → pas
    // d'association objet ni de FK physique. Nullable : les alertes saisies manuellement
    // ne visent pas forcément un dépotoir. (Le remplissage automatique de ce champ par le
    // moteur de seuils relève de P0-6, module IoT réservé.)
    @Column(name = "depotoirId")
    UUID depotoirId;

    // Lot 1 / lot 2 du backlog : une alerte avait un debut et jamais de fin. « Ouverte » se lit
    // desormais `resolvedAt == null` — un capteur qui reemet, un point qui est vide, referment ce
    // qu'ils avaient ouvert.
    @Column(name = "resolvedAt")
    java.time.LocalDateTime resolvedAt;

    @Column(name = "resolvedBy", length = 50)
    String resolvedBy;

    @OneToOne(fetch = FetchType.LAZY,  cascade = CascadeType.ALL)
    @JoinColumn(name = "coordinateId", nullable = true)
    @JsonIgnore
    @ToString.Exclude
    CoordinateEntity coordinate;

    // P1-3 / ADR-0005 : le BLOB `displayPicture` en table ALERT est supprimé.
    // L'image d'une alerte est désormais portée par `image` (référence de fichier).

    @OneToOne(cascade = CascadeType.ALL,
            fetch = FetchType.LAZY )
    @JoinColumn(name = "ImageId")
    @JsonIgnore
    @ToString.Exclude
    private ImageEntity image;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        AlertEntity alert = (AlertEntity) o;
        return getAlertId() != null && Objects.equals(getAlertId(), alert.getAlertId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
