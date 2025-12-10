package com.teamexp.learnflowapi.lecture.service;

import com.teamexp.learnflowapi.lecture.dto.request.ChapterCreateRequest;
import com.teamexp.learnflowapi.lecture.dto.request.LectureCreateRequest;
import com.teamexp.learnflowapi.lecture.dto.request.LectureFullCreateRequest;
import com.teamexp.learnflowapi.lecture.dto.request.LessonCreateRequest;
import com.teamexp.learnflowapi.lecture.dto.response.LectureFullCreateResponse;
import com.teamexp.learnflowapi.lecture.dto.response.LectureResponse;
import com.teamexp.learnflowapi.lecture.dto.response.PublishedResponse;
import com.teamexp.learnflowapi.lecture.model.*;
import com.teamexp.learnflowapi.lecture.repository.LectureRepository;
import com.teamexp.learnflowapi.lecture.repository.LectureStatisticRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional(readOnly = true)
public class LectureService {

    private final LectureRepository lectureRepository;
    private final LectureStatisticRepository lectureStatisticRepository;

    public LectureService(LectureRepository lectureRepository, LectureStatisticRepository lectureStatisticRepository) {
        this.lectureRepository = lectureRepository;
        this.lectureStatisticRepository = lectureStatisticRepository;
    }

    @Transactional
    public LectureResponse createLecture(LectureCreateRequest lectureCreateRequest, String instructorId) {
        // 1. 정적 팩토리 메서드로 생성 (객체 생성 로직은 엔티티에 위임)
        Lecture lecture = Lecture.createLecture(
            lectureCreateRequest.title(),
            lectureCreateRequest.description(),
            LectureLevel.forEntity(lectureCreateRequest.level()),
            lectureCreateRequest.categoryId(), // check category existence if needed
            instructorId
        );

        // 2. 저장
        Lecture savedLecture = lectureRepository.save(lecture);
        return LectureResponse.simpleFrom(savedLecture);
    }


    // TODO : 현재는 초기 curriculum 구성 메서드를 lesson, chapter 추가 메서드 정의했지만, Lecture 의 PUT/PATCH 메서드로 생각해서 수정하는 것도 고려해볼 것
    // 대량 데이터로 인한 성능 이슈 발생 시 별도 배치 작업으로 분리하는 것도 고려해볼 것(ex. 배치 처리 후 DB에 반영)
    // 강의 curriculum 구성 메서드들
    @Transactional
    public LectureFullCreateResponse createLectureFullCurriculum(
        Long lectureId,
        LectureFullCreateRequest request,
        String instructorId
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

                IntStream.range(0, chapterRequest.lessons().size())
                    .forEach(lessonIndex -> {
                        LectureFullCreateRequest.LessonRequest lessonRequest = chapterRequest.lessons().get(lessonIndex);
                        Lesson lesson = Lesson.createLesson(
                            LessonType.forEntity(lessonRequest.lessonType()),
                            lessonRequest.lessonTitle(),
                            lessonIndex,
                            lessonRequest.isFreePreview()
                        );
                        chapter.addLesson(lesson);
                    });
            });

        // 2단계: **여기서 save** - ID 생성 보장
        lectureRepository.save(lecture);

        // 3단계: 이제 ID가 채워졌으므로 DTO 변환 가능
        List<LectureFullCreateResponse.ChapterResponse> chapterResponses =
            IntStream.range(0, lecture.getChapters().size())
                .mapToObj(chapterIndex -> {
                    Chapter chapter = lecture.getChapters().get(chapterIndex);
                    List<LectureFullCreateResponse.LessonResponse> lessonResponses =
                        IntStream.range(0, chapter.getLessons().size())
                            .mapToObj(lessonIndex -> {
                                Lesson lesson = chapter.getLessons().get(lessonIndex);
                                return new LectureFullCreateResponse.LessonResponse(
                                    lesson.getId(),
                                    lesson.getLessonTitle(),
                                    lesson.getLessonOrder(),
                                    lesson.getLessonType().getDisplayName(),
                                    lesson.getIsFreePreview()
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
            chapterResponses
        );
    }



    // 챕터 추가
    @Transactional
    public LectureResponse addChapter(ChapterCreateRequest request, Long lectureId, String instructorId) {
        Lecture lecture = findLectureWithValidation(lectureId, instructorId);

        Chapter chapter = Chapter.createChapter(
            request.chapterTitle(),
            lecture.getChapters().size()
        );

        lecture.addChapter(chapter);

        return LectureResponse.from(lecture);
    }

    // 레슨 추가
    @Transactional
    public LectureResponse addLesson(LessonCreateRequest request, Long lectureId, Long chapterId, String instructorId) {
        Lecture lecture = lectureRepository.findByIdWithChaptersAndLessons(lectureId)
            .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        validateInstructor(lecture, instructorId);

        Chapter chapter = lecture.getChapters().stream()
            .filter(c -> c.getId().equals(chapterId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("챕터를 찾을 수 없습니다."));

        Lesson lesson = createLessonByType(request, chapter.getLessons().size());
        chapter.addLesson(lesson);

        return LectureResponse.from(lecture);
    }

    // 강의 발행
    // 동시에 여러 강의 발행 요청이 올 경우를 대비해 강의 상태 변경 시 낙관적 락(Optimistic Lock) 적용 고려
    @Transactional
    public PublishedResponse makeAvailableLecture(Long lectureId, String instructorId) {
        Lecture lecture = findLectureWithChaptersAndLessons(lectureId);
        validateInstructor(lecture, instructorId);

        lecture.makeAvailable();

        return PublishedResponse.from(lecture.getId(), lecture.getStatus());
    }

    // 강의 목록 조회
    public List<LectureResponse> getAllLectures() {
        return lectureRepository.findByStatus(LectureStatus.AVAILABLE).stream()
            .map(LectureResponse::simpleFrom)
            .collect(Collectors.toList());
    }

    // 강의 목록 조회 (필터링 및 페이지네이션)
    public Page<LectureResponse> getAllLecturesWithFilters(String category, String level, String sort, Pageable pageable) {
        // "ALL" 값 처리
        Integer categoryId = "ALL".equals(category) ? null : (category != null ? Integer.parseInt(category) : null);
        LectureLevel lectureLevel = "ALL".equals(level) ? null : (level != null ? LectureLevel.forEntity(level) : null);
        
        // Repository에서 필터링된 강의 조회
        Page<Lecture> lecturePage = lectureRepository.findByFiltersWithStats(
            categoryId,
            lectureLevel,
            LectureStatus.AVAILABLE,
            sort,
            pageable
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
            return LectureResponse.simpleFromWithStats(lecture, statistic);
        });
    }

    // 강의 단건 조회
    public LectureResponse getLecture(Long lectureId) {
        Lecture lecture = findLectureWithChaptersAndLessons(lectureId);
        return LectureResponse.from(lecture);
    }

    // 강사의 강의 목록 조회
    public List<LectureResponse> getLecturesByInstructor(String instructorId) {
        return lectureRepository.findByInstructorId(instructorId).stream()
            .map(LectureResponse::simpleFrom)
            .collect(Collectors.toList());
    }

    // 카테고리별 발행된 강의 목록 조회
    public List<LectureResponse> getPublishedLecturesByCategory(Integer categoryId) {
        return lectureRepository.findByCategoryIdAndStatus(categoryId, LectureStatus.AVAILABLE).stream()
            .map(LectureResponse::simpleFrom)
            .collect(Collectors.toList());
    }

    // 강의 삭제
    @Transactional
    public void deleteLecture(Long lectureId, String instructorId) {
        Lecture lecture = findLectureWithValidation(lectureId, instructorId);
        lectureRepository.delete(lecture);
    }

    private Lecture findLectureWithValidation(Long lectureId, String instructorId) {
        Lecture lecture = lectureRepository.findByIdWithChapters(lectureId)
            .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다: " + lectureId));
        validateInstructor(lecture, instructorId);
        return lecture;
    }

    // TODO : custom exception handling 고려
    private Lecture findLectureWithChaptersAndLessons(Long lectureId) {
        return lectureRepository.findByIdWithChaptersAndLessons(lectureId)
            .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다: " + lectureId));
    }

    private void validateInstructor(Lecture lecture, String instructorId) {
        if (!lecture.getInstructorId().equals(instructorId)) {
            throw new IllegalStateException("해당 강의에 대한 권한이 없습니다.");
        }
    }

    // 레슨 타입에 따른 레슨 생성, Quiz&Video content async upload 처리 후 setter 호출 예정
    private Lesson createLessonByType(LessonCreateRequest request, int orderIndex) {
        return Lesson.createLesson(
            request.lessonType(),
            request.lessonTitle(),
            orderIndex,
            request.isFreePreview()
        );

    }


}