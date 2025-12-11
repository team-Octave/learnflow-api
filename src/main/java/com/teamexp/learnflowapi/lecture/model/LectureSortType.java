package com.teamexp.learnflowapi.lecture.model;

import com.teamexp.learnflowapi.lecture.exception.LectureSortTypeInvalidException;

public enum LectureSortType {
    POPULAR("POPULAR"),
    RATING("RATING"),
    LATEST("LATEST");

    private final String displayName;

    LectureSortType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static LectureSortType forEntity(String displayName) {
        for (LectureSortType sortType : LectureSortType.values()) {
            if (sortType.getDisplayName().equals(displayName)) {
                return sortType;
            }
        }
        throw new LectureSortTypeInvalidException();
    }
}
