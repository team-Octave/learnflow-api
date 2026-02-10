package com.teamexp.learnflowapi.content.model;

import com.teamexp.learnflowapi.content.exception.MediaAlreadyBoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContentMediaTest {

    private ContentMedia pendingMedia(String fileName) {
        String fileKey = "videos/" + UUID.randomUUID() + "_" + fileName;
        return ContentMedia.createPending(fileKey, fileName);
    }

    @Test
    @DisplayName("ContentMedia 생성 - createPending 기본값")
    void tc18_content_media_create_pending_default() {
        String fileName = "media.mp4";
        ContentMedia media = pendingMedia(fileName);

        assertThat(media.getStatus()).isEqualTo(MediaStatus.PENDING);
        assertThat(media.getLessonId()).isNull();
        assertThat(media.getFileName()).isEqualTo(fileName);
        assertThat(media.getDurationSec()).isNull();

        assertThat(media.getFileKey()).startsWith("videos/").contains("media.mp4");
    }

    @Test
    @DisplayName("업로드 완료 처리")
    void tc19_completedUpload_success() {
        ContentMedia media = pendingMedia("media.mp4");

        int durationSec = 4000;
        media.completeUpload(durationSec);

        assertThat(media.getStatus()).isEqualTo(MediaStatus.COMPLETED);
        assertThat(media.getDurationSec()).isEqualTo(durationSec);
    }

    @Test
    @DisplayName("레슨 연결 성공 - 최초 attach")
    void tc20_attachToLesson_initial_success() {
        ContentMedia media = pendingMedia("media.mp4");

        Long lessonId = 1234L;
        media.attachToLesson(lessonId);

        assertThat(media.getLessonId()).isEqualTo(lessonId);
    }

    @Test
    @DisplayName("레슨 연결 - 같은 레슨 재호출")
    void tc21_attachToLesson_same_lesson_success() {
        ContentMedia media = pendingMedia("media.mp4");

        Long lessonId = 1234L;
        media.attachToLesson(lessonId);
        media.attachToLesson(lessonId); // 같은 레슨 재호출

        assertThatCode(() -> media.attachToLesson(lessonId)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("레슨 연결 실패 - 다른 레슨")
    void tc22_attachToLesson_different_lesson_failure() {
        ContentMedia media = pendingMedia("media.mp4");

        Long lessonId1 = 1234L;
        Long lessonId2 = 5678L;

        media.attachToLesson(lessonId1);

        assertThatThrownBy(() -> media.attachToLesson(lessonId2))
                .isInstanceOf(MediaAlreadyBoundException.class);

        // 기존 레슨Id가 변경되지 않음을 확인
        assertThat(media.getLessonId()).isEqualTo(lessonId1);
    }

}
