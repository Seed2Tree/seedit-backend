package com.seedit.feature.report.planner;

import tools.jackson.databind.JsonNode;
import com.seedit.feature.report.domain.FinancialStatement;
import com.seedit.feature.report.external.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import static com.seedit.feature.report.external.DartAccountMapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 종목 1개에 대해 N개 기간의 재무 데이터를 결정형 순서로 수집한다.
 * (LLM이 아니라 서버가 수집 순서를 통제 = 서버 주도 planner)
 *
 * 폴백 순서:
 *   1) 요청한 연도 + 요청 보고서코드
 *   2) 데이터가 하나도 없으면 → 최근 연도 자동 탐색(같은 보고서코드)
 *   3) 그래도 없으면 → 사업보고서 대신 최신 분기/반기(11014→11012→11013) 탐색
 */
@Component
public class ReportPlanner {

    private static final int MAX_PERIODS = 3;   // 최대 수집 기간 수(최근 N개)
    private static final int LOOKBACK    = 5;   // 연도 자동 탐색 범위(올해부터 과거 N년)
    private static final Logger log = LoggerFactory.getLogger(ReportPlanner.class);

    // 사업보고서(11011)가 없을 때 시도할 보고서코드: 3분기 → 반기 → 1분기
    private static final List<String> QUARTER_FALLBACK = List.of("11014", "11012", "11013");

    private final CorpCodeProvider corpCodes;
    private final DartApiClient dart;

    public ReportPlanner(CorpCodeProvider corpCodes, DartApiClient dart) {
        this.corpCodes = corpCodes;
        this.dart = dart;
    }

    /**
     * @param ticker     종목코드 6자리
     * @param years      조회 연도 리스트 (예: [2025,2024,2023])
     * @param reprtCode  11011/11012/11013/11014
     */
    public List<FinancialStatement> collect(String ticker, List<Integer> years, String reprtCode) {
        String corp = corpCodes.toCorpCode(ticker);
        if (corp == null) throw new IllegalArgumentException("corp_code 매핑 실패: " + ticker);

        // 1) 요청대로 시도
        List<FinancialStatement> result = tryCollect(corp, years, reprtCode);
        if (!result.isEmpty()) return result;

        // 2) 연도 자동 탐색(같은 보고서코드)
        List<Integer> recent = recentYears();
        result = tryCollect(corp, recent, reprtCode);
        if (!result.isEmpty()) return result;

        // 3) 보고서코드 폴백 — 사업보고서가 없으면 최신 분기/반기로
        for (String rc : QUARTER_FALLBACK) {
            result = tryCollect(corp, recent, rc);
            if (!result.isEmpty()) return result;
        }
        return result; // 끝내 비면 호출부가 REPORT_NO_DATA 처리
    }

    /** 주어진 연도들을 순서대로 시도하여 데이터가 있는 것만 최대 MAX_PERIODS개 수집. */
    private List<FinancialStatement> tryCollect(String corp, List<Integer> years, String reprtCode) {
        List<FinancialStatement> out = new ArrayList<>();
        for (Integer year : years) {
            if (out.size() >= MAX_PERIODS) break;
            FinancialStatement fs = collectOne(corp, String.valueOf(year), reprtCode);
            if (fs != null) out.add(fs);
        }
        return out;
    }

    /** 단일 연도+보고서코드 수집. 데이터 없으면 null. */
    private FinancialStatement collectOne(String corp, String y, String reprtCode) {
        // 전체 재무제표 — 연결(CFS) 우선, 없으면 개별(OFS)
        JsonNode list = dart.fetchSingleAcntAll(corp, y, reprtCode, "CFS");
        if (list == null || !list.isArray() || list.isEmpty()) {
            list = dart.fetchSingleAcntAll(corp, y, reprtCode, "OFS");
        }
        if (list == null || !list.isArray() || list.isEmpty()) return null;

        JsonNode head = list.get(0);
        String actualYear  = head.path("bsns_year").asText(y);
        String actualReprt = head.path("reprt_code").asText(reprtCode);
        String respCorp    = head.path("corp_code").asText("");
        String rceptNo    = head.path("rcept_no").asText("");

        if (!respCorp.isEmpty() && !respCorp.equals(corp)) {
            log.warn("corp_code 불일치: 요청 corp={}, 응답 corp={} (ticker 매핑 오류 의심) → 폐기",
                    corp, respCorp);
            return null;
        }

        BigDecimal roe = extractRoe(dart.fetchSingleIndx(corp, actualYear, actualReprt, "M210000"));

        return FinancialStatement.builder()
                .rceptNo(rceptNo)
                .bsnsYear(actualYear).reprtCode(actualReprt)
                .revenue(pick(list, REVENUE, "매출액", "수익(매출액)"))
                .operatingIncome(pick(list, OP_INCOME, "영업이익"))
                .netIncome(pick(list, NET_INCOME, "당기순이익"))
                .assets(pick(list, ASSETS, "자산총계"))
                .liabilities(pick(list, LIAB, "부채총계"))
                .equity(pick(list, EQUITY, "자본총계"))
                .currentAssets(pick(list, CUR_ASSETS, "유동자산"))
                .currentLiabilities(pick(list, CUR_LIAB, "유동부채"))
                .cashFlowOperating(pick(list, CF_OP, "영업활동현금흐름"))
                .cashFlowInvesting(pick(list, CF_INV, "투자활동현금흐름"))
                .cashFlowFinancing(pick(list, CF_FIN, "재무활동현금흐름"))
                .roe(roe)
                // 추가 항목
                .interestExpense(pick(list, INTEREST, "이자비용", "금융비용"))
                .cash(pick(list, CASH, "현금및현금성자산"))
                .borrowings(sumByNm(list, "차입금", "사채"))
                .receivables(pick(list, RECEIVABLES, "매출채권"))
                .inventory(pick(list, INVENTORY, "재고자산"))
                .build();
    }

    /** 올해부터 과거 LOOKBACK년 (내림차순). */
    private List<Integer> recentYears() {
        int now = Year.now().getValue();
        List<Integer> ys = new ArrayList<>();
        for (int y = now; y > now - LOOKBACK; y--) ys.add(y);
        return ys;
    }

    private BigDecimal extractRoe(JsonNode indxList) {
        if (indxList == null || !indxList.isArray()) return null;
        for (JsonNode n : indxList) {
            String nm = n.path("idx_nm").asText();
            if (nm.contains("ROE") || nm.contains("자기자본이익률")) {
                String v = n.path("idx_val").asText("").replace(",", "").trim();
                if (!v.isEmpty()) try { return new BigDecimal(v); } catch (Exception ignore) {}
            }
        }
        return null;
    }
}