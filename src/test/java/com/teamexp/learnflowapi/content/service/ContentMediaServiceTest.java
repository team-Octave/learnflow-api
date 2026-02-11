package com.teamexp.learnflowapi.content.service;

import com.teamexp.learnflowapi.content.exception.LessonAlreadyBoundToMediaException;
import com.teamexp.learnflowapi.content.external.GcpSignedUrlService;
import com.teamexp.learnflowapi.content.model.ContentMedia;
import com.teamexp.learnflowapi.content.model.MediaStatus;
import com.teamexp.learnflowapi.content.repository.ContentMediaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ContentMediaServiceTest {

    @InjectMocks
    ContentMediaService contentMediaService;

    @Mock
    ContentMediaRepository contentMediaRepository;

    @Mock
    GcpSignedUrlService gcpSignedUrlService;

    @Test
    @DisplayName("ContentMedia 바인딩 실패 - lesson에 이미 다른 media가 바인딩됨")
    void tc36_bindMediaToLesson_fail_lesson_already_bound_to_media() {
        Long lessonId = 123L;
        Long mediaId = 4L;

        ContentMedia contentMedia = mock(ContentMedia.class);
        ContentMedia existingMedia = mock(ContentMedia.class);

        given(contentMediaRepository.findById(mediaId))
                .willReturn(Optional.of(contentMedia));
        given(contentMediaRepository.findByLessonId(lessonId))
                .willReturn(Optional.of(existingMedia));
        given(contentMedia.getStatus()).willReturn(MediaStatus.COMPLETED);

        given(existingMedia.getId()).willReturn(999L);
        given(contentMediaRepository.findByLessonId(lessonId)).willReturn(Optional.of(existingMedia));


        assertThatThrownBy(() -> contentMediaService.bindMediaToLesson(lessonId, mediaId))
                .isInstanceOf(LessonAlreadyBoundToMediaException.class);

        verify(contentMedia, never()).attachToLesson(anyLong());
        verify(contentMediaRepository, never()).flush();
    }

}
