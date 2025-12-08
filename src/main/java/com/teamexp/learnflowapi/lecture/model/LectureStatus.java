package com.teamexp.learnflowapi.lecture.model;

public enum LectureStatus {
    AVAILABLE("수강 가능"),
    UNAVAILABLE("수강 불가");

    private final String displayName;

    LectureStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
