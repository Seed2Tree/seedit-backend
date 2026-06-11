package com.seedit.global.auth.service;

import com.seedit.feature.user.domain.Role;
import com.seedit.feature.user.domain.UserAccount;
import com.seedit.feature.user.service.UserService;
import com.seedit.global.auth.dto.request.LoginRequest;
import com.seedit.global.auth.dto.request.SignUpRequest;
import com.seedit.global.auth.dto.response.LoginResponse;
import com.seedit.global.auth.dto.response.SignupResponse;
import com.seedit.global.error.BusinessException;
import com.seedit.global.error.ErrorCode;
import com.seedit.global.util.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtutil;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager, JWTUtil jwtutil, UserService userService, PasswordEncoder passwordEncoder){
        this.authenticationManager = authenticationManager;
        this.jwtutil = jwtutil;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest loginRequest, HttpServletResponse response){
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );

        String accessToken = jwtutil.generateAccessToken(auth.getName(),userService.getUserByEmail(auth.getName()).getRole().name());
        String refreshToken = jwtutil.generateRefreshToken(auth.getName());

        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // TODO 배포시 true로 변경
        cookie.setPath("/");
        cookie.setMaxAge(14 * 24 * 60 * 60); // 14일

        response.addCookie(cookie);

        UserAccount userEntity = userService.getUserByEmail(auth.getName());
        LoginResponse.LoginUser loginUser = new LoginResponse.LoginUser(
                userEntity.getUserId(),
                userEntity.getEmail(),
                userEntity.getUsername(),
                userEntity.getName()
        );

        return new LoginResponse(accessToken, "Bearer", loginUser);
    }

    public void logout(HttpServletResponse response){
        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // TODO 배포시 true로 변경
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }

    @Transactional
    public SignupResponse signup(SignUpRequest request){
        if(userService.existsByEmail(request.email())){
            throw new BusinessException(ErrorCode.AUTH_EMAIL_DUPLICATED);
        }
        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(request.username());
        userAccount.setName(request.name());
        userAccount.setBirth(request.birth());
        userAccount.setEmail(request.email());

        String encodedPassword = passwordEncoder.encode(request.password());
        userAccount.setPasswordHash(encodedPassword);
        userAccount.setRole(Role.ROLE_USER);

        try{
            UserAccount saved = userService.addUser(userAccount);
            return SignupResponse.from(saved,1,0);
        } catch (DataIntegrityViolationException e){
            throw new BusinessException(ErrorCode.COMMON_INVALID_FORMAT, e.getMessage());
        }
    }

    public LoginResponse refresh(String refreshToken, HttpServletResponse response){
        if(refreshToken == null || !jwtutil.validateToken(refreshToken)){
            throw new BusinessException(ErrorCode.AUTH_TOKEN_EXPIRED);
        }
        String email = jwtutil.getSubject(refreshToken);

        String newAccessToken = jwtutil.generateAccessToken(email,userService.getUserByEmail(email).getRole().name());

        UserAccount user = userService.getUserByEmail(email);
        LoginResponse.LoginUser loginUser = new LoginResponse.LoginUser(
                user.getUserId(), user.getEmail(), user.getUsername(), user.getName()
        );
        return new LoginResponse(newAccessToken, "Bearer",loginUser);
    }
}
