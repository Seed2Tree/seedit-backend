package com.seedit.feature.user.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordUpdateRequest (
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(
                regexp="^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,20}$",
                message="비밀번호는 8~20자이며 영문, 숫자, 특수문자를 포함해야 합니다."
        )
        String newPassword
){}
// 동일한 password 인가?