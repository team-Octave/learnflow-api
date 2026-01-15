package com.teamexp.learnflowapi.lecture.service;

import com.teamexp.learnflowapi.lecture.dto.request.ChapterCreateRequest;
import com.teamexp.learnflowapi.lecture.dto.request.ChapterUpdateRequest;
import com.teamexp.learnflowapi.lecture.dto.response.ChapterResponse;
import com.teamexp.learnflowapi.lecture.exception.LectureAlreadyPublishedException;
import com.teamexp.learnflowapi.lecture.exception.LectureNotFoundException;
import com.teamexp.learnflowapi.lecture.model.Chapter;
import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.LectureStatus;
import com.teamexp.learnflowapi.lecture.repository.ChapterRepository;
import com.teamexp.learnflowapi.lecture.repository.LectureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Chapter 도메인의 비즈니스 로직을 담당하는 서비스.
 */
@Service
@Transactional(readOnly = true)
public class ChapterService {

    private final LectureRepository lectureRepository;
    private final ChapterRepository chapterRepository;
    private final LectureAccessValidator lectureAccessValidator;

    public ChapterService(
        LectureRepository lectureRepository,
        ChapterRepository chapterRepository,
        LectureAccessValidator lectureAccessValidator
    ) {
        this.lectureRepository = lectureRepository;
        this.chapterRepository = chapterRepository;
        this.lectureAccessValidator = lectureAccessValidator;
    }

    @Transactional
    public ChapterResponse addChapter(Long lectureId, ChapterCreateRequest request, String instructorId) {
        Lecture lecture = findEditableLectureWithChaptersAndLessons(lectureId, instructorId);

        Chapter chapter = Chapter.createChapter(request.chapterTitle(), lecture.getChapters().size());
        lecture.addChapter(chapter);

        Chapter savedChapter = chapterRepository.save(chapter);
        return ChapterResponse.from(savedChapter);
    }

    @Transactional
    public ChapterResponse updateChapter(Long lectureId, Long chapterId, ChapterUpdateRequest request, String instructorId) {
        Lecture lecture = findEditableLectureWithChaptersAndLessons(lectureId, instructorId);

        Chapter chapter = lecture.findByChapterId(chapterId);
        chapter.updateTitle(request.chapterTitle());

        Chapter savedChapter = chapterRepository.save(chapter);
        return ChapterResponse.from(savedChapter);
    }

    @Transactional
    public void deleteChapter(Long lectureId, Long chapterId, String instructorId) {
        Lecture lecture = findEditableLectureWithChaptersAndLessons(lectureId, instructorId);
        lecture.removeChapter(chapterId);
        lectureRepository.save(lecture);
    }

    private Lecture findEditableLectureWithChaptersAndLessons(Long lectureId, String instructorId) {
        Lecture lecture = findLectureWithChaptersAndLessons(lectureId);
        validateNotDeleted(lecture);
        lectureAccessValidator.validateOwnership(lecture, instructorId);
        if (lecture.getStatus() == LectureStatus.AVAILABLE) {
            throw new LectureAlreadyPublishedException();
        }
        return lecture;
    }

    private Lecture findLectureWithChaptersAndLessons(Long lectureId) {
        Lecture lecture = lectureRepository.findByIdWithChaptersAndLessons(lectureId)
            .orElseThrow(() -> new LectureNotFoundException());
        validateNotDeleted(lecture);
        return lecture;
    }

    private void validateNotDeleted(Lecture lecture) {
        if (lecture.isDeleteFlag()) {
            throw new LectureNotFoundException();
        }
    }
}
