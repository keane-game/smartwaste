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
 * Regroupe des {@link AwarenessMessage} (et des {@link Quiz}) sous un thème commun (G3, suite).
 *
 * <p>Le message unique existait déjà et suffit pour un rappel ponctuel. Une campagne répond à un
 * besoin différent : « sensibiliser au tri pendant tout le mois d'août », plusieurs messages et
 * un quiz, qu'un superviseur veut pouvoir designer et suivre comme un seul effort plutôt que
 * comme des envois isolés sans rapport apparent entre eux.
 *
 * <p>Une campagne ne diffuse rien par elle-même — elle nomme un groupe. Ce qui part reste porté
 * par {@code AwarenessMessage} (via {@code campaignId}, référence par identifiant intra-contexte)
 * et par les réponses aux quiz qui lui sont rattachés.
 */
@Entity
@Table(name = "AWARENESSCAMPAIGN")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
public class AwarenessCampaign extends AbstractAuditingEntity<UUID> {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "campaignId")
    UUID campaignId;

    @Column(name = "name", nullable = false, length = 120)
    String name;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "startDate", nullable = false)
    Instant startDate;

    /** {@code null} = campagne ouverte, sans date de clôture prévue. */
    @Column(name = "endDate")
    Instant endDate;

    /** Une campagne close le reste sans être supprimée : son historique (messages, quiz) reste lisible. */
    @Column(name = "active", nullable = false)
    boolean active = true;

    @Column(name = "authorId")
    UUID authorId;
}
