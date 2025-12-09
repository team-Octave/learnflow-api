package com.teamexp.learnflowapi.review.dto;

import com.teamexp.learnflowapi.review.model.Review;
import java.time.Instant;

public record ReviewResponse(
    Long reviewId,
    Long lectureId,
    String lectureTitle,
    String userId,
    String nickname,        // [New] 작성자 닉네임 (조회 시 필요)
    Integer rating,
    String content,
    String instructorReply, // [New] 강사 답글 (조회 시 필요)
    Instant createdAt
) {
    // 닉네임은 Review 엔티티에 없으므로, Service에서 찾아서 넣어줘야 합니다.
    // 그래서 메서드 이름을 'of'로 짓고 nickname을 인자로 받습니다.
    public static ReviewResponse of(Review review, String nickname, String lectureTitle) {
        return new ReviewResponse(
            review.getId(),
            review.getLectureId(),
            lectureTitle,
            review.getUserId(),
            nickname,
            review.getRating(),
            review.getContent(),
            review.getInstructorReply(),
            review.getCreatedAt()
        );
    }
}
