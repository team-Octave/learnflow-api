package com.teamexp.learnflowapi.ai.exception;

import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class AiTaskNotFoundException extends AiException {
    public AiTaskNotFoundException() {
        super(ErrorCode.AI_TASK_NOT_FOUND);
    }
}
