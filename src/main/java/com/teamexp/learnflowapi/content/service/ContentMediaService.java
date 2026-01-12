package com.teamexp.learnflowapi.content.service;

import com.teamexp.learnflowapi.content.dto.UploadVideoRequest;
import com.teamexp.learnflowapi.content.model.ContentMedia;
import com.teamexp.learnflowapi.content.repository.ContentMediaRepository;

import java.io.File;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ContentMediaService {

    private final ContentMediaRepository contentMediaRepository;
    private final VideoUploadAsyncService videoUploadAsyncService;
    private static final Logger log = LoggerFactory.getLogger(ContentMediaService.class);

    /*
     * 1. 영상 파일 형식 .mp4
     * 2. 영상 파일 업로드 파일 양식 mp4
     * 3. 업로드 최대 사이즈 1GB
     * */
    private static final String ALLOWED_EXTENSION = ".mp4";
    private static final String ALLOWED_MIME = "video/mp4";
    private static final long MAX_FILE_SIZE = 1024L * 1024L * 1024L;

    @Transactional
    public Long requestVideoUpload(UploadVideoRequest request) throws IOException {

        MultipartFile file = request.file();

        // 업로드 파일 검증
        validateFile(file);

        // multipartFile -> 임시 파일로 복사하기
        File tempFile = File.createTempFile("upload-", ".mp4");
        file.transferTo(tempFile);

        // lesson Id로 먼저 ContentMedia 생성
        ContentMedia media = ContentMedia.createPending(request.lessonId());
        contentMediaRepository.save(media);

        try {
            // 비동기 업로드 작업
            videoUploadAsyncService.uploadVideoFileAsync(media.getId(), tempFile);

        }catch(TaskRejectedException ex){
                // 🔥 큐/스레드풀 꽉 차서 비동기 작업 조차 못 들어간 경우

                // 상태를 FAILED로 바꿈 → 고아 PENDING 방지
                media.failUpload();

                // temp 파일 삭제 (비동기 실행이 안 됐으니까 우리가 직접 삭제)
                if (tempFile.exists()) {
                    tempFile.delete();
                }

                // 로깅
                log.error("[VideoUpload] 업로드 작업 제출 실패 - mediaId={}, error={}",
                        media.getId(), ex.getMessage(), ex);

                throw ex;

            }
        return media.getId();
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
