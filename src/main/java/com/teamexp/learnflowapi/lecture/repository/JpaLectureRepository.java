package com.teamexp.learnflowapi.lecture.repository;

import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.LectureLevel;
import com.teamexp.learnflowapi.lecture.model.LectureStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaLectureRepository extends JpaRepository<Lecture, Long>, LectureRepository {

    @Override
    @Query("SELECT l FROM Lecture l LEFT JOIN FETCH l.chapters WHERE l.id = :id AND l.deleteFlag = false")
    Optional<Lecture> findByIdWithChapters(@Param("id") Long id);

    @Override
    @Query("SELECT DISTINCT l FROM Lecture l " +
            "LEFT JOIN FETCH l.chapters c " +
            "LEFT JOIN FETCH c.lessons " +
            "LEFT JOIN FETCH l.statistic " +
            "WHERE l.id = :id AND l.deleteFlag = false")
    Optional<Lecture> findByIdWithChaptersAndLessons(@Param("id") Long id);

    @Override
    @Query("SELECT DISTINCT l FROM Lecture l " +
            "LEFT JOIN FETCH l.chapters c " +
            "LEFT JOIN FETCH c.lessons ls " +
            "LEFT JOIN FETCH ls.quizzes " +
            "LEFT JOIN FETCH l.statistic " +
            "WHERE l.id = :id AND l.deleteFlag = false")
    Optional<Lecture> findByIdWithChaptersAndLessonsAndQuizzes(@Param("id") Long id);

    @Override
    List<Lecture> findByInstructorId(String instructorId);

    @Override
    Page<Lecture> findByInstructorId(String instructorId, Pageable pageable);

    @Override
    List<Lecture> findByStatus(LectureStatus status);

    @Override
    List<Lecture> findByCategoryId(Integer categoryId);

    @Override
    List<Lecture> findByCategoryIdAndStatus(Integer categoryId, LectureStatus status);

    @Override
    List<Lecture> findByCategoryIdAndStatusAndDeleteFlagFalse(Integer categoryId, LectureStatus status);

    @EntityGraph("Lecture.withStatistic")
    @Override
    default Page<Lecture> findByFiltersWithStats(
            Integer categoryId,
            LectureLevel level,
            LectureStatus status,
            String sortBy,
            Pageable pageable
    ) {
        return switch (sortBy) {
            case "POPULAR" -> findByFiltersWithStatsOrderByPopular(categoryId, level, status, pageable);
            case "RATING" -> findByFiltersWithStatsOrderByRating(categoryId, level, status, pageable);
            case "LATEST" -> findByFiltersWithStatsOrderByLatest(categoryId, level, status, pageable);
            default -> findByFiltersWithStatsOrderByPopular(categoryId, level, status, pageable);
        };
    }

    @EntityGraph("Lecture.withStatistic")
    @Override
    default Page<Lecture> findAllWithStatsForAdmin(String sortBy, Pageable pageable) {
        return switch (sortBy) {
            case "POPULAR" -> findAllWithStatsForAdminOrderByPopular(pageable);
            case "RATING" -> findAllWithStatsForAdminOrderByRating(pageable);
            case "LATEST" -> findAllWithStatsForAdminOrderByLatest(pageable);
            default -> findAllWithStatsForAdminOrderByPopular(pageable);
        };
    }

    @EntityGraph("Lecture.withStatistic")
    @Query("""
        SELECT l FROM Lecture l
        LEFT JOIN l.statistic ls
        ORDER BY ls.enrollmentCount DESC NULLS LAST
        """)
    Page<Lecture> findAllWithStatsForAdminOrderByPopular(Pageable pageable);

    @EntityGraph("Lecture.withStatistic")
    @Query("""
        SELECT l FROM Lecture l
        LEFT JOIN l.statistic ls
        ORDER BY ls.ratingAverage DESC NULLS LAST
        """)
    Page<Lecture> findAllWithStatsForAdminOrderByRating(Pageable pageable);

    @EntityGraph("Lecture.withStatistic")
    @Query("""
        SELECT l FROM Lecture l
        LEFT JOIN l.statistic ls
        ORDER BY l.updatedAt DESC NULLS LAST, l.createdAt DESC
        """)
    Page<Lecture> findAllWithStatsForAdminOrderByLatest(Pageable pageable);

    @Query("SELECT l FROM Lecture l LEFT JOIN FETCH l.chapters WHERE l.id = :id")
    Optional<Lecture> findByIdWithChaptersForAdmin(@Param("id") Long id);

    @Query("SELECT DISTINCT l FROM Lecture l " +
            "LEFT JOIN FETCH l.chapters c " +
            "LEFT JOIN FETCH c.lessons " +
            "LEFT JOIN FETCH l.statistic " +
            "WHERE l.id = :id")
    Optional<Lecture> findByIdWithChaptersAndLessonsForAdmin(@Param("id") Long id);

    @EntityGraph("Lecture.withStatistic")
    @Query("""
        SELECT l FROM Lecture l
        LEFT JOIN l.statistic ls
        WHERE l.id = :id
        """)
    Optional<Lecture> findByIdWithStatisticForAdmin(@Param("id") Long id);

    @EntityGraph("Lecture.withStatistic")
    @Query("""
        SELECT l FROM Lecture l
        LEFT JOIN l.statistic ls
        WHERE l.status = :status
        AND l.deleteFlag = false
        AND (:categoryId IS NULL OR l.categoryId = :categoryId)
        AND (:level IS NULL OR l.level = :level)
        ORDER BY ls.enrollmentCount DESC NULLS LAST
        """)
    Page<Lecture> findByFiltersWithStatsOrderByPopular(
            @Param("categoryId") Integer categoryId,
            @Param("level") LectureLevel level,
            @Param("status") LectureStatus status,
            Pageable pageable
    );

    @Query("""
        SELECT l FROM Lecture l
        LEFT JOIN l.statistic ls
        WHERE l.status = :status
        AND l.deleteFlag = false
        AND (:categoryId IS NULL OR l.categoryId = :categoryId)
        AND (:level IS NULL OR l.level = :level)
        ORDER BY ls.ratingAverage DESC NULLS LAST
        """)
    Page<Lecture> findByFiltersWithStatsOrderByRating(
            @Param("categoryId") Integer categoryId,
            @Param("level") LectureLevel level,
            @Param("status") LectureStatus status,
            Pageable pageable
    );

    @EntityGraph("Lecture.withStatistic")
    @Query("""
        SELECT l FROM Lecture l
        LEFT JOIN l.statistic ls
        WHERE l.status = :status
        AND l.deleteFlag = false
        AND (:categoryId IS NULL OR l.categoryId = :categoryId)
        AND (:level IS NULL OR l.level = :level)
        ORDER BY l.updatedAt DESC NULLS LAST, l.createdAt DESC
        """)
    Page<Lecture> findByFiltersWithStatsOrderByLatest(
            @Param("categoryId") Integer categoryId,
            @Param("level") LectureLevel level,
            @Param("status") LectureStatus status,
            Pageable pageable
    );

    @EntityGraph("Lecture.withStatistic")
    @Query("""
        SELECT l FROM Lecture l
        WHERE l.instructorId = :instructorId
        AND l.deleteFlag = false
        ORDER BY l.updatedAt DESC NULLS LAST, l.createdAt DESC
        """)
    Page<Lecture> findByInstructorIdOrderByUpdatedAtDesc(
            @Param("instructorId") String instructorId,
            Pageable pageable
    );
}