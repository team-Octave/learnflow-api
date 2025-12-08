package com.teamexp.learnflowapi.lecture.model;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public enum LessonType {
    VIDEO("영상"),
    QUIZ("퀴즈");

    private final String displayName;

    LessonType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
