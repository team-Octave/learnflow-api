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
    private static final String FIXED_CONTENT_TYPE = "video/mp4";
    private static final int MIN_STREAMING_TTL_MINUTES = 30;

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
        String contentType = "video/mp4";
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileKey)
                .setContentType(FIXED_CONTENT_TYPE)
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

    /**
     * 영상 재생용 Signed Url 생성
     * */
    public String streamingCreateSignedUrl(String fileKey, Integer durationSec) {

        long ttlMinutes = Math.round((durationSec * 1.5) / 60);

        if (ttlMinutes < MIN_STREAMING_TTL_MINUTES) {
            ttlMinutes = MIN_STREAMING_TTL_MINUTES;
        }

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileKey).build();

        URL url = storage.signUrl(
                blobInfo,
                ttlMinutes,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.GET),
                Storage.SignUrlOption.withV4Signature()
        );

        return url.toString();
    }

    /**
     *   AI 워커 다운로드용 Signed URL 생성
     * - 재생 시간이 아닌 단순 만료 시간(초)을 기준으로 URL 생성
     */
    public String generateDownloadUrl(String fileKey, long expirationSeconds) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileKey).build();

        URL url = storage.signUrl(
            blobInfo,
            expirationSeconds,
            TimeUnit.SECONDS, // 초 단위 설정
            Storage.SignUrlOption.httpMethod(HttpMethod.GET),
            Storage.SignUrlOption.withV4Signature()
        );

        return url.toString();
    }
}
