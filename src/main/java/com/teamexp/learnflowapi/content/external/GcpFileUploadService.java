package com.teamexp.learnflowapi.content.external;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GcpFileUploadService {

    @Value("${spring.cloud.gcp.storage.bucket-name}")
    private String bucketName;

    @Value("${spring.cloud.gcp.storage.json-key-location}")
    private String keyFileName;

    private Storage createStorage() throws IOException {
        InputStream keyFile = ResourceUtils.getURL("classpath:" + keyFileName).openStream();

        return StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(keyFile))
                .build()
                .getService();
    }

    /**
     * 공통 업로드 로직
     */
    public void uploadFile(String fileName, MultipartFile file) throws IOException {
        Storage storage = createStorage();

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).build();

        storage.create(blobInfo, file.getInputStream());
    }

    public String uploadFile(File file, String directory) throws IOException {
        Storage storage = createStorage();

        // GCS에 저장할 objectName 생성 (디렉토리 + UUID 조합 등)
        String objectName = directory + UUID.randomUUID() + ".mp4";
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName).build();

        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            storage.createFrom(blobInfo, inputStream);
        }

        return objectName; // 이게 DB에 넣을 fileKey
    }


    /**
     * 공개 리소스용 Public URL 생성
     */
    public String createPublicUrl(String fileName) {
        return "https://storage.googleapis.com/" + bucketName + "/" + fileName;
    }

    /**
     * 비공개 리소스용 Signed URL 생성
     */
    public String createSignedUrl(String fileName, long minutes) throws IOException {
        Storage storage = createStorage();

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).build();

        URL signedUrl = storage.signUrl(
                blobInfo,
                minutes,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.withV4Signature()
        );

        return signedUrl.toString();
    }
}
