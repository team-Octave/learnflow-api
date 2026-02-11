package com.teamexp.learnflowapi.ai.exception;

import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class AiSerializationException extends AiException {
    public AiSerializationException() {
        super(ErrorCode.AI_SUMMARY_SERIALIZATION_FAILED);
    }
}
