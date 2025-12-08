package com.teamexp.learnflowapi.content.repository;

import com.teamexp.learnflowapi.content.model.Quiz;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("QuizRepository Tests")
class QuizRepositoryTest {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        quizRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save and retrieve Quiz")
    void shouldSaveAndRetrieveQuiz() {
        // Given
        Quiz quiz = new Quiz(1L, 1, "What is Java?", true);

        // When
        Quiz saved = quizRepository.save(quiz);
        entityManager.flush();
        Optional<Quiz> retrieved = quizRepository.findById(saved.getId());

        // Then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getLessonId()).isEqualTo(1L);
        assertThat(retrieved.get().getOrderIndex()).isEqualTo(1);
        assertThat(retrieved.get().getQuestion()).isEqualTo("What is Java?");
        assertThat(retrieved.get().isCorrect()).isTrue();
    }

    @Test
    @DisplayName("Should find all quizzes by lessonId ordered by orderIndex")
    void shouldFindAllQuizzesByLessonIdOrderedByOrderIndex() {
        // Given
        Long lessonId = 5L;
        Quiz quiz1 = new Quiz(lessonId, 3, "Question 3", true);
        Quiz quiz2 = new Quiz(lessonId, 1, "Question 1", false);
        Quiz quiz3 = new Quiz(lessonId, 2, "Question 2", true);
        
        quizRepository.save(quiz1);
        quizRepository.save(quiz2);
        quizRepository.save(quiz3);
        entityManager.flush();

        // When
        List<Quiz> quizzes = quizRepository.findAllByLessonIdOrderByOrderIndexAsc(lessonId);

        // Then
        assertThat(quizzes).hasSize(3);
        assertThat(quizzes.get(0).getOrderIndex()).isEqualTo(1);
        assertThat(quizzes.get(0).getQuestion()).isEqualTo("Question 1");
        assertThat(quizzes.get(1).getOrderIndex()).isEqualTo(2);
        assertThat(quizzes.get(1).getQuestion()).isEqualTo("Question 2");
        assertThat(quizzes.get(2).getOrderIndex()).isEqualTo(3);
        assertThat(quizzes.get(2).getQuestion()).isEqualTo("Question 3");
    }

    @Test
    @DisplayName("Should return empty list when no quizzes found for lessonId")
    void shouldReturnEmptyListWhenNoQuizzesFoundForLessonId() {
        // Given
        Long nonExistentLessonId = 999L;

        // When
        List<Quiz> quizzes = quizRepository.findAllByLessonIdOrderByOrderIndexAsc(nonExistentLessonId);

        // Then
        assertThat(quizzes).isEmpty();
    }

    @Test
    @DisplayName("Should delete quizzes by lessonId")
    void shouldDeleteQuizzesByLessonId() {
        // Given
        Long lessonId = 3L;
        Quiz quiz1 = new Quiz(lessonId, 1, "Q1", true);
        Quiz quiz2 = new Quiz(lessonId, 2, "Q2", false);
        quizRepository.save(quiz1);
        quizRepository.save(quiz2);
        entityManager.flush();

        // When
        quizRepository.deleteByLessonId(lessonId);
        entityManager.flush();

        // Then
        List<Quiz> found = quizRepository.findAllByLessonIdOrderByOrderIndexAsc(lessonId);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should delete only matching lessonId quizzes")
    void shouldDeleteOnlyMatchingLessonIdQuizzes() {
        // Given
        Quiz quiz1 = new Quiz(1L, 1, "Q1", true);
        Quiz quiz2 = new Quiz(2L, 1, "Q2", false);
        Quiz quiz3 = new Quiz(1L, 2, "Q3", true);
        quizRepository.save(quiz1);
        quizRepository.save(quiz2);
        quizRepository.save(quiz3);
        entityManager.flush();

        // When
        quizRepository.deleteByLessonId(1L);
        entityManager.flush();

        // Then
        assertThat(quizRepository.findAllByLessonIdOrderByOrderIndexAsc(1L)).isEmpty();
        assertThat(quizRepository.findAllByLessonIdOrderByOrderIndexAsc(2L)).hasSize(1);
    }

    @Test
    @DisplayName("Should save quiz with null question")
    void shouldSaveQuizWithNullQuestion() {
        // Given
        Quiz quiz = new Quiz(1L, 1, null, true);

        // When
        Quiz saved = quizRepository.save(quiz);
        entityManager.flush();

        // Then
        Optional<Quiz> retrieved = quizRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getQuestion()).isNull();
    }

    @Test
    @DisplayName("Should update existing quiz")
    void shouldUpdateExistingQuiz() {
        // Given
        Quiz quiz = new Quiz(1L, 1, "Old question", false);
        Quiz saved = quizRepository.save(quiz);
        entityManager.flush();

        // When
        saved.update("New question", true, 2);
        quizRepository.save(saved);
        entityManager.flush();

        // Then
        Optional<Quiz> updated = quizRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getQuestion()).isEqualTo("New question");
        assertThat(updated.get().isCorrect()).isTrue();
        assertThat(updated.get().getOrderIndex()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle multiple quizzes with same orderIndex for different lessons")
    void shouldHandleMultipleQuizzesWithSameOrderIndexForDifferentLessons() {
        // Given
        Quiz quiz1 = new Quiz(1L, 1, "Lesson 1 Q1", true);
        Quiz quiz2 = new Quiz(2L, 1, "Lesson 2 Q1", false);
        Quiz quiz3 = new Quiz(3L, 1, "Lesson 3 Q1", true);

        // When
        quizRepository.save(quiz1);
        quizRepository.save(quiz2);
        quizRepository.save(quiz3);
        entityManager.flush();

        // Then
        assertThat(quizRepository.findAllByLessonIdOrderByOrderIndexAsc(1L)).hasSize(1);
        assertThat(quizRepository.findAllByLessonIdOrderByOrderIndexAsc(2L)).hasSize(1);
        assertThat(quizRepository.findAllByLessonIdOrderByOrderIndexAsc(3L)).hasSize(1);
    }

    @Test
    @DisplayName("Should save quiz with negative orderIndex")
    void shouldSaveQuizWithNegativeOrderIndex() {
        // Given
        Quiz quiz = new Quiz(1L, -1, "Question", true);

        // When
        Quiz saved = quizRepository.save(quiz);
        entityManager.flush();

        // Then
        Optional<Quiz> retrieved = quizRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getOrderIndex()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Should handle orderIndex sorting with negative values")
    void shouldHandleOrderIndexSortingWithNegativeValues() {
        // Given
        Long lessonId = 1L;
        Quiz quiz1 = new Quiz(lessonId, 5, "Q5", true);
        Quiz quiz2 = new Quiz(lessonId, -1, "Q-1", false);
        Quiz quiz3 = new Quiz(lessonId, 0, "Q0", true);
        
        quizRepository.save(quiz1);
        quizRepository.save(quiz2);
        quizRepository.save(quiz3);
        entityManager.flush();

        // When
        List<Quiz> quizzes = quizRepository.findAllByLessonIdOrderByOrderIndexAsc(lessonId);

        // Then
        assertThat(quizzes).hasSize(3);
        assertThat(quizzes.get(0).getOrderIndex()).isEqualTo(-1);
        assertThat(quizzes.get(1).getOrderIndex()).isEqualTo(0);
        assertThat(quizzes.get(2).getOrderIndex()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should save quiz with very long question")
    void shouldSaveQuizWithVeryLongQuestion() {
        // Given
        String longQuestion = "Q: " + "a".repeat(500);
        Quiz quiz = new Quiz(1L, 1, longQuestion, true);

        // When
        Quiz saved = quizRepository.save(quiz);
        entityManager.flush();

        // Then
        Optional<Quiz> retrieved = quizRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getQuestion()).hasSize(longQuestion.length());
    }

    @Test
    @DisplayName("Should handle deleteByLessonId for non-existent lessonId")
    void shouldHandleDeleteByLessonIdForNonExistentLessonId() {
        // Given
        Long nonExistentLessonId = 999L;

        // When & Then - should not throw exception
        quizRepository.deleteByLessonId(nonExistentLessonId);
        entityManager.flush();
    }

    @Test
    @DisplayName("Should save quiz with empty string question")
    void shouldSaveQuizWithEmptyStringQuestion() {
        // Given
        Quiz quiz = new Quiz(1L, 1, "", true);

        // When
        Quiz saved = quizRepository.save(quiz);
        entityManager.flush();

        // Then
        Optional<Quiz> retrieved = quizRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getQuestion()).isEmpty();
    }

    @Test
    @DisplayName("Should allow duplicate orderIndex values for same lesson")
    void shouldAllowDuplicateOrderIndexValuesForSameLesson() {
        // Given
        Long lessonId = 1L;
        Quiz quiz1 = new Quiz(lessonId, 1, "Q1", true);
        Quiz quiz2 = new Quiz(lessonId, 1, "Q2", false);

        // When
        quizRepository.save(quiz1);
        quizRepository.save(quiz2);
        entityManager.flush();

        // Then
        List<Quiz> quizzes = quizRepository.findAllByLessonIdOrderByOrderIndexAsc(lessonId);
        assertThat(quizzes).hasSize(2);
        assertThat(quizzes.get(0).getOrderIndex()).isEqualTo(1);
        assertThat(quizzes.get(1).getOrderIndex()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find quizzes with zero orderIndex")
    void shouldFindQuizzesWithZeroOrderIndex() {
        // Given
        Long lessonId = 1L;
        Quiz quiz = new Quiz(lessonId, 0, "Question", true);
        quizRepository.save(quiz);
        entityManager.flush();

        // When
        List<Quiz> quizzes = quizRepository.findAllByLessonIdOrderByOrderIndexAsc(lessonId);

        // Then
        assertThat(quizzes).hasSize(1);
        assertThat(quizzes.get(0).getOrderIndex()).isZero();
    }
}