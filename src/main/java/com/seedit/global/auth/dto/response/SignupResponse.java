package com.seedit.global.auth.dto.response;

import com.seedit.feature.user.domain.UserAccount;

public record SignupResponse (
        Long userId,
        String username,
        String email,
        String name,
        int level,
        int point
){

    public static SignupResponse from(UserAccount user, int level, int point) {
        return new SignupResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                level,
                point
        );
    }
}
