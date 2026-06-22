package com.seedit.global.error;


import org.springframework.http.HttpStatus;

/** 공통 에러 코드 (API_CONTRACT.md 기준). 개발하며 추가 가능. */
public enum ErrorCode {

    COMMON_VALIDATION(HttpStatus.BAD_REQUEST, "COMMON_VALIDATION", "입력값이 올바르지 않습니다."),
    COMMON_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "COMMON_INVALID_FORMAT", "요청 형식이 올바르지 않습니다."),
    COMMON_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_NOT_FOUND", "요청한 자원을 찾을 수 없습니다."),
    COMMON_INTERNAL(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_INTERNAL", "일시적인 오류가 발생했습니다."),

    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."),
    AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_TOKEN_EXPIRED", "로그인이 만료되었습니다. 다시 로그인해 주세요."),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_FORBIDDEN", "접근 권한이 없습니다."),
    AUTH_EMAIL_DUPLICATED(HttpStatus.CONFLICT, "AUTH_EMAIL_DUPLICATED", "이미 가입된 이메일입니다."),

    TRADE_INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "TRADE_INSUFFICIENT_BALANCE", "잔액이 부족합니다."),
    TRADE_INSUFFICIENT_QUANTITY(HttpStatus.BAD_REQUEST, "TRADE_INSUFFICIENT_QUANTITY", "보유 수량을 초과해 매도할 수 없습니다."),
    TRADE_DUPLICATED_REQUEST(HttpStatus.CONFLICT, "TRADE_DUPLICATED_REQUEST", "중복된 거래 요청입니다."),

    REASON_REQUIRED_ON_BUY(HttpStatus.BAD_REQUEST, "REASON_REQUIRED_ON_BUY", "매수 시 투자 가설은 필수입니다."),
    REASON_REQUIRED_ON_SELL(HttpStatus.BAD_REQUEST, "REASON_REQUIRED_ON_SELL", "매도 시 복기 작성은 필수입니다."),

    DIARY_ALREADY_WRITTEN_TODAY(HttpStatus.CONFLICT, "DIARY_ALREADY_WRITTEN_TODAY", "오늘은 이미 일기를 작성했습니다."),
    DIARY_FEEDBACK_ALREADY_EXISTS(HttpStatus.CONFLICT, "DIARY_FEEDBACK_ALREADY_EXISTS", "이미 AI 피드백이 생성되었습니다."),
    WATCHLIST_DUPLICATED(HttpStatus.CONFLICT, "WATCHLIST_DUPLICATED", "이미 등록된 관심 종목입니다."),
    STUDY_BOOKMARK_DUPLICATED(HttpStatus.CONFLICT, "STUDY_BOOKMARK_DUPLICATED", "이미 즐겨찾기한 콘텐츠입니다."),

    AI_GENERATION_FAILED(HttpStatus.OK, "AI_GENERATION_FAILED", "AI 응답 생성에 실패했습니다. 잠시 후 다시 시도해 주세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
}
