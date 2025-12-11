package com.teamexp.learnflowapi.lecture.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import org.springframework.data.annotation.LastModifiedBy;


import java.time.Instant;

@Entity
@Table(name = "lecture_statistics",
    indexes = {
        @Index(name = "idx_rating_average", columnList = "rating_average"),
        @Index(name = "idx_enrollment_count", columnList = "enrollment_count")
    })
public class LectureStatistic {

    // Lecture and Statistic have bijective relationship
    // Since make loosely coupled relationship, not use @MapsId
    @Id
    @Column(name = "lecture_id")
    private Long lectureId;

    @Column(name = "rating_sum")
    private Long ratingSum;

    @Column(name = "rating_count")
    private Long ratingCount;

    @Column(name = "rating_average")
    private Double ratingAverage;

    @Column(name = "enrollment_count")
    private Long enrollmentCount;

    @LastModifiedBy
    @Column(name = "updated_at")
    private Instant updatedAt;

    protected LectureStatistic() {
    }

    private LectureStatistic(Long lectureId, Long ratingSum, Long ratingCount, Long enrollmentCount, Double ratingAverage) {
        this.lectureId = lectureId;
        this.ratingSum = ratingSum;
        this.ratingCount = ratingCount;
        this.enrollmentCount = enrollmentCount;
        this.ratingAverage = ratingAverage;
    }

    // 정적 팩토리 메서드: 리뷰 추가 시 사용
    public static LectureStatistic createForNewReview(Long lectureId, Integer rating) {
        Long ratingSum = (long) rating;
        Long ratingCount = 1L;
        Double ratingAverage = (double) rating;
        return new LectureStatistic(lectureId, ratingSum, ratingCount, 0L, ratingAverage);
    }


    public Long getLectureId() {
        return lectureId;
    }

    public Long getRatingSum() {
        return ratingSum;
    }

    public Long getRatingCount() {
        return ratingCount;
    }

    public Double getRatingAverage() {
        if (ratingAverage == null && ratingCount != null && ratingCount > 0) {
            calculateRatingAverage();
        }
        return ratingAverage;
    }

    public Long getEnrollmentCount() {
        return enrollmentCount;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @PostLoad
    private void calculateRatingAverageIfNeeded() {
        // DB에서 로드 시 ratingAverage가 null이고 ratingCount > 0이면 계산
        if (ratingAverage == null && ratingCount != null && ratingCount > 0) {
            calculateRatingAverage();
        }
    }

    public void calculateRatingAverage() {
        this.ratingAverage = (ratingCount != null && ratingCount > 0)
            ? (double) ratingSum / ratingCount
            : 0.0;
    }

    // 통계 업데이트 메서드
    public void addRating(Integer rating) {
        this.ratingSum = (this.ratingSum != null ? this.ratingSum : 0L) + rating;
        this.ratingCount = (this.ratingCount != null ? this.ratingCount : 0L) + 1;
        calculateRatingAverage();
    }

    public void removeRating(Integer rating) {
        this.ratingSum = Math.max(0, (this.ratingSum != null ? this.ratingSum : 0L) - rating);
        this.ratingCount = Math.max(0, (this.ratingCount != null ? this.ratingCount : 0L) - 1);
        calculateRatingAverage();
    }

}
