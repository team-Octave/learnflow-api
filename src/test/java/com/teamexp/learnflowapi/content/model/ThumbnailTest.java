package com.teamexp.learnflowapi.content.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Thumbnail Model Tests")
class ThumbnailTest {

    @Test
    @DisplayName("Should create Thumbnail with valid parameters")
    void shouldCreateThumbnailWithValidParameters() {
        // Given
        Long lectureId = 1L;
        String fileKey = "thumbnails/lecture-1-thumb.jpg";

        // When
        Thumbnail thumbnail = new Thumbnail(lectureId, fileKey);

        // Then
        assertThat(thumbnail).isNotNull();
        assertThat(thumbnail.getLectureId()).isEqualTo(lectureId);
        assertThat(thumbnail.getFileKey()).isEqualTo(fileKey);
        assertThat(thumbnail.getId()).isNull(); // ID not set until persisted
    }

    @Test
    @DisplayName("Should create Thumbnail with null lectureId")
    void shouldCreateThumbnailWithNullLectureId() {
        // Given
        Long lectureId = null;
        String fileKey = "thumbnails/test.jpg";

        // When
        Thumbnail thumbnail = new Thumbnail(lectureId, fileKey);

        // Then
        assertThat(thumbnail.getLectureId()).isNull();
        assertThat(thumbnail.getFileKey()).isEqualTo(fileKey);
    }

    @Test
    @DisplayName("Should create Thumbnail with null fileKey")
    void shouldCreateThumbnailWithNullFileKey() {
        // Given
        Long lectureId = 1L;
        String fileKey = null;

        // When
        Thumbnail thumbnail = new Thumbnail(lectureId, fileKey);

        // Then
        assertThat(thumbnail.getLectureId()).isEqualTo(lectureId);
        assertThat(thumbnail.getFileKey()).isNull();
    }

    @Test
    @DisplayName("Should create Thumbnail with both null parameters")
    void shouldCreateThumbnailWithBothNullParameters() {
        // Given
        Long lectureId = null;
        String fileKey = null;

        // When
        Thumbnail thumbnail = new Thumbnail(lectureId, fileKey);

        // Then
        assertThat(thumbnail.getLectureId()).isNull();
        assertThat(thumbnail.getFileKey()).isNull();
    }

    @Test
    @DisplayName("Should change fileKey to new value")
    void shouldChangeFileKeyToNewValue() {
        // Given
        Thumbnail thumbnail = new Thumbnail(1L, "thumbnails/old.jpg");
        String newFileKey = "thumbnails/new.jpg";

        // When
        thumbnail.changeFileKey(newFileKey);

        // Then
        assertThat(thumbnail.getFileKey()).isEqualTo(newFileKey);
        assertThat(thumbnail.getLectureId()).isEqualTo(1L); // lectureId should remain unchanged
    }

    @Test
    @DisplayName("Should change fileKey to null")
    void shouldChangeFileKeyToNull() {
        // Given
        Thumbnail thumbnail = new Thumbnail(1L, "thumbnails/old.jpg");

        // When
        thumbnail.changeFileKey(null);

        // Then
        assertThat(thumbnail.getFileKey()).isNull();
        assertThat(thumbnail.getLectureId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should maintain lectureId after multiple fileKey changes")
    void shouldMaintainLectureIdAfterMultipleFileKeyChanges() {
        // Given
        Long originalLectureId = 5L;
        Thumbnail thumbnail = new Thumbnail(originalLectureId, "thumbnails/v1.jpg");

        // When
        thumbnail.changeFileKey("thumbnails/v2.jpg");
        thumbnail.changeFileKey("thumbnails/v3.jpg");
        thumbnail.changeFileKey("thumbnails/v4.jpg");

        // Then
        assertThat(thumbnail.getLectureId()).isEqualTo(originalLectureId);
        assertThat(thumbnail.getFileKey()).isEqualTo("thumbnails/v4.jpg");
    }

    @Test
    @DisplayName("Should handle empty string fileKey")
    void shouldHandleEmptyStringFileKey() {
        // Given
        String emptyFileKey = "";

        // When
        Thumbnail thumbnail = new Thumbnail(1L, emptyFileKey);

        // Then
        assertThat(thumbnail.getFileKey()).isEqualTo("");
        assertThat(thumbnail.getFileKey()).isEmpty();
    }

    @Test
    @DisplayName("Should handle very long fileKey")
    void shouldHandleVeryLongFileKey() {
        // Given
        String longFileKey = "thumbnails/" + "a".repeat(1000) + ".jpg";

        // When
        Thumbnail thumbnail = new Thumbnail(1L, longFileKey);

        // Then
        assertThat(thumbnail.getFileKey()).isEqualTo(longFileKey);
        assertThat(thumbnail.getFileKey()).hasSize(1015);
    }

    @Test
    @DisplayName("Should handle fileKey with special characters")
    void shouldHandleFileKeyWithSpecialCharacters() {
        // Given
        String specialFileKey = "thumbnails/lecture-1_intro@2024!.jpg";

        // When
        Thumbnail thumbnail = new Thumbnail(1L, specialFileKey);

        // Then
        assertThat(thumbnail.getFileKey()).isEqualTo(specialFileKey);
    }

    @Test
    @DisplayName("Should handle fileKey with different image extensions")
    void shouldHandleFileKeyWithDifferentImageExtensions() {
        // Given & When & Then
        assertThat(new Thumbnail(1L, "thumbnails/img.jpg").getFileKey()).endsWith(".jpg");
        assertThat(new Thumbnail(1L, "thumbnails/img.png").getFileKey()).endsWith(".png");
        assertThat(new Thumbnail(1L, "thumbnails/img.gif").getFileKey()).endsWith(".gif");
        assertThat(new Thumbnail(1L, "thumbnails/img.webp").getFileKey()).endsWith(".webp");
    }

    @Test
    @DisplayName("Should handle fileKey with path separators")
    void shouldHandleFileKeyWithPathSeparators() {
        // Given
        String pathWithSeparators = "thumbnails/2024/12/lecture-1.jpg";

        // When
        Thumbnail thumbnail = new Thumbnail(1L, pathWithSeparators);

        // Then
        assertThat(thumbnail.getFileKey()).isEqualTo(pathWithSeparators);
        assertThat(thumbnail.getFileKey()).contains("/");
    }

    @Test
    @DisplayName("Should handle fileKey with unicode characters")
    void shouldHandleFileKeyWithUnicodeCharacters() {
        // Given
        String unicodeFileKey = "thumbnails/강의-1.jpg";

        // When
        Thumbnail thumbnail = new Thumbnail(1L, unicodeFileKey);

        // Then
        assertThat(thumbnail.getFileKey()).isEqualTo(unicodeFileKey);
    }

    @Test
    @DisplayName("Should create Thumbnail with large lectureId")
    void shouldCreateThumbnailWithLargeLectureId() {
        // Given
        Long largeLectureId = Long.MAX_VALUE;

        // When
        Thumbnail thumbnail = new Thumbnail(largeLectureId, "thumbnails/test.jpg");

        // Then
        assertThat(thumbnail.getLectureId()).isEqualTo(largeLectureId);
    }

    @Test
    @DisplayName("Should create Thumbnail with zero lectureId")
    void shouldCreateThumbnailWithZeroLectureId() {
        // Given
        Long zeroLectureId = 0L;

        // When
        Thumbnail thumbnail = new Thumbnail(zeroLectureId, "thumbnails/test.jpg");

        // Then
        assertThat(thumbnail.getLectureId()).isZero();
    }

    @Test
    @DisplayName("Should create Thumbnail with negative lectureId")
    void shouldCreateThumbnailWithNegativeLectureId() {
        // Given
        Long negativeLectureId = -1L;

        // When
        Thumbnail thumbnail = new Thumbnail(negativeLectureId, "thumbnails/test.jpg");

        // Then
        assertThat(thumbnail.getLectureId()).isEqualTo(-1L);
    }

    @Test
    @DisplayName("Should allow consecutive changeFileKey operations")
    void shouldAllowConsecutiveChangeFileKeyOperations() {
        // Given
        Thumbnail thumbnail = new Thumbnail(1L, "thumbnails/v1.jpg");

        // When
        thumbnail.changeFileKey("thumbnails/v2.jpg");
        thumbnail.changeFileKey("thumbnails/v3.jpg");
        thumbnail.changeFileKey("thumbnails/v4.jpg");
        thumbnail.changeFileKey("thumbnails/v5.jpg");

        // Then
        assertThat(thumbnail.getFileKey()).isEqualTo("thumbnails/v5.jpg");
    }

    @Test
    @DisplayName("Should change fileKey with same value as current")
    void shouldChangeFileKeyWithSameValueAsCurrent() {
        // Given
        String fileKey = "thumbnails/same.jpg";
        Thumbnail thumbnail = new Thumbnail(1L, fileKey);

        // When
        thumbnail.changeFileKey(fileKey);

        // Then
        assertThat(thumbnail.getFileKey()).isEqualTo(fileKey);
    }

    @Test
    @DisplayName("Should handle whitespace in fileKey")
    void shouldHandleWhitespaceInFileKey() {
        // Given
        String fileKeyWithSpaces = "thumbnails/my file.jpg";

        // When
        Thumbnail thumbnail = new Thumbnail(1L, fileKeyWithSpaces);

        // Then
        assertThat(thumbnail.getFileKey()).isEqualTo(fileKeyWithSpaces);
        assertThat(thumbnail.getFileKey()).contains(" ");
    }
}