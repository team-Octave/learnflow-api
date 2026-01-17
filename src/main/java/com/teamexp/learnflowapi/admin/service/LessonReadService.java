package com.teamexp.learnflowapi.admin.service;

import com.teamexp.learnflowapi.admin.dto.LessonReadDetailResponse;
import com.teamexp.learnflowapi.admin.exception.LectureNotFoundException;
import com.teamexp.learnflowapi.content.service.ContentMediaService;
import com.teamexp.learnflowapi.lecture.exception.LessonNotFoundException;
import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.Lesson;
import com.teamexp.learnflowapi.lecture.repository.LectureAdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LessonReadService {

    // TODO : Lesson 조회하는 Service가 있어야 하나 ?
    private final LectureAdminRepository lectureAdminRepository;
    // TODO : Signed URL을 생성하기 위해 해당 서비스를 가져오는게 맞을까 ?..
    private final ContentMediaService contentMediaService;

    public LessonReadService(LectureAdminRepository lectureAdminRepository, ContentMediaService contentMediaService) {
        this.lectureAdminRepository = lectureAdminRepository;
        this.contentMediaService = contentMediaService;
    }

    public LessonReadDetailResponse readLesson(Long lectureId, Long lessonId) {

        Lecture foundLecture = lectureAdminRepository
            .findByIdWithChaptersAndLessonsAndQuizzes(lectureId)
            .orElseThrow(LectureNotFoundException::new);

        Lesson foundLesson = foundLecture.getChapters().stream()
            .flatMap(chapter -> chapter.getLessons().stream())
            .filter(lesson -> lesson.getId().equals(lessonId))
            .findFirst()
            .orElseThrow(LessonNotFoundException::new);

        // Signed URL
        String signedUrl = null;
        if (foundLesson.getLessonType().getDisplayName().equals("VIDEO")) {
            signedUrl = contentMediaService.getStreamingUrl(foundLesson.getId());
        }


        return LessonReadDetailResponse.from(foundLesson, signedUrl);
    }

}
