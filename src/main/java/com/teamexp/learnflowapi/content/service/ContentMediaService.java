package com.teamexp.learnflowapi.content.service;

import com.teamexp.learnflowapi.content.dto.UploadCompleteRequest;
import com.teamexp.learnflowapi.content.dto.UploadCompleteResponse;
import com.teamexp.learnflowapi.content.dto.UploadInitRequest;
import com.teamexp.learnflowapi.content.dto.UploadInitResponse;
import com.teamexp.learnflowapi.content.exception.FileNameEmptyException;
import com.teamexp.learnflowapi.content.exception.InvalidVideoExtensionException;
import com.teamexp.learnflowapi.content.exception.LessonVideoNotFoundException;
import com.teamexp.learnflowapi.content.exception.MediaAlreadyCompletedException;
import com.teamexp.learnflowapi.content.exception.MediaNotFoundException;
import com.teamexp.learnflowapi.content.external.GcpSignedUrlService;
import com.teamexp.learnflowapi.content.model.ContentMedia;
import com.teamexp.learnflowapi.content.model.MediaStatus;
import com.teamexp.learnflowapi.content.repository.ContentMediaRepository;
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

    @Transactional
    public void completeUpload(UploadCompleteRequest uploadCompleteRequest) {

        // mediaId로만 조회
        ContentMedia media = contentMediaRepository.findById(uploadCompleteRequest.mediaId())
                .orElseThrow(MediaNotFoundException::new);

        // 상태 체크
        if (media.getStatus() != MediaStatus.PENDING){
            throw new MediaAlreadyCompletedException();
        }

        // 업로드 상태 업데이트
        media.completeUpload(uploadCompleteRequest.durationSec());
    }

    public String getStreamingUrl (Long lessonId){
        // 레슨 id조회
        ContentMedia media =contentMediaRepository.findById(lessonId)
                .orElseThrow(LessonVideoNotFoundException::new);

        try {

            return gcpSignedUrlService.streamingCreateSignedUrl(
                    media.getFileKey(),
                    media.getDurationSec()
            );
        } catch (Exception e){
            throw new IllegalArgumentException("영상 재생 URL 생성 중 오류가 발생했습니다.");
        }
    }

    private void validateInitRequest(UploadInitRequest req) {

        String filename = req.filename();
        // 빈 문자열 체크
        if (filename == null || filename.isBlank()) {
            throw new FileNameEmptyException();
        }

        // 확장자 체크
        if (req.filename() == null || !req.filename().toLowerCase().endsWith(ALLOWED_EXTENSION)) {
            throw new InvalidVideoExtensionException();
        }

    }
}
