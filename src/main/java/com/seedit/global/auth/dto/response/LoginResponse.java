package com.seedit.global.auth.dto.response;


public record LoginResponse (
        String accessToken,
        String tokenType, // "Bearer"
        LoginUser user
){

    public record LoginUser (
            Long userId,
            String email,
            String username,
            String name
    ) {}
}
