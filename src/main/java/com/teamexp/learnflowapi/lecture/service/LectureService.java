package com.teamexp.learnflowapi.lecture.service;

import com.teamexp.learnflowapi.admin.model.Approval;
import com.teamexp.learnflowapi.admin.repository.ApprovalRepository;
import com.teamexp.learnflowapi.lecture.model.Quiz;
import com.teamexp.learnflowapi.lecture.repository.QuizRepository;
import com.teamexp.learnflowapi.content.repository.ThumbnailRepository;
import com.teamexp.learnflowapi.lecture.dto.request.CurriculumBindRequest;
import com.teamexp.learnflowapi.lecture.dto.request.LectureBaseUpdateRequest;
import com.teamexp.learnflowapi.lecture.dto.request.LectureCreateRequest;
import com.teamexp.learnflowapi.lecture.dto.request.LectureCreateRequestV2;
import com.teamexp.learnflowapi.lecture.dto.request.LectureFullCreateRequest;
import com.teamexp.learnflowapi.lecture.dto.response.LectureFullCreateResponse;
import com.teamexp.learnflowapi.lecture.dto.response.LectureResponse;
import com.teamexp.learnflowapi.lecture.dto.response.PublishedResponse;
import com.teamexp.learnflowapi.lecture.exception.LectureAlreadyPublishedException;
import com.teamexp.learnflowapi.lecture.exception.LectureCannotUpdateException;
import com.teamexp.learnflowapi.lecture.exception.LectureDeleteBlockedException;
import com.teamexp.learnflowapi.lecture.exception.LectureNotFoundException;
import com.teamexp.learnflowapi.lecture.model.*;
import com.teamexp.learnflowapi.lecture.repository.LectureRepository;
import com.teamexp.learnflowapi.lecture.repository.LectureStatisticRepository;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Lecture 도메인의 비즈니스 로직을 담당하는 서비스.
 * 
 * <p>TODO [Phase 1-1] SecurityEventLogger 통합 계획:
 * - LectureAccessValidator와 연계하여 보안 이벤트 로깅
 * - 민감한 작업(삭제, 발행 등) 수행 시 감사 로그 기록
 * 
 * @see LectureAccessValidator
 */
@Service
@Transactional(readOnly = true)
public class LectureService {

    // TODO : 썸네일 업로드 오류 시, 저장 안되는 오류가 있음. 이를 방지하고자 기본 이미지 URL 설정
    @Value("${spring.application.default-thumbnail}")
    private String defaultThumbnailUrl;

    // Repository
    private final LectureRepository lectureRepository;
    private final LectureStatisticRepository lectureStatisticRepository;
    private final ThumbnailRepository thumbnailRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;

    // Service or Validator
    private final LectureAccessValidator lectureAccessValidator;
    private final ApprovalRepository approvalRepository;
    
    // TODO [Phase 1-1]: SecurityEventLogger 의존성 주입 예정
    // private final SecurityEventLogger securityEventLogger;

    public LectureService(
        LectureRepository lectureRepository, 
        LectureStatisticRepository lectureStatisticRepository, 
        ThumbnailRepository thumbnailRepository, 
        UserRepository userRepository, 
        QuizRepository quizRepository, 
        LectureAccessValidator lectureAccessValidator,
        ApprovalRepository approvalRepository
    ) {
        this.lectureRepository = lectureRepository;
        this.lectureStatisticRepository = lectureStatisticRepository;
        this.thumbnailRepository = thumbnailRepository;
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
        this.lectureAccessValidator = lectureAccessValidator;
        this.approvalRepository = approvalRepository;
    }

    @Deprecated
    @Transactional
    public LectureResponse createLecture(LectureCreateRequest lectureCreateRequest, String instructorId,String userNickname) {
        // 1. 정적 팩토리 메서드로 생성 (객체 생성 로직은 엔티티에 위임)
        Lecture lecture = Lecture.createLecture(
            lectureCreateRequest.title(),
            lectureCreateRequest.description(),
            LectureLevel.forEntity(lectureCreateRequest.level()),
            lectureCreateRequest.categoryId(), // check category existence if needed
            instructorId,
            lectureCreateRequest.thumbnailUrl() != null ? lectureCreateRequest.thumbnailUrl() : defaultThumbnailUrl // temperary thumbnailUrl
        );

        // 2. 저장
        Lecture savedLecture = lectureRepository.save(lecture);

        // 3. LectureStatistic 초기 생성 (모든 값이 0으로 초기화)
        LectureStatistic initialStatistic = LectureStatistic.createInitial(savedLecture);
        lectureStatisticRepository.save(initialStatistic);

        return LectureResponse.simpleFrom(savedLecture, userNickname);
    }

    // OVERLOAD for V2
    @Transactional
    public LectureResponse createLecture(LectureCreateRequestV2 lectureCreateRequest, String instructorId,String userNickname) {
        // 1. 정적 팩토리 메서드로 생성 (객체 생성 로직은 엔티티에 위임)
        Lecture lecture = Lecture.createLecture(
            lectureCreateRequest.title(),
            lectureCreateRequest.description(),
            LectureLevel.forEntity(lectureCreateRequest.level()),
            lectureCreateRequest.categoryId(), // check category existence if needed
            instructorId,
            lectureCreateRequest.thumbnailUrl()
        );

        // 2. 저장
        Lecture savedLecture = lectureRepository.save(lecture);

        // 3. LectureStatistic 초기 생성 (모든 값이 0으로 초기화)
        LectureStatistic initialStatistic = LectureStatistic.createInitial(savedLecture);
        lectureStatisticRepository.save(initialStatistic);

        
        // TODO : Change Get Thumbnail_Adapter API with lectureId, if null, return defaultThumbnailUrl
        return LectureResponse.simpleFrom(savedLecture, userNickname);
    }



    @Deprecated
    @Transactional
    public LectureFullCreateResponse createLectureFullCurriculum(
        Long lectureId,
        LectureFullCreateRequest request,
        String instructorId,
        String userNickname
    ) {
        Lecture lecture = findLectureWithValidation(lectureId, instructorId);

        // 1) Chapter/Lesson 엔티티 생성(아직 DTO 변환 안 함)
        IntStream.range(0, request.chapters().size())
            .forEach(chapterIndex -> {
                LectureFullCreateRequest.ChapterRequest chapterRequest = request.chapters().get(chapterIndex);
                Chapter chapter = createChapter(lecture, chapterRequest, chapterIndex);

                IntStream.range(0, chapterRequest.lessons().size())
                    .forEach(lessonIndex -> {
                        LectureFullCreateRequest.LessonRequest lessonRequest = chapterRequest.lessons().get(lessonIndex);
                        createLesson(chapter, lessonRequest, lessonIndex);
                    });
            });

        // 2) save - ID 생성 보장
        persistLecture(lecture);

        // 3) 정렬 확정 + 후처리(현재는 Quiz 바인딩)
        bindAndReorderCurriculum(lecture, request);

        // 4) Response 생성
        return buildCurriculumResponse(lecture, userNickname);
    }
    @Deprecated
    private Chapter createChapter(
        Lecture lecture,
        LectureFullCreateRequest.ChapterRequest request,
        int chapterOrder
    ) {
        Chapter chapter = Chapter.createChapter(request.chapterTitle(), chapterOrder);
        lecture.addChapter(chapter);
        return chapter;
    }
    @Deprecated
    private Lesson createLesson(
        Chapter chapter,
        LectureFullCreateRequest.LessonRequest request,
        int lessonOrder
    ) {
        Lesson lesson = Lesson.createLesson(
            LessonType.forEntity(request.lessonType()),
            request.lessonTitle(),
            lessonOrder,
            request.isFreePreview(),
            request.videoUrl()
        );
        chapter.addLesson(lesson);
        return lesson;
    }
    @Deprecated
    private void persistLecture(Lecture lecture) {
        lectureRepository.save(lecture);
    }

    /**
     * V1 bulk 생성 흐름에서는, save 후 lessonId가 생성된 상태에서 quiz를 생성/저장하고 lesson에 바인딩한다.
     *
     * <p>NOTE: 향후 V2 단계별 생성 + bind/reorder(use-case)에서도 재사용될 수 있도록 메서드 이름을 유지한다.
     */
    @Deprecated
    private void bindAndReorderCurriculum(Lecture lecture, LectureFullCreateRequest request) {
        // Set을 정렬된 List로 한 번만 변환
        List<Chapter> sortedChapters = lecture.getChapters().stream()
            .sorted(Comparator.comparing(Chapter::getChapterOrder))
            .toList();

        for (int chapterIndex = 0; chapterIndex < request.chapters().size(); chapterIndex++) {
            LectureFullCreateRequest.ChapterRequest chapterRequest = request.chapters().get(chapterIndex);
            Chapter chapter = sortedChapters.get(chapterIndex);

            // Lesson도 정렬된 List로 한 번만 변환
            List<Lesson> sortedLessons = chapter.getLessons().stream()
                .sorted(Comparator.comparing(Lesson::getLessonOrder))
                .toList();

            for (int lessonIndex = 0; lessonIndex < chapterRequest.lessons().size(); lessonIndex++) {
                LectureFullCreateRequest.LessonRequest lessonRequest = chapterRequest.lessons().get(lessonIndex);
                Lesson lesson = sortedLessons.get(lessonIndex);

                if (lesson.getLessonType() == LessonType.QUIZ && lessonRequest.quizQuestions() != null) {
                    List<Quiz> quizList = lessonRequest.quizQuestions().stream()
                        .map(quizQuestion -> Quiz.createQuiz(
                            quizQuestion.questionOrder(),
                            quizQuestion.question(),
                            quizQuestion.correct()
                        ))
                        .toList();

                    quizList.forEach(lesson::addQuiz);
                    quizList.forEach(quizRepository::save);
                }
            }
        }
    }

    @Deprecated
    private LectureFullCreateResponse buildCurriculumResponse(Lecture lecture, String userNickname) {
        List<LectureFullCreateResponse.ChapterResponse> chapterResponses = lecture.getChapters().stream()
            .map(chapter -> {
                List<LectureFullCreateResponse.LessonResponse> lessonResponses = chapter.getLessons().stream()
                    .map(lesson -> {
                        if (lesson.getLessonType() == LessonType.QUIZ) {
                            List<LectureFullCreateResponse.LessonResponse.QuizQuestionResponse> quizQuestions =
                                lesson.getQuizzes().stream()
                                    .sorted(Comparator.comparing(Quiz::getOrderIndex))
                                    .map(quizQuestion -> new LectureFullCreateResponse.LessonResponse.QuizQuestionResponse(
                                        quizQuestion.getId(),
                                        quizQuestion.getQuestion(),
                                        quizQuestion.getOrderIndex(),
                                        quizQuestion.getCorrect()
                                    ))
                                    .collect(Collectors.toList());

                            return LectureFullCreateResponse.LessonResponse.withoutVideo(
                                lesson.getId(),
                                lesson.getLessonTitle(),
                                lesson.getLessonOrder(),
                                lesson.getLessonType().getDisplayName(),
                                lesson.getIsFreePreview(),
                                quizQuestions
                            );
                        }

                        // NOTE: VIDEO 레슨의 signedUrl은 강의 상세/목록/커리큘럼 응답에서 내려주지 않음.
                        // 재생 시점에 V2 레슨 단건 조회에서 signedUrl을 발급한다.
                        return LectureFullCreateResponse.LessonResponse.withoutQuiz(
                            lesson.getId(),
                            lesson.getLessonTitle(),
                            lesson.getLessonOrder(),
                            lesson.getLessonType().getDisplayName(),
                            lesson.getIsFreePreview(),
                            null
                        );
                    })
                    .collect(Collectors.toList());

                return new LectureFullCreateResponse.ChapterResponse(
                    chapter.getId(),
                    chapter.getChapterTitle(),
                    chapter.getChapterOrder(),
                    lessonResponses
                );
            })
            .collect(Collectors.toList());

        return new LectureFullCreateResponse(
            lecture.getId(),
            lecture.getTitle(),
            lecture.getDescription(),
            lecture.getCategoryId(),
            lecture.getLevel().getDisplayName(),
            chapterResponses,
            userNickname
        );
    }

    // =========================
    // V2 Curriculum (incremental)
    // =========================

    @Transactional
    public void bindAndReorderCurriculumV2(Long lectureId, CurriculumBindRequest request, String instructorId) {
        Lecture lecture = findEditableLectureWithChaptersAndLessons(lectureId, instructorId);

        for (CurriculumBindRequest.ChapterOrderRequest chapterOrder : request.chapters()) {
            Chapter chapter = lecture.findByChapterId(chapterOrder.chapterId());
            chapter.changeOrder(chapterOrder.order());

            for (CurriculumBindRequest.LessonOrderRequest lessonOrder : chapterOrder.lessons()) {
                Lesson lesson = chapter.findByLessonId(lessonOrder.lessonId());
                lesson.changeOrder(lessonOrder.order());
            }
        }

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

    // 강의 발행
    // 동시에 여러 강의 발행 요청이 올 경우를 대비해 강의 상태 변경 시 낙관적 락(Optimistic Lock) 적용 고려
    @Transactional
    public PublishedResponse makeAvailableLecture(Long lectureId, String instructorId) {
        Lecture lecture = findLectureWithChaptersAndLessons(lectureId);
        lectureAccessValidator.validateOwnership(lecture, instructorId);

        lecture.makeSubmitted();

        return PublishedResponse.from(lecture.getId(), lecture.getStatus());
    }

    // 강의 목록 조회 (필터링 및 페이지네이션)
    public Page<LectureResponse> getAllLecturesWithFilters(String category, String level, String sort, Pageable pageable) {
        // "ALL" 값 처리
        Integer categoryId = "ALL".equals(category) ? null : (category != null ? Integer.parseInt(category) : null);
        LectureLevel lectureLevel = "ALL".equals(level) ? null : (level != null ? LectureLevel.forEntity(level) : null);
        
        // 정렬 타입 파싱 및 기본값 처리
        String sortBy = sort != null && !sort.isEmpty() ? sort : "POPULAR";
        try {
            LectureSortType.forEntity(sortBy); // 유효성 검증
        } catch (Exception e) {
            sortBy = "POPULAR"; // 유효하지 않은 값은 기본값으로 폴백
        }
        
        // Pageable의 Sort를 제거하여 정렬 타입 파라미터와의 충돌 방지
        Pageable pageableWithoutSort = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize()
        );
        
        // @TODO : 1. make view in DB Lecture with LectureStatistic.
        // @TODO : 2. make index at view case popular/newest/rating.
        // @TODO : 3. get from repository with custom query
        // Since Get Lecture_list case has statistic info? (여러개의 lecture 목록을 호출하는 경우는 반드시 statistic 정보가 필요? 하지않나???)
        // 근데 이거 view 만들어서 처리하는게 맞는지는 잘 모르겠음. 구조는 그런거 생각하고 bijective 하게 만든건 맞는데
        // -> 단건 조회도 사용하긴 함. 생성말고는 다 사용할지도?

        // Repository에서 필터링된 강의 조회
        Page<Lecture> lecturePage = lectureRepository.findByFiltersWithStats(
            categoryId,
            lectureLevel,
            LectureStatus.AVAILABLE,
            sortBy,
            pageableWithoutSort
        );

        // Page<LectureResponse>로 변환
        return lecturePage.map(lecture -> {
            LectureStatistic statistic = lecture.getStatistic();

            // TODO : Change Get Instructor Nickname_list response from Adapter API with userId_list
            String instructorNickname = userRepository.findById(lecture.getInstructorId())
                .map(user -> user.getNickname())
                .orElse("Unknown Instructor");

            return LectureResponse.simpleFromWithStats(lecture, statistic, instructorNickname);
        });
    }

    // 강의 단건 조회
    @Transactional(readOnly = true)
    public LectureResponse getLecture(Long lectureId) {
        Lecture lecture = lectureRepository.findByIdWithChaptersAndLessonsAndQuizzes(lectureId)
            .orElseThrow(LectureNotFoundException::new);
        validateNotDeleted(lecture);

        LectureStatistic statistic = lecture.getStatistic();


        // @Deprecated // TODO: 모든 lecture의 thumbnailUrl 역정규화 완료 후 제거
        // 사실 read 에 이런 setter가 있으면 안되지만, 내부 데이터의 변경에서 사용하는 개념이라 여기에 둠
        if (lecture.getThumbnailUrl() == null) {
            String thumbnailUrl = thumbnailRepository.findFileUrlById(lecture.getThumbnailId());
            if (thumbnailUrl != null) {
                lecture.setThumbnailUrl(thumbnailUrl);
                updateLectureThumbnailUrl(lectureId, thumbnailUrl);
            } else {
                lecture.setThumbnailUrl(defaultThumbnailUrl);
            }
        }

        // TODO : Change Get Instructor Nickname response from Adapter API with userId
        String instructorNickname = userRepository.findById(lecture.getInstructorId())
            .map(user -> user.getNickname())
            .orElse("Unknown Instructor");
        return LectureResponse.fromWithStatics(lecture, statistic, instructorNickname);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateLectureThumbnailUrl(Long lectureId, String thumbnailUrl) {
        Lecture lecture = lectureRepository.findById(lectureId)
            .orElse(null);
        if (lecture == null) {
            return;
        }
        lecture.updateThumbnailUrl(thumbnailUrl);
        lectureRepository.save(lecture);
    }

    /**
     * 강사의 강의 목록 조회 (반려 사유 포함)
     * REJECTED 상태인 강의에는 최신 반려 카테고리/상세 사유가 포함됨
     */
    public Page<LectureResponse> getLecturesByInstructor(String instructorId, Pageable pageable) {
        // Repository에서 페이지네이션된 강의 조회
        // - 정렬은 Lecture.updatedAt DESC로 강제 (통계 업데이트로 목록 순서가 흔들리지 않게)
        // - statistic은 EntityGraph로 함께 로딩
        Pageable pageableWithoutSort = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize()
        );
        Page<Lecture> lecturePage = lectureRepository.findByInstructorIdOrderByUpdatedAtDesc(instructorId, pageableWithoutSort);

        // 반려 사유 배치 조회 (N+1 방지)
        List<Long> rejectedLectureIds = lecturePage.getContent().stream()
            .filter(lecture -> lecture.getStatus() == LectureStatus.REJECTED)
            .map(Lecture::getId)
            .collect(Collectors.toList());

        Map<Long, Approval> approvalMap = rejectedLectureIds.isEmpty()
            ? Map.of()
            : approvalRepository.findLatestByLectureIds(rejectedLectureIds).stream()
                .collect(Collectors.toMap(Approval::getLectureId, approval -> approval));

        // Page<LectureResponse>로 변환
        return lecturePage.map(lecture -> {
            LectureStatistic statistic = lecture.getStatistic();
            String instructorNickname = userRepository.findById(lecture.getInstructorId())
                .map(user -> user.getNickname())
                .orElse("Unknown Instructor");

            // REJECTED 상태인 경우만 반려 사유 포함
            Approval latestApproval = null;
            if (lecture.getStatus() == LectureStatus.REJECTED) {
                latestApproval = approvalMap.get(lecture.getId());
            }

            return LectureResponse.simpleFromWithStats(lecture, statistic, instructorNickname, latestApproval);
        });
    }

    // 카테고리별 발행된 강의 목록 조회
    public List<LectureResponse> getPublishedLecturesByCategory(Integer categoryId) {
        return lectureRepository.findByCategoryIdAndStatusAndDeleteFlagFalse(categoryId, LectureStatus.AVAILABLE).stream()
            .map(
                lecture -> {
                    // TODO : Change Get Instructor Nickname response from Adapter API with userId
                    String instructorNickname = userRepository.findById(lecture.getInstructorId())
                        .map(user -> user.getNickname())
                        .orElse("Unknown Instructor");
                    return LectureResponse.from(lecture, instructorNickname);
                }
            )
            .collect(Collectors.toList());
    }

    @Transactional
    public LectureResponse updateLecture(Long lectureId, LectureBaseUpdateRequest request, String instructorId) {
        Lecture lecture = lectureRepository.findById(lectureId)
            .orElseThrow(LectureNotFoundException::new);

        validateNotDeleted(lecture);
        lectureAccessValidator.validateOwnership(lecture, instructorId);
        validateUpdatable(lecture);
        
        if (request.title() != null && !request.title().isBlank()) {
            lecture.updateTitle(request.title());
        }
        if (request.description() != null && !request.description().isBlank()) {
            lecture.updateDescription(request.description());
        }
        if (request.thumbnailUrl() != null && !request.thumbnailUrl().isBlank()) {
            lecture.updateThumbnailUrl(request.thumbnailUrl());
        }
        Lecture savedLecture = lectureRepository.save(lecture);
        String instructorNickname = userRepository.findById(savedLecture.getInstructorId())
            .map(user -> user.getNickname())
            .orElse("Unknown Instructor");
        return LectureResponse.simpleFrom(savedLecture, instructorNickname);
    }

    // 강의 삭제
    @Transactional
    public void deleteLecture(Long lectureId, String instructorId) {
        Lecture lecture = findLectureWithValidation(lectureId, instructorId);
        // lecture가 publish 상태이면 삭제 되면 안됨.
        if (lecture.getStatus() == LectureStatus.AVAILABLE) {
            throw new LectureDeleteBlockedException();
        }

        lecture.softDelete();

        lectureRepository.save(lecture);

        // NOTE(Soft delete policy):
        // - soft delete는 데이터 보존이 목적이므로 통계/연관 데이터를 hard delete 하지 않는다.
        // - hard delete(관리자 전용)가 필요해지면 별도 admin use-case로 분리한다.
    }

    // 강의 조회 및 강사 검증
    private Lecture findLectureWithValidation(Long lectureId, String instructorId) {
        Lecture lecture = lectureRepository.findByIdWithChapters(lectureId)
            .orElseThrow(() -> new LectureNotFoundException());
        validateNotDeleted(lecture);
        lectureAccessValidator.validateOwnership(lecture, instructorId);
        return lecture;
    }

    public Lecture findLectureWithChaptersAndLessons(Long lectureId) {
        Lecture lecture = lectureRepository.findByIdWithChaptersAndLessons(lectureId)
            .orElseThrow(() -> new LectureNotFoundException());
        validateNotDeleted(lecture);
        return lecture;
    }

    private void validateNotDeleted(Lecture lecture) {
        // policy: delete_flag=true 강의는 instructor/member 모두 조회 불가 (admin만 별도 경로로 조회)
        if (lecture.isDeleteFlag()) {
            // intentionally hide existence from non-admin APIs
            throw new LectureNotFoundException();
        }
    }

    private void validateUpdatable(Lecture lecture) {
        if (lecture.getStatus() == LectureStatus.AVAILABLE || lecture.getStatus() == LectureStatus.SUBMITTED) {
            throw new LectureCannotUpdateException();
        }
    }

}
