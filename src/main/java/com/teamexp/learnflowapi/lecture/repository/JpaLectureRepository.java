package com.teamexp.learnflowapi.lecture.repository;

import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.LectureLevel;
import com.teamexp.learnflowapi.lecture.model.LectureStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaLectureRepository extends JpaRepository<Lecture, Long>, LectureRepository {

    @Override
    @Query("SELECT l FROM Lecture l LEFT JOIN FETCH l.chapters WHERE l.id = :id")
    Optional<Lecture> findByIdWithChapters(@Param("id") Long id);

    @Override
    @Query("SELECT DISTINCT l FROM Lecture l " +
        "LEFT JOIN FETCH l.chapters c " +
        "LEFT JOIN FETCH c.lessons " +
        "WHERE l.id = :id")
    Optional<Lecture> findByIdWithChaptersAndLessons(@Param("id") Long id);

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

    @Query("""
        SELECT l FROM Lecture l
        LEFT JOIN LectureStatistic ls ON ls.lectureId = l.id
        WHERE l.status = :status
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
        LEFT JOIN LectureStatistic ls ON ls.lectureId = l.id
        WHERE l.status = :status
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

    @Query("""
        SELECT l FROM Lecture l
        LEFT JOIN LectureStatistic ls ON ls.lectureId = l.id
        WHERE l.status = :status
        AND (:categoryId IS NULL OR l.categoryId = :categoryId)
        AND (:level IS NULL OR l.level = :level)
        ORDER BY ls.updatedAt DESC NULLS LAST, l.createdAt DESC
        """)
    Page<Lecture> findByFiltersWithStatsOrderByLatest(
        @Param("categoryId") Integer categoryId,
        @Param("level") LectureLevel level,
        @Param("status") LectureStatus status,
        Pageable pageable
    );
}
