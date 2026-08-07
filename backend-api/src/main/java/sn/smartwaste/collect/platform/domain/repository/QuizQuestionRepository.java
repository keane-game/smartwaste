package sn.smartwaste.collect.platform.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import sn.smartwaste.collect.platform.domain.model.QuizQuestion;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, UUID> {

    List<QuizQuestion> findByQuizIdOrderByDisplayOrder(UUID quizId);
}
