package com.seedit.global.error;

import com.seedit.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 모든 예외를 공통 응답 봉투로 변환. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        ErrorCode ec = e.getErrorCode();
        return ResponseEntity.status(ec.getHttpStatus())
                .body(ApiResponse.error(ec.getCode(), e.getMessage()));
    }

    // @Valid 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        FieldError fe = e.getBindingResult().getFieldError();
        String msg = (fe != null) ? fe.getDefaultMessage() : ErrorCode.COMMON_VALIDATION.getMessage();
        ErrorCode ec = ErrorCode.COMMON_VALIDATION;
        return ResponseEntity.status(ec.getHttpStatus())
                .body(ApiResponse.error(ec.getCode(), msg));
    }

    // 그 외 예측 못한 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleEtc(Exception e) {
        // TODO: 운영용 로깅(logger.error) 추가
        ErrorCode ec = ErrorCode.COMMON_INTERNAL;
        return ResponseEntity.status(ec.getHttpStatus())
                .body(ApiResponse.error(ec.getCode(), ec.getMessage()));
    }
}
