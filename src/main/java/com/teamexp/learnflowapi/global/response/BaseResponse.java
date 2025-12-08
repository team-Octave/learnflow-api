package com.teamexp.learnflowapi.global.response;

public class BaseResponse<T> {
    private boolean success;
    private String code;
    private String message;
    private T data;

    private BaseResponse(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> BaseResponse<T> ok(T data) {
        return new BaseResponse<>(true, "SUCCESS", "요청이 성공했습니다.", data);
    }

    public static <T> BaseResponse<T> error(String code, String message) {
        return new BaseResponse<>(false, code, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
