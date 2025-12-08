package com.teamexp.learnflowapi.content.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3PresignedUrlService Tests")
class S3PresignedUrlServiceTest {

    @Mock
    private S3Presigner presigner;

    private S3PresignedUrlService s3PresignedUrlService;

    @BeforeEach
    void setUp() {
        s3PresignedUrlService = new S3PresignedUrlService(presigner);
    }

    @Test
    @DisplayName("Should create upload URL with valid parameters")
    void shouldCreateUploadUrlWithValidParameters() throws MalformedURLException {
        // Given
        String key = "videos/lesson-1-intro.mp4";
        Duration expiresIn = Duration.ofMinutes(30);
        String contentType = "video/mp4";
        URL expectedUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/videos/lesson-1-intro.mp4?signature=xyz");

        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(expectedUrl);
        when(presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

        // When
        URL result = s3PresignedUrlService.createUploadUrl(key, expiresIn, contentType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedUrl);
        verify(presigner, times(1)).presignPutObject(any(PutObjectPresignRequest.class));
    }

    @Test
    @DisplayName("Should create upload URL with correct bucket name")
    void shouldCreateUploadUrlWithCorrectBucketName() throws MalformedURLException {
        // Given
        String key = "videos/test.mp4";
        Duration expiresIn = Duration.ofMinutes(15);
        String contentType = "video/mp4";
        URL expectedUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/videos/test.mp4");

        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(expectedUrl);
        when(presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

        // When
        s3PresignedUrlService.createUploadUrl(key, expiresIn, contentType);

        // Then
        verify(presigner).presignPutObject(argThat(request -> {
            PutObjectRequest putRequest = request.putObjectRequest();
            return putRequest.bucket().equals("my-lxp-videos");
        }));
    }

    @Test
    @DisplayName("Should create upload URL with correct key")
    void shouldCreateUploadUrlWithCorrectKey() throws MalformedURLException {
        // Given
        String key = "thumbnails/lecture-5.jpg";
        Duration expiresIn = Duration.ofMinutes(10);
        String contentType = "image/jpeg";
        URL expectedUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/thumbnails/lecture-5.jpg");

        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(expectedUrl);
        when(presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

        // When
        s3PresignedUrlService.createUploadUrl(key, expiresIn, contentType);

        // Then
        verify(presigner).presignPutObject(argThat(request -> {
            PutObjectRequest putRequest = request.putObjectRequest();
            return putRequest.key().equals(key);
        }));
    }

    @Test
    @DisplayName("Should create upload URL with correct content type")
    void shouldCreateUploadUrlWithCorrectContentType() throws MalformedURLException {
        // Given
        String key = "videos/test.mp4";
        Duration expiresIn = Duration.ofMinutes(30);
        String contentType = "video/mp4";
        URL expectedUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/videos/test.mp4");

        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(expectedUrl);
        when(presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

        // When
        s3PresignedUrlService.createUploadUrl(key, expiresIn, contentType);

        // Then
        verify(presigner).presignPutObject(argThat(request -> {
            PutObjectRequest putRequest = request.putObjectRequest();
            return putRequest.contentType().equals(contentType);
        }));
    }

    @Test
    @DisplayName("Should create upload URL with correct expiration duration")
    void shouldCreateUploadUrlWithCorrectExpirationDuration() throws MalformedURLException {
        // Given
        String key = "videos/test.mp4";
        Duration expiresIn = Duration.ofMinutes(45);
        String contentType = "video/mp4";
        URL expectedUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/videos/test.mp4");

        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(expectedUrl);
        when(presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

        // When
        s3PresignedUrlService.createUploadUrl(key, expiresIn, contentType);

        // Then
        verify(presigner).presignPutObject(argThat(request -> 
            request.signatureDuration().equals(expiresIn)
        ));
    }

    @Test
    @DisplayName("Should handle image content type")
    void shouldHandleImageContentType() throws MalformedURLException {
        // Given
        String key = "thumbnails/lecture-1.jpg";
        Duration expiresIn = Duration.ofMinutes(10);
        String contentType = "image/jpeg";
        URL expectedUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/thumbnails/lecture-1.jpg");

        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(expectedUrl);
        when(presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

        // When
        URL result = s3PresignedUrlService.createUploadUrl(key, expiresIn, contentType);

        // Then
        assertThat(result).isNotNull();
        verify(presigner).presignPutObject(any(PutObjectPresignRequest.class));
    }

    @Test
    @DisplayName("Should handle PNG content type")
    void shouldHandlePngContentType() throws MalformedURLException {
        // Given
        String key = "thumbnails/lecture-1.png";
        Duration expiresIn = Duration.ofMinutes(10);
        String contentType = "image/png";
        URL expectedUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/thumbnails/lecture-1.png");

        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(expectedUrl);
        when(presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

        // When
        URL result = s3PresignedUrlService.createUploadUrl(key, expiresIn, contentType);

        // Then
        assertThat(result).isNotNull();
        verify(presigner).presignPutObject(argThat(request -> 
            request.putObjectRequest().contentType().equals("image/png")
        ));
    }

    @Test
    @DisplayName("Should handle short expiration duration")
    void shouldHandleShortExpirationDuration() throws MalformedURLException {
        // Given
        String key = "videos/test.mp4";
        Duration expiresIn = Duration.ofMinutes(1);
        String contentType = "video/mp4";
        URL expectedUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/videos/test.mp4");

        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(expectedUrl);
        when(presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

        // When
        URL result = s3PresignedUrlService.createUploadUrl(key, expiresIn, contentType);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should handle long expiration duration")
    void shouldHandleLongExpirationDuration() throws MalformedURLException {
        // Given
        String key = "videos/test.mp4";
        Duration expiresIn = Duration.ofHours(2);
        String contentType = "video/mp4";
        URL expectedUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/videos/test.mp4");

        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(expectedUrl);
        when(presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

        // When
        URL result = s3PresignedUrlService.createUploadUrl(key, expiresIn, contentType);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should handle key with special characters")
    void shouldHandleKeyWithSpecialCharacters() throws MalformedURLException {
        // Given
        String key = "videos/lesson-1_intro@2024.mp4";
        Duration expiresIn = Duration.ofMinutes(30);
        String contentType = "video/mp4";
        URL expectedUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/videos/lesson-1_intro@2024.mp4");

        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(expectedUrl);
        when(presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

        // When
        URL result = s3PresignedUrlService.createUploadUrl(key, expiresIn, contentType);

        // Then
        assertThat(result).isNotNull();
        verify(presigner).presignPutObject(argThat(request -> 
            request.putObjectRequest().key().equals(key)
        ));
    }

    @Test
    @DisplayName("Should handle key with subdirectories")
    void shouldHandleKeyWithSubdirectories() throws MalformedURLException {
        // Given
        String key = "videos/2024/12/lesson-1.mp4";
        Duration expiresIn = Duration.ofMinutes(30);
        String contentType = "video/mp4";
        URL expectedUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/videos/2024/12/lesson-1.mp4");

        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(expectedUrl);
        when(presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

        // When
        URL result = s3PresignedUrlService.createUploadUrl(key, expiresIn, contentType);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should call presigner exactly once")
    void shouldCallPresignerExactlyOnce() throws MalformedURLException {
        // Given
        String key = "videos/test.mp4";
        Duration expiresIn = Duration.ofMinutes(30);
        String contentType = "video/mp4";
        URL expectedUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/videos/test.mp4");

        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(expectedUrl);
        when(presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

        // When
        s3PresignedUrlService.createUploadUrl(key, expiresIn, contentType);

        // Then
        verify(presigner, times(1)).presignPutObject(any(PutObjectPresignRequest.class));
        verifyNoMoreInteractions(presigner);
    }

    @Test
    @DisplayName("Should handle null content type")
    void shouldHandleNullContentType() throws MalformedURLException {
        // Given
        String key = "videos/test.mp4";
        Duration expiresIn = Duration.ofMinutes(30);
        String contentType = null;
        URL expectedUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/videos/test.mp4");

        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(expectedUrl);
        when(presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

        // When
        URL result = s3PresignedUrlService.createUploadUrl(key, expiresIn, contentType);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should handle empty key")
    void shouldHandleEmptyKey() throws MalformedURLException {
        // Given
        String key = "";
        Duration expiresIn = Duration.ofMinutes(30);
        String contentType = "video/mp4";
        URL expectedUrl = new URL("https://my-lxp-videos.s3.amazonaws.com/");

        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(expectedUrl);
        when(presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

        // When
        URL result = s3PresignedUrlService.createUploadUrl(key, expiresIn, contentType);

        // Then
        assertThat(result).isNotNull();
    }
}