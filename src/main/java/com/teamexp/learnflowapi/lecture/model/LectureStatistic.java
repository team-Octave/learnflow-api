package com.teamexp.learnflowapi.lecture.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.annotation.LastModifiedBy;


import java.time.Instant;
import java.time.OffsetDateTime;

@Entity
@Table(name = "lecture_statistics",
    indexes = {
        // make index for frequently accessed columns like rating_average and enrollment_count
        @Index(name = "idx_rating_average", columnList = "rating_average"),
        @Index(name = "idx_enrollment_count", columnList = "enrollment_count")
})
public class LectureStatistic {

    // Lecture and Statistic have bijective relationship
    // Since make loosely coupled relationship, not use @MapsId
    @Id
    @Column(name="lecture_id")
    private Long lectureId;

    @Column(name="rating_sum")
    private Long ratingSum;

    @Column(name="rating_count")
    private Long ratingCount;

    // Not Actually stored in DB
    //TODO: Use DSL in repository to get order by rating average directly in query
    @Transient
    private Double ratingAverage;

    @Column(name="enrollment_count")
    private Long enrollmentCount;

    @LastModifiedBy
    @Column(name = "updated_at")
    private Instant updatedAt;

    protected LectureStatistic() {}

    private LectureStatistic(Long lectureId, Long ratingSum, Long ratingCount, Long enrollmentCount) {
        this.lectureId = lectureId;
        this.ratingSum = ratingSum;
        this.ratingCount = ratingCount;
        this.enrollmentCount = enrollmentCount;
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
        if (ratingAverage == null) {
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
    private void calculateRatingAverage() {
        this.ratingAverage = (ratingCount != null && ratingCount > 0)
            ? (double) ratingSum / ratingCount
            : 0.0;
    }


}
