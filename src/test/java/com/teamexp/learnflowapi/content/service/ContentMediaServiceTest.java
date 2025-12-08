package com.teamexp.learnflowapi.content.service;

import com.teamexp.learnflowapi.content.dto.PresignedUrlResponse;
import com.teamexp.learnflowapi.content.repository.ContentMediaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentMediaService Tests")
class ContentMediaServiceTest {

    @Mock
    private ContentMediaRepository contentMediaRepository;

    @Mock
    private S3PresignedUrlService s3PresignedUrlService;

    private ContentMediaService contentMediaService;

    @BeforeEach
    void setUp() {
        contentMediaService = new ContentMediaService(contentMediaRepository, s3PresignedUrlService);
    }

    @Test
    @DisplayName("Should create video upload URL with valid parameters")
    void shouldCreateVideoUploadUrlWithValidParameters() throws MalformedURLException {
        // Given
        Long lessonId = 1L;
        String fileName = "intro.mp4";
        String contentType = "video/mp4";
        String expectedKey = "videos/lesson-1-intro.mp4";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/videos/lesson-1-intro.mp4?signature=xyz");

        when(s3PresignedUrlService.createUploadUrl(eq(expectedKey), any(Duration.class), eq(contentType)))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = contentMediaService.createVideoUploadUrl(lessonId, fileName, contentType);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.uploadUrl()).isEqualTo(mockUrl.toString());
        assertThat(response.fileKey()).isEqualTo(expectedKey);
        verify(s3PresignedUrlService, times(1)).createUploadUrl(eq(expectedKey), eq(Duration.ofMinutes(30)), eq(contentType));
    }

    @Test
    @DisplayName("Should generate correct key format for video")
    void shouldGenerateCorrectKeyFormatForVideo() throws MalformedURLException {
        // Given
        Long lessonId = 5L;
        String fileName = "advanced-lesson.mp4";
        String contentType = "video/mp4";
        String expectedKey = "videos/lesson-5-advanced-lesson.mp4";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/" + expectedKey);

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = contentMediaService.createVideoUploadUrl(lessonId, fileName, contentType);

        // Then
        assertThat(response.fileKey()).isEqualTo(expectedKey);
        assertThat(response.fileKey()).startsWith("videos/lesson-");
        assertThat(response.fileKey()).contains("-" + fileName);
    }

    @Test
    @DisplayName("Should use 30 minutes expiration for video upload")
    void shouldUse30MinutesExpirationForVideoUpload() throws MalformedURLException {
        // Given
        Long lessonId = 1L;
        String fileName = "test.mp4";
        String contentType = "video/mp4";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/videos/test.mp4");

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        contentMediaService.createVideoUploadUrl(lessonId, fileName, contentType);

        // Then
        verify(s3PresignedUrlService).createUploadUrl(anyString(), eq(Duration.ofMinutes(30)), anyString());
    }

    @Test
    @DisplayName("Should handle fileName with special characters")
    void shouldHandleFileNameWithSpecialCharacters() throws MalformedURLException {
        // Given
        Long lessonId = 1L;
        String fileName = "intro_part-1@2024.mp4";
        String contentType = "video/mp4";
        String expectedKey = "videos/lesson-1-intro_part-1@2024.mp4";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/" + expectedKey);

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = contentMediaService.createVideoUploadUrl(lessonId, fileName, contentType);

        // Then
        assertThat(response.fileKey()).isEqualTo(expectedKey);
    }

    @Test
    @DisplayName("Should handle different video content types")
    void shouldHandleDifferentVideoContentTypes() throws MalformedURLException {
        // Given
        Long lessonId = 1L;
        String fileName = "test.webm";
        String contentType = "video/webm";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/videos/lesson-1-test.webm");

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = contentMediaService.createVideoUploadUrl(lessonId, fileName, contentType);

        // Then
        assertThat(response).isNotNull();
        verify(s3PresignedUrlService).createUploadUrl(anyString(), any(Duration.class), eq(contentType));
    }

    @Test
    @DisplayName("Should handle null lessonId")
    void shouldHandleNullLessonId() throws MalformedURLException {
        // Given
        Long lessonId = null;
        String fileName = "test.mp4";
        String contentType = "video/mp4";
        String expectedKey = "videos/lesson-null-test.mp4";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/" + expectedKey);

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = contentMediaService.createVideoUploadUrl(lessonId, fileName, contentType);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.fileKey()).contains("lesson-null-");
    }

    @Test
    @DisplayName("Should handle empty fileName")
    void shouldHandleEmptyFileName() throws MalformedURLException {
        // Given
        Long lessonId = 1L;
        String fileName = "";
        String contentType = "video/mp4";
        String expectedKey = "videos/lesson-1-";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/" + expectedKey);

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = contentMediaService.createVideoUploadUrl(lessonId, fileName, contentType);

        // Then
        assertThat(response.fileKey()).isEqualTo(expectedKey);
    }

    @Test
    @DisplayName("Should handle fileName with spaces")
    void shouldHandleFileNameWithSpaces() throws MalformedURLException {
        // Given
        Long lessonId = 1L;
        String fileName = "my video file.mp4";
        String contentType = "video/mp4";
        String expectedKey = "videos/lesson-1-my video file.mp4";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/videos/test.mp4");

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = contentMediaService.createVideoUploadUrl(lessonId, fileName, contentType);

        // Then
        assertThat(response.fileKey()).isEqualTo(expectedKey);
    }

    @Test
    @DisplayName("Should handle large lessonId")
    void shouldHandleLargeLessonId() throws MalformedURLException {
        // Given
        Long lessonId = Long.MAX_VALUE;
        String fileName = "test.mp4";
        String contentType = "video/mp4";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/videos/test.mp4");

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = contentMediaService.createVideoUploadUrl(lessonId, fileName, contentType);

        // Then
        assertThat(response.fileKey()).contains("lesson-" + Long.MAX_VALUE);
    }

    @Test
    @DisplayName("Should return URL as string in response")
    void shouldReturnUrlAsStringInResponse() throws MalformedURLException {
        // Given
        Long lessonId = 1L;
        String fileName = "test.mp4";
        String contentType = "video/mp4";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/videos/lesson-1-test.mp4?X-Amz-Signature=abc123");

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = contentMediaService.createVideoUploadUrl(lessonId, fileName, contentType);

        // Then
        assertThat(response.uploadUrl()).isEqualTo(mockUrl.toString());
    }

    @Test
    @DisplayName("Should not interact with repository when creating upload URL")
    void shouldNotInteractWithRepositoryWhenCreatingUploadUrl() throws MalformedURLException {
        // Given
        Long lessonId = 1L;
        String fileName = "test.mp4";
        String contentType = "video/mp4";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/videos/test.mp4");

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        contentMediaService.createVideoUploadUrl(lessonId, fileName, contentType);

        // Then
        verifyNoInteractions(contentMediaRepository);
    }

    @Test
    @DisplayName("Should handle fileName without extension")
    void shouldHandleFileNameWithoutExtension() throws MalformedURLException {
        // Given
        Long lessonId = 1L;
        String fileName = "videofile";
        String contentType = "video/mp4";
        String expectedKey = "videos/lesson-1-videofile";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/" + expectedKey);

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = contentMediaService.createVideoUploadUrl(lessonId, fileName, contentType);

        // Then
        assertThat(response.fileKey()).isEqualTo(expectedKey);
    }

    @Test
    @DisplayName("Should handle very long fileName")
    void shouldHandleVeryLongFileName() throws MalformedURLException {
        // Given
        Long lessonId = 1L;
        String fileName = "a".repeat(200) + ".mp4";
        String contentType = "video/mp4";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/videos/test.mp4");

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = contentMediaService.createVideoUploadUrl(lessonId, fileName, contentType);

        // Then
        assertThat(response.fileKey()).startsWith("videos/lesson-1-");
        assertThat(response.fileKey()).contains(fileName);
    }

    @Test
    @DisplayName("Should invoke S3 service exactly once")
    void shouldInvokeS3ServiceExactlyOnce() throws MalformedURLException {
        // Given
        Long lessonId = 1L;
        String fileName = "test.mp4";
        String contentType = "video/mp4";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/videos/test.mp4");

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        contentMediaService.createVideoUploadUrl(lessonId, fileName, contentType);

        // Then
        verify(s3PresignedUrlService, times(1)).createUploadUrl(anyString(), any(Duration.class), anyString());
        verifyNoMoreInteractions(s3PresignedUrlService);
    }
}