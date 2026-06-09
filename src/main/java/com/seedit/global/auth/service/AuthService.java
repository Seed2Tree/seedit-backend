package com.seedit.global.auth.service;

import com.seedit.global.auth.dto.request.LoginRequest;
import com.seedit.global.auth.dto.response.LoginResponse;
import com.seedit.global.util.JWTUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtutil;

    public AuthService(AuthenticationManager authenticationManager, JWTUtil jwtutil){
        this.authenticationManager = authenticationManager;
        this.jwtutil = jwtutil;
    }

    public LoginResponse login(LoginRequest loginRequest){
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        String accessToken = jwtutil.generateAccessToken(auth.getName());
        String refreshToken = jwtutil.generateRefreshToken(auth.getName());

        return new LoginResponse(accessToken, refreshToken, "Bearer", auth.getName());
    }
}
