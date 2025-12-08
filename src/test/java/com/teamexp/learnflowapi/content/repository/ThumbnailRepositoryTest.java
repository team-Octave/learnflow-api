package com.teamexp.learnflowapi.content.repository;

import com.teamexp.learnflowapi.content.model.Thumbnail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@DisplayName("ThumbnailRepository Tests")
class ThumbnailRepositoryTest {

    @Autowired
    private ThumbnailRepository thumbnailRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        thumbnailRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save and retrieve Thumbnail")
    void shouldSaveAndRetrieveThumbnail() {
        // Given
        Thumbnail thumbnail = new Thumbnail(1L, "thumbnails/lecture-1.jpg");

        // When
        Thumbnail saved = thumbnailRepository.save(thumbnail);
        entityManager.flush();
        Optional<Thumbnail> retrieved = thumbnailRepository.findById(saved.getId());

        // Then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getLectureId()).isEqualTo(1L);
        assertThat(retrieved.get().getFileKey()).isEqualTo("thumbnails/lecture-1.jpg");
    }

    @Test
    @DisplayName("Should find Thumbnail by lectureId")
    void shouldFindThumbnailByLectureId() {
        // Given
        Long lectureId = 5L;
        Thumbnail thumbnail = new Thumbnail(lectureId, "thumbnails/lecture-5.jpg");
        thumbnailRepository.save(thumbnail);
        entityManager.flush();

        // When
        Optional<Thumbnail> found = thumbnailRepository.findByLectureId(lectureId);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getLectureId()).isEqualTo(lectureId);
        assertThat(found.get().getFileKey()).isEqualTo("thumbnails/lecture-5.jpg");
    }

    @Test
    @DisplayName("Should return empty when lectureId not found")
    void shouldReturnEmptyWhenLectureIdNotFound() {
        // Given
        Long nonExistentLectureId = 999L;

        // When
        Optional<Thumbnail> found = thumbnailRepository.findByLectureId(nonExistentLectureId);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should enforce unique constraint on lectureId")
    void shouldEnforceUniqueConstraintOnLectureId() {
        // Given
        Long lectureId = 1L;
        Thumbnail thumbnail1 = new Thumbnail(lectureId, "thumbnails/v1.jpg");
        Thumbnail thumbnail2 = new Thumbnail(lectureId, "thumbnails/v2.jpg");

        thumbnailRepository.save(thumbnail1);
        entityManager.flush();

        // When & Then
        assertThatThrownBy(() -> {
            thumbnailRepository.save(thumbnail2);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should delete Thumbnail by lectureId")
    void shouldDeleteThumbnailByLectureId() {
        // Given
        Long lectureId = 3L;
        Thumbnail thumbnail = new Thumbnail(lectureId, "thumbnails/lecture-3.jpg");
        thumbnailRepository.save(thumbnail);
        entityManager.flush();

        // When
        thumbnailRepository.deleteByLectureId(lectureId);
        entityManager.flush();

        // Then
        Optional<Thumbnail> found = thumbnailRepository.findByLectureId(lectureId);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should delete only matching lectureId")
    void shouldDeleteOnlyMatchingLectureId() {
        // Given
        Thumbnail thumbnail1 = new Thumbnail(1L, "thumbnails/t1.jpg");
        Thumbnail thumbnail2 = new Thumbnail(2L, "thumbnails/t2.jpg");
        thumbnailRepository.save(thumbnail1);
        thumbnailRepository.save(thumbnail2);
        entityManager.flush();

        // When
        thumbnailRepository.deleteByLectureId(1L);
        entityManager.flush();

        // Then
        assertThat(thumbnailRepository.findByLectureId(1L)).isEmpty();
        assertThat(thumbnailRepository.findByLectureId(2L)).isPresent();
    }

    @Test
    @DisplayName("Should save Thumbnail with null fileKey")
    void shouldSaveThumbnailWithNullFileKey() {
        // Given
        Thumbnail thumbnail = new Thumbnail(1L, null);

        // When
        Thumbnail saved = thumbnailRepository.save(thumbnail);
        entityManager.flush();

        // Then
        Optional<Thumbnail> retrieved = thumbnailRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getFileKey()).isNull();
    }

    @Test
    @DisplayName("Should update existing Thumbnail")
    void shouldUpdateExistingThumbnail() {
        // Given
        Thumbnail thumbnail = new Thumbnail(1L, "thumbnails/old.jpg");
        Thumbnail saved = thumbnailRepository.save(thumbnail);
        entityManager.flush();

        // When
        saved.changeFileKey("thumbnails/new.jpg");
        thumbnailRepository.save(saved);
        entityManager.flush();

        // Then
        Optional<Thumbnail> updated = thumbnailRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getFileKey()).isEqualTo("thumbnails/new.jpg");
    }

    @Test
    @DisplayName("Should handle multiple Thumbnails for different lectures")
    void shouldHandleMultipleThumbnailsForDifferentLectures() {
        // Given
        Thumbnail t1 = new Thumbnail(1L, "thumbnails/lecture-1.jpg");
        Thumbnail t2 = new Thumbnail(2L, "thumbnails/lecture-2.jpg");
        Thumbnail t3 = new Thumbnail(3L, "thumbnails/lecture-3.jpg");

        // When
        thumbnailRepository.save(t1);
        thumbnailRepository.save(t2);
        thumbnailRepository.save(t3);
        entityManager.flush();

        // Then
        assertThat(thumbnailRepository.count()).isEqualTo(3);
        assertThat(thumbnailRepository.findByLectureId(1L)).isPresent();
        assertThat(thumbnailRepository.findByLectureId(2L)).isPresent();
        assertThat(thumbnailRepository.findByLectureId(3L)).isPresent();
    }

    @Test
    @DisplayName("Should handle deleteByLectureId for non-existent lectureId")
    void shouldHandleDeleteByLectureIdForNonExistentLectureId() {
        // Given
        Long nonExistentLectureId = 999L;

        // When & Then - should not throw exception
        thumbnailRepository.deleteByLectureId(nonExistentLectureId);
        entityManager.flush();
    }

    @Test
    @DisplayName("Should save Thumbnail with very long fileKey")
    void shouldSaveThumbnailWithVeryLongFileKey() {
        // Given
        String longFileKey = "thumbnails/" + "a".repeat(200) + ".jpg";
        Thumbnail thumbnail = new Thumbnail(1L, longFileKey);

        // When
        Thumbnail saved = thumbnailRepository.save(thumbnail);
        entityManager.flush();

        // Then
        Optional<Thumbnail> retrieved = thumbnailRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getFileKey()).isEqualTo(longFileKey);
    }

    @Test
    @DisplayName("Should save Thumbnail with empty string fileKey")
    void shouldSaveThumbnailWithEmptyStringFileKey() {
        // Given
        Thumbnail thumbnail = new Thumbnail(1L, "");

        // When
        Thumbnail saved = thumbnailRepository.save(thumbnail);
        entityManager.flush();

        // Then
        Optional<Thumbnail> retrieved = thumbnailRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getFileKey()).isEmpty();
    }

    @Test
    @DisplayName("Should save Thumbnail with special characters in fileKey")
    void shouldSaveThumbnailWithSpecialCharactersInFileKey() {
        // Given
        String specialFileKey = "thumbnails/lecture_1-intro@2024!.jpg";
        Thumbnail thumbnail = new Thumbnail(1L, specialFileKey);

        // When
        Thumbnail saved = thumbnailRepository.save(thumbnail);
        entityManager.flush();

        // Then
        Optional<Thumbnail> retrieved = thumbnailRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getFileKey()).isEqualTo(specialFileKey);
    }

    @Test
    @DisplayName("Should save Thumbnail with different image extensions")
    void shouldSaveThumbnailWithDifferentImageExtensions() {
        // Given & When
        Thumbnail jpg = thumbnailRepository.save(new Thumbnail(1L, "thumbnails/t1.jpg"));
        Thumbnail png = thumbnailRepository.save(new Thumbnail(2L, "thumbnails/t2.png"));
        Thumbnail webp = thumbnailRepository.save(new Thumbnail(3L, "thumbnails/t3.webp"));
        entityManager.flush();

        // Then
        assertThat(thumbnailRepository.findByLectureId(1L).get().getFileKey()).endsWith(".jpg");
        assertThat(thumbnailRepository.findByLectureId(2L).get().getFileKey()).endsWith(".png");
        assertThat(thumbnailRepository.findByLectureId(3L).get().getFileKey()).endsWith(".webp");
    }
}