package sn.smartwaste.collect.platform.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import sn.smartwaste.collect.platform.domain.model.QuizResponse;

public interface QuizResponseRepository extends JpaRepository<QuizResponse, UUID> {

    boolean existsByQuizIdAndUserId(UUID quizId, UUID userId);

    long countByQuizId(UUID quizId);

    /** Toutes les réponses d'un quiz — sert à calculer le score moyen côté service. */
    List<QuizResponse> findByQuizId(UUID quizId);
}
