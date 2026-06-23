package com.seedit.feature.report.service;

import com.seedit.feature.report.domain.FinancialStatement;
import com.seedit.feature.stock.domain.StockDetail;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

/**
 * LLM 입력용 재무 데이터 압축기.
 *
 * <p>토큰 절감 포인트
 * <ol>
 *   <li>원(₩) 15자리 → '조원' 단위 소수 1자리로 반올림</li>
 *   <li>비율 지표를 서버에서 미리 계산 → 출력 토큰 감소 + 모델 산술 오류 방지</li>
 *   <li>JSON 대신 마크다운 표 → 따옴표·필드명 반복 제거</li>
 * </ol>
 *
 * <pre>{@code
 *   String userData = CompactFinancialFormatter.build(stmts, stockDetail);
 * }</pre>
 */
public final class CompactFinancialFormatter {

    private static final BigDecimal JO = new BigDecimal("1000000000000"); // 1조

    private CompactFinancialFormatter() {}

    public static String build(List<FinancialStatement> stmts, StockDetail d) {
        List<FinancialStatement> rows = stmts.stream()
                .sorted(Comparator.comparing(FinancialStatement::bsnsYear)) // 연도 오름차순
                .toList();

        StringBuilder sb = new StringBuilder();

        // 0) 종목 식별 헤더 (어떤 기업 리포트인지 출력에 명시)
        if (d != null) {
            sb.append("종목: ")
                    .append(d.getCompanyName() == null ? "데이터 없음" : d.getCompanyName())
                    .append(" (").append(d.getTicker()).append(")\n\n");
        }

        // 1) 핵심 재무 표 (조원 단위)
        sb.append("(단위: 조원)\n")
                .append("연도 | 매출 | 영업익 | 순익 | 자산 | 부채 | 자본 | 유동자산 | 유동부채 | 현금 | 차입금 | 매출채권 | 재고 | 영업CF | 투자CF | 재무CF\n")
                .append("---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---\n");
        for (FinancialStatement s : rows) {
            sb.append(s.bsnsYear()).append(" | ")
                    .append(jo(s.revenue())).append(" | ")
                    .append(jo(s.operatingIncome())).append(" | ")
                    .append(jo(s.netIncome())).append(" | ")
                    .append(jo(s.assets())).append(" | ")
                    .append(jo(s.liabilities())).append(" | ")
                    .append(jo(s.equity())).append(" | ")
                    .append(jo(s.currentAssets())).append(" | ")
                    .append(jo(s.currentLiabilities())).append(" | ")
                    .append(jo(s.cash())).append(" | ")
                    .append(jo(s.borrowings())).append(" | ")
                    .append(jo(s.receivables())).append(" | ")
                    .append(jo(s.inventory())).append(" | ")
                    .append(jo(s.cashFlowOperating())).append(" | ")
                    .append(jo(s.cashFlowInvesting())).append(" | ")
                    .append(jo(s.cashFlowFinancing())).append("\n");
        }

        // 2) 미리 계산한 지표
        sb.append("\n미리 계산된 지표 (비율 단위: %, 이자보상배율: 배)\n")
                .append("연도 | 영업이익률 | 순이익률 | 부채비율 | 유동비율 | ROE | 매출성장률 | 이자보상배율\n")
                .append("---|---|---|---|---|---|---|---\n");
        FinancialStatement prev = null;
        for (FinancialStatement s : rows) {
            sb.append(s.bsnsYear()).append(" | ")
                    .append(pct(s.operatingMargin())).append(" | ")
                    .append(pct(ratio(s.netIncome(), s.revenue()))).append(" | ")
                    .append(pct(s.debtRatio())).append(" | ")
                    .append(pct(s.currentRatio())).append(" | ")
                    .append(roe(s)).append(" | ")
                    .append(prev == null ? "-" : pct(growth(s.revenue(), prev.revenue()))).append(" | ")
                    .append(num(s.interestCoverage()))
                    .append("\n");
            prev = s;
        }

        // 3) 주가지표 (자사 DB)
        sb.append("\n주가지표 (출처: 자사 DB): ")
                .append("PER ").append(nv(d == null ? null : d.getPer())).append(" | ")
                .append("PBR ").append(nv(d == null ? null : d.getPbr())).append(" | ")
                .append("EPS ").append(d == null || d.getEps() == null ? "데이터 없음" : d.getEps()).append(" | ")
                .append("BPS ").append(d == null || d.getBps() == null ? "데이터 없음" : d.getBps()).append("\n");

        // 4) notes (사업보고서 본문 — 현재 미수집)
        sb.append("매출 증감 이유·일회성 항목: 데이터 없음 (사업보고서 본문 미수집)\n");

        return sb.toString();
    }

    /** 원 → 조원, 소수 1자리. */
    private static String jo(BigDecimal won) {
        if (won == null) return "데이터 없음";
        return won.divide(JO, 1, RoundingMode.HALF_UP).toPlainString();
    }

    /** 비율(소수) → 퍼센트 문자열. */
    private static String pct(BigDecimal ratio) {
        if (ratio == null) return "데이터 없음";
        return ratio.multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP).toPlainString();
    }

    /** 배수 등 일반 수치(소수1자리). */
    private static String num(BigDecimal v) {
        return v == null ? "데이터 없음" : v.setScale(1, RoundingMode.HALF_UP).toPlainString();
    }

    private static String nv(BigDecimal v) { return v == null ? "데이터 없음" : v.toPlainString(); }

    private static BigDecimal ratio(BigDecimal a, BigDecimal b) {
        if (a == null || b == null || b.signum() == 0) return null;
        return a.divide(b, 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal growth(BigDecimal cur, BigDecimal prev) {
        if (cur == null || prev == null || prev.signum() == 0) return null;
        return cur.subtract(prev).divide(prev, 4, RoundingMode.HALF_UP);
    }

    /** ROE: DART 제공값(이미 %) 우선, 없으면 순이익/자본으로 보완 계산. */
    private static String roe(FinancialStatement s) {
        if (s.roe() != null) return s.roe().setScale(1, RoundingMode.HALF_UP).toPlainString();
        return pct(ratio(s.netIncome(), s.equity()));
    }
}