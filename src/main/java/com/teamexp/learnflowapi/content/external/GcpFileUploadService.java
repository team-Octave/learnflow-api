package com.teamexp.learnflowapi.content.external;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class GcpFileUploadService {

    private final Storage storage;
    private final String bucketName;

    public GcpFileUploadService(
            Storage storage,
            @Value("${spring.cloud.gcp.storage.bucket-name}") String bucketName
    ) {
        this.storage = storage;
        this.bucketName = bucketName;
    }

    /**
     * 서버에서 GCS로 직접 업로드할 때 사용하는 공통 업로드 로직
     * (썸네일)
     */
    public void uploadFile(String fileName, MultipartFile file) throws IOException {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).build();
        storage.create(blobInfo, file.getInputStream());
    }

    /**
     * 공개 리소스용 Public URL 생성
     * (썸네일이 public 접근 가능한 버킷/오브젝트일 때)
     */
    public String createPublicUrl(String fileName) {
        return "https://storage.googleapis.com/" + bucketName + "/" + fileName;
    }
}
