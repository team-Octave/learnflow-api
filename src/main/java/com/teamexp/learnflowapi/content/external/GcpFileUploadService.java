package com.teamexp.learnflowapi.content.external;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;
import com.google.cloud.storage.Blob;


@Service
@RequiredArgsConstructor
public class GcpFileUploadService {

    @Value("${spring.cloud.gcp.storage.bucket-name}")
    private String bucketName;

    @Value("${spring.cloud.gcp.storage.json-key-location}")
    private String keyFileName;

    public String createUploadUrl(String uploadFileName,  MultipartFile file) throws IOException {

        InputStream keyFile = ResourceUtils.getURL("classpath:" + keyFileName).openStream();

        Storage storage = StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(keyFile))
                .build()
                .getService();

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, uploadFileName)
                .build();

        Blob blob = (Blob) storage.create(blobInfo, file.getInputStream());

        URL signedUrl = storage.signUrl(
                blob,
                15,
                java.util.concurrent.TimeUnit.MINUTES,
                Storage.SignUrlOption.withV4Signature()
        );


        return signedUrl.toString();
    }
}
