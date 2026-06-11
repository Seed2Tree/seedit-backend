package com.seedit.global.auth.controller;

import com.seedit.feature.user.service.UserService;
import com.seedit.global.auth.dto.request.LoginRequest;
import com.seedit.global.auth.dto.request.SignUpRequest;
import com.seedit.global.auth.dto.response.LoginResponse;
import com.seedit.global.auth.dto.response.SignupResponse;
import com.seedit.global.auth.service.AuthService;
import com.seedit.global.error.ErrorCode;
import com.seedit.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name="인증 API", description = "회원가입, 로그인 및 토큰 관리 API")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;


    /**
     * 1. 회원 가입 API
     * /api/auth/signup 경로로 요청
     */
    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signUp(@Valid @RequestBody SignUpRequest signUpRequest){

        return ApiResponse.ok(authService.signup(signUpRequest));
    }
    /**
     * 2. 로그인 API
     * /api/auth/login 경로로 이메일/비번을 받아 JWT를 리턴
     */
    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody LoginRequest loginRequest,
                                HttpServletResponse response){
        return ApiResponse.ok(authService.login(loginRequest, response));
    }
    /**
     * 3. 로그아웃 API
     * /api/auth/logout
     */
    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpServletResponse response){
        authService.logout(response);

        return ApiResponse.ok("로그아웃이 성공적으로 처리되었습니다.");
    }

    /**
     * 4. 이메일 중복 확인 API
     * /api/auth/check-email
     */
    @PostMapping("/check-email")
    public ApiResponse<String> checkEmail(@RequestParam String email){
        if(userService.existsByEmail(email)){
            return ApiResponse.error(ErrorCode.AUTH_EMAIL_DUPLICATED);
        }
        return ApiResponse.ok("사용 가능한 이메일입니다.");
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(
            @CookieValue(value="refresh_token", required = false) String refreshToken,
            HttpServletResponse response){
        return ApiResponse.ok(authService.refresh(refreshToken, response));
    }

}
