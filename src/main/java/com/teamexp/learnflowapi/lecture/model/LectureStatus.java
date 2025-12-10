package com.teamexp.learnflowapi.lecture.model;

public enum LectureStatus {
    AVAILABLE("PUBLISHED"),
    UNAVAILABLE("UNPUBLISHED");

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
        throw new IllegalArgumentException("No enum constant with displayName " + displayName);
    }
}
