package com.seedit.feature.report.controller;

import com.seedit.feature.report.dto.response.AvailableReport;
import com.seedit.feature.report.dto.response.CompanyReportPeriod;
import com.seedit.feature.report.dto.response.CompanyReportResponse;
import com.seedit.feature.report.service.CompanyReportService;
import com.seedit.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks/{ticker}/report")
@Tag(name = "종목 AI 분석 리포트 API")
@RequiredArgsConstructor
public class CompanyReportController {

    private final CompanyReportService service;

    /**
     * 저장된 AI 분석 조회. 없으면 data=null (프론트가 '생성하기' 버튼 노출).
     * 예: GET /api/stocks/005490/report?bsnsYear=2024&reprtCode=11011
     */
    @GetMapping
    public ApiResponse<CompanyReportResponse> get(
            @PathVariable String ticker,
            @RequestParam Integer bsnsYear,
            @RequestParam(defaultValue = "11011") String reprtCode) {
        return ApiResponse.ok(service.getSavedReport(ticker, bsnsYear, reprtCode));
    }

    /**
     * AI 분석 생성 + 저장(이미 있으면 갱신) 후 반환.
     * 예: POST /api/stocks/005490/report?bsnsYear=2024&reprtCode=11011
     */
    @PostMapping
    public ApiResponse<CompanyReportResponse> generate(
            @PathVariable String ticker,
            @RequestParam Integer bsnsYear,
            @RequestParam(defaultValue = "11011") String reprtCode) {
        return ApiResponse.ok(service.generateAndSave(ticker, bsnsYear, reprtCode));
    }

    /**
     * 저장된 분기 목록(최신순) — 프론트 분기 필터 구성용.
     * 예: GET /api/stocks/005490/report/periods
     */
    @GetMapping("/periods")
    public ApiResponse<List<CompanyReportPeriod>> periods(@PathVariable String ticker) {
        return ApiResponse.ok(service.getPeriods(ticker));
    }

    /** DART에 실제 존재하는 정기보고서 목록 — 프론트 드롭다운 구성용. */
    @GetMapping("/available")
    public ApiResponse<List<AvailableReport>> available(@PathVariable String ticker) {
        return ApiResponse.ok(service.getAvailablePeriods(ticker));
    }
}