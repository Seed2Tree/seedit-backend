package com.seedit.feature.user.controller;

import com.seedit.feature.user.domain.UserAccount;
import com.seedit.feature.user.dto.request.PasswordUpdateRequest;
import com.seedit.feature.user.dto.request.UserUpdateRequest;
import com.seedit.feature.user.dto.response.UserProfileResponse;
import com.seedit.feature.user.service.UserService;
import com.seedit.global.error.BusinessException;
import com.seedit.global.error.ErrorCode;
import com.seedit.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name="사용자 API", description="마이페이지 및 계정 관리 API")
public class UserController {

    private final UserService userService;

    /**
     * 1. 내 정보 조회 API
     *
     */
    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getMyInfo(Authentication authentication){
        String currentEmail = authentication.getName();

        UserProfileResponse profile = userService.getMyProfile(currentEmail);

        return ApiResponse.ok(profile);
    }

    /**
     * 2. 내 정보 수정 API
     * 내 정보를 수정합니다.
     *
     * @param authentication, UserUpdateRequest 수정된 정보가 포함된 User 객체
     * @return 성공 시 200(OK), 실패 시 500(INTERNAL_SERVER_ERROR)
     */
    @PutMapping("/me")
    public ApiResponse<String> updateUser(Authentication authentication,
                                          @Valid @RequestBody UserUpdateRequest updateRequest){
        String currentEmail = authentication.getName();
        UserAccount userAccount = userService.getUserByEmail(currentEmail);

        userAccount.setUsername(updateRequest.username()); // 닉네임 변경!
        userAccount.setBirth(updateRequest.birth());

        if(!userService.updateUser(userAccount)){
            throw new BusinessException(ErrorCode.COMMON_INTERNAL, "사용자 정보 수정에 실패했습니다.");
        }
        return ApiResponse.ok("사용자 정보가 정상적으로 수정되었습니다.");
    }

    /**
     * 3. 사용자 비밀번호 수정 API
     *
     * @param authentication, PasswordUpdateRequest 수정된 정보가 포함된 User 객체
     * @return 성공 시 200(OK), 실패 시 500(INTERNAL_SERVER_ERROR)
     */
    @PutMapping("/password")
    public ApiResponse<String> changePassword(
            Authentication authentication,
            @Valid @RequestBody PasswordUpdateRequest request
            ){

        String currentEmail = authentication.getName();

        boolean isSucess = userService.updatePassword(currentEmail,request.curPassword(),request.newPassword());
        if(!isSucess){
            throw new BusinessException(ErrorCode.COMMON_INTERNAL, "비밀번호 변경에 실패했습니다.");
        }
        return ApiResponse.ok("비밀번호가 성공적으로 변경되었습니다.");
    }

    /**
     * 4. 회원 탈퇴 API
     *  회원 정보를 삭제합니다.
     *
     * @param authentication
     * @return 성공 시 200(OK), 실패 시 500(INTERNAL_SERVER_ERROR)
     */
    @DeleteMapping("/me")
    public ApiResponse<String> deleteUser(
            Authentication authentication){
        String currentEmail = authentication.getName();
        UserAccount userAccount = userService.getUserByEmail(currentEmail);

        if(!userService.deleteUser(userAccount.getUserId())){
            throw new BusinessException(ErrorCode.COMMON_INTERNAL,  "회원 탈퇴 처리에 실패했습니다.");
        }
        return ApiResponse.ok("회원 탈퇴가 정상적으로 삭제되었습니다.");
    }

    @PostMapping("/me/reset")
    public ApiResponse<String> resetUser(
            Authentication authentication){
        String currentEmail = authentication.getName();
        userService.resetUser(currentEmail);
        return ApiResponse.ok("회원 초기화가 정상적으로 완료되었습니다.");
    }




}
