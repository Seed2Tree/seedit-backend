package com.seedit.feature.portfolio.controller;

import com.seedit.feature.balance.domain.BalanceHistory;
import com.seedit.feature.balance.dto.response.BalanceHistoryResponse;
import com.seedit.feature.balance.service.BalanceHistoryService;
import com.seedit.feature.portfolio.dto.response.PortfolioResponse;
import com.seedit.feature.portfolio.service.PortfolioService;
import com.seedit.feature.user.domain.UserAccount;
import com.seedit.feature.user.service.UserService;
import com.seedit.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portfolio")
@Tag(name="Portfolio 보유 종목 API", description = "보유 종목 추가, 수정, 조회 API")
public class PortfolioController {

    private final UserService userService;

    private final PortfolioService portfolioService;


    @GetMapping
    public ApiResponse<List<PortfolioResponse>> getPortfolios(Authentication authentication){
        UserAccount me = userService.getUserByEmail(authentication.getName());
        Long userId = me.getUserId();
        List<PortfolioResponse> response = portfolioService.findAllByUserId(userId);
        return ApiResponse.ok(response);
    }


}