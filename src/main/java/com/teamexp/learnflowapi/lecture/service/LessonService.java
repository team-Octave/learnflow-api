package com.teamexp.learnflowapi.lecture.service;

import com.teamexp.learnflowapi.lecture.dto.request.LessonCreateRequest;
import com.teamexp.learnflowapi.lecture.dto.request.LessonUpdateRequest;
import com.teamexp.learnflowapi.lecture.dto.request.QuizUpdateListRequest;
import com.teamexp.learnflowapi.lecture.dto.request.QuizUpdateRequest;
import com.teamexp.learnflowapi.lecture.dto.response.LessonResponse;
import com.teamexp.learnflowapi.lecture.exception.LectureAlreadyPublishedException;
import com.teamexp.learnflowapi.lecture.exception.LectureNotFoundException;
import com.teamexp.learnflowapi.lecture.exception.LessonNotFoundException;
import com.teamexp.learnflowapi.lecture.exception.LessonQuizCountInvalidException;
import com.teamexp.learnflowapi.lecture.exception.LessonTypeInvalidException;
import com.teamexp.learnflowapi.lecture.exception.LessonVideoUrlInvalidException;
import com.teamexp.learnflowapi.lecture.exception.QuizLessonMismatchException;
import com.teamexp.learnflowapi.lecture.exception.QuizNotFoundException;
import com.teamexp.learnflowapi.lecture.model.Chapter;
import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.LectureStatus;
import com.teamexp.learnflowapi.lecture.model.Lesson;
import com.teamexp.learnflowapi.lecture.model.LessonType;
import com.teamexp.learnflowapi.lecture.model.Quiz;
import com.teamexp.learnflowapi.lecture.repository.LectureRepository;
import com.teamexp.learnflowapi.lecture.repository.LessonRepository;
import com.teamexp.learnflowapi.lecture.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lesson 도메인의 비즈니스 로직을 담당하는 서비스.
 */
@Service
@Transactional(readOnly = true)
public class LessonService {

    private final LectureRepository lectureRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final LectureAccessValidator lectureAccessValidator;

    public LessonService(
        LectureRepository lectureRepository,
        LessonRepository lessonRepository,
        QuizRepository quizRepository,
        LectureAccessValidator lectureAccessValidator
    ) {
        this.lectureRepository = lectureRepository;
        this.lessonRepository = lessonRepository;
        this.quizRepository = quizRepository;
        this.lectureAccessValidator = lectureAccessValidator;
    }

    @Transactional
    public LessonResponse addLesson(Long lectureId, Long chapterId, LessonCreateRequest request, String instructorId) {
        Lecture lecture = findEditableLectureWithChaptersAndLessons(lectureId, instructorId);

        validateLessonCreateRequest(request);

        Chapter chapter = lecture.findByChapterId(chapterId);
        Lesson lesson = Lesson.createLesson(
            request.lessonType(),
            request.lessonTitle(),
            chapter.getLessons().size(),
            request.isFreePreview(),
            (request.lessonType() == LessonType.QUIZ ? null : request.videoUrl())
        );
        chapter.addLesson(lesson);

        if (lesson.getLessonType() == LessonType.QUIZ) {
            request.quizQuestions().forEach(q ->
                lesson.addQuiz(Quiz.createQuiz(
                    q.questionOrder(),
                    q.question(),
                    q.correct()
                ))
            );
        }

        Lesson savedLesson = lessonRepository.save(lesson);
        return toLessonResponse(savedLesson);
    }

    @Transactional
    public LessonResponse updateLesson(Long lectureId, Long lessonId, LessonUpdateRequest request, String instructorId) {
        Lecture lecture = findEditableLectureWithChaptersAndLessons(lectureId, instructorId);
        Chapter chapter = findChapterContainingLesson(lecture, lessonId);
        Lesson lesson = chapter.findByLessonId(lessonId);

        if (request.lessonTitle() != null) {
            lesson.updateTitle(request.lessonTitle());
        }
        if (request.isFreePreview() != null) {
            lesson.updateFreePreview(request.isFreePreview());
        }
        if (request.videoUrl() != null) {
            validateLessonVideoUrlUpdate(lesson, request.videoUrl());
            lesson.updateVideoUrl(request.videoUrl());
        }

        if (request.quizQuestions() != null) {
            validateLessonQuizQuestionsUpdate(lesson, request.quizQuestions());
            upsertLessonQuizzes(lesson, request.quizQuestions());
            validateQuizCountInvariant(lesson);
        }

        Lesson savedLesson = lessonRepository.save(lesson);
        return toLessonResponse(savedLesson);
    }

    @Transactional
    public void deleteLesson(Long lectureId, Long lessonId, String instructorId) {
        Lecture lecture = findEditableLectureWithChaptersAndLessons(lectureId, instructorId);
        Chapter chapter = findChapterContainingLesson(lecture, lessonId);

        quizRepository.deleteByLessonId(lessonId);
        chapter.removeLesson(lessonId);

        lectureRepository.save(lecture);
    }

    public LessonResponse getLesson(Long lectureId, Long lessonId) {
        Lecture lecture = findLectureWithChaptersAndLessons(lectureId);

        Chapter chapter = findChapterContainingLesson(lecture, lessonId);
        Lesson lesson = chapter.findByLessonId(lessonId);

        return toLessonResponse(lesson);
    }

    // ===== Private Helper Methods =====

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

    private Chapter findChapterContainingLesson(Lecture lecture, Long lessonId) {
        return lecture.getChapters().stream()
            .filter(chapter -> chapter.getLessons().stream().anyMatch(lesson -> lesson.getId().equals(lessonId)))
            .findFirst()
            .orElseThrow(LessonNotFoundException::new);
    }

    private void validateLessonCreateRequest(LessonCreateRequest request) {
        if (request == null || request.lessonType() == null) {
            throw new LessonTypeInvalidException();
        }

        if (request.lessonType() == LessonType.QUIZ) {
            // QUIZ: videoUrl must be null/blank
            if (request.videoUrl() != null && !request.videoUrl().isBlank()) {
                throw new LessonVideoUrlInvalidException();
            }
            int quizCount = request.quizQuestions() == null ? 0 : request.quizQuestions().size();
            if (quizCount < 1 || quizCount > 10) {
                throw new LessonQuizCountInvalidException();
            }
            return;
        }

        // VIDEO
        if (request.videoUrl() == null || request.videoUrl().isBlank()) {
            throw new LessonVideoUrlInvalidException();
        }
        if (request.quizQuestions() != null && !request.quizQuestions().isEmpty()) {
            throw new LessonQuizCountInvalidException();
        }
    }

    private void validateLessonVideoUrlUpdate(Lesson lesson, String videoUrl) {
        if (lesson == null || lesson.getLessonType() == null) {
            throw new LessonTypeInvalidException();
        }

        if (lesson.getLessonType() == LessonType.QUIZ) {
            // QUIZ: videoUrl update is not allowed
            throw new LessonVideoUrlInvalidException();
        }

        // VIDEO: videoUrl must be non-blank when provided
        if (videoUrl == null || videoUrl.isBlank()) {
            throw new LessonVideoUrlInvalidException();
        }
    }

    private void validateLessonQuizQuestionsUpdate(Lesson lesson, QuizUpdateListRequest request) {
        if (lesson == null || lesson.getLessonType() == null) {
            throw new LessonTypeInvalidException();
        }
        if (lesson.getLessonType() != LessonType.QUIZ) {
            // VIDEO: quiz payload is not allowed
            throw new LessonQuizCountInvalidException();
        }
        int count = request == null || request.quizzes() == null ? 0 : request.quizzes().size();
        if (count < 1 || count > 10) {
            throw new LessonQuizCountInvalidException();
        }
    }

    private void validateQuizCountInvariant(Lesson lesson) {
        if (lesson == null || lesson.getLessonType() == null) {
            return;
        }
        if (lesson.getLessonType() != LessonType.QUIZ) {
            return;
        }
        int count = lesson.getQuizzes() == null ? 0 : lesson.getQuizzes().size();
        if (count < 1 || count > 10) {
            throw new LessonQuizCountInvalidException();
        }
    }

    private void upsertLessonQuizzes(Lesson lesson, QuizUpdateListRequest request) {
        if (request == null || request.quizzes() == null) {
            return;
        }

        List<QuizUpdateRequest> items = request.quizzes();
        List<Long> requestedIds = items.stream()
            .map(QuizUpdateRequest::id)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        if (!requestedIds.isEmpty()) {
            List<Quiz> byIds = quizRepository.findAllById(requestedIds);
            if (byIds.size() != requestedIds.size()) {
                throw new QuizNotFoundException();
            }
            boolean hasMismatch = byIds.stream()
                .anyMatch(q ->
                    q.getLesson() == null
                        || q.getLesson().getId() == null
                        || !q.getLesson().getId().equals(lesson.getId())
                );
            if (hasMismatch) {
                throw new QuizLessonMismatchException();
            }
        }

        Set<Long> keepIds = new HashSet<>(requestedIds);
        lesson.getQuizzes().removeIf(q -> q.getId() != null && !keepIds.contains(q.getId()));

        Map<Long, Quiz> existingById = lesson.getQuizzes().stream()
            .filter(q -> q.getId() != null)
            .collect(Collectors.toMap(Quiz::getId, q -> q));

        for (QuizUpdateRequest item : items) {
            if (item.id() == null) {
                Quiz newQuiz = Quiz.createQuiz(item.orderIndex(), item.question(), item.correct());
                lesson.addQuiz(newQuiz);
                continue;
            }

            Quiz quiz = existingById.get(item.id());
            if (quiz == null) {
                throw new QuizNotFoundException();
            }
            quiz.update(item.orderIndex(), item.question(), item.correct());
        }
    }

    private LessonResponse toLessonResponse(Lesson lesson) {
        if (lesson.getLessonType() == LessonType.QUIZ) {
            List<LessonResponse.QuizQuestionResponse> quizQuestions =
                lesson.getQuizzes().stream()
                    .sorted(Comparator.comparing(Quiz::getOrderIndex))
                    .map(q -> new LessonResponse.QuizQuestionResponse(
                        q.getId(),
                        q.getQuestion(),
                        q.getOrderIndex(),
                        q.getCorrect()
                    ))
                    .collect(Collectors.toList());

            return LessonResponse.withoutVideo(
                lesson.getId(),
                lesson.getLessonTitle(),
                lesson.getLessonType().getDisplayName(),
                lesson.getLessonOrder(),
                lesson.getIsFreePreview(),
                quizQuestions
            );
        }

        return LessonResponse.withoutQuiz(
            lesson.getId(),
            lesson.getLessonTitle(),
            lesson.getLessonType().getDisplayName(),
            lesson.getLessonOrder(),
            lesson.getIsFreePreview(),
            lesson.getVideoUrl()
        );
    }
}
