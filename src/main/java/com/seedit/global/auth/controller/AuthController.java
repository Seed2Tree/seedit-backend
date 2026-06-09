package com.seedit.global.auth.controller;

import com.seedit.feature.user.domain.UserAccount;
import com.seedit.feature.user.service.UserService;
import com.seedit.global.auth.dto.request.LoginRequest;
import com.seedit.global.auth.dto.request.SignUpRequest;
import com.seedit.global.auth.dto.response.LoginResponse;
import com.seedit.global.auth.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name="인증 API", description = "회원가입, 로그인 및 토큰 관리 API")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 1. 회원 가입 API
     * /api/auth/signup 경로로 요청
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody SignUpRequest signUpRequest){
        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(signUpRequest.getUsername());
        userAccount.setName(signUpRequest.getName());
        userAccount.setBirth(signUpRequest.getBirth());
        userAccount.setEmail(signUpRequest.getEmail());

        String encodedPassword = passwordEncoder.encode(signUpRequest.getPassword());
        userAccount.setPasswordHash(encodedPassword);

        if(userService.addUser(userAccount)){
            return ResponseEntity.status(HttpStatus.CREATED).body("사용자 등록이 정상적으로 등록되었습니다.");
        }
        return ResponseEntity.internalServerError().body("사용자 정보 등록에 실패했습니다.");
    }
    /**
     * 2. 로그인 API
     * /api/auth/login 경로로 이메일/비번을 받아 JWT를 리턴
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest){
        try {
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            // 사용자 없음 / 비밀번호 불일치 / 계정 비활성화 등 모두 잡힘
            return ResponseEntity.status(401).body(Map.of("error", "인증에 실패했습니다."));
        }
    }
}
