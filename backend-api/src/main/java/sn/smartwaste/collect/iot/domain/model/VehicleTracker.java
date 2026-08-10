package sn.smartwaste.collect.iot.domain.model;

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
 * Traceur GPS embarqué dans un véhicule de collecte.
 *
 * <p>Distinct de {@code Sensor} — qui instrumente un point de collecte fixe — parce que la cible,
 * la charge utile et le rythme d'émission n'ont rien de commun : un bac transmet un niveau toutes
 * les quelques heures, un camion une position toutes les quelques secondes. Les confondre aurait
 * impose a l'un les contraintes de l'autre.
 *
 * <p>{@code vehicleId} est une <b>référence par identifiant</b> vers le contexte « Déchets »
 * (ADR-0012) : le véhicule est un actif de la flotte, pas un objet de la chaîne d'ingestion.
 */
/**
 * Cloisonnement multi-tenant (ADR-0020) : filtre inerte tant qu'aucune session ne l'active.
 * {@code @FilterDef} n'est déclaré qu'une fois, sur {@code CommuneEntity}.
 */
@Filter(name = "organizationFilter", condition = "organizationid = :organizationId")
@Entity
@Table(name = "vehicletracker")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleTracker extends AbstractAuditingEntity<UUID> {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "trackerId")
    UUID trackerId;

    /** Collectivité propriétaire (ADR-0020). */
    @Column(name = "organizationId", nullable = false)
    UUID organizationId;

    @Column(name = "deviceCode", nullable = false, unique = true)
    String deviceCode;

    /** Empreinte SHA-256 de la clé d'API — jamais la clé elle-même. */
    @Column(name = "apiKeyHash", nullable = false, unique = true, length = 64)
    String apiKeyHash;

    @Column(name = "vehicleId", nullable = false)
    UUID vehicleId;

    @Column(name = "active", nullable = false)
    boolean active = true;

    @Column(name = "lastSeenAt")
    Instant lastSeenAt;
}
