package com.seedit.feature.settlement.controller;

import com.seedit.feature.settlement.domain.SettlementStatus;
import com.seedit.feature.settlement.dto.response.SettlementResponse;
import com.seedit.feature.settlement.service.SettlementService;
import com.seedit.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
@Tag(name="정산 API", description="예수금 정산 예정/내역 조회")
public class SettlementController {

    private final SettlementService settlementService;

    /** status=PENDING → 정산 예정, status=SETTLED → 입금 완료 내역, 없으면 전체 */
    @GetMapping
    public ApiResponse<List<SettlementResponse>> getMySettlements(
            Authentication authentication,
            @RequestParam(required = false) SettlementStatus status) {
        return ApiResponse.ok(settlementService.getMySettlements(authentication.getName(), status));
    }
}