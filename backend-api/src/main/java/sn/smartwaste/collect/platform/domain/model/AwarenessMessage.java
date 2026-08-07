package sn.smartwaste.collect.platform.domain.model;

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
import org.hibernate.annotations.UuidGenerator;

import sn.smartwaste.collect.shared.domain.model.AbstractAuditingEntity;
import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;

/**
 * Un message de sensibilisation adressé aux habitants (G3 du backlog).
 *
 * <p>Le mémoire les liste parmi les fonctionnalités du système (§3.1.5.2), et cite le manque de
 * sensibilisation comme <b>cause</b> du problème — pas seulement comme confort.
 *
 * <p><b>Le message survit à son envoi.</b> Un citoyen doit pouvoir relire ce qu'il a reçu, et
 * l'administration dire ce qu'elle a diffusé, quand, et à combien d'appareils. Un envoi qui ne
 * laisse aucune trace ne se corrige pas et ne se défend pas.
 */
@Entity
@Table(name = "AWARENESSMESSAGE")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
public class AwarenessMessage extends AbstractAuditingEntity<UUID> {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "messageId")
    UUID messageId;

    @Column(name = "title", nullable = false, length = 120)
    String title;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    String body;

    /** Quartier visé ; {@code null} vise tous les abonnés, quel que soit leur quartier. */
    @Column(name = "quartierId")
    UUID quartierId;

    @Column(name = "scheduledAt", nullable = false)
    Instant scheduledAt;

    /** {@code null} tant que le message n'est pas parti : c'est ce qui le rend « dû à envoyer ». */
    @Column(name = "sentAt")
    Instant sentAt;

    /** Nombre d'appareils effectivement joints — ce qui a été fait, non ce qui était visé. */
    @Column(name = "recipientCount")
    Integer recipientCount;

    @Column(name = "authorId")
    UUID authorId;

    /** Campagne d'appartenance ; {@code null} = message ponctuel, hors campagne. */
    @Column(name = "campaignId")
    UUID campaignId;
}
