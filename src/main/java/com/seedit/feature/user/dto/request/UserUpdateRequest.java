package com.seedit.feature.user.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * 프로필 수정 요청 시 (닉네임, 생년월일 등 수정 가능한 항목만)
 */
public record UserUpdateRequest (
        @NotBlank(message = "닉네임은 필수입니다.")
        String username,

        @NotNull
        @JsonFormat(pattern = "yyyy.MM.dd")
        LocalDate birth
) {}
