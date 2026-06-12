package com.seedit.feature.balance.controller;

import com.seedit.feature.balance.domain.BalanceHistory;
import com.seedit.feature.balance.dto.response.BalanceHistoryResponse;
import com.seedit.feature.balance.service.BalanceHistoryService;
import com.seedit.feature.user.domain.UserAccount;
import com.seedit.feature.user.service.UserService;
import com.seedit.global.error.BusinessException;
import com.seedit.global.error.ErrorCode;
import com.seedit.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/balance-histories")
@Tag(name="사용자 잔액 조회 API", description = "사용자의 잔액 이력 조회 API")
public class BalanceHistoryController {

    private final UserService userService;

    private final BalanceHistoryService balanceHistoryService;

    /**
     * 잔액 단건 조회 API
     * bhid를 통해 특정 잔액을 조회합니다.
     * @param bhid
     * @return
     */
    @GetMapping("/{bhid}")
    public ApiResponse<BalanceHistoryResponse> getBalanceHistory(@PathVariable("bhid") Long bhid,
                                                                 Authentication authentication){
        UserAccount me = userService.getUserByEmail(authentication.getName());
        BalanceHistory balanceHistory = balanceHistoryService.findBalanceHistory(bhid);
        if(!balanceHistory.getUserId().equals(me.getUserId())){
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN,"잘못된 접근입니다.");
        }

        return ApiResponse.ok(BalanceHistoryResponse.from(balanceHistory));
    }

    /**
     * 2. 사용자의 모든 거래 내역 조회 API
     * 사용자의 username(email)을 기준으로 거래 내역을 조죄합니다.
     * @param authentication
     * @return ApiResponse<List<BalanceHistory>>
     */
    @GetMapping
    public ApiResponse<List<BalanceHistoryResponse>> getBalanceHistories(Authentication authentication){
        String currentEmail = authentication.getName();
        UserAccount userAccount = userService.getUserByEmail(currentEmail);

        List<BalanceHistoryResponse> responses = balanceHistoryService
                .findBalanceHistories(userAccount.getUserId())
                .stream()
                .map(BalanceHistoryResponse::from)
                .toList();

        return ApiResponse.ok(responses);
    }

}
