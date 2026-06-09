package com.seedit.feature.user.dto.request;

import lombok.Data;

@Data
public class UserUpdateRequest {
    /**
     * 프로필 수정 요청 시 (이름, 비밀번호, 생년월일 등 수정 가능한 항목만)
     */
    private String username;
    private String birth;
}
