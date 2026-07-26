
package sonaged.collecte.master.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
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
import org.hibernate.proxy.HibernateProxy;
import sonaged.collecte.master.enums.AlertCode;

import java.util.List;
import java.util.Objects;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "ALERT")
public class AlertEntity extends AbstractAuditingEntity<Long> {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AlertId")
    Long alertId;

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
    Long depotoirId;

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
