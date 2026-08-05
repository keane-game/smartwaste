package sn.smartwaste.collect.identity.domain.model;

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
import org.hibernate.annotations.UuidGenerator;

import sn.smartwaste.collect.shared.domain.model.AbstractAuditingEntity;
import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;

/**
 * Territoire couvert par un agent de collecte (G1 du backlog).
 *
 * <p><b>Pourquoi cette table existe.</b> Le référentiel compte 12 communes et 71 points. Sans
 * affectation, tout agent verrait la totalité : l'écran deviendrait inexploitable, et surtout la
 * notion de « tournée du jour » perdrait son sens — une tournée est celle de quelqu'un, sur un
 * territoire donné.
 *
 * <p><b>Pourquoi ici et non sur {@code UserEntity}.</b> Même raison que pour l'appartenance à une
 * collectivité : un utilisateur n'est pas toujours un agent, et poser un champ « communes » sur le
 * compte obligerait tous les autres à le porter à vide.
 *
 * <p>{@code communeId} est une référence <b>cross-contexte</b> vers le référentiel territorial :
 * par identifiant, sans clé étrangère (ADR-0012).
 */
@Entity
@Table(name = "AGENTASSIGNMENT")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
public class AgentAssignment extends AbstractAuditingEntity<UUID> {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "assignmentId")
    UUID assignmentId;

    @Column(name = "userId", nullable = false)
    UUID userId;

    @Column(name = "communeId", nullable = false)
    UUID communeId;
}
