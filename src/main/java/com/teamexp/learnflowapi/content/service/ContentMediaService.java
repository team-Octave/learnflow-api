package com.teamexp.learnflowapi.content.service;

import com.teamexp.learnflowapi.content.dto.UploadVideoRequest;
import com.teamexp.learnflowapi.content.external.GcpFileUploadService;
import com.teamexp.learnflowapi.content.repository.ContentMediaRepository;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    public void createVideoUploadUrl(UploadVideoRequest request)
            throws IOException {

        MultipartFile file = request.file();
        Long lessonId = request.lessonId();

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


        String videoFileName = "videos/lesson-" + lessonId + "-" + java.util.UUID.randomUUID() + extension;

        String url = gcpFileUploadService.createUploadUrl(
                videoFileName,
                file
        );


        //todo: 1. contentMedia file_key에 파일 url 필드 저장
        //      2. duration 추출 후 저장





    }
}
