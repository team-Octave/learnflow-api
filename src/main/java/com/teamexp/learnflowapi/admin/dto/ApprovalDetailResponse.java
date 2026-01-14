package com.teamexp.learnflowapi.admin.dto;

import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.user.model.User;
import java.util.List;

public record ApprovalDetailResponse(
    Long lectureId,
    String title,
    String description,
    String instructorName,
    String level,
    Integer categoryId,
    List<ChapterDto> chapters

) {
    public static ApprovalDetailResponse of(Lecture lecture, User instructor) {
        return new ApprovalDetailResponse(
            lecture.getId(),
            lecture.getTitle(),
            lecture.getDescription(),
            // instructorName이 null이면 알 수 없음으로 표시
            instructor == null ? "알 수 없음" :  instructor.getNickname(),
            lecture.getLevel().getDisplayName(),
            lecture.getCategoryId(),
            lecture.getChapters().stream()
                .map(ChapterDto::from)
                .toList()
        );
    }

}
