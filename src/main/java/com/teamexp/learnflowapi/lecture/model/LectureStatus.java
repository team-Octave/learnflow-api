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
}
