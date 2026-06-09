package com.seedit.feature.user.controller;

import com.seedit.feature.user.domain.UserAccount;
import com.seedit.feature.user.dto.request.PasswordUpdateRequest;
import com.seedit.feature.user.dto.request.UserUpdateRequest;
import com.seedit.feature.user.dto.response.UserProfileResponse;
import com.seedit.feature.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@Tag(name="사용자 API", description="회원 가입, 로그인 및 계정 관리 API")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     */
    /**
     * 2. 사용자 전체 목록 조회 API
     * DB에 저장된 모든 주인 정보를 조회합니다.
     *
     * @return 사용자 목록(List<User>), 없으면 204(NO_CONTENT)
     */
    @GetMapping
    public ResponseEntity<List<UserProfileResponse>> getAllUsers(){
        List<UserAccount> userAccounts = userService.getAllUsers();
        if(userAccounts != null && !userAccounts.isEmpty()){
            // Entity List -> DTO List로 변환
            List<UserProfileResponse> responses = userAccounts.stream()
                    .map(UserProfileResponse::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * 3. 특정 사용자 상세 조회 API
     * ID 값을 기준으로 해당 주인 정보를 조회합니다.
     *
     * @param userId 조회할 사용자의 고유 ID
     * @return UserProfileResponse 객체, 없으면 404(NOT_FOUND)
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable int userId){
        UserAccount userAccount = userService.getUserById(userId);
        if(userAccount != null){
            return ResponseEntity.ok(new UserProfileResponse(userAccount));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 2-. 사용자 정보 수정 API
     * ID를 기준으로 사용자 정보를 수정합니다.
     *
     * @param userId 삭제할 사용자의 ID
     * @param updateRequest 수정된 정보가 포함된 User 객체
     * @return 성공 시 200(OK), 실패 시 500(INTERNAL_SERVER_ERROR)
     */
    @PutMapping("/{userId}")
    public ResponseEntity<String> updateUser(@PathVariable int userId, @RequestBody UserUpdateRequest updateRequest){

        UserAccount userAccount = userService.getUserById(userId);
        if(userAccount == null){
            return ResponseEntity.notFound().build(); // 유저 없음
        }

        userAccount.setUserId(userId);
        userAccount.setUsername(updateRequest.getUsername()); // 닉네임 변경!
        userAccount.setBirth(updateRequest.getBirth());

        if(userService.updateUser(userAccount)){
            return ResponseEntity.ok("사용자 정보가 정상적으로 수정되었습니다.");
        }
        return ResponseEntity.internalServerError().body("사용자 정보 수정에 실패했습니다.");
    }

    /**
     * . 사용자 비밀번호 수정 API
     *
     * @param PasswordUpdateRequest 수정된 정보가 포함된 User 객체
     * @return 성공 시 200(OK), 실패 시 500(INTERNAL_SERVER_ERROR)
     */
    @PutMapping("/password")
    public ResponseEntity<String> changePassword(
            @RequestBody PasswordUpdateRequest request,
            Authentication authentication){

        String currentEmail = authentication.getName();

        boolean isSucess = userService.updatePassword(currentEmail,request.getNewPassword());

        if(isSucess){
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        }
        return ResponseEntity.internalServerError().body("비밀번호 변경에 실패했습니다.");
    }

    /**
     * 2-. 사용자 정보 삭제 API
     * ID를 기준으로 사용자 정보를 삭제합니다.
     *
     * @param userId 삭제할 사용자의 ID
     * @return 성공 시 200(OK), 실패 시 500(INTERNAL_SERVER_ERROR)
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable int userId){
        if(userService.deleteUser(userId)){
            return ResponseEntity.ok("사용자 정보가 정상적으로 삭제되었습니다.");
        }
        return ResponseEntity.internalServerError().body("사용자 정보 삭제에 실패했습니다.");
    }

}
