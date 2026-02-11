package com.teamexp.learnflowapi.lecture.service;

import com.teamexp.learnflowapi.enrollment.repository.EnrollmentRepository;
import com.teamexp.learnflowapi.content.service.ContentMediaService;
import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;
import com.teamexp.learnflowapi.lecture.dto.request.LessonCreateRequest;
import com.teamexp.learnflowapi.lecture.dto.request.LessonUpdateRequest;
import com.teamexp.learnflowapi.lecture.dto.request.QuizUpdateListRequest;
import com.teamexp.learnflowapi.lecture.dto.request.QuizUpdateRequest;
import com.teamexp.learnflowapi.lecture.dto.response.LessonResponse;
import com.teamexp.learnflowapi.lecture.exception.LectureAlreadyPublishedException;
import com.teamexp.learnflowapi.lecture.exception.LectureNotFoundException;
import com.teamexp.learnflowapi.lecture.exception.LessonAccessDeniedException;
import com.teamexp.learnflowapi.lecture.exception.LessonNotFoundException;
import com.teamexp.learnflowapi.lecture.exception.LessonQuizCountInvalidException;
import com.teamexp.learnflowapi.lecture.exception.LessonTypeInvalidException;
import com.teamexp.learnflowapi.lecture.exception.LessonVideoUrlInvalidException;
import com.teamexp.learnflowapi.lecture.exception.MembershipNotFoundException;
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
import com.teamexp.learnflowapi.membership.model.Membership;
import com.teamexp.learnflowapi.membership.repository.MembershipRepository;
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
    private final ContentMediaService contentMediaService;
    private final EnrollmentRepository enrollmentRepository;
    private final MembershipRepository  membershipRepository;

    public LessonService(
            LectureRepository lectureRepository,
            LessonRepository lessonRepository,
            QuizRepository quizRepository,
            LectureAccessValidator lectureAccessValidator,
            ContentMediaService contentMediaService,
            EnrollmentRepository enrollmentRepository, MembershipRepository membershipRepository
    ) {
        this.lectureRepository = lectureRepository;
        this.lessonRepository = lessonRepository;
        this.quizRepository = quizRepository;
        this.lectureAccessValidator = lectureAccessValidator;
        this.contentMediaService = contentMediaService;
        this.enrollmentRepository = enrollmentRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional
    public LessonResponse addLesson(Long lectureId, Long chapterId, LessonCreateRequest request, String instructorId) {
        Lecture lecture = findEditableLectureWithChaptersAndLessons(lectureId, instructorId);

        validateLessonCreateRequest(request);

        Chapter chapter = lecture.findByChapterId(chapterId);
        // VIDEO 타입도 videoUrl은 저장하지 않음 (mediaId 기반 바인딩 사용)
        Lesson lesson = Lesson.createLesson(
            request.lessonType(),
            request.lessonTitle(),
            chapter.getLessons().size(),
            request.isFreePreview(),
            null  // videoUrl is no longer stored; use mediaId binding instead
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

        // VIDEO 타입이고 mediaId가 제공된 경우, lessonId와 바인딩
        if (savedLesson.getLessonType() == LessonType.VIDEO && request.mediaId() != null) {
            contentMediaService.bindMediaToLesson(savedLesson.getId(), request.mediaId());
        }

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
        // mediaId로 새 미디어 바인딩 (VIDEO 타입만)
        if (request.mediaId() != null) {
            validateLessonMediaIdUpdate(lesson);
            contentMediaService.bindMediaToLesson(lessonId, request.mediaId());
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

    /**
     * V2 레슨 단건 조회.
     *
     * <p>NOTE: VIDEO 레슨의 videoUrl은 보안을 위해 signedUrl로 내려준다.
     * 강의 상세/목록 응답에서는 VIDEO의 videoUrl을 내려주지 않는다.
     */
    public LessonResponse getLesson(Long lectureId, Long lessonId, String userId) {
        if (userId == null || userId.isBlank()) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        Lecture lecture = findLectureWithChaptersAndLessons(lectureId);

        Chapter chapter = findChapterContainingLesson(lecture, lessonId);
        Lesson lesson = chapter.findByLessonId(lessonId);

        if (lesson.getLessonType() == LessonType.QUIZ) {
            return toLessonResponse(lesson);
        }

        validateVideoLessonAccess(lecture, lectureId, lesson, userId);

        String signedUrl = contentMediaService.getStreamingUrl(lessonId);
        return LessonResponse.withoutQuiz(
            lesson.getId(),
            lesson.getLessonTitle(),
            lesson.getLessonType().getDisplayName(),
            lesson.getLessonOrder(),
            lesson.getIsFreePreview(),
            signedUrl
        );
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
            if (request.mediaId() != null) {
                throw new LessonVideoUrlInvalidException();
            }
            int quizCount = request.quizQuestions() == null ? 0 : request.quizQuestions().size();
            if (quizCount < 1 || quizCount > 10) {
                throw new LessonQuizCountInvalidException();
            }
            return;
        }

        // VIDEO
        if (request.mediaId() == null) {
            throw new LessonVideoUrlInvalidException();
        }
        if (request.quizQuestions() != null && !request.quizQuestions().isEmpty()) {
            throw new LessonQuizCountInvalidException();
        }
    }

    private void validateLessonMediaIdUpdate(Lesson lesson) {
        if (lesson == null || lesson.getLessonType() == null) {
            throw new LessonTypeInvalidException();
        }

        if (lesson.getLessonType() == LessonType.QUIZ) {
            // QUIZ: mediaId update is not allowed
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
        int count = request == null || request.quizQuestions() == null ? 0 : request.quizQuestions().size();
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
        if (request == null || request.quizQuestions() == null) {
            return;
        }

        List<QuizUpdateRequest> items = request.quizQuestions();
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
                Quiz newQuiz = Quiz.createQuiz(item.questionOrder(), item.question(), item.correct());
                lesson.addQuiz(newQuiz);
                continue;
            }

            Quiz quiz = existingById.get(item.id());
            if (quiz == null) {
                throw new QuizNotFoundException();
            }
            quiz.update(item.questionOrder(), item.question(), item.correct());
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

        // NOTE: VIDEO 레슨의 signedUrl은 레슨 단건 조회(getLesson)에서만 발급하여 내려준다.
        return LessonResponse.withoutQuiz(
            lesson.getId(),
            lesson.getLessonTitle(),
            lesson.getLessonType().getDisplayName(),
            lesson.getLessonOrder(),
            lesson.getIsFreePreview(),
            null
        );
    }

    /**
     * VIDEO 레슨 스트리밍 URL 접근 제어:
     * - freeLecture: 로그인만 되어있으면 허용
     * - 그 외: 강의 소유 강사 또는 수강(enrollment) 중인 유저만 허용
     */
    private void validateVideoLessonAccess(Lecture lecture, Long lectureId, Lesson lesson, String userId) {
        Membership membership = membershipRepository.findByUserId(userId).orElseThrow(MembershipNotFoundException::new);

        if (Boolean.TRUE.equals(lecture.isFreeLecture())) {
            return;
        }
        if (lecture.getInstructorId() != null && lecture.getInstructorId().equals(userId)) { // 강의 소유자 여부 확인
            return;
        }
        if(!membership.isActive()){
            return;
        }
        // 기존: 수강(enrollment) 중인 유저 여부 확인 -> 수정: 수강생 여부 확인+ membership 활성 상태 확인
        if (enrollmentRepository.existsByUserIdAndLectureId(userId, lectureId)) { // 수강생 여부 확인
            return;
        }

        throw new LessonAccessDeniedException();
    }
}
