package com.teamexp.learnflowapi.ai.util;

import org.springframework.dao.DataIntegrityViolationException;

public final class AiDatabaseUtils {

    private AiDatabaseUtils() {
    }

    /**
     * DataIntegrityViolationException이 중복 키 오류인지 확인
     */
    public static boolean isDuplicateKeyError(DataIntegrityViolationException e) {
        String msg = e.getMessage();
        if (msg != null && msg.contains("Duplicate entry")) {
            return true;
        }
        Throwable cause = e.getCause();
        return cause != null && cause.getMessage() != null && cause.getMessage().contains("Duplicate entry");
    }
}
