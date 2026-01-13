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

    public Long requestVideoUpload(UploadVideoRequest request) throws IOException {

        MultipartFile file = request.file();
        Long lessonId = request.lessonId();

        // 업로드 파일 검증
        validateFile(file);

        // multipartFile -> 임시 파일로 복사하기
        File tempFile = File.createTempFile("upload-", ".mp4");

        ContentMedia media = ContentMedia.createPending(lessonId);
        contentMediaRepository.save(media);

        try {
            file.transferTo(tempFile);

            // 비동기 업로드 작업
            videoUploadAsyncService.uploadVideoFileAsync(media.getId(), tempFile);

            return media.getId();

        }catch(TaskRejectedException ex) {
            // 큐/ 스레드풀 꽉 차서 비동기 작업 실행 안된 경우
            media.failUpload();   // 상태 = FAILED
            contentMediaRepository.save(media);

            // temp 파일 삭제
            if (tempFile.exists()) {
                tempFile.delete();
            }

            log.error("[VideoUpload] 큐 거절 - mediaId={}, error={}",
                    media.getId(), ex.getMessage(), ex);

            throw ex;

        } catch (Exception ex) {
            // 다른 예외 상황의 경우
            media.failUpload();   // 상태 = FAILED
            contentMediaRepository.save(media);

            if (tempFile.exists()) {
                tempFile.delete();
            }

            log.error("[VideoUpload] 업로드 전 단계에서 예외 발생 - lessonId={}, error={}",
                    request.lessonId(), ex.getMessage(), ex);

            throw ex;
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
