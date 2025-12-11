package com.teamexp.learnflowapi.lecture.model;


import com.teamexp.learnflowapi.lecture.exception.LessonTypeInvalidException;

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
        throw new LessonTypeInvalidException();
    }
}
