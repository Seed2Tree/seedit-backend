package com.seedit.seedit.global.auth;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional                 // 각 테스트 종료 시 자동 롤백
@DisplayName("인증 API 통합 테스트")
class AuthApiIntegrationTest {

    @Autowired MockMvc mockMvc;

    // 테스트마다 충돌 안 나도록 유니크 이메일 생성
    private String uniqueEmail() {
        return "it-" + System.nanoTime() + "@seedit.com";
    }

    private String signupBody(String email, String password) {
        return """
            {
              "username": "tester",
              "password": "%s",
              "name": "테스터",
              "birth": "2000.01.01",
              "email": "%s"
            }
            """.formatted(password, email);
    }

    @Test
    @DisplayName("회원가입 성공 시 200 + userId/level 반환")
    void signup_success() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody(uniqueEmail(), "abcd1234!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").isNumber())
                .andExpect(jsonPath("$.data.level").value(1));
    }

    @Test
    @DisplayName("중복 이메일이면 409 AUTH_EMAIL_DUPLICATED")
    void signup_duplicateEmail() throws Exception {
        String email = uniqueEmail();
        // 1차 가입
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody(email, "abcd1234!")))
                .andExpect(status().isOk());
        // 같은 이메일 2차 가입 → 409
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody(email, "abcd1234!")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_EMAIL_DUPLICATED"));
    }

    @Test
    @DisplayName("비밀번호 규칙 위반이면 400 COMMON_VALIDATION")
    void signup_weakPassword() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody(uniqueEmail(), "1234")))   // 특수문자·영문 없음
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_VALIDATION"));
    }

    @Test
    @DisplayName("로그인 성공 시 200 + accessToken + refresh_token 쿠키")
    void login_success() throws Exception {
        String email = uniqueEmail();
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody(email, "abcd1234!")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "email": "%s", "password": "abcd1234!" }
                            """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.email").value(email))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true));
    }

    @Test
    @DisplayName("비밀번호 틀리면 401 AUTH_INVALID_CREDENTIALS")
    void login_wrongPassword() throws Exception {
        String email = uniqueEmail();
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody(email, "abcd1234!")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "email": "%s", "password": "wrong1234!" }
                            """.formatted(email)))
                // ⚠️ 만약 여기서 500이 나오면, GlobalExceptionHandler에
                //    @ExceptionHandler(AuthenticationException.class)가 빠진 거야 (지난번 피드백).
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("로그아웃 시 refresh_token 쿠키가 만료(maxAge=0)된다")
    void logout_clearsCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("refresh_token", 0));
    }

    @Test
    @DisplayName("토큰 없이 보호 자원 접근 시 4xx")
    void protectedEndpoint_withoutToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("로그인 토큰으로 /api/users/me 조회 시 level·balance 포함")
    void me_withToken() throws Exception {
        String email = uniqueEmail();
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody(email, "abcd1234!")))
                .andExpect(status().isOk());

        String loginBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "email": "%s", "password": "abcd1234!" }
                            """.formatted(email)))
                .andReturn().getResponse().getContentAsString();
        String accessToken = JsonPath.read(loginBody, "$.data.accessToken");

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.level.level").value(1))
                .andExpect(jsonPath("$.data.balance").value(5000000));
    }
}