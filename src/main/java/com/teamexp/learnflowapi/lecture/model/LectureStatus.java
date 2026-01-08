package com.teamexp.learnflowapi.lecture.model;

import com.teamexp.learnflowapi.lecture.exception.LectureStatusInvalidException;

public enum LectureStatus {
    AVAILABLE("PUBLISHED"),
    UNAVAILABLE("UNPUBLISHED"),
    SUBMITTED("SUBMITTED"),
    REJECTED("REJECTED");

    private final String displayName;

    LectureStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static LectureStatus forEntity(String displayName) {
        for (LectureStatus status : LectureStatus.values()) {
            if (status.getDisplayName().equals(displayName)) {
                return status;
            }
        }
        throw new LectureStatusInvalidException();
    }

    public boolean isPublic() {
        // 도메인 지식: 공개 상태 판별
        return this == AVAILABLE;
    }

}
