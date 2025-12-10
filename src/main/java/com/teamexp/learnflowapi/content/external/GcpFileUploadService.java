package com.teamexp.learnflowapi.content.external;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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

    /**
     * 공개 리소스용 Public URL 생성
     */
    public String createPublicUrl(String fileName) {
        return "https://storage.googleapis.com/learnflow-file-storage" + bucketName + "/" + fileName;
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
