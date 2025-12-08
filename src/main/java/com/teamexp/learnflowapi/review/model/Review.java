package com.teamexp.learnflowapi.review.model;

import com.teamexp.learnflowapi.enrollment.model.Enrollment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
// [변경] 기존의 복합 유니크 제약 조건(user_id + lecture_id) 삭제
@Table(name = "review")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;
    // unique = true 설정으로 "하나의 수강신청당 하나의 리뷰만" 작성 가능하도록 DB 제약 설정
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false, unique = true)
    private Enrollment enrollment;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "instructor_reply", length = 1000)
    private String instructorReply;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewStatus status;

    @CreatedDate
    @Column(name = "created_at", columnDefinition = "TIMESTAMP", updatable = false, nullable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP", nullable = false)
    private Instant updatedAt;
    // 생성자 인자 수정
    private Review(Enrollment enrollment, String content, Integer rating) {
        validateRating(rating);
        this.enrollment = enrollment;
        this.content = content;
        this.rating = rating;
        this.status = ReviewStatus.POSTED;
    }
    // 팩토리 메서드 인자 변경
    public static Review create(Enrollment enrollment, String content, Integer rating) {
        return new Review(enrollment, content, rating);
    }

    public void reply(String replyContent) {
        this.instructorReply = replyContent;
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1점에서 5점 사이여야 합니다.");
        }
    }

    // 편의 메서드: 기존 코드들이 getUserId(), getLectureId()를 호출해도 문제 없도록 위임
    public String getUserId() {
        return String.valueOf(this.enrollment.getUserId());
    }

    public Long getLectureId() {
        return this.enrollment.getLectureId();
    }

}
