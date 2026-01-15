package com.teamexp.learnflowapi.admin.service;

import com.teamexp.learnflowapi.admin.dto.LessonReadDetailResponse;
import com.teamexp.learnflowapi.admin.exception.LectureNotFoundException;
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

    public LessonReadService(LectureAdminRepository lectureAdminRepository) {
        this.lectureAdminRepository = lectureAdminRepository;
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

        return LessonReadDetailResponse.from(foundLesson);
    }

}
