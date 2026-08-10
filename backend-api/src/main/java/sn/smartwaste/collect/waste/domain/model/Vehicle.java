package sn.smartwaste.collect.waste.domain.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UuidGenerator;

import sn.smartwaste.collect.shared.domain.model.AbstractAuditingEntity;
import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;

/**
 * Véhicule de la flotte de collecte.
 *
 * <p>Le mémoire demande de pouvoir localiser « les poubelles, les dépôts sauvages <b>et les
 * véhicules de collecte</b> ». Les deux premiers existaient ; la flotte n'était modélisée nulle
 * part, alors que l'application mobile embarque déjà un écran de suivi en direct.
 *
 * <p>La position est <b>dénormalisée</b> ici : la carte a besoin du dernier point connu, pas de la
 * trace. Conserver l'historique complet des positions serait un tout autre volume — un camion émet
 * plusieurs fois par minute — et un autre besoin (reconstitution de tournée).
 */
/**
 * Cloisonnement multi-tenant (ADR-0020) : filtre inerte tant qu'aucune session ne l'active.
 * {@code @FilterDef} n'est déclaré qu'une fois, sur {@code CommuneEntity}.
 */
@Filter(name = "organizationFilter", condition = "organizationid = :organizationId")
@Entity
@Table(name = "vehicle")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Vehicle extends AbstractAuditingEntity<UUID> {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "vehicleId")
    UUID vehicleId;

    /** Collectivité propriétaire (ADR-0020). */
    @Column(name = "organizationId", nullable = false)
    UUID organizationId;

    /** Immatriculation — identifiant que le terrain utilise réellement. */
    @Column(name = "registration", nullable = false, unique = true)
    String registration;

    @Column(name = "label")
    String label;

    /** Circuit habituellement desservi (même contexte), facultatif. */
    @Column(name = "circuitCollectId")
    UUID circuitCollectId;

    @Column(name = "active", nullable = false)
    boolean active = true;

    // ---- Dernière position connue ----

    @Column(name = "lastLatitude")
    Double lastLatitude;

    @Column(name = "lastLongitude")
    Double lastLongitude;

    @Column(name = "lastPositionAt")
    Instant lastPositionAt;
}
