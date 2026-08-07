package sn.smartwaste.collect.platform.application.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.platform.application.service.QuizService.AnswerInput;
import sn.smartwaste.collect.platform.application.service.QuizService.QuestionInput;
import sn.smartwaste.collect.platform.domain.model.Quiz;
import sn.smartwaste.collect.platform.domain.model.QuizQuestion;
import sn.smartwaste.collect.platform.domain.model.QuizResponse;
import sn.smartwaste.collect.platform.domain.repository.QuizQuestionRepository;
import sn.smartwaste.collect.platform.domain.repository.QuizRepository;
import sn.smartwaste.collect.platform.domain.repository.QuizResponseRepository;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Quiz de sensibilisation (G3, suite).
 *
 * <p>Les garanties testées ici : un quiz sans question n'a rien à évaluer, une bonne réponse doit
 * désigner une option réellement renseignée, une seule tentative compte par citoyen, et le score se
 * lit sur les questions du quiz — pas sur ce qui a été soumis, qui peut en sauter certaines.
 */
@ExtendWith(MockitoExtension.class)
class QuizServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-08-07T10:00:00Z");
    private static final UUID AUTEUR = UUID.randomUUID();
    private static final UUID CITOYEN = UUID.randomUUID();

    @Mock private QuizRepository quizRepository;
    @Mock private QuizQuestionRepository questionRepository;
    @Mock private QuizResponseRepository responseRepository;
    @Mock private CurrentUserProvider currentUserProvider;

    private QuizServiceImpl service() {
        return new QuizServiceImpl(quizRepository, questionRepository, responseRepository,
                currentUserProvider, Clock.fixed(NOW, ZoneId.of("UTC")));
    }

    private QuestionInput validQuestion(String correct) {
        return new QuestionInput("Que faire d'un bac plein ?", "Le vider", "Le laisser deborder",
                "L'ignorer", null, correct);
    }

    @Test
    @DisplayName("un quiz sans titre est refuse")
    void titleIsRequired() {
        assertThatThrownBy(() -> service().create("", null, List.of(validQuestion("A"))))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    @DisplayName("un quiz sans question n'a rien a evaluer")
    void quizWithoutQuestionsIsRejected() {
        assertThatThrownBy(() -> service().create("Tri des dechets", null, List.of()))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> service().create("Tri des dechets", null, null))
                .isInstanceOf(ResponseStatusException.class);
        verify(quizRepository, never()).save(any());
    }

    @Test
    @DisplayName("une bonne reponse doit designer une option renseignee")
    void correctOptionMustReferenceAFilledOption() {
        // La question n'a que A, B, C (optionD = null) : designer D comme bonne reponse ne peut
        // jamais etre corrige, le quiz mentirait a chaque tentative.
        assertThatThrownBy(() -> service().create("Tri des dechets", null, List.of(validQuestion("D"))))
                .isInstanceOf(ResponseStatusException.class);
        verify(quizRepository, never()).save(any());
    }

    @Test
    @DisplayName("un quiz valide est enregistre avec ses questions, dans l'ordre")
    void validQuizIsCreatedWithOrderedQuestions() {
        when(currentUserProvider.requireCurrentUserId()).thenReturn(AUTEUR);
        when(quizRepository.save(any(Quiz.class))).thenAnswer(i -> {
            Quiz q = i.getArgument(0);
            q.setQuizId(UUID.randomUUID());
            return q;
        });

        var quiz = service().create("Tri des dechets", null,
                List.of(validQuestion("A"), validQuestion("B")));

        assertThat(quiz.isActive()).isTrue();
        assertThat(quiz.getAuthorId()).isEqualTo(AUTEUR);
        var saved = ArgumentCaptor.forClass(QuizQuestion.class);
        verify(questionRepository, org.mockito.Mockito.times(2)).save(saved.capture());
        assertThat(saved.getAllValues()).extracting(QuizQuestion::getDisplayOrder).containsExactly(0, 1);
        assertThat(saved.getAllValues()).extracting(QuizQuestion::getCorrectOption).containsExactly("A", "B");
    }

    @Test
    @DisplayName("consulter les questions d'un quiz inconnu leve ResourceNotFoundException")
    void questionsForUnknownQuizThrows() {
        var quizId = UUID.randomUUID();
        when(quizRepository.existsById(quizId)).thenReturn(false);

        assertThatThrownBy(() -> service().questionsFor(quizId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("repondre a un quiz inconnu leve ResourceNotFoundException")
    void submittingToUnknownQuizThrows() {
        var quizId = UUID.randomUUID();
        when(quizRepository.existsById(quizId)).thenReturn(false);

        assertThatThrownBy(() -> service().submit(quizId, CITOYEN, List.of()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("une deuxieme tentative est refusee")
    void secondAttemptIsRejected() {
        var quizId = UUID.randomUUID();
        when(quizRepository.existsById(quizId)).thenReturn(true);
        when(responseRepository.existsByQuizIdAndUserId(quizId, CITOYEN)).thenReturn(true);

        assertThatThrownBy(() -> service().submit(quizId, CITOYEN, List.of()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("deja");
        verify(responseRepository, never()).save(any());
    }

    @Test
    @DisplayName("le score compte les bonnes reponses, le total vient du quiz, pas des reponses soumises")
    void scoreCountsCorrectAnswersAgainstQuizTotal() {
        var quizId = UUID.randomUUID();
        var q1 = question(quizId, "A");
        var q2 = question(quizId, "B");
        var q3 = question(quizId, "C");
        when(quizRepository.existsById(quizId)).thenReturn(true);
        when(responseRepository.existsByQuizIdAndUserId(quizId, CITOYEN)).thenReturn(false);
        when(questionRepository.findByQuizIdOrderByDisplayOrder(quizId)).thenReturn(List.of(q1, q2, q3));

        // q1 correcte, q2 fausse, q3 non repondue (sautee) : le total reste 3, le score 1.
        var result = service().submit(quizId, CITOYEN, List.of(
                new AnswerInput(q1.getQuestionId(), "A"),
                new AnswerInput(q2.getQuestionId(), "D")));

        assertThat(result.score()).isEqualTo(1);
        assertThat(result.totalQuestions()).isEqualTo(3);

        var saved = ArgumentCaptor.forClass(QuizResponse.class);
        verify(responseRepository).save(saved.capture());
        assertThat(saved.getValue().getScore()).isEqualTo(1);
        assertThat(saved.getValue().getTotalQuestions()).isEqualTo(3);
        assertThat(saved.getValue().getUserId()).isEqualTo(CITOYEN);
        assertThat(saved.getValue().getRespondedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("une reponse insensible a la casse reste correcte")
    void answerComparisonIsCaseInsensitive() {
        var quizId = UUID.randomUUID();
        var q1 = question(quizId, "A");
        lenient().when(quizRepository.existsById(quizId)).thenReturn(true);
        when(questionRepository.findByQuizIdOrderByDisplayOrder(quizId)).thenReturn(List.of(q1));

        var result = service().submit(quizId, CITOYEN, List.of(new AnswerInput(q1.getQuestionId(), "a")));

        assertThat(result.score()).isEqualTo(1);
    }

    private QuizQuestion question(UUID quizId, String correctOption) {
        var q = new QuizQuestion();
        q.setQuestionId(UUID.randomUUID());
        q.setQuizId(quizId);
        q.setQuestionText("Question");
        q.setOptionA("A");
        q.setOptionB("B");
        q.setOptionC("C");
        q.setCorrectOption(correctOption);
        return q;
    }
}
