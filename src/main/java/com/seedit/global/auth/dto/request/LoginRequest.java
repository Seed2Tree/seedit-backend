package com.seedit.global.auth.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * POST /api/auth/login 요청 바디.
 * 로그인 요청 시 (이메일, 비밀번호)
 * 예: { "email": "ssafy@ssafy.com", "password": "1q2w3e4r" }
 */
public record LoginRequest (
    @NotBlank(message="이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String email,

    @NotBlank(message = "비밀번호는 필수입니다.")
    String password
) {}
