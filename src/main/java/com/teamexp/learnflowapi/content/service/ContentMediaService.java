package com.teamexp.learnflowapi.content.service;

import com.teamexp.learnflowapi.content.dto.UploadInitRequest;
import com.teamexp.learnflowapi.content.dto.UploadInitResponse;
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
    private static final String ALLOWED_MIME = "video/mp4";
    private static final long MAX_FILE_SIZE = 1024L * 1024L * 1024L;

    @Transactional
    public UploadInitResponse initUpload(UploadInitRequest uploadInitRequest) {

        validateInitRequest(uploadInitRequest);

        // GCP 업로드용 파일 key 생성
        String fileKey = buildFileKey(uploadInitRequest.filename());

        // PENDING 상태로 DB insert
        ContentMedia media = ContentMedia.createPending(fileKey);
        contentMediaRepository.save(media);


        // Signed URL 발급
        String uploadUrl = gcpSignedUrlService.createSignedUrl(
                fileKey,
                uploadInitRequest.contentType(),
                uploadInitRequest.filesize()
        );

        return new UploadInitResponse(
                media.getId(),
                uploadUrl,
                fileKey,
                bucketName
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
            throw new IllegalArgumentException("mp4 확장자만 허용됩니다.");
        }

        if (!"video/mp4".equalsIgnoreCase(req.contentType())) {
            throw new IllegalArgumentException(ALLOWED_MIME + "만 업로드 가능합니다.");
        }

        if (req.filesize() == null || req.filesize() <= 0 || req.filesize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 0보다 크고 1GB 이하여야 합니다.");
        }
    }
}
