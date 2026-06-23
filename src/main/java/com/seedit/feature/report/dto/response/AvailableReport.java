package com.seedit.feature.report.dto.response;

/** DART에 실제 존재하는 정기보고서 (프론트 드롭다운용) */
public record AvailableReport(
        Integer bsnsYear,
        String reprtCode,
        String reportNm,    // "사업보고서(연간)" 등 표시용 라벨
        String receiptDate  // rcept_dt (YYYYMMDD)
) {}