package sn.smartwaste.collect.platform.presentation.controller;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.platform.application.service.QuizService;
import sn.smartwaste.collect.platform.application.service.QuizService.AnswerInput;
import sn.smartwaste.collect.platform.application.service.QuizService.QuestionInput;

/**
 * Quiz de sensibilisation (`/v1/quizzes`, G3 suite).
 *
 * <p>⚠️ <b>La bonne réponse ne quitte jamais le serveur avant la correction.</b> {@code questions()}
 * et {@code active()} projettent volontairement {@link sn.smartwaste.collect.platform.domain.model.QuizQuestion}
 * sans son champ {@code correctOption} — l'exposer rendrait le quiz trivialement trichable en
 * lisant simplement la réponse HTTP.
 */
@RestController
@RequestMapping("/v1/quizzes")
public class QuizController {

    private final QuizService quizService;
    private final CurrentUserProvider currentUserProvider;

    public QuizController(QuizService quizService, CurrentUserProvider currentUserProvider) {
        this.quizService = quizService;
        this.currentUserProvider = currentUserProvider;
    }

    @Operation(summary = "Creer un quiz de sensibilisation",
               description = "Au moins une question, chacune avec deux a quatre options et une "
                       + "bonne reponse designant une option renseignee.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SEND_AWARENESS')")
    public QuizCree create(@RequestBody QuizRequest body) {
        if (body == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Corps de requete manquant");
        }
        var questions = (body.questions() == null ? List.<QuestionRequest>of() : body.questions()).stream()
                .map(q -> new QuestionInput(q.questionText(), q.optionA(), q.optionB(),
                        q.optionC(), q.optionD(), q.correctOption()))
                .toList();
        var quiz = quizService.create(body.title(), body.campaignId(), questions);
        return new QuizCree(quiz.getQuizId(), quiz.getTitle());
    }

    @Operation(summary = "Quiz actifs, sans la bonne reponse")
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public List<QuizVue> active() {
        return quizService.activeQuizzes().stream()
                .map(q -> new QuizVue(q.getQuizId(), q.getTitle(), q.getCampaignId()))
                .toList();
    }

    @Operation(summary = "Questions d'un quiz, sans la bonne reponse")
    @GetMapping("/{quizId}/questions")
    @PreAuthorize("isAuthenticated()")
    public List<QuestionVue> questions(@PathVariable("quizId") UUID quizId) {
        return quizService.questionsFor(quizId).stream()
                .map(q -> new QuestionVue(q.getQuestionId(), q.getQuestionText(),
                        q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()))
                .toList();
    }

    @Operation(summary = "Repondre a un quiz",
               description = "Une seule tentative par citoyen : une deuxieme soumission est refusee (409).")
    @PostMapping("/{quizId}/answers")
    @PreAuthorize("isAuthenticated()")
    public QuizService.QuizResult submit(@PathVariable("quizId") UUID quizId,
                                         @RequestBody(required = false) List<AnswerRequest> body) {
        var answers = (body == null ? List.<AnswerRequest>of() : body).stream()
                .map(a -> new AnswerInput(a.questionId(), a.selectedOption()))
                .toList();
        return quizService.submit(quizId, currentUserProvider.requireCurrentUserId(), answers);
    }

    public record QuestionRequest(String questionText, String optionA, String optionB,
                                  String optionC, String optionD, String correctOption) { }

    public record QuizRequest(String title, UUID campaignId, List<QuestionRequest> questions) { }

    public record QuizCree(UUID quizId, String title) { }

    public record QuizVue(UUID quizId, String title, UUID campaignId) { }

    /** Sans {@code correctOption} : c'est tout le sens de cette projection. */
    public record QuestionVue(UUID questionId, String questionText,
                              String optionA, String optionB, String optionC, String optionD) { }

    public record AnswerRequest(UUID questionId, String selectedOption) { }
}
