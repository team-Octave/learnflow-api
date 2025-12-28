package com.teamexp.learnflowapi.lecture.service;

import com.teamexp.learnflowapi.content.exception.LectureThumbnailNotFoundException;
import com.teamexp.learnflowapi.content.model.Quiz;
import com.teamexp.learnflowapi.content.model.Thumbnail;
import com.teamexp.learnflowapi.content.repository.QuizRepository;
import com.teamexp.learnflowapi.content.repository.ThumbnailRepository;
import com.teamexp.learnflowapi.lecture.dto.request.ChapterCreateRequest;
import com.teamexp.learnflowapi.lecture.dto.request.LectureCreateRequest;
import com.teamexp.learnflowapi.lecture.dto.request.LectureCreateRequestV2;
import com.teamexp.learnflowapi.lecture.dto.request.LectureFullCreateRequest;
import com.teamexp.learnflowapi.lecture.dto.response.LectureFullCreateResponse;
import com.teamexp.learnflowapi.lecture.dto.response.LectureResponse;
import com.teamexp.learnflowapi.lecture.dto.response.PublishedResponse;
import com.teamexp.learnflowapi.lecture.exception.LectureDeleteBlockedException;
import com.teamexp.learnflowapi.lecture.exception.LectureNotFoundException;
import com.teamexp.learnflowapi.lecture.exception.LectureInstructorUnauthorizedException;
import com.teamexp.learnflowapi.lecture.model.*;
import com.teamexp.learnflowapi.lecture.repository.LectureRepository;
import com.teamexp.learnflowapi.lecture.repository.LectureStatisticRepository;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
    
    // TODO [Phase 1-1]: SecurityEventLogger 의존성 주입 예정
    // private final SecurityEventLogger securityEventLogger;

    public LectureService(LectureRepository lectureRepository, LectureStatisticRepository lectureStatisticRepository, ThumbnailRepository thumbnailRepository, UserRepository userRepository, QuizRepository quizRepository, LectureAccessValidator lectureAccessValidator) {
        this.lectureRepository = lectureRepository;
        this.lectureStatisticRepository = lectureStatisticRepository;
        this.thumbnailRepository = thumbnailRepository;
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;

        this.lectureAccessValidator = lectureAccessValidator;

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
            lectureCreateRequest.thumbnailId() != null ? lectureCreateRequest.thumbnailId() : 0L // temperary thumbnailId
        );

        // 2. 저장
        Lecture savedLecture = lectureRepository.save(lecture);

        // 3. LectureStatistic 초기 생성 (모든 값이 0으로 초기화)
        LectureStatistic initialStatistic = LectureStatistic.createInitial(savedLecture.getId());
        lectureStatisticRepository.save(initialStatistic);

        String thumbnailUrl = thumbnailRepository.findByLectureId(savedLecture.getId())
            .map(Thumbnail::getFileUrl)
            .orElse(defaultThumbnailUrl); // TODO : 기본 이미지 URL 반환 처리 -> 추후 content upload API 오류 시, 되돌려야 하는 지 체크 필요
            

        return LectureResponse.simpleFrom(savedLecture, userNickname, thumbnailUrl);
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
            lectureCreateRequest.thumbnailId() != null ? lectureCreateRequest.thumbnailId() : 0L // temperary thumbnailId
        );

        // 2. 저장
        Lecture savedLecture = lectureRepository.save(lecture);

        // 3. LectureStatistic 초기 생성 (모든 값이 0으로 초기화)
        LectureStatistic initialStatistic = LectureStatistic.createInitial(savedLecture.getId());
        lectureStatisticRepository.save(initialStatistic);

        
        // TODO : Change Get Thumbnail_Adapter API with lectureId, if null, return defaultThumbnailUrl
        String thumbnailUrl = thumbnailRepository.findByLectureId(savedLecture.getId())
            .map(Thumbnail::getFileUrl)
            .orElse(defaultThumbnailUrl); // TODO : 기본 이미지 URL 반환 처리 -> 추후 content upload API 오류 시, 되돌려야 하는 지 체크 필요
            

        return LectureResponse.simpleFrom(savedLecture, userNickname, thumbnailUrl);
    }


    // TODO : 현재는 초기 curriculum 구성 메서드를 lesson, chapter 추가 메서드 정의했지만, Lecture 의 PUT/PATCH 메서드로 생각해서 수정하는 것도 고려해볼 것
    // 대량 데이터로 인한 성능 이슈 발생 시 별도 배치 작업으로 분리하는 것도 고려해볼 것(ex. 배치 처리 후 DB에 반영)
    // 강의 curriculum 구성 메서드들
    @Transactional
    public LectureFullCreateResponse createLectureFullCurriculum(
        Long lectureId,
        LectureFullCreateRequest request,
        String instructorId,
        String userNickname
    ) {
        Lecture lecture = findLectureWithValidation(lectureId, instructorId);

        // 1단계: 엔티티만 생성 및 추가 (아직 DTO 변환 안 함)
        IntStream.range(0, request.chapters().size())
            .forEach(chapterIndex -> {
                LectureFullCreateRequest.ChapterRequest chapterRequest = request.chapters().get(chapterIndex);
                Chapter chapter = Chapter.createChapter(
                    chapterRequest.chapterTitle(),
                    chapterIndex
                );
                lecture.addChapter(chapter);

                // case1. lessonType: VIDEO
                // case2. lessonType: QUIZ
                IntStream.range(0, chapterRequest.lessons().size())
                    .forEach(lessonIndex -> {
                        LectureFullCreateRequest.LessonRequest lessonRequest = chapterRequest.lessons().get(lessonIndex);
                        Lesson lesson = Lesson.createLesson(
                            LessonType.forEntity(lessonRequest.lessonType()),
                            lessonRequest.lessonTitle(),
                            lessonIndex,
                            lessonRequest.isFreePreview(),
                            lessonRequest.videoUrl()
                        );
                        chapter.addLesson(lesson);
                    });

            });

        // 2단계: **여기서 save** - ID 생성 보장
        lectureRepository.save(lecture);

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
                            lesson.getId(),
                            quizQuestion.questionOrder(),
                            quizQuestion.question(),
                            quizQuestion.correct()
                        ))
                        .toList();

                    quizRepository.saveAll(quizList);
                    lesson.bindQuizzes(quizList);
                }
            }
        }



        // 3단계: 이제 ID가 채워졌으므로 DTO 변환 가능
        List<LectureFullCreateResponse.ChapterResponse> chapterResponses = lecture.getChapters().stream()
            .map(chapter -> {
                List<LectureFullCreateResponse.LessonResponse> lessonResponses = chapter.getLessons().stream()
                    .map(lesson -> {
                        if (lesson.getLessonType() == LessonType.QUIZ) {
                            return LectureFullCreateResponse.LessonResponse.withoutVideo(
                                lesson.getId(),
                                lesson.getLessonTitle(),
                                lesson.getLessonOrder(),
                                lesson.getLessonType().getDisplayName(),
                                lesson.getIsFreePreview(),
                                lesson.unpackingQuizzes().stream()
                                    .map(quizQuestion -> new LectureFullCreateResponse.LessonResponse.QuizQuestionResponse(
                                        quizQuestion.getId(),
                                        quizQuestion.getQuestion(),
                                        quizQuestion.getOrderIndex(),
                                        quizQuestion.getCorrect()
                                    ))
                                    .collect(Collectors.toList())
                            );
                        } else {
                            return LectureFullCreateResponse.LessonResponse.withoutQuiz(
                                lesson.getId(),
                                lesson.getLessonTitle(),
                                lesson.getLessonOrder(),
                                lesson.getLessonType().getDisplayName(),
                                lesson.getIsFreePreview(),
                                lesson.getVideoUrl()
                            );
                        }
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



//    // 챕터 추가
//    // TODO: ChapterUpdatedResponse 반환
//    @Transactional
//    public LectureResponse addChapter(ChapterCreateRequest request, Long lectureId, String instructorId) {
//        Lecture lecture = findLectureWithValidation(lectureId, instructorId);
//
//        // findByLectureId 가 Optional이라 null 처리 필요
//        Thumbnail thumbnail = thumbnailRepository.findByLectureId(lectureId).orElseThrow(
//            () -> new LectureThumbnailNotFoundException()
//        );
//
//        String thumbnailUrl = thumbnail.getFileUrl();
//
//
//        Chapter chapter = Chapter.createChapter(
//            request.chapterTitle(),
//            lecture.getChapters().size()
//        );
//
//        lecture.addChapter(chapter);
//
//        return LectureResponse.from(lecture,thumbnailUrl);
//    }
//
//    // 레슨 추가
//    // TODO: LessonUpdatedResponse 반환
//    @Transactional
//    public LectureResponse addLesson(LessonCreateRequest request, Long lectureId, Long chapterId, String instructorId) {
//        Lecture lecture = lectureRepository.findByIdWithChaptersAndLessons(lectureId)
//            .orElseThrow(() -> new LectureNotFoundException());
//
//        validateInstructor(lecture, instructorId);
//
//        Chapter chapter = lecture.getChapters().stream()
//            .filter(c -> c.getId().equals(chapterId))
//            .findFirst()
//            .orElseThrow(() -> new ChapterNotFoundException() );
//
//        // findByLectureId 가 Optional이라 null 처리 필요
//        Thumbnail thumbnail = thumbnailRepository.findByLectureId(lectureId).orElseThrow(
//            () -> new LectureThumbnailNotFoundException()
//        );
//
//        String thumbnailUrl = thumbnail.getFileUrl();
//
//        Lesson lesson = createLessonByType(request, chapter.getLessons().size());
//        chapter.addLesson(lesson);
//
//        return LectureResponse.from(lecture,thumbnailUrl);
//    }

    // 강의 발행
    // 동시에 여러 강의 발행 요청이 올 경우를 대비해 강의 상태 변경 시 낙관적 락(Optimistic Lock) 적용 고려
    @Transactional
    public PublishedResponse makeAvailableLecture(Long lectureId, String instructorId) {
        Lecture lecture = findLectureWithChaptersAndLessons(lectureId);
        lectureAccessValidator.validateOwnership(lecture, instructorId);

        lecture.makeAvailable();

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

        // N+1 문제 방지를 위해 모든 Lecture ID에 대한 통계 정보를 한 번에 조회
        List<Long> lectureIds = lecturePage.getContent().stream()
            .map(Lecture::getId)
            .collect(Collectors.toList());
        
        Map<Long, LectureStatistic> statisticMap = lectureStatisticRepository.findAllById(lectureIds).stream()
            .collect(Collectors.toMap(LectureStatistic::getLectureId, stat -> stat));

        // Page<LectureResponse>로 변환
        return lecturePage.map(lecture -> {
            LectureStatistic statistic = statisticMap.get(lecture.getId());

            // @TODO : Change Get Thumbnail_url_list from content upload API with lectureId_list
            String thumbnailUrl = thumbnailRepository.findByLectureId(lecture.getId())
                .map(Thumbnail::getFileUrl)
                .orElse(defaultThumbnailUrl); // @TODO : 기본 이미지 URL 반환 처리 -> 추후 content upload API 오류 시, 되돌려야 하는 지 체크 필요
//                .orElseThrow(() -> new LectureThumbnailNotFoundException());

            // TODO : Change Get Instructor Nickname_list response from Adapter API with userId_list
            String instructorNickname = userRepository.findById(lecture.getInstructorId())
                .map(user -> user.getNickname())
                .orElse("Unknown Instructor");

            return LectureResponse.simpleFromWithStats(lecture, statistic, thumbnailUrl, instructorNickname);
        });
    }

    // 강의 단건 조회
    public LectureResponse getLecture(Long lectureId) {
        Lecture lecture = findLectureWithChaptersAndLessons(lectureId);

        // TODO : 임시 코드
        List<Long> lectureIds = List.of(lectureId);
        Map<Long, LectureStatistic> statisticMap = lectureStatisticRepository.findAllById(lectureIds).stream()
            .collect(Collectors.toMap(LectureStatistic::getLectureId, stat -> stat));

        LectureStatistic statistic = statisticMap.get(lecture.getId());

        // TODO : Change Get Thumbnail_url from content upload API with lectureId
        String thumbnailUrl = thumbnailRepository.findByLectureId(lectureId)
            .map(Thumbnail::getFileUrl)
            .orElse(defaultThumbnailUrl); // TODO : 기본 이미지 URL 반환 처리 -> 추후 content upload API 오류 시, 되돌려야 하는 지 체크 필요
//            .orElseThrow(() -> new LectureThumbnailNotFoundException());

        // TODO : Change Get Instructor Nickname response from Adapter API with userId
        String instructorNickname = userRepository.findById(lecture.getInstructorId())
            .map(user -> user.getNickname())
            .orElse("Unknown Instructor");

        lecture.getChapters().forEach(chapter -> {
            chapter.getLessons().forEach(lesson -> {
                if (lesson.getLessonType() == LessonType.QUIZ) {
                        List<Quiz> quizzes = quizRepository.findByLessonIdOrderByOrderIndexAsc(lesson.getId());
                        lesson.bindQuizzes(quizzes);
                    }
            });
        });

        return LectureResponse.fromWithStatics(lecture, statistic, thumbnailUrl, instructorNickname);
    }

    public Page<LectureResponse> getLecturesByInstructor(String instructorId, Pageable pageable) {
        // Repository에서 페이지네이션된 강의 조회
        Page<Lecture> lecturePage = lectureRepository.findByInstructorId(instructorId, pageable);

        // N+1 문제 방지를 위해 모든 Lecture ID에 대한 통계 정보를 한 번에 조회
        List<Long> lectureIds = lecturePage.getContent().stream()
            .map(Lecture::getId)
            .collect(Collectors.toList());
        
        Map<Long, LectureStatistic> statisticMap = lectureStatisticRepository.findAllById(lectureIds).stream()
            .collect(Collectors.toMap(LectureStatistic::getLectureId, stat -> stat));

        // Page<LectureResponse>로 변환
        return lecturePage.map(lecture -> {
            LectureStatistic statistic = statisticMap.get(lecture.getId());
            String thumbnailUrl = thumbnailRepository.findByLectureId(lecture.getId())
                .map(Thumbnail::getFileUrl)
                .orElse(defaultThumbnailUrl); // TODO : 기본 이미지 URL 반환 처리 -> 추후 content upload API 오류 시, 되돌려야 하는 지 체크 필요
//                .orElseThrow(() -> new LectureThumbnailNotFoundException());

            String instructorNickname = userRepository.findById(lecture.getInstructorId())
                .map(user -> user.getNickname())
                .orElse("Unknown Instructor");

            return LectureResponse.simpleFromWithStats(lecture, statistic, thumbnailUrl, instructorNickname);
        });
    }

    // 카테고리별 발행된 강의 목록 조회
    public List<LectureResponse> getPublishedLecturesByCategory(Integer categoryId) {
        return lectureRepository.findByCategoryIdAndStatus(categoryId, LectureStatus.AVAILABLE).stream()
            .map(
                lecture -> {
                    // TODO : Change Get Thumbnail_url from content upload API with lectureId
                    String thumbnail = thumbnailRepository.findByLectureId(lecture.getId())
                        .map(Thumbnail::getFileUrl)
                        .orElseThrow(() -> new LectureThumbnailNotFoundException());

                    // TODO : Change Get Instructor Nickname response from Adapter API with userId
                    String instructorNickname = userRepository.findById(lecture.getInstructorId())
                        .map(user -> user.getNickname())
                        .orElse("Unknown Instructor");

                    return LectureResponse.from(lecture, thumbnail, instructorNickname);
                }
            )
            .collect(Collectors.toList());
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

        // @TODO: 해당 요청도 해당 서비스에 위임
        // 고아 객체가 남아있지 않도록 썸네일이랑, 강의 통계도 같이 삭제
        thumbnailRepository.deleteByLectureId(lectureId);
        lectureStatisticRepository.deleteById(lectureId);
    }

    // 강의 조회 및 강사 검증
    private Lecture findLectureWithValidation(Long lectureId, String instructorId) {
        Lecture lecture = lectureRepository.findByIdWithChapters(lectureId)
            .orElseThrow(() -> new LectureNotFoundException());
        lectureAccessValidator.validateOwnership(lecture, instructorId);
        return lecture;
    }

    private Lecture findLectureWithChaptersAndLessons(Long lectureId) {
        return lectureRepository.findByIdWithChaptersAndLessons(lectureId)
            .orElseThrow(() -> new LectureNotFoundException());
    }


//    // 레슨 타입에 따른 레슨 생성, Quiz&Video content async upload 처리 후 setter 호출 예정
//    private Lesson createLessonByType(LessonCreateRequest request, int orderIndex) {
//        return Lesson.createLesson(
//            request.lessonType(),
//            request.lessonTitle(),
//            orderIndex,
//            request.isFreePreview()
//        );
//    }


}
