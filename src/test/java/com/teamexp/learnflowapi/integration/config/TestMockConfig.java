package com.teamexp.learnflowapi.integration.config;

import com.google.cloud.storage.Storage;
import com.teamexp.learnflowapi.content.external.GcpFileUploadService;
import com.teamexp.learnflowapi.content.external.GcpSignedUrlService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestMockConfig {

    @Bean
    public Storage storage() {
        return Mockito.mock(Storage.class);
    }

    @Bean
    public GcpSignedUrlService gcpSignedUrlService() {
        return Mockito.mock(GcpSignedUrlService.class);
    }

    @Bean
    public GcpFileUploadService gcpFileUploadService() {
        return Mockito.mock(GcpFileUploadService.class);
    }
}
