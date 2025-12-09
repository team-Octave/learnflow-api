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

    // Need to add custom exception handling or custom exception class
    public static LectureLevel forEntity(String displayName) {
        for (LectureLevel level : LectureLevel.values()) {
            if (level.getDisplayName().equals(displayName)) {
                return level;
            }
        }
        throw new IllegalArgumentException("No enum constant with displayName " + displayName);
    }
}
