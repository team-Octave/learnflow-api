package com.teamexp.learnflowapi.global.response;

import org.slf4j.MDC;

public class BaseResponse<T> {
    private boolean success;
    private String code;
    private String message;
    private T data;
    private String traceId; // <-- 추적 ID 필드 추가

    // 생성자에 traceId 추가
    private BaseResponse(boolean success, String code, String message, T data, String traceId) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.traceId = traceId;
    }

    // 성공 응답: MDC에서 자동으로 traceId를 가져와 주입합니다.
    public static <T> BaseResponse<T> ok(T data) {
        return new BaseResponse<>(true, "SUCCESS", "요청이 성공했습니다.", data, MDC.get("traceId"));
    }

    // 에러 응답 1: traceId를 외부(ExceptionHandler)에서 받아 처리
    public static <T> BaseResponse<T> error(String code, String message, String traceId) {
        return new BaseResponse<>(false, code, message, null, traceId);
    }

    // 에러 응답 2: 데이터가 포함된 경우
    public static <T> BaseResponse<T> error(String code, String message, T data, String traceId) {
        return new BaseResponse<>(false, code, message, data, traceId);
    }

    // Getter들
    public boolean isSuccess() { return success; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public String getTraceId() { return traceId; } // <-- Getter 추가
}
