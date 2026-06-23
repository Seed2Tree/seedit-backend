package com.seedit.feature.report.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** ai_report 테이블 매핑 (분기별 AI 리포트 캐시) */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiReport {
    private Long arid;
    private String rceptNo;
    private String stockCode;     // 종목코드(=ticker)
    private Integer bsnsYear;     // 사업연도
    private String reprtCode;     // 11011/11012/11013/11014
    private String content;       // 모델 생성 JSON 문자열
    private String model;         // 생성 모델명
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}