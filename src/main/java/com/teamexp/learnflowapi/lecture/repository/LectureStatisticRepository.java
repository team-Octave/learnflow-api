package com.teamexp.learnflowapi.lecture.repository;

import com.teamexp.learnflowapi.lecture.model.LectureStatistic;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectureStatisticRepository extends JpaRepository<LectureStatistic, Long> {

    // ORDER By rating average DESC offset n limit 16
    @Query("""
        SELECT ls FROM LectureStatistic ls
        ORDER BY (CASE WHEN ls.ratingCount > 0
                  THEN CAST(ls.ratingSum AS double) / ls.ratingCount
                  ELSE 0 END) DESC
    """)
    List<LectureStatistic> findTopOrderByRatingAverage(Pageable pageable);

    // ORDER By enrollment count DESC offset n limit 16
    @Query("""
        SELECT ls FROM LectureStatistic ls
        ORDER BY ls.enrollmentCount DESC
    """)
    List<LectureStatistic> findTopOrderByEnrollmentCount(Pageable pageable);
}
