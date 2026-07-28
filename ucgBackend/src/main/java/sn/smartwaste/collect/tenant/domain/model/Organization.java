package sn.smartwaste.collect.tenant.domain.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import sn.smartwaste.collect.shared.domain.model.AbstractAuditingEntity;
import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;

/**
 * Collectivité cliente — le <b>tenant</b> du SaaS (ADR-0013 §4).
 *
 * <p>Volontairement minimal. La facturation n'y figure pas : l'ADR impose que la notion
 * d'abonnement reste découplée de tout fournisseur (Stripe/Paddle), via un port et une
 * implémentation d'infrastructure. Y mettre aujourd'hui des champs de facturation reviendrait à
 * choisir ce fournisseur par anticipation.
 */
@Entity
@Table(name = "organization")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Organization extends AbstractAuditingEntity<UUID> {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "organizationId")
    UUID organizationId;

    /** Nom affiché de la collectivité (« Ville de Pikine »). */
    @Column(name = "name", nullable = false)
    String name;

    /** Code court et stable, utilisé en intégration. Unique. */
    @Column(name = "code", nullable = false, unique = true)
    String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    OrganizationStatus status = OrganizationStatus.ACTIVE;
}
