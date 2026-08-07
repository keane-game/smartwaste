package sn.smartwaste.collect.waste.domain.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Ce qu'un agent a réellement fait sur un point de collecte (G1 du backlog).
 *
 * <p><b>Le maillon qui manquait au produit.</b> La boucle métier est « détecter → alerter →
 * collecter → constater ». Les trois premiers maillons fonctionnaient ; le quatrième n'existait
 * pas : aucun moyen d'enregistrer qu'un point avait été vidé. Le niveau de remplissage ne retombait
 * donc que si un capteur le disait — alors que 71 points sur 71 n'en ont pas.
 *
 * <p>{@code agentId} référence le contexte « Identité & Accès » <b>par identifiant</b> (ADR-0012).
 */
@Entity
@Table(name = "COLLECTIONPASSAGE")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
public class CollectionPassage extends AbstractAuditingEntity<UUID> {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "passageId")
    UUID passageId;

    @Column(name = "depotoirId", nullable = false)
    UUID depotoirId;

    @Column(name = "agentId")
    UUID agentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 30)
    PassageOutcome outcome;

    /** Motif, renseigné pour un point inaccessible : sans lui, l'échec n'apprend rien. */
    @Column(name = "reason")
    String reason;

    @Column(name = "occurredAt", nullable = false)
    Instant occurredAt;
}
