package com.teamexp.learnflowapi.content.external;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class GcpSignedUrlService {

    private final Storage storage;

    @Value("${spring.cloud.gcp.storage.bucket-name}")
    private String bucketName;

    public GcpSignedUrlService(
            @Value("${spring.cloud.gcp.storage.json-key-location}") String keyLocation
    ) throws IOException {

        // cloud.gcp.storage.json-key-location 직접 읽어서 credentials 생성
        Resource resource = new DefaultResourceLoader().getResource(keyLocation);

        GoogleCredentials credentials = GoogleCredentials
                .fromStream(resource.getInputStream())
                .createScoped(
                        List.of("https://www.googleapis.com/auth/devstorage.read_write")
                );

        this.storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }

    public String createSignedUrl(String fileKey, String contentType, long fileSize) {


        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileKey)
                .setContentType(contentType)
                .build();

        URL url = storage.signUrl(
                blobInfo,
                15,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.withContentType()
        );

        return url.toString();
    }
}
