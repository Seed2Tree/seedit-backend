package com.seedit.global.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * 회원가입 요청 시 (ID, 비번, 이름, 이메일, 생년월일)
 */
public record SignUpRequest (

        @NotBlank(message = "닉네임은 필수입니다.")
    String username,

        @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
            regexp="^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,20}$",
            message="비밀번호는 8~20자이며 영문, 숫자, 특수문자를 포함해야 합니다."
    )
    String password,

        @NotBlank(message = "이름은 필수입니다.")
    String name,

    @NotNull
    @JsonFormat(pattern = "yyyy.MM.dd")
    LocalDate birth,

    @NotBlank(message="이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String email
) {}