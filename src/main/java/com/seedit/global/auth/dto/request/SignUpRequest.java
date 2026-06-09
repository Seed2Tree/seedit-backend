package com.seedit.global.auth.dto.request;

import lombok.Data;

@Data
public class SignUpRequest {
    /**
     * 회원가입 요청 시 (ID, 비번, 이름, 이메일, 생년월일)
     */
    private String username;
    private String password;
    private String name;
    private String birth;
    private String email;
}
