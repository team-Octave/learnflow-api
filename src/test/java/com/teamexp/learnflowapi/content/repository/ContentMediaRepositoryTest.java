package com.teamexp.learnflowapi.content.repository;

import com.teamexp.learnflowapi.content.model.ContentMedia;
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
@DisplayName("ContentMediaRepository Tests")
class ContentMediaRepositoryTest {

    @Autowired
    private ContentMediaRepository contentMediaRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        contentMediaRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save and retrieve ContentMedia")
    void shouldSaveAndRetrieveContentMedia() {
        // Given
        ContentMedia contentMedia = new ContentMedia(1L, "videos/test.mp4", 300);

        // When
        ContentMedia saved = contentMediaRepository.save(contentMedia);
        entityManager.flush();
        Optional<ContentMedia> retrieved = contentMediaRepository.findById(saved.getId());

        // Then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getLessonId()).isEqualTo(1L);
        assertThat(retrieved.get().getFileKey()).isEqualTo("videos/test.mp4");
        assertThat(retrieved.get().getDurationSec()).isEqualTo(300);
    }

    @Test
    @DisplayName("Should find ContentMedia by lessonId")
    void shouldFindContentMediaByLessonId() {
        // Given
        Long lessonId = 5L;
        ContentMedia contentMedia = new ContentMedia(lessonId, "videos/lesson-5.mp4", 600);
        contentMediaRepository.save(contentMedia);
        entityManager.flush();

        // When
        Optional<ContentMedia> found = contentMediaRepository.findByLessonId(lessonId);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getLessonId()).isEqualTo(lessonId);
        assertThat(found.get().getFileKey()).isEqualTo("videos/lesson-5.mp4");
    }

    @Test
    @DisplayName("Should return empty when lessonId not found")
    void shouldReturnEmptyWhenLessonIdNotFound() {
        // Given
        Long nonExistentLessonId = 999L;

        // When
        Optional<ContentMedia> found = contentMediaRepository.findByLessonId(nonExistentLessonId);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should enforce unique constraint on lessonId")
    void shouldEnforceUniqueConstraintOnLessonId() {
        // Given
        Long lessonId = 1L;
        ContentMedia contentMedia1 = new ContentMedia(lessonId, "videos/v1.mp4", 100);
        ContentMedia contentMedia2 = new ContentMedia(lessonId, "videos/v2.mp4", 200);

        contentMediaRepository.save(contentMedia1);
        entityManager.flush();

        // When & Then
        assertThatThrownBy(() -> {
            contentMediaRepository.save(contentMedia2);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should delete ContentMedia by lessonId")
    void shouldDeleteContentMediaByLessonId() {
        // Given
        Long lessonId = 3L;
        ContentMedia contentMedia = new ContentMedia(lessonId, "videos/test.mp4", 300);
        contentMediaRepository.save(contentMedia);
        entityManager.flush();

        // When
        contentMediaRepository.deleteByLessonId(lessonId);
        entityManager.flush();

        // Then
        Optional<ContentMedia> found = contentMediaRepository.findByLessonId(lessonId);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should delete only matching lessonId")
    void shouldDeleteOnlyMatchingLessonId() {
        // Given
        ContentMedia contentMedia1 = new ContentMedia(1L, "videos/v1.mp4", 100);
        ContentMedia contentMedia2 = new ContentMedia(2L, "videos/v2.mp4", 200);
        contentMediaRepository.save(contentMedia1);
        contentMediaRepository.save(contentMedia2);
        entityManager.flush();

        // When
        contentMediaRepository.deleteByLessonId(1L);
        entityManager.flush();

        // Then
        assertThat(contentMediaRepository.findByLessonId(1L)).isEmpty();
        assertThat(contentMediaRepository.findByLessonId(2L)).isPresent();
    }

    @Test
    @DisplayName("Should save ContentMedia with null durationSec")
    void shouldSaveContentMediaWithNullDurationSec() {
        // Given
        ContentMedia contentMedia = new ContentMedia(1L, "videos/test.mp4", null);

        // When
        ContentMedia saved = contentMediaRepository.save(contentMedia);
        entityManager.flush();

        // Then
        Optional<ContentMedia> retrieved = contentMediaRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getDurationSec()).isNull();
    }

    @Test
    @DisplayName("Should update existing ContentMedia")
    void shouldUpdateExistingContentMedia() {
        // Given
        ContentMedia contentMedia = new ContentMedia(1L, "videos/old.mp4", 100);
        ContentMedia saved = contentMediaRepository.save(contentMedia);
        entityManager.flush();

        // When
        saved.changeFile("videos/new.mp4", 200);
        contentMediaRepository.save(saved);
        entityManager.flush();

        // Then
        Optional<ContentMedia> updated = contentMediaRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getFileKey()).isEqualTo("videos/new.mp4");
        assertThat(updated.get().getDurationSec()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should handle multiple ContentMedia for different lessons")
    void shouldHandleMultipleContentMediaForDifferentLessons() {
        // Given
        ContentMedia cm1 = new ContentMedia(1L, "videos/lesson-1.mp4", 100);
        ContentMedia cm2 = new ContentMedia(2L, "videos/lesson-2.mp4", 200);
        ContentMedia cm3 = new ContentMedia(3L, "videos/lesson-3.mp4", 300);

        // When
        contentMediaRepository.save(cm1);
        contentMediaRepository.save(cm2);
        contentMediaRepository.save(cm3);
        entityManager.flush();

        // Then
        assertThat(contentMediaRepository.count()).isEqualTo(3);
        assertThat(contentMediaRepository.findByLessonId(1L)).isPresent();
        assertThat(contentMediaRepository.findByLessonId(2L)).isPresent();
        assertThat(contentMediaRepository.findByLessonId(3L)).isPresent();
    }

    @Test
    @DisplayName("Should save ContentMedia with zero duration")
    void shouldSaveContentMediaWithZeroDuration() {
        // Given
        ContentMedia contentMedia = new ContentMedia(1L, "videos/test.mp4", 0);

        // When
        ContentMedia saved = contentMediaRepository.save(contentMedia);
        entityManager.flush();

        // Then
        Optional<ContentMedia> retrieved = contentMediaRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getDurationSec()).isZero();
    }

    @Test
    @DisplayName("Should save ContentMedia with large duration")
    void shouldSaveContentMediaWithLargeDuration() {
        // Given
        ContentMedia contentMedia = new ContentMedia(1L, "videos/test.mp4", Integer.MAX_VALUE);

        // When
        ContentMedia saved = contentMediaRepository.save(contentMedia);
        entityManager.flush();

        // Then
        Optional<ContentMedia> retrieved = contentMediaRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getDurationSec()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    @DisplayName("Should handle deleteByLessonId for non-existent lessonId")
    void shouldHandleDeleteByLessonIdForNonExistentLessonId() {
        // Given
        Long nonExistentLessonId = 999L;

        // When & Then - should not throw exception
        contentMediaRepository.deleteByLessonId(nonExistentLessonId);
        entityManager.flush();
    }

    @Test
    @DisplayName("Should save ContentMedia with very long fileKey")
    void shouldSaveContentMediaWithVeryLongFileKey() {
        // Given
        String longFileKey = "videos/" + "a".repeat(200) + ".mp4";
        ContentMedia contentMedia = new ContentMedia(1L, longFileKey, 100);

        // When
        ContentMedia saved = contentMediaRepository.save(contentMedia);
        entityManager.flush();

        // Then
        Optional<ContentMedia> retrieved = contentMediaRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getFileKey()).isEqualTo(longFileKey);
    }
}