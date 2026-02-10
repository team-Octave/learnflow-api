package com.teamexp.learnflowapi.content.external;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GcpSignedUrlServiceTest {

    @Mock
    Storage storage;

    @Captor
    ArgumentCaptor<BlobInfo> blobInfoCaptor;

    @Captor
    ArgumentCaptor<Long> durationCaptor;

    @Captor
    ArgumentCaptor<TimeUnit> timeUnitCaptor;

    @Captor
    ArgumentCaptor<Storage.SignUrlOption[]> optionsCaptor;

    private GcpSignedUrlService service;

    @BeforeEach
    void setUp() {
        service = new GcpSignedUrlService(storage, "test-bucket");
    }

    @Test
    void tc24_issue_upload_signed_url() throws Exception {
        // given
        URL stubUrl = new URL("https://example.com/upload-signed");
        when(storage.signUrl(any(BlobInfo.class), anyLong(), any(TimeUnit.class), any(Storage.SignUrlOption[].class)))
                .thenReturn(stubUrl);

        // when
        String result = service.createSignedUrl("video/abc.mp4");

        // then
        verify(storage, times(1)).signUrl(
                blobInfoCaptor.capture(),
                eq(15L),
                eq(TimeUnit.MINUTES),
                optionsCaptor.capture()
        );

        BlobInfo captured = blobInfoCaptor.getValue();
        assertThat(captured.getBucket()).isEqualTo("test-bucket");
        assertThat(captured.getName()).isEqualTo("video/abc.mp4");
        assertThat(captured.getContentType()).isEqualTo("video/mp4"); // FIXED_CONTENT_TYPE 검증

        assertThat(result).isEqualTo(stubUrl.toString());
    }

    @Test
    void tc25_streaming_signed_url_min_ttl_30min() throws Exception {
        // given
        URL stubUrl = new URL("https://example.com/streaming-signed");
        when(storage.signUrl(any(BlobInfo.class), anyLong(), any(TimeUnit.class), any(Storage.SignUrlOption[].class)))
                .thenReturn(stubUrl);

        int durationSec = 300; // 5분 요청

        // when
        String result = service.streamingCreateSignedUrl("video/stream.mp4", durationSec);

        // then
        verify(storage, times(1)).signUrl(
                blobInfoCaptor.capture(),
                durationCaptor.capture(),
                timeUnitCaptor.capture(),
                optionsCaptor.capture()
        );

        BlobInfo captured = blobInfoCaptor.getValue();
        assertThat(captured.getBucket()).isEqualTo("test-bucket");
        assertThat(captured.getName()).isEqualTo("video/stream.mp4");

        assertThat(timeUnitCaptor.getValue()).isEqualTo(TimeUnit.MINUTES);
        assertThat(durationCaptor.getValue()).isEqualTo(30L); // 최소 30분 보장

        assertThat(result).isEqualTo(stubUrl.toString());
    }
}
