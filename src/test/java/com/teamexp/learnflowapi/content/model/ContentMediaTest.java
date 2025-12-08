package com.teamexp.learnflowapi.content.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ContentMedia Model Tests")
class ContentMediaTest {

    @Test
    @DisplayName("Should create ContentMedia with valid parameters")
    void shouldCreateContentMediaWithValidParameters() {
        // Given
        Long lessonId = 1L;
        String fileKey = "videos/lesson-1-intro.mp4";
        Integer durationSec = 300;

        // When
        ContentMedia contentMedia = new ContentMedia(lessonId, fileKey, durationSec);

        // Then
        assertThat(contentMedia).isNotNull();
        assertThat(contentMedia.getLessonId()).isEqualTo(lessonId);
        assertThat(contentMedia.getFileKey()).isEqualTo(fileKey);
        assertThat(contentMedia.getDurationSec()).isEqualTo(durationSec);
        assertThat(contentMedia.getId()).isNull(); // ID not set until persisted
    }

    @Test
    @DisplayName("Should create ContentMedia with null lessonId")
    void shouldCreateContentMediaWithNullLessonId() {
        // Given
        Long lessonId = null;
        String fileKey = "videos/test.mp4";
        Integer durationSec = 120;

        // When
        ContentMedia contentMedia = new ContentMedia(lessonId, fileKey, durationSec);

        // Then
        assertThat(contentMedia).isNotNull();
        assertThat(contentMedia.getLessonId()).isNull();
        assertThat(contentMedia.getFileKey()).isEqualTo(fileKey);
        assertThat(contentMedia.getDurationSec()).isEqualTo(durationSec);
    }

    @Test
    @DisplayName("Should create ContentMedia with null fileKey")
    void shouldCreateContentMediaWithNullFileKey() {
        // Given
        Long lessonId = 1L;
        String fileKey = null;
        Integer durationSec = 120;

        // When
        ContentMedia contentMedia = new ContentMedia(lessonId, fileKey, durationSec);

        // Then
        assertThat(contentMedia).isNotNull();
        assertThat(contentMedia.getLessonId()).isEqualTo(lessonId);
        assertThat(contentMedia.getFileKey()).isNull();
        assertThat(contentMedia.getDurationSec()).isEqualTo(durationSec);
    }

    @Test
    @DisplayName("Should create ContentMedia with null durationSec")
    void shouldCreateContentMediaWithNullDurationSec() {
        // Given
        Long lessonId = 1L;
        String fileKey = "videos/test.mp4";
        Integer durationSec = null;

        // When
        ContentMedia contentMedia = new ContentMedia(lessonId, fileKey, durationSec);

        // Then
        assertThat(contentMedia).isNotNull();
        assertThat(contentMedia.getLessonId()).isEqualTo(lessonId);
        assertThat(contentMedia.getFileKey()).isEqualTo(fileKey);
        assertThat(contentMedia.getDurationSec()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 60, 3600, 7200, Integer.MAX_VALUE})
    @DisplayName("Should accept various valid duration values")
    void shouldAcceptVariousValidDurationValues(int duration) {
        // Given
        Long lessonId = 1L;
        String fileKey = "videos/test.mp4";

        // When
        ContentMedia contentMedia = new ContentMedia(lessonId, fileKey, duration);

        // Then
        assertThat(contentMedia.getDurationSec()).isEqualTo(duration);
    }

    @Test
    @DisplayName("Should accept negative duration value")
    void shouldAcceptNegativeDurationValue() {
        // Given
        Long lessonId = 1L;
        String fileKey = "videos/test.mp4";
        Integer durationSec = -1;

        // When
        ContentMedia contentMedia = new ContentMedia(lessonId, fileKey, durationSec);

        // Then
        assertThat(contentMedia.getDurationSec()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Should change file with new fileKey and duration")
    void shouldChangeFileWithNewFileKeyAndDuration() {
        // Given
        ContentMedia contentMedia = new ContentMedia(1L, "videos/old.mp4", 100);
        String newFileKey = "videos/new.mp4";
        Integer newDuration = 200;

        // When
        contentMedia.changeFile(newFileKey, newDuration);

        // Then
        assertThat(contentMedia.getFileKey()).isEqualTo(newFileKey);
        assertThat(contentMedia.getDurationSec()).isEqualTo(newDuration);
        assertThat(contentMedia.getLessonId()).isEqualTo(1L); // lessonId should remain unchanged
    }

    @Test
    @DisplayName("Should change file with null fileKey")
    void shouldChangeFileWithNullFileKey() {
        // Given
        ContentMedia contentMedia = new ContentMedia(1L, "videos/old.mp4", 100);

        // When
        contentMedia.changeFile(null, 200);

        // Then
        assertThat(contentMedia.getFileKey()).isNull();
        assertThat(contentMedia.getDurationSec()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should change file with null duration")
    void shouldChangeFileWithNullDuration() {
        // Given
        ContentMedia contentMedia = new ContentMedia(1L, "videos/old.mp4", 100);

        // When
        contentMedia.changeFile("videos/new.mp4", null);

        // Then
        assertThat(contentMedia.getFileKey()).isEqualTo("videos/new.mp4");
        assertThat(contentMedia.getDurationSec()).isNull();
    }

    @Test
    @DisplayName("Should change file with both null values")
    void shouldChangeFileWithBothNullValues() {
        // Given
        ContentMedia contentMedia = new ContentMedia(1L, "videos/old.mp4", 100);

        // When
        contentMedia.changeFile(null, null);

        // Then
        assertThat(contentMedia.getFileKey()).isNull();
        assertThat(contentMedia.getDurationSec()).isNull();
    }

    @Test
    @DisplayName("Should maintain lessonId after multiple file changes")
    void shouldMaintainLessonIdAfterMultipleFileChanges() {
        // Given
        Long originalLessonId = 5L;
        ContentMedia contentMedia = new ContentMedia(originalLessonId, "videos/v1.mp4", 100);

        // When
        contentMedia.changeFile("videos/v2.mp4", 200);
        contentMedia.changeFile("videos/v3.mp4", 300);

        // Then
        assertThat(contentMedia.getLessonId()).isEqualTo(originalLessonId);
    }

    @Test
    @DisplayName("Should handle empty string fileKey")
    void shouldHandleEmptyStringFileKey() {
        // Given
        String emptyFileKey = "";

        // When
        ContentMedia contentMedia = new ContentMedia(1L, emptyFileKey, 100);

        // Then
        assertThat(contentMedia.getFileKey()).isEqualTo("");
        assertThat(contentMedia.getFileKey()).isEmpty();
    }

    @Test
    @DisplayName("Should handle very long fileKey")
    void shouldHandleVeryLongFileKey() {
        // Given
        String longFileKey = "videos/" + "a".repeat(1000) + ".mp4";

        // When
        ContentMedia contentMedia = new ContentMedia(1L, longFileKey, 100);

        // Then
        assertThat(contentMedia.getFileKey()).isEqualTo(longFileKey);
        assertThat(contentMedia.getFileKey()).hasSize(1011);
    }

    @Test
    @DisplayName("Should handle fileKey with special characters")
    void shouldHandleFileKeyWithSpecialCharacters() {
        // Given
        String specialFileKey = "videos/lesson-1_intro@2024!.mp4";

        // When
        ContentMedia contentMedia = new ContentMedia(1L, specialFileKey, 100);

        // Then
        assertThat(contentMedia.getFileKey()).isEqualTo(specialFileKey);
    }

    @Test
    @DisplayName("Should handle zero duration")
    void shouldHandleZeroDuration() {
        // Given
        Integer zeroDuration = 0;

        // When
        ContentMedia contentMedia = new ContentMedia(1L, "videos/test.mp4", zeroDuration);

        // Then
        assertThat(contentMedia.getDurationSec()).isZero();
    }

    @Test
    @DisplayName("Should create ContentMedia with large lessonId")
    void shouldCreateContentMediaWithLargeLessonId() {
        // Given
        Long largeLessonId = Long.MAX_VALUE;

        // When
        ContentMedia contentMedia = new ContentMedia(largeLessonId, "videos/test.mp4", 100);

        // Then
        assertThat(contentMedia.getLessonId()).isEqualTo(largeLessonId);
    }

    @Test
    @DisplayName("Should allow consecutive changeFile operations")
    void shouldAllowConsecutiveChangeFileOperations() {
        // Given
        ContentMedia contentMedia = new ContentMedia(1L, "videos/v1.mp4", 100);

        // When
        contentMedia.changeFile("videos/v2.mp4", 200);
        contentMedia.changeFile("videos/v3.mp4", 300);
        contentMedia.changeFile("videos/v4.mp4", 400);

        // Then
        assertThat(contentMedia.getFileKey()).isEqualTo("videos/v4.mp4");
        assertThat(contentMedia.getDurationSec()).isEqualTo(400);
    }
}