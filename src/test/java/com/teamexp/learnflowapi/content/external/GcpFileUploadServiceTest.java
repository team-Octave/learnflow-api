package com.teamexp.learnflowapi.content.external;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GcpFileUploadServiceTest {

    @Mock
    Storage storage;

    @Mock
    MultipartFile multipartFile;

    @Captor
    ArgumentCaptor<BlobInfo> blobInfoCaptor;

    @Captor
    ArgumentCaptor<InputStream> inputStreamCaptor;

    private GcpFileUploadService gcpFileUploadService;

    @BeforeEach
    void setup() {
        gcpFileUploadService = new GcpFileUploadService(storage, "test-bucket-name");
    }

    @Test
    @DisplayName("썸네일 업로드 성공")
    void tc23_thumbnail_upload_success() throws IOException {
        // given
        String fileName = "thumbnail/test-thumbnail.jpg";
        InputStream fileStream = new ByteArrayInputStream("dummy".getBytes());
        when(multipartFile.getInputStream()).thenReturn(fileStream);

        // when
        gcpFileUploadService.uploadFile(fileName, multipartFile);

        // then
        verify(storage, times(1)).create(blobInfoCaptor.capture(), inputStreamCaptor.capture());

        BlobInfo captured = blobInfoCaptor.getValue();
        assertThat(captured.getBucket()).isEqualTo("test-bucket-name");
        assertThat(captured.getName()).isEqualTo("thumbnail/test-thumbnail.jpg");
    }
}
