package com.teamexp.learnflowapi.lecture.model;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public enum LessonType {
    VIDEO("VIDEO"),
    QUIZ("QUIZ");

    private final String displayName;

    LessonType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static LessonType forEntity(String displayName) {
        for (LessonType type : LessonType.values()) {
            if (type.getDisplayName().equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant with displayName " + displayName);
    }
}
