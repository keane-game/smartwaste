package sn.smartwaste.collect.territory.domain.model;

import org.springframework.modulith.NamedInterface;

import org.hibernate.annotations.UuidGenerator;
import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;

import java.util.UUID;

import sn.smartwaste.collect.shared.domain.model.AbstractAuditingEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;


/**
 * Point d'un contour géographique.
 *
 * <p><b>Exposé</b> ({@code @NamedInterface("geo")}) pour la même raison que {@code GeometryEntity} :
 * une alerte compose une coordonnée (ADR-0012 §1). Voir la note de dette sur {@code GeometryEntity}.
 */
@NamedInterface("geo")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "COORDINATE")
public class CoordinateEntity extends AbstractAuditingEntity<UUID> {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "CoordinateId")
    UUID coordinateId;

    @Column(name = "Latitude")
    String latitude;

    @Column(name = "Longitude")
    String longitude;

    @Column(name = "Altitude")
    String altitude;

}
