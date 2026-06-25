package com.seedit.feature.reason.controller;

import com.seedit.feature.reason.service.ReasonService;
import com.seedit.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reasons")
@RequiredArgsConstructor
@Tag(name = "투자 가설 API", description = "가설 검증")
public class ReasonController {

    private final ReasonService reasonService;

    @PatchMapping("/{rid}/verify")
    public ApiResponse<Void> verifyReason(Authentication authentication, @PathVariable Long rid) {
        reasonService.verify(authentication.getName(), rid);
        return ApiResponse.ok(null);
    }
}