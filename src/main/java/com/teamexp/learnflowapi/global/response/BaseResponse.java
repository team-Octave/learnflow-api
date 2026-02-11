package com.teamexp.learnflowapi.global.response;

import org.slf4j.MDC;

public class BaseResponse<T> {
    private boolean success;
    private String code;
    private String message;
    private T data;
    private String traceId; // 추적 ID

    /**
     * ✨ [수정] Controller에서 new BaseResponse<>(data) 형태로 호출할 때 필요한 생성자
     * 성공(SUCCESS) 상태로 초기화하며, MDC에서 traceId를 자동으로 가져옵니다.
     */
    public BaseResponse(T data) {
        this.success = true;
        this.code = "SUCCESS";
        this.message = "요청이 성공했습니다.";
        this.data = data;
        this.traceId = MDC.get("traceId"); // 현재 스레드의 TraceID 주입
    }

    /**
     * 내부 전용 생성자 (모든 필드 초기화)
     */
    private BaseResponse(boolean success, String code, String message, T data, String traceId) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.traceId = traceId;
    }

    // --- Static Factory Methods ---

    // 성공 응답 (Static 메서드 사용 시)
    public static <T> BaseResponse<T> ok(T data) {
        return new BaseResponse<>(true, "SUCCESS", "요청이 성공했습니다.", data, MDC.get("traceId"));
    }

    // 에러 응답 1: 데이터 없음
    public static <T> BaseResponse<T> error(String code, String message, String traceId) {
        return new BaseResponse<>(false, code, message, null, traceId);
    }

    // 에러 응답 2: 데이터 포함
    public static <T> BaseResponse<T> error(String code, String message, T data, String traceId) {
        return new BaseResponse<>(false, code, message, data, traceId);
    }

    // --- Getter Methods ---
    public boolean isSuccess() { return success; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public String getTraceId() { return traceId; }
}
