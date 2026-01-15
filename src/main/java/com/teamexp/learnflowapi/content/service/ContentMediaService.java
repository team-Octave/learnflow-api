package com.teamexp.learnflowapi.content.service;

import com.teamexp.learnflowapi.content.dto.UploadInitRequest;
import com.teamexp.learnflowapi.content.dto.UploadInitResponse;
import com.teamexp.learnflowapi.content.exception.InvalidVideoExtensionException;
import com.teamexp.learnflowapi.content.external.GcpSignedUrlService;
import com.teamexp.learnflowapi.content.model.ContentMedia;
import com.teamexp.learnflowapi.content.repository.ContentMediaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ContentMediaService {

    private final ContentMediaRepository contentMediaRepository;
    private final GcpSignedUrlService gcpSignedUrlService;

    public ContentMediaService(ContentMediaRepository contentMediaRepository,
                               GcpSignedUrlService gcpSignedUrlService) {
        this.contentMediaRepository = contentMediaRepository;
        this.gcpSignedUrlService = gcpSignedUrlService;
    }

    @Value("${spring.cloud.gcp.storage.bucket-name}")
    private String bucketName;

    /*
     * 1. 영상 파일 형식 .mp4
     * 2. 영상 파일 업로드 파일 양식 mp4
     * 3. 업로드 최대 사이즈 1GB
     * */
    private static final String ALLOWED_EXTENSION = ".mp4";

    @Transactional
    public UploadInitResponse initUpload(UploadInitRequest uploadInitRequest) {

        validateInitRequest(uploadInitRequest);

        // GCP 업로드용 파일 key 생성
        String fileKey = buildFileKey(uploadInitRequest.filename());

        // PENDING 상태로 DB insert
        ContentMedia media = ContentMedia.createPending(fileKey, uploadInitRequest.filename());
        contentMediaRepository.save(media);


        // Signed URL 발급
        String uploadUrl = gcpSignedUrlService.createSignedUrl(
                fileKey
        );

        return new UploadInitResponse(
                media.getId(),
                uploadUrl
        );

    }
    private String buildFileKey(String filename) {

        // 파일명에서 경로 구분자 제거 및 안전한 문자만 허용
        String sanitizedFilename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");

        // 랜덤키 사용
        return "videos/%s_%s".formatted(
                UUID.randomUUID().toString(),
                sanitizedFilename
        );
    }

    private void validateInitRequest(UploadInitRequest req) {

        if (req.filename() == null || !req.filename().toLowerCase().endsWith(ALLOWED_EXTENSION)) {
            throw new InvalidVideoExtensionException();
        }

    }
}
