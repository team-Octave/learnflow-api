package com.teamexp.learnflowapi.lecture.dto.response;

import com.teamexp.learnflowapi.admin.model.Approval;
import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.LectureLevel;
import com.teamexp.learnflowapi.lecture.model.LectureStatistic;
import com.teamexp.learnflowapi.lecture.model.PaymentType;

import java.time.Instant;

import java.util.List;
import java.util.stream.Collectors;

public record LectureResponse(
    Long id,
    String title,
    String description,
    Integer categoryId,
    LectureLevel level,
    String levelDisplayName,
    String statusDisplayName,
    String instructorId,
    String instructorDisplayName,
    int totalChapterCount,
    int totalLessonCount,
    Instant createdAt,
    List<ChapterResponse> chapters,
    Double ratingAverage,
    Long enrollmentCount,
    String thumbnailUrl,
    String paymentType,
    // 반려 사유 (REJECTED 상태일 때만 포함, 툴팁용)
    List<String> rejectCategories,
    String rejectReason
) {
    /**
     * 강의 상세 응답 생성.
     *
     * <p>NOTE: VIDEO 레슨의 videoUrl(signedUrl)은 강의 상세/목록 응답에서 내려주지 않는다.
     * 재생 시점에 V2 레슨 단건 조회에서 signedUrl을 발급하여 내려준다.
     */
    public static LectureResponse from(Lecture lecture, String instructorDisplayName) {
        return new LectureResponse(
            lecture.getId(),
            lecture.getTitle(),
            lecture.getDescription(),
            lecture.getCategoryId(),
            lecture.getLevel(),
            lecture.getLevel().getDisplayName(),
            lecture.getStatus().getDisplayName(),
            lecture.getInstructorId(),
            instructorDisplayName,
            lecture.getChapters().size(),
            lecture.getTotalLessonCount(),
            lecture.getCreatedAt(),
            lecture.getChapters().stream()
                .map(ChapterResponse::from)
                .collect(Collectors.toList()),
            null,
            null,
            lecture.getThumbnailUrl(),
            lecture.getPaymentType() != null ? lecture.getPaymentType().getDisplayName() : PaymentType.FREE.getDisplayName(),
            null,
            null
        );
    }

    /**
     * 강의 상세 응답 생성 (통계 포함).
     *
     * <p>NOTE: VIDEO 레슨의 videoUrl(signedUrl)은 강의 상세/목록 응답에서 내려주지 않는다.
     * 재생 시점에 V2 레슨 단건 조회에서 signedUrl을 발급하여 내려준다.
     */
    public static LectureResponse fromWithStatics(Lecture lecture, LectureStatistic statistic, String instructorDisplayName) {
        Double ratingAvg = 0.0;
        Long enrollmentCnt = 0L;

        if (statistic != null) {
            ratingAvg = statistic.getRatingAverage() != null ? statistic.getRatingAverage() : 0.0;
            enrollmentCnt = statistic.getEnrollmentCount() != null ? statistic.getEnrollmentCount() : 0L;
        }

        return new LectureResponse(
            lecture.getId(),
            lecture.getTitle(),
            lecture.getDescription(),
            lecture.getCategoryId(),
            lecture.getLevel(),
            lecture.getLevel().getDisplayName(),
            lecture.getStatus().getDisplayName(),
            lecture.getInstructorId(),
            instructorDisplayName,
            lecture.getChapters().size(),
            lecture.getTotalLessonCount(),
            lecture.getCreatedAt(),
            lecture.getChapters().stream()
                .map(ChapterResponse::from)
                .collect(Collectors.toList()),
            ratingAvg,
            enrollmentCnt,
            lecture.getThumbnailUrl(),
            lecture.getPaymentType() != null ? lecture.getPaymentType().getDisplayName() : PaymentType.FREE.getDisplayName(),
            null,
            null
        );
    }

    // Full detail including chapters and lessons info  without counts
    public static LectureResponse simpleFrom(Lecture lecture, String instructorDisplayName) {
        return new LectureResponse(
            lecture.getId(),
            lecture.getTitle(),
            lecture.getDescription(),
            lecture.getCategoryId(),
            lecture.getLevel(),
            lecture.getLevel().getDisplayName(),
            lecture.getStatus().getDisplayName(),
            lecture.getInstructorId(),
            instructorDisplayName,
            0,
            0,
            lecture.getCreatedAt(),
            null,
            null,
            null,
            lecture.getThumbnailUrl(),
            lecture.getPaymentType() != null ? lecture.getPaymentType().getDisplayName() : PaymentType.FREE.getDisplayName(),
            null,
            null
        );
    }

    // Simple response with statistics for list view
    public static LectureResponse simpleFromWithStats(Lecture lecture, LectureStatistic statistic, String instructorDisplayName) {
        return simpleFromWithStats(lecture, statistic, instructorDisplayName, null);
    }

    /**
     * 강사용 목록 응답 생성 (통계 포함, 반려 사유 포함)
     * REJECTED 상태인 경우 반려 카테고리와 상세 사유를 포함
     */
    public static LectureResponse simpleFromWithStats(Lecture lecture, LectureStatistic statistic, String instructorDisplayName, Approval latestApproval) {
        Double ratingAvg = 0.0;
        Long enrollmentCnt = 0L;
        
        if (statistic != null) {
            ratingAvg = statistic.getRatingAverage() != null ? statistic.getRatingAverage() : 0.0;
            enrollmentCnt = statistic.getEnrollmentCount() != null ? statistic.getEnrollmentCount() : 0L;
        }

        // 반려 사유 (REJECTED 상태이고 Approval 정보가 있을 때만)
        List<String> rejectCats = null;
        String rejectMsg = null;
        if (latestApproval != null && latestApproval.getRejectCategory() != null) {
            rejectCats = latestApproval.getRejectCategory().toCategories();
            rejectMsg = latestApproval.getRejectReason();
        }
        
        return new LectureResponse(
            lecture.getId(),
            lecture.getTitle(),
            lecture.getDescription(),
            lecture.getCategoryId(),
            lecture.getLevel(),
            lecture.getLevel().getDisplayName(),
            lecture.getStatus().getDisplayName(),
            lecture.getInstructorId(),
            instructorDisplayName,
            0,
            0,
            lecture.getCreatedAt(),
            null,
            ratingAvg,
            enrollmentCnt,
            lecture.getThumbnailUrl(),
            lecture.getPaymentType() != null ? lecture.getPaymentType().getDisplayName() : PaymentType.FREE.getDisplayName(),
            rejectCats,
            rejectMsg
        );
    }
}
