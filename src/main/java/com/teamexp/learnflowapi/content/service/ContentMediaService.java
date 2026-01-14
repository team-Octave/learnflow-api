package com.teamexp.learnflowapi.content.service;

import com.teamexp.learnflowapi.content.dto.UploadInitRequest;
import com.teamexp.learnflowapi.content.dto.UploadInitResponse;
import com.teamexp.learnflowapi.content.external.GcpSignedUrlService;
import com.teamexp.learnflowapi.content.model.ContentMedia;
import com.teamexp.learnflowapi.content.repository.ContentMediaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public UploadInitResponse initUpload(Long lessonId, UploadInitRequest uploadInitRequest) {

        validateInitRequest(uploadInitRequest);

        // 레슨 id 중복 체크
        contentMediaRepository.findByLessonId(lessonId)
                .ifPresent(existing -> {
                    throw new IllegalStateException("이미 해당 레슨에 영상이 존재합니다.");
                });

        // GCP 업로드용 파일 key 생성
        String fileKey = buildFileKey(lessonId, uploadInitRequest.filename());

        // PENDING 상태로 DB insert
        ContentMedia media = ContentMedia.createPending(lessonId, fileKey);
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
    private String buildFileKey(Long lessonId, String filename) {
        return "lessons/%d/videos/%d_%s".formatted(
                lessonId,
                System.currentTimeMillis(),
                filename
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
