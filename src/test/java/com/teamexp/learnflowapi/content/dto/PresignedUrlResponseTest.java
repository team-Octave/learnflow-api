package com.teamexp.learnflowapi.content.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PresignedUrlResponse DTO Tests")
class PresignedUrlResponseTest {

    @Test
    @DisplayName("Should create PresignedUrlResponse with valid parameters")
    void shouldCreatePresignedUrlResponseWithValidParameters() {
        // Given
        String uploadUrl = "https://s3.amazonaws.com/my-lxp-videos/videos/lesson-1-intro.mp4?signature=xyz";
        String fileKey = "videos/lesson-1-intro.mp4";

        // When
        PresignedUrlResponse response = new PresignedUrlResponse(uploadUrl, fileKey);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.uploadUrl()).isEqualTo(uploadUrl);
        assertThat(response.fileKey()).isEqualTo(fileKey);
    }

    @Test
    @DisplayName("Should create PresignedUrlResponse with null uploadUrl")
    void shouldCreatePresignedUrlResponseWithNullUploadUrl() {
        // Given
        String uploadUrl = null;
        String fileKey = "videos/test.mp4";

        // When
        PresignedUrlResponse response = new PresignedUrlResponse(uploadUrl, fileKey);

        // Then
        assertThat(response.uploadUrl()).isNull();
        assertThat(response.fileKey()).isEqualTo(fileKey);
    }

    @Test
    @DisplayName("Should create PresignedUrlResponse with null fileKey")
    void shouldCreatePresignedUrlResponseWithNullFileKey() {
        // Given
        String uploadUrl = "https://s3.amazonaws.com/bucket/key";
        String fileKey = null;

        // When
        PresignedUrlResponse response = new PresignedUrlResponse(uploadUrl, fileKey);

        // Then
        assertThat(response.uploadUrl()).isEqualTo(uploadUrl);
        assertThat(response.fileKey()).isNull();
    }

    @Test
    @DisplayName("Should create PresignedUrlResponse with both null parameters")
    void shouldCreatePresignedUrlResponseWithBothNullParameters() {
        // Given
        String uploadUrl = null;
        String fileKey = null;

        // When
        PresignedUrlResponse response = new PresignedUrlResponse(uploadUrl, fileKey);

        // Then
        assertThat(response.uploadUrl()).isNull();
        assertThat(response.fileKey()).isNull();
    }

    @Test
    @DisplayName("Should handle empty string uploadUrl")
    void shouldHandleEmptyStringUploadUrl() {
        // Given
        String emptyUrl = "";
        String fileKey = "videos/test.mp4";

        // When
        PresignedUrlResponse response = new PresignedUrlResponse(emptyUrl, fileKey);

        // Then
        assertThat(response.uploadUrl()).isEmpty();
    }

    @Test
    @DisplayName("Should handle empty string fileKey")
    void shouldHandleEmptyStringFileKey() {
        // Given
        String uploadUrl = "https://s3.amazonaws.com/bucket/key";
        String emptyFileKey = "";

        // When
        PresignedUrlResponse response = new PresignedUrlResponse(uploadUrl, emptyFileKey);

        // Then
        assertThat(response.fileKey()).isEmpty();
    }

    @Test
    @DisplayName("Should handle URL with query parameters")
    void shouldHandleUrlWithQueryParameters() {
        // Given
        String uploadUrl = "https://s3.amazonaws.com/bucket/key?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=xyz";
        String fileKey = "videos/test.mp4";

        // When
        PresignedUrlResponse response = new PresignedUrlResponse(uploadUrl, fileKey);

        // Then
        assertThat(response.uploadUrl()).isEqualTo(uploadUrl);
        assertThat(response.uploadUrl()).contains("?");
        assertThat(response.uploadUrl()).contains("X-Amz-Algorithm");
    }

    @Test
    @DisplayName("Should handle very long uploadUrl")
    void shouldHandleVeryLongUploadUrl() {
        // Given
        String longUrl = "https://s3.amazonaws.com/bucket/" + "a".repeat(1000) + ".mp4";
        String fileKey = "videos/test.mp4";

        // When
        PresignedUrlResponse response = new PresignedUrlResponse(longUrl, fileKey);

        // Then
        assertThat(response.uploadUrl()).hasSize(longUrl.length());
    }

    @Test
    @DisplayName("Should handle fileKey with special characters")
    void shouldHandleFileKeyWithSpecialCharacters() {
        // Given
        String uploadUrl = "https://s3.amazonaws.com/bucket/key";
        String specialFileKey = "videos/lesson-1_intro@2024!.mp4";

        // When
        PresignedUrlResponse response = new PresignedUrlResponse(uploadUrl, specialFileKey);

        // Then
        assertThat(response.fileKey()).isEqualTo(specialFileKey);
    }

    @Test
    @DisplayName("Should support record equality")
    void shouldSupportRecordEquality() {
        // Given
        String uploadUrl = "https://s3.amazonaws.com/bucket/key";
        String fileKey = "videos/test.mp4";
        PresignedUrlResponse response1 = new PresignedUrlResponse(uploadUrl, fileKey);
        PresignedUrlResponse response2 = new PresignedUrlResponse(uploadUrl, fileKey);

        // Then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    @DisplayName("Should support record inequality for different uploadUrl")
    void shouldSupportRecordInequalityForDifferentUploadUrl() {
        // Given
        String fileKey = "videos/test.mp4";
        PresignedUrlResponse response1 = new PresignedUrlResponse("https://url1.com", fileKey);
        PresignedUrlResponse response2 = new PresignedUrlResponse("https://url2.com", fileKey);

        // Then
        assertThat(response1).isNotEqualTo(response2);
    }

    @Test
    @DisplayName("Should support record inequality for different fileKey")
    void shouldSupportRecordInequalityForDifferentFileKey() {
        // Given
        String uploadUrl = "https://s3.amazonaws.com/bucket/key";
        PresignedUrlResponse response1 = new PresignedUrlResponse(uploadUrl, "videos/file1.mp4");
        PresignedUrlResponse response2 = new PresignedUrlResponse(uploadUrl, "videos/file2.mp4");

        // Then
        assertThat(response1).isNotEqualTo(response2);
    }

    @Test
    @DisplayName("Should have meaningful toString representation")
    void shouldHaveMeaningfulToStringRepresentation() {
        // Given
        String uploadUrl = "https://s3.amazonaws.com/bucket/key";
        String fileKey = "videos/test.mp4";
        PresignedUrlResponse response = new PresignedUrlResponse(uploadUrl, fileKey);

        // When
        String toString = response.toString();

        // Then
        assertThat(toString).contains("PresignedUrlResponse");
        assertThat(toString).contains(uploadUrl);
        assertThat(toString).contains(fileKey);
    }

    @Test
    @DisplayName("Should handle URL with fragment")
    void shouldHandleUrlWithFragment() {
        // Given
        String uploadUrl = "https://s3.amazonaws.com/bucket/key#fragment";
        String fileKey = "videos/test.mp4";

        // When
        PresignedUrlResponse response = new PresignedUrlResponse(uploadUrl, fileKey);

        // Then
        assertThat(response.uploadUrl()).isEqualTo(uploadUrl);
        assertThat(response.uploadUrl()).contains("#");
    }

    @Test
    @DisplayName("Should handle thumbnail fileKey")
    void shouldHandleThumbnailFileKey() {
        // Given
        String uploadUrl = "https://s3.amazonaws.com/bucket/thumbnails/lecture-1.jpg";
        String fileKey = "thumbnails/lecture-1.jpg";

        // When
        PresignedUrlResponse response = new PresignedUrlResponse(uploadUrl, fileKey);

        // Then
        assertThat(response.fileKey()).startsWith("thumbnails/");
        assertThat(response.fileKey()).endsWith(".jpg");
    }

    @Test
    @DisplayName("Should handle video fileKey")
    void shouldHandleVideoFileKey() {
        // Given
        String uploadUrl = "https://s3.amazonaws.com/bucket/videos/lesson-1.mp4";
        String fileKey = "videos/lesson-1.mp4";

        // When
        PresignedUrlResponse response = new PresignedUrlResponse(uploadUrl, fileKey);

        // Then
        assertThat(response.fileKey()).startsWith("videos/");
        assertThat(response.fileKey()).endsWith(".mp4");
    }

    @Test
    @DisplayName("Should handle URL with port number")
    void shouldHandleUrlWithPortNumber() {
        // Given
        String uploadUrl = "https://s3.amazonaws.com:8080/bucket/key";
        String fileKey = "videos/test.mp4";

        // When
        PresignedUrlResponse response = new PresignedUrlResponse(uploadUrl, fileKey);

        // Then
        assertThat(response.uploadUrl()).contains(":8080");
    }

    @Test
    @DisplayName("Should handle local development URL")
    void shouldHandleLocalDevelopmentUrl() {
        // Given
        String uploadUrl = "http://localhost:9000/bucket/key";
        String fileKey = "videos/test.mp4";

        // When
        PresignedUrlResponse response = new PresignedUrlResponse(uploadUrl, fileKey);

        // Then
        assertThat(response.uploadUrl()).startsWith("http://localhost");
    }
}