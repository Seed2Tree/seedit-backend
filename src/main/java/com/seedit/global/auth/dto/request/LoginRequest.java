package com.seedit.global.auth.dto.request;

import lombok.Data;
@Data
public class LoginRequest {
    /**
     * POST /api/auth/login 요청 바디.
     * 로그인 요청 시 (이메일, 비밀번호)
     * 예: { "email": "ssafy@ssafy.com", "password": "1q2w3e4r" }
     */
    private String email;
    private String password;
}
