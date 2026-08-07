package sn.smartwaste.collect.platform.domain.model;

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
 * Un quiz de sensibilisation, rattaché ou non à une {@link AwarenessCampaign} (G3, suite).
 *
 * <p>Le mémoire cite le quiz parmi les formats de sensibilisation attendus — un message qu'on lit
 * une fois n'engage pas de la même façon qu'une question à laquelle on répond. Les questions et
 * leur bonne réponse vivent dans {@link QuizQuestion} ; ce qu'un citoyen a répondu, dans
 * {@link QuizResponse}.
 */
@Entity
@Table(name = "QUIZ")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
public class Quiz extends AbstractAuditingEntity<UUID> {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "quizId")
    UUID quizId;

    /** Campagne d'appartenance ; {@code null} = quiz autonome, hors campagne. */
    @Column(name = "campaignId")
    UUID campaignId;

    @Column(name = "title", nullable = false, length = 120)
    String title;

    /** Un quiz désactivé n'apparaît plus dans la liste active, sans perdre les réponses déjà reçues. */
    @Column(name = "active", nullable = false)
    boolean active = true;

    @Column(name = "authorId")
    UUID authorId;
}
