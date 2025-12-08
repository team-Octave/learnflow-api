package com.teamexp.learnflowapi.content.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Quiz Model Tests")
class QuizTest {

    @Test
    @DisplayName("Should create Quiz with valid parameters")
    void shouldCreateQuizWithValidParameters() {
        // Given
        Long lessonId = 1L;
        int orderIndex = 1;
        String question = "What is Java?";
        boolean correct = true;

        // When
        Quiz quiz = new Quiz(lessonId, orderIndex, question, correct);

        // Then
        assertThat(quiz).isNotNull();
        assertThat(quiz.getLessonId()).isEqualTo(lessonId);
        assertThat(quiz.getOrderIndex()).isEqualTo(orderIndex);
        assertThat(quiz.getQuestion()).isEqualTo(question);
        assertThat(quiz.isCorrect()).isTrue();
        assertThat(quiz.getId()).isNull(); // ID not set until persisted
    }

    @Test
    @DisplayName("Should create Quiz with correct flag as false")
    void shouldCreateQuizWithCorrectFlagAsFalse() {
        // Given
        Long lessonId = 1L;
        int orderIndex = 1;
        String question = "Is this wrong?";
        boolean correct = false;

        // When
        Quiz quiz = new Quiz(lessonId, orderIndex, question, correct);

        // Then
        assertThat(quiz.isCorrect()).isFalse();
    }

    @Test
    @DisplayName("Should create Quiz with null lessonId")
    void shouldCreateQuizWithNullLessonId() {
        // Given
        Long lessonId = null;
        int orderIndex = 1;
        String question = "Test question";
        boolean correct = true;

        // When
        Quiz quiz = new Quiz(lessonId, orderIndex, question, correct);

        // Then
        assertThat(quiz.getLessonId()).isNull();
        assertThat(quiz.getOrderIndex()).isEqualTo(orderIndex);
        assertThat(quiz.getQuestion()).isEqualTo(question);
    }

    @Test
    @DisplayName("Should create Quiz with null question")
    void shouldCreateQuizWithNullQuestion() {
        // Given
        Long lessonId = 1L;
        int orderIndex = 1;
        String question = null;
        boolean correct = true;

        // When
        Quiz quiz = new Quiz(lessonId, orderIndex, question, correct);

        // Then
        assertThat(quiz.getQuestion()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 10, 100, Integer.MAX_VALUE})
    @DisplayName("Should accept various valid orderIndex values")
    void shouldAcceptVariousValidOrderIndexValues(int orderIndex) {
        // Given
        Long lessonId = 1L;
        String question = "Test question";
        boolean correct = true;

        // When
        Quiz quiz = new Quiz(lessonId, orderIndex, question, correct);

        // Then
        assertThat(quiz.getOrderIndex()).isEqualTo(orderIndex);
    }

    @Test
    @DisplayName("Should accept negative orderIndex")
    void shouldAcceptNegativeOrderIndex() {
        // Given
        int negativeIndex = -1;

        // When
        Quiz quiz = new Quiz(1L, negativeIndex, "Test", true);

        // Then
        assertThat(quiz.getOrderIndex()).isEqualTo(negativeIndex);
    }

    @Test
    @DisplayName("Should update quiz with new values")
    void shouldUpdateQuizWithNewValues() {
        // Given
        Quiz quiz = new Quiz(1L, 1, "Old question", false);
        String newQuestion = "New question";
        boolean newCorrect = true;
        int newOrderIndex = 2;

        // When
        quiz.update(newQuestion, newCorrect, newOrderIndex);

        // Then
        assertThat(quiz.getQuestion()).isEqualTo(newQuestion);
        assertThat(quiz.isCorrect()).isTrue();
        assertThat(quiz.getOrderIndex()).isEqualTo(newOrderIndex);
        assertThat(quiz.getLessonId()).isEqualTo(1L); // lessonId should remain unchanged
    }

    @Test
    @DisplayName("Should update quiz with null question")
    void shouldUpdateQuizWithNullQuestion() {
        // Given
        Quiz quiz = new Quiz(1L, 1, "Old question", true);

        // When
        quiz.update(null, false, 2);

        // Then
        assertThat(quiz.getQuestion()).isNull();
        assertThat(quiz.isCorrect()).isFalse();
        assertThat(quiz.getOrderIndex()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should toggle correct flag through update")
    void shouldToggleCorrectFlagThroughUpdate() {
        // Given
        Quiz quiz = new Quiz(1L, 1, "Question", true);

        // When
        quiz.update("Question", false, 1);

        // Then
        assertThat(quiz.isCorrect()).isFalse();

        // When
        quiz.update("Question", true, 1);

        // Then
        assertThat(quiz.isCorrect()).isTrue();
    }

    @Test
    @DisplayName("Should maintain lessonId after multiple updates")
    void shouldMaintainLessonIdAfterMultipleUpdates() {
        // Given
        Long originalLessonId = 5L;
        Quiz quiz = new Quiz(originalLessonId, 1, "Q1", true);

        // When
        quiz.update("Q2", false, 2);
        quiz.update("Q3", true, 3);
        quiz.update("Q4", false, 4);

        // Then
        assertThat(quiz.getLessonId()).isEqualTo(originalLessonId);
    }

    @ParameterizedTest
    @CsvSource({
        "0, true",
        "1, false",
        "-1, true",
        "100, false",
        "999, true"
    })
    @DisplayName("Should update with various orderIndex and correct combinations")
    void shouldUpdateWithVariousOrderIndexAndCorrectCombinations(int orderIndex, boolean correct) {
        // Given
        Quiz quiz = new Quiz(1L, 0, "Initial", false);

        // When
        quiz.update("Updated", correct, orderIndex);

        // Then
        assertThat(quiz.getOrderIndex()).isEqualTo(orderIndex);
        assertThat(quiz.isCorrect()).isEqualTo(correct);
    }

    @Test
    @DisplayName("Should handle empty string question")
    void shouldHandleEmptyStringQuestion() {
        // Given
        String emptyQuestion = "";

        // When
        Quiz quiz = new Quiz(1L, 1, emptyQuestion, true);

        // Then
        assertThat(quiz.getQuestion()).isEqualTo("");
        assertThat(quiz.getQuestion()).isEmpty();
    }

    @Test
    @DisplayName("Should handle very long question")
    void shouldHandleVeryLongQuestion() {
        // Given
        String longQuestion = "Q: " + "a".repeat(1000);

        // When
        Quiz quiz = new Quiz(1L, 1, longQuestion, true);

        // Then
        assertThat(quiz.getQuestion()).isEqualTo(longQuestion);
        assertThat(quiz.getQuestion()).hasSize(1003);
    }

    @Test
    @DisplayName("Should handle question with special characters")
    void shouldHandleQuestionWithSpecialCharacters() {
        // Given
        String specialQuestion = "What is 2+2? (Answer: 4!) #math @test";

        // When
        Quiz quiz = new Quiz(1L, 1, specialQuestion, true);

        // Then
        assertThat(quiz.getQuestion()).isEqualTo(specialQuestion);
    }

    @Test
    @DisplayName("Should handle question with line breaks")
    void shouldHandleQuestionWithLineBreaks() {
        // Given
        String multilineQuestion = "Line 1\nLine 2\nLine 3";

        // When
        Quiz quiz = new Quiz(1L, 1, multilineQuestion, true);

        // Then
        assertThat(quiz.getQuestion()).isEqualTo(multilineQuestion);
        assertThat(quiz.getQuestion()).contains("\n");
    }

    @Test
    @DisplayName("Should handle question with unicode characters")
    void shouldHandleQuestionWithUnicodeCharacters() {
        // Given
        String unicodeQuestion = "¿Qué es Java? 你好 😀";

        // When
        Quiz quiz = new Quiz(1L, 1, unicodeQuestion, true);

        // Then
        assertThat(quiz.getQuestion()).isEqualTo(unicodeQuestion);
    }

    @Test
    @DisplayName("Should handle whitespace-only question")
    void shouldHandleWhitespaceOnlyQuestion() {
        // Given
        String whitespaceQuestion = "   ";

        // When
        Quiz quiz = new Quiz(1L, 1, whitespaceQuestion, true);

        // Then
        assertThat(quiz.getQuestion()).isEqualTo(whitespaceQuestion);
        assertThat(quiz.getQuestion()).isBlank();
    }

    @Test
    @DisplayName("Should create Quiz with large lessonId")
    void shouldCreateQuizWithLargeLessonId() {
        // Given
        Long largeLessonId = Long.MAX_VALUE;

        // When
        Quiz quiz = new Quiz(largeLessonId, 1, "Question", true);

        // Then
        assertThat(quiz.getLessonId()).isEqualTo(largeLessonId);
    }

    @Test
    @DisplayName("Should allow consecutive update operations")
    void shouldAllowConsecutiveUpdateOperations() {
        // Given
        Quiz quiz = new Quiz(1L, 1, "Q1", true);

        // When
        quiz.update("Q2", false, 2);
        quiz.update("Q3", true, 3);
        quiz.update("Q4", false, 4);
        quiz.update("Q5", true, 5);

        // Then
        assertThat(quiz.getQuestion()).isEqualTo("Q5");
        assertThat(quiz.isCorrect()).isTrue();
        assertThat(quiz.getOrderIndex()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should update with same values as current")
    void shouldUpdateWithSameValuesAsCurrent() {
        // Given
        String question = "Same question";
        boolean correct = true;
        int orderIndex = 5;
        Quiz quiz = new Quiz(1L, orderIndex, question, correct);

        // When
        quiz.update(question, correct, orderIndex);

        // Then
        assertThat(quiz.getQuestion()).isEqualTo(question);
        assertThat(quiz.isCorrect()).isTrue();
        assertThat(quiz.getOrderIndex()).isEqualTo(orderIndex);
    }
}