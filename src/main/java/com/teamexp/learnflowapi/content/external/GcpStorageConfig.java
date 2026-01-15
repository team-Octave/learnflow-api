package com.teamexp.learnflowapi.content.external;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Configuration
public class GcpStorageConfig {
    private final Resource keyResource;
    public GcpStorageConfig(
            @Value("${spring.cloud.gcp.storage.json-key-location}") Resource keyResource
    ) {
        this.keyResource = keyResource;
    }
    @Bean
    public Storage gcpStorage() throws IOException {
        try (InputStream is = keyResource.getInputStream()) {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(is)
                    .createScoped(List.of("https://www.googleapis.com/auth/devstorage.read_write"));
            return StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .build()
                    .getService();
        }
    }
}
