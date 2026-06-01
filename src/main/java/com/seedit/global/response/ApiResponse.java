package com.seedit.global.response;

/**
 * 모든 API 공통 응답 봉투.
 *  성공: { "success": true,  "data": {...}, "error": null }
 *  실패: { "success": false, "data": null,  "error": { "code", "message" } }
 */
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorBody error;

    private ApiResponse(boolean success, T data, ErrorBody error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorBody(code, message));
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public ErrorBody getError() { return error; }

    public static class ErrorBody {
        private final String code;
        private final String message;
        public ErrorBody(String code, String message) {
            this.code = code;
            this.message = message;
        }
        public String getCode() { return code; }
        public String getMessage() { return message; }
    }
}
