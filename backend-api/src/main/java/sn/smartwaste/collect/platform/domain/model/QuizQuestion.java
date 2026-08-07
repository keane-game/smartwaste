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

import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;

/**
 * Une question à choix unique d'un {@link Quiz}.
 *
 * <p>{@code quizId} référence son quiz par identifiant plutôt que par association objet : une
 * question n'a aucune existence ni aucun sens hors de son quiz, mais rien ici n'a besoin de
 * naviguer de la question vers le quiz — seule la lecture inverse (quiz → questions, via
 * {@code QuizQuestionRepository.findByQuizId...}) est jamais faite.
 *
 * <p><b>La bonne réponse ({@code correctOption}) ne doit jamais atteindre un citoyen avant qu'il
 * ait répondu</b> — c'est au DTO exposé par le contrôleur de l'omettre, pas à cette entité.
 */
@Entity
@Table(name = "QUIZQUESTION")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
public class QuizQuestion {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "questionId")
    UUID questionId;

    @Column(name = "quizId", nullable = false)
    UUID quizId;

    @Column(name = "questionText", nullable = false, columnDefinition = "TEXT")
    String questionText;

    @Column(name = "optionA", nullable = false, length = 255)
    String optionA;

    @Column(name = "optionB", nullable = false, length = 255)
    String optionB;

    @Column(name = "optionC", length = 255)
    String optionC;

    @Column(name = "optionD", length = 255)
    String optionD;

    /** 'A', 'B', 'C' ou 'D' — doit désigner une option effectivement renseignée. */
    @Column(name = "correctOption", nullable = false, length = 1)
    String correctOption;

    /** Ordre d'affichage dans le quiz — sans lui, l'ordre suivrait un hasard de requête. */
    @Column(name = "displayOrder", nullable = false)
    int displayOrder;
}
