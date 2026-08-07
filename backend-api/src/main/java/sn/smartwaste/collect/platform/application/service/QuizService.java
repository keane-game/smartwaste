package sn.smartwaste.collect.platform.application.service;

import java.util.List;
import java.util.UUID;

import sn.smartwaste.collect.platform.domain.model.Quiz;
import sn.smartwaste.collect.platform.domain.model.QuizQuestion;

/**
 * Quiz de sensibilisation, rattachés ou non à une campagne (G3, suite).
 *
 * <p>Le mémoire cite le quiz comme format distinct du simple message : une question à laquelle on
 * répond engage autrement qu'un texte qu'on lit une fois.
 */
public interface QuizService {

    /** @throws org.springframework.web.server.ResponseStatusException si aucune question n'est fournie */
    Quiz create(String title, UUID campaignId, List<QuestionInput> questions);

    /** Quiz actifs, le plus récent d'abord. */
    List<Quiz> activeQuizzes();

    /** @throws sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException si le quiz n'existe pas */
    List<QuizQuestion> questionsFor(UUID quizId);

    /**
     * Enregistre la réponse d'un citoyen — une seule tentative par quiz.
     *
     * @throws org.springframework.web.server.ResponseStatusException 409 si ce citoyen a déjà répondu
     */
    QuizResult submit(UUID quizId, UUID userId, List<AnswerInput> answers);

    record QuestionInput(String questionText, String optionA, String optionB,
                         String optionC, String optionD, String correctOption) { }

    record AnswerInput(UUID questionId, String selectedOption) { }

    record QuizResult(int score, int totalQuestions) { }
}
