package com.teamexp.learnflowapi.content.service;

import com.teamexp.learnflowapi.content.dto.PresignedUrlResponse;
import com.teamexp.learnflowapi.content.repository.ThumbnailRepository;
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
@DisplayName("ThumbnailService Tests")
class ThumbnailServiceTest {

    @Mock
    private ThumbnailRepository thumbnailRepository;

    @Mock
    private S3PresignedUrlService s3PresignedUrlService;

    private ThumbnailService thumbnailService;

    @BeforeEach
    void setUp() {
        thumbnailService = new ThumbnailService(thumbnailRepository, s3PresignedUrlService);
    }

    @Test
    @DisplayName("Should create thumbnail upload URL with valid parameters")
    void shouldCreateThumbnailUploadUrlWithValidParameters() throws MalformedURLException {
        // Given
        Long lectureId = 1L;
        String fileName = "thumbnail.jpg";
        String contentType = "image/jpeg";
        String expectedKey = "thumbnails/lecture-1-thumbnail.jpg";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/thumbnails/lecture-1-thumbnail.jpg?signature=xyz");

        when(s3PresignedUrlService.createUploadUrl(eq(expectedKey), any(Duration.class), eq(contentType)))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = thumbnailService.createUploadUrl(lectureId, fileName, contentType);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.uploadUrl()).isEqualTo(mockUrl.toString());
        assertThat(response.fileKey()).isEqualTo(expectedKey);
        verify(s3PresignedUrlService, times(1)).createUploadUrl(eq(expectedKey), eq(Duration.ofMinutes(10)), eq(contentType));
    }

    @Test
    @DisplayName("Should generate correct key format for thumbnail")
    void shouldGenerateCorrectKeyFormatForThumbnail() throws MalformedURLException {
        // Given
        Long lectureId = 5L;
        String fileName = "course-image.jpg";
        String contentType = "image/jpeg";
        String expectedKey = "thumbnails/lecture-5-course-image.jpg";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/" + expectedKey);

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = thumbnailService.createUploadUrl(lectureId, fileName, contentType);

        // Then
        assertThat(response.fileKey()).isEqualTo(expectedKey);
        assertThat(response.fileKey()).startsWith("thumbnails/lecture-");
        assertThat(response.fileKey()).contains("-" + fileName);
    }

    @Test
    @DisplayName("Should use 10 minutes expiration for thumbnail upload")
    void shouldUse10MinutesExpirationForThumbnailUpload() throws MalformedURLException {
        // Given
        Long lectureId = 1L;
        String fileName = "test.jpg";
        String contentType = "image/jpeg";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/thumbnails/test.jpg");

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        thumbnailService.createUploadUrl(lectureId, fileName, contentType);

        // Then
        verify(s3PresignedUrlService).createUploadUrl(anyString(), eq(Duration.ofMinutes(10)), anyString());
    }

    @Test
    @DisplayName("Should handle fileName with special characters")
    void shouldHandleFileNameWithSpecialCharacters() throws MalformedURLException {
        // Given
        Long lectureId = 1L;
        String fileName = "thumb_part-1@2024.jpg";
        String contentType = "image/jpeg";
        String expectedKey = "thumbnails/lecture-1-thumb_part-1@2024.jpg";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/" + expectedKey);

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = thumbnailService.createUploadUrl(lectureId, fileName, contentType);

        // Then
        assertThat(response.fileKey()).isEqualTo(expectedKey);
    }

    @Test
    @DisplayName("Should handle different image content types")
    void shouldHandleDifferentImageContentTypes() throws MalformedURLException {
        // Given
        Long lectureId = 1L;
        String fileName = "test.png";
        String contentType = "image/png";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/thumbnails/lecture-1-test.png");

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = thumbnailService.createUploadUrl(lectureId, fileName, contentType);

        // Then
        assertThat(response).isNotNull();
        verify(s3PresignedUrlService).createUploadUrl(anyString(), any(Duration.class), eq(contentType));
    }

    @Test
    @DisplayName("Should handle WebP content type")
    void shouldHandleWebPContentType() throws MalformedURLException {
        // Given
        Long lectureId = 1L;
        String fileName = "test.webp";
        String contentType = "image/webp";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/thumbnails/test.webp");

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = thumbnailService.createUploadUrl(lectureId, fileName, contentType);

        // Then
        assertThat(response).isNotNull();
        verify(s3PresignedUrlService).createUploadUrl(anyString(), any(Duration.class), eq(contentType));
    }

    @Test
    @DisplayName("Should handle null lectureId")
    void shouldHandleNullLectureId() throws MalformedURLException {
        // Given
        Long lectureId = null;
        String fileName = "test.jpg";
        String contentType = "image/jpeg";
        String expectedKey = "thumbnails/lecture-null-test.jpg";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/" + expectedKey);

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = thumbnailService.createUploadUrl(lectureId, fileName, contentType);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.fileKey()).contains("lecture-null-");
    }

    @Test
    @DisplayName("Should handle empty fileName")
    void shouldHandleEmptyFileName() throws MalformedURLException {
        // Given
        Long lectureId = 1L;
        String fileName = "";
        String contentType = "image/jpeg";
        String expectedKey = "thumbnails/lecture-1-";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/" + expectedKey);

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = thumbnailService.createUploadUrl(lectureId, fileName, contentType);

        // Then
        assertThat(response.fileKey()).isEqualTo(expectedKey);
    }

    @Test
    @DisplayName("Should handle fileName with spaces")
    void shouldHandleFileNameWithSpaces() throws MalformedURLException {
        // Given
        Long lectureId = 1L;
        String fileName = "my thumbnail image.jpg";
        String contentType = "image/jpeg";
        String expectedKey = "thumbnails/lecture-1-my thumbnail image.jpg";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/thumbnails/test.jpg");

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = thumbnailService.createUploadUrl(lectureId, fileName, contentType);

        // Then
        assertThat(response.fileKey()).isEqualTo(expectedKey);
    }

    @Test
    @DisplayName("Should handle large lectureId")
    void shouldHandleLargeLectureId() throws MalformedURLException {
        // Given
        Long lectureId = Long.MAX_VALUE;
        String fileName = "test.jpg";
        String contentType = "image/jpeg";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/thumbnails/test.jpg");

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = thumbnailService.createUploadUrl(lectureId, fileName, contentType);

        // Then
        assertThat(response.fileKey()).contains("lecture-" + Long.MAX_VALUE);
    }

    @Test
    @DisplayName("Should return URL as string in response")
    void shouldReturnUrlAsStringInResponse() throws MalformedURLException {
        // Given
        Long lectureId = 1L;
        String fileName = "test.jpg";
        String contentType = "image/jpeg";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/thumbnails/lecture-1-test.jpg?X-Amz-Signature=abc123");

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = thumbnailService.createUploadUrl(lectureId, fileName, contentType);

        // Then
        assertThat(response.uploadUrl()).isEqualTo(mockUrl.toString());
    }

    @Test
    @DisplayName("Should not interact with repository when creating upload URL")
    void shouldNotInteractWithRepositoryWhenCreatingUploadUrl() throws MalformedURLException {
        // Given
        Long lectureId = 1L;
        String fileName = "test.jpg";
        String contentType = "image/jpeg";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/thumbnails/test.jpg");

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        thumbnailService.createUploadUrl(lectureId, fileName, contentType);

        // Then
        verifyNoInteractions(thumbnailRepository);
    }

    @Test
    @DisplayName("Should handle fileName without extension")
    void shouldHandleFileNameWithoutExtension() throws MalformedURLException {
        // Given
        Long lectureId = 1L;
        String fileName = "thumbnail";
        String contentType = "image/jpeg";
        String expectedKey = "thumbnails/lecture-1-thumbnail";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/" + expectedKey);

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = thumbnailService.createUploadUrl(lectureId, fileName, contentType);

        // Then
        assertThat(response.fileKey()).isEqualTo(expectedKey);
    }

    @Test
    @DisplayName("Should handle very long fileName")
    void shouldHandleVeryLongFileName() throws MalformedURLException {
        // Given
        Long lectureId = 1L;
        String fileName = "a".repeat(200) + ".jpg";
        String contentType = "image/jpeg";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/thumbnails/test.jpg");

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = thumbnailService.createUploadUrl(lectureId, fileName, contentType);

        // Then
        assertThat(response.fileKey()).startsWith("thumbnails/lecture-1-");
        assertThat(response.fileKey()).contains(fileName);
    }

    @Test
    @DisplayName("Should invoke S3 service exactly once")
    void shouldInvokeS3ServiceExactlyOnce() throws MalformedURLException {
        // Given
        Long lectureId = 1L;
        String fileName = "test.jpg";
        String contentType = "image/jpeg";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/thumbnails/test.jpg");

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        thumbnailService.createUploadUrl(lectureId, fileName, contentType);

        // Then
        verify(s3PresignedUrlService, times(1)).createUploadUrl(anyString(), any(Duration.class), anyString());
        verifyNoMoreInteractions(s3PresignedUrlService);
    }

    @Test
    @DisplayName("Should handle GIF content type")
    void shouldHandleGifContentType() throws MalformedURLException {
        // Given
        Long lectureId = 1L;
        String fileName = "animation.gif";
        String contentType = "image/gif";
        URL mockUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/thumbnails/animation.gif");

        when(s3PresignedUrlService.createUploadUrl(anyString(), any(Duration.class), anyString()))
            .thenReturn(mockUrl);

        // When
        PresignedUrlResponse response = thumbnailService.createUploadUrl(lectureId, fileName, contentType);

        // Then
        assertThat(response).isNotNull();
        verify(s3PresignedUrlService).createUploadUrl(anyString(), any(Duration.class), eq(contentType));
    }
}