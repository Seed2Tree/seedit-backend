package com.seedit.feature.report.dto.response;

/** 저장된 리포트 분기 목록 항목 (프론트 필터용) */
public record CompanyReportPeriod(
        Integer bsnsYear,
        String reprtCode,
        String updatedAt
) {}