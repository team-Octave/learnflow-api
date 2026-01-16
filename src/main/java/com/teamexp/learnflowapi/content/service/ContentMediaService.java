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
import com.teamexp.learnflowapi.content.exception.MediaNotReadyException;
import com.teamexp.learnflowapi.content.exception.SignedUrlCreationException;
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

    @Transactional
    public void bindMediaToLesson(Long lessonId, Long mediaId){

        // Media id 조회
        ContentMedia media = contentMediaRepository.findById(mediaId)
                .orElseThrow(MediaNotFoundException::new);

        // 업로드 상태 확인하기(업로드 성공 상태인지 확인)
        if (media.getStatus() != MediaStatus.COMPLETED){
            throw new MediaNotReadyException();
        }

        // mediaId와 lessonId와 바인딩
        media.attachToLesson(lessonId);
    }

    public String getStreamingUrl (Long lessonId){
        // 레슨 id조회
        ContentMedia media =contentMediaRepository.findByLessonId(lessonId)
                .orElseThrow(LessonVideoNotFoundException::new);

        // 강의 영상 상태 검증(COMPLETED인지)
        if (media.getStatus() != MediaStatus.COMPLETED){
            throw new MediaNotReadyException();
        }

        // - 정상 업로드 완료된 영상은 durationSec이 반드시 존재하고 1초 이상이어야 함
        // - null 또는 0이면 업로드 완료 전에 lessonId가 매핑되었거나 비정상 데이터로 판단
        if (media.getDurationSec() == null || media.getDurationSec() == 0){
            throw new LessonVideoNotFoundException();
        }

        try {

            return gcpSignedUrlService.streamingCreateSignedUrl(
                    media.getFileKey(),
                    media.getDurationSec()
            );
        } catch (Exception e){
            throw new SignedUrlCreationException();
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
