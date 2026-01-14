package com.teamexp.learnflowapi.content.external;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.concurrent.TimeUnit;

@Service
public class GcpSignedUrlService {

    private final Storage storage;

    public GcpSignedUrlService(Storage storage) {
        this.storage = storage;
    }

    @Value("${spring.cloud.gcp.storage.bucket-name}")
    private String bucketName;

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
