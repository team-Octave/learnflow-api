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
    private final String bucketName;

    public GcpSignedUrlService(
            Storage storage,
            @Value("${spring.cloud.gcp.storage.bucket-name}") String bucketName
    ) {
        this.storage = storage;
        this.bucketName = bucketName;
    }

    /**
     * 업로드용 Signed URL (프론트가 GCS로 직접 업로드)
     */
    public String createSignedUrl(String fileKey) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileKey)
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
