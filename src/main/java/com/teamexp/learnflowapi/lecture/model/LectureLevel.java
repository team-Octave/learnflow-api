package com.teamexp.learnflowapi.lecture.model;

public enum LectureLevel {
    BEGINNER("BEGINNER"),
    INTERMEDIATE("INTERMEDIATE"),
    ADVANCED("ADVANCED");

    private final String displayName;

    LectureLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
