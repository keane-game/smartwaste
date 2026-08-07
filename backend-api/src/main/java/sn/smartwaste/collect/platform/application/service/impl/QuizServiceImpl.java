package sn.smartwaste.collect.platform.application.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.platform.application.service.QuizService;
import sn.smartwaste.collect.platform.domain.model.Quiz;
import sn.smartwaste.collect.platform.domain.model.QuizQuestion;
import sn.smartwaste.collect.platform.domain.model.QuizResponse;
import sn.smartwaste.collect.platform.domain.repository.QuizQuestionRepository;
import sn.smartwaste.collect.platform.domain.repository.QuizRepository;
import sn.smartwaste.collect.platform.domain.repository.QuizResponseRepository;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;

/**
 * Programme les quiz et note les réponses (G3, suite).
 *
 * <p><b>Une seule tentative par citoyen.</b> Sans cette garde, un quiz de sensibilisation devient
 * une devinette par essais successifs — jouable jusqu'à obtenir un score parfait, ce qui ne dit
 * plus rien de ce qui a été réellement compris. La contrainte est posée aux deux niveaux : ici, et
 * en base ({@code uk_quizresponse_quiz_user}), pour la même raison qu'ailleurs dans ce dépôt — une
 * règle qui ne vit qu'en application n'a jamais protégé personne contre une écriture concurrente.
 *
 * <p><b>Le score compte les questions du quiz, pas les réponses soumises.</b> Un citoyen qui saute
 * une question ne doit pas voir son total réduit en conséquence : {@code totalQuestions} vient du
 * quiz, {@code score} du nombre de bonnes réponses parmi ce qui a été soumis.
 */
@Service
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizResponseRepository responseRepository;
    private final CurrentUserProvider currentUserProvider;
    private final Clock clock;

    public QuizServiceImpl(QuizRepository quizRepository,
                           QuizQuestionRepository questionRepository,
                           QuizResponseRepository responseRepository,
                           CurrentUserProvider currentUserProvider,
                           Clock clock) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.responseRepository = responseRepository;
        this.currentUserProvider = currentUserProvider;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Quiz create(String title, UUID campaignId, List<QuestionInput> questions) {
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le titre du quiz est obligatoire");
        }
        if (questions == null || questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un quiz sans question n'a rien a evaluer");
        }
        for (QuestionInput q : questions) {
            validateQuestion(q);
        }

        var quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setCampaignId(campaignId);
        quiz.setAuthorId(currentUserProvider.requireCurrentUserId());
        quiz = quizRepository.save(quiz);

        int order = 0;
        for (QuestionInput q : questions) {
            var question = new QuizQuestion();
            question.setQuizId(quiz.getQuizId());
            question.setQuestionText(q.questionText());
            question.setOptionA(q.optionA());
            question.setOptionB(q.optionB());
            question.setOptionC(q.optionC());
            question.setOptionD(q.optionD());
            question.setCorrectOption(q.correctOption().toUpperCase());
            question.setDisplayOrder(order++);
            questionRepository.save(question);
        }
        return quiz;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Quiz> activeQuizzes() {
        return quizRepository.findByActiveTrueOrderByCreatedDateDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizQuestion> questionsFor(UUID quizId) {
        requireQuiz(quizId);
        return questionRepository.findByQuizIdOrderByDisplayOrder(quizId);
    }

    @Override
    @Transactional
    public QuizResult submit(UUID quizId, UUID userId, List<AnswerInput> answers) {
        requireQuiz(quizId);
        if (responseRepository.existsByQuizIdAndUserId(quizId, userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Une reponse a deja ete enregistree pour ce quiz");
        }

        List<QuizQuestion> questions = questionRepository.findByQuizIdOrderByDisplayOrder(quizId);
        Map<UUID, String> correctByQuestion = questions.stream()
                .collect(Collectors.toMap(QuizQuestion::getQuestionId, QuizQuestion::getCorrectOption));

        int score = 0;
        if (answers != null) {
            for (AnswerInput answer : answers) {
                String correct = correctByQuestion.get(answer.questionId());
                if (correct != null && answer.selectedOption() != null
                        && correct.equalsIgnoreCase(answer.selectedOption())) {
                    score++;
                }
            }
        }

        var response = new QuizResponse();
        response.setQuizId(quizId);
        response.setUserId(userId);
        response.setScore(score);
        response.setTotalQuestions(questions.size());
        response.setRespondedAt(Instant.now(clock));
        responseRepository.save(response);

        return new QuizResult(score, questions.size());
    }

    private void requireQuiz(UUID quizId) {
        if (!quizRepository.existsById(quizId)) {
            throw new ResourceNotFoundException("Quiz [%s] introuvable".formatted(quizId));
        }
    }

    private void validateQuestion(QuestionInput q) {
        if (q.questionText() == null || q.questionText().isBlank()
                || q.optionA() == null || q.optionA().isBlank()
                || q.optionB() == null || q.optionB().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Chaque question exige un enonce et au moins deux options (A, B)");
        }
        if (q.correctOption() == null || !List.of("A", "B", "C", "D").contains(q.correctOption().toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La bonne reponse doit designer une option existante (A-D)");
        }
        boolean optionExists = switch (q.correctOption().toUpperCase()) {
            case "A" -> true;
            case "B" -> true;
            case "C" -> q.optionC() != null && !q.optionC().isBlank();
            case "D" -> q.optionD() != null && !q.optionD().isBlank();
            default -> false;
        };
        if (!optionExists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La bonne reponse designe une option (%s) qui n'est pas renseignee"
                            .formatted(q.correctOption()));
        }
    }
}
