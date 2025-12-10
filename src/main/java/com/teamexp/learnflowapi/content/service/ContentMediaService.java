package com.teamexp.learnflowapi.content.service;

import com.teamexp.learnflowapi.content.dto.UploadVideoRequest;
import com.teamexp.learnflowapi.content.external.GcpFileUploadService;
import com.teamexp.learnflowapi.content.model.ContentMedia;
import com.teamexp.learnflowapi.content.repository.ContentMediaRepository;

import java.io.File;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ContentMediaService {

    private final ContentMediaRepository contentMediaRepository;
    private final GcpFileUploadService gcpFileUploadService;

    /*
     * 1. 영상 파일 형식 .mp4
     * 2. 영상 파일 업로드 파일 양식 mp4
     * 3. 업로드 최대 사이즈 1GB
     * */
    private static final String ALLOWED_EXTENSION = ".mp4";
    private static final String ALLOWED_MIME = "video/mp4";
    private static final long MAX_FILE_SIZE = 1024L * 1024L * 1024L;

    /**
     * 영상 업로드 처리 전체 흐름
     * 1) 파일 검증
     * 2) 업로드용 key 생성
     * 3) GCP 업로드 수행
     * 4) 영상 길이(duration) 추출
     * 5) DB 반영 (기존 있으면 update, 없으면 insert)
     */
    @Transactional
    public void createVideoUploadUrl(UploadVideoRequest request)
            throws IOException {

        MultipartFile file = request.file();
        Long lessonId = request.lessonId();

        // 업로드 파일 검증
        validateFile(file);

        // 저장될 파일 key 생성
        String extension = ".mp4";
        String videoFileName = "videos/lesson-" + lessonId + "-" + java.util.UUID.randomUUID() + extension;

        // 업로드된 영상 길이 추출
        Integer durationSec = extractDuration(file);

        // GCP 업로드 및 URL 생성
        gcpFileUploadService.createUploadUrl(videoFileName,file);

        // DB 저장 (기존 존재 시 업데이트 or 없으면 신규 생성)
        ContentMedia foundContentMedia = contentMediaRepository.findByLessonId(lessonId)
                .orElse(null);

        if (foundContentMedia != null) {
            // 기존 영상 있으면 -> 파일, 길이 업데이트
            foundContentMedia.changeFile(videoFileName, durationSec);
        } else {
            // 없으면 신규 영상 저장
            ContentMedia created = ContentMedia.createContentMedia(
                    lessonId, videoFileName, durationSec
            );
            contentMediaRepository.save(created);
        }
    }

    /**
     * FFmpegFrameGrabber 사용하여 영상 duration(초 단위) 추출
     * - MultipartFile → 임시 파일 변환 후 분석
     * - 마이크로초 기반 duration 값을 초 단위로 환산
     */
    private Integer extractDuration(MultipartFile file) {
        File tempFile = null;

        try {
            // MultipartFile → 임시 파일 저장
            tempFile = File.createTempFile("upload-", ".mp4");
            file.transferTo(tempFile);

            // FFmpegFrameGrabber로 영상 메타데이터 분석
            try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(tempFile)) {
                grabber.start();

                long durationMicro = grabber.getLengthInTime(); // 마이크로초
                double secondsDouble = durationMicro / 1_000_000.0;

                long seconds = (long) Math.ceil(secondsDouble); // 올림 처리해서 초 단위 계산

                grabber.stop();
                return (int) seconds;

            }
        } catch (Exception e) {
            throw new RuntimeException("영상 길이 분석 실패", e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }

        }
    }

    private void validateFile(MultipartFile file){
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("유효하지 않은 파일명입니다. 확장자가 필요합니다.");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

        if (!extension.equals(ALLOWED_EXTENSION)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. mp4만 업로드 가능합니다.");
        }

        if (!ALLOWED_MIME.equalsIgnoreCase(file.getContentType())) {
            throw new IllegalArgumentException("지원하지 않는 MIME 타입입니다. video/mp4만 허용됩니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일이 너무 큽니다. 최대 업로드 크기는 1GB입니다.");
        }
    }
}
