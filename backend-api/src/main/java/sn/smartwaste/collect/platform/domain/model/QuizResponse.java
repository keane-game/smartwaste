package sn.smartwaste.collect.platform.domain.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;

/**
 * Réponse d'un citoyen à un {@link Quiz} — une tentative, jamais deux (G3, suite).
 *
 * <p>Autoriser à rejouer transformerait un quiz de sensibilisation en devinette par essais
 * successifs : la contrainte d'unicité (quizId, userId), portée aussi bien en base qu'en
 * application, fait qu'une seule réponse compte, la première.
 *
 * <p>Seul le score agrégé est gardé, pas la réponse à chaque question : ce que cette fonction doit
 * savoir dire, c'est « combien de citoyens ont bien comrpis », pas construire un profil détaillé de
 * qui a répondu quoi.
 */
@Entity
@Table(name = "quizresponse",
       uniqueConstraints = @UniqueConstraint(name = "uk_quizresponse_quiz_user",
                                             columnNames = {"quizId", "userId"}))
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
public class QuizResponse {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "responseId")
    UUID responseId;

    @Column(name = "quizId", nullable = false)
    UUID quizId;

    /** Référence par identifiant vers « Identité & Accès » (ADR-0012) : pas de FK traversante. */
    @Column(name = "userId", nullable = false)
    UUID userId;

    @Column(name = "score", nullable = false)
    int score;

    @Column(name = "totalQuestions", nullable = false)
    int totalQuestions;

    @Column(name = "respondedAt", nullable = false)
    Instant respondedAt;
}
