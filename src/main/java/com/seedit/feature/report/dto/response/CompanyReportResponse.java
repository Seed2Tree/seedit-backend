package com.seedit.feature.report.dto.response;

import tools.jackson.databind.JsonNode;

/**
 * AI 분석 리포트 응답.
 * report: 모델이 생성한 구조화 JSON(파싱된 객체) — 프론트가 그대로 렌더.
 */
public record CompanyReportResponse(
        String stockCode,
        Integer bsnsYear,
        String reprtCode,
        JsonNode report,
        String model,
        String createdAt
) {}