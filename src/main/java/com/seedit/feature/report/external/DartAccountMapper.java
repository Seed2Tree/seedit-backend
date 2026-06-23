package com.seedit.feature.report.external;


import tools.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.Set;

/**
 * fnlttSinglAcntAll list 에서 표준계정ID(account_id) 우선으로 금액을 뽑는다.
 * account_id 가 '-표준계정코드 없음-' 인 항목은 account_nm 폴백.
 * thstrm_amount = 당기 금액.
 */
public final class DartAccountMapper {

    private DartAccountMapper() {}

    // IFRS 표준 account_id (대표값; 회사별 누락 대비 후보 복수)
    public static final Set<String> REVENUE   = Set.of("ifrs-full_Revenue", "ifrs_Revenue");
    public static final Set<String> OP_INCOME = Set.of("dart_OperatingIncomeLoss",
            "ifrs-full_ProfitLossFromOperatingActivities");
    public static final Set<String> NET_INCOME= Set.of("ifrs-full_ProfitLoss");
    public static final Set<String> ASSETS    = Set.of("ifrs-full_Assets");
    public static final Set<String> LIAB      = Set.of("ifrs-full_Liabilities");
    public static final Set<String> EQUITY    = Set.of("ifrs-full_Equity");
    public static final Set<String> CUR_ASSETS= Set.of("ifrs-full_CurrentAssets");
    public static final Set<String> CUR_LIAB  = Set.of("ifrs-full_CurrentLiabilities");
    // 현금흐름 (영업/투자/재무)
    public static final Set<String> CF_OP = Set.of("ifrs-full_CashFlowsFromUsedInOperatingActivities");
    public static final Set<String> CF_INV= Set.of("ifrs-full_CashFlowsFromUsedInInvestingActivities");
    public static final Set<String> CF_FIN= Set.of("ifrs-full_CashFlowsFromUsedInFinancingActivities");

    public static final Set<String> INTEREST    = Set.of("ifrs-full_InterestExpense",
            "ifrs-full_FinanceCosts");                                  // 이자비용/금융비용
    public static final Set<String> CASH        = Set.of("ifrs-full_CashAndCashEquivalents"); // 현금및현금성자산
    public static final Set<String> RECEIVABLES = Set.of("ifrs-full_TradeAndOtherCurrentReceivables",
            "ifrs-full_CurrentTradeReceivables");                       // 매출채권
    public static final Set<String> INVENTORY   = Set.of("ifrs-full_Inventories");            // 재고자산

    /** account_id 우선 매칭, 실패 시 account_nm 부분일치 폴백. 당기금액 반환. */
    public static BigDecimal pick(JsonNode list, Set<String> ids, String... nmFallback) {
        if (list == null || !list.isArray()) return null;
        for (JsonNode n : list) {
            if (ids.contains(n.path("account_id").asText())) {
                return amount(n);
            }
        }
        for (JsonNode n : list) {                       // 폴백: 계정명 부분일치
            String nm = n.path("account_nm").asText().replaceAll("\\s", "");
            for (String f : nmFallback) {
                if (nm.contains(f.replaceAll("\\s", ""))) return amount(n);
            }
        }
        return null;
    }

    /**
     * 계정명에 키워드가 포함되는 모든 항목의 당기금액을 합산.
     * 차입금처럼 단기/장기/사채로 흩어진 계정을 하나로 합칠 때 사용.
     * 하나도 못 찾으면 null.
     */
    public static BigDecimal sumByNm(JsonNode list, String... nmKeywords) {
        if (list == null || !list.isArray()) return null;
        BigDecimal sum = null;
        for (JsonNode n : list) {
            String nm = n.path("account_nm").asText().replaceAll("\\s", "");
            for (String k : nmKeywords) {
                if (nm.contains(k.replaceAll("\\s", ""))) {
                    BigDecimal a = amount(n);
                    if (a != null) sum = (sum == null ? BigDecimal.ZERO : sum).add(a);
                    break;  // 같은 항목 중복 합산 방지
                }
            }
        }
        return sum;
    }

    private static BigDecimal amount(JsonNode n) {
        String raw = n.path("thstrm_amount").asText("").replace(",", "").trim();
        if (raw.isEmpty()) return null;
        try { return new BigDecimal(raw); } catch (NumberFormatException e) { return null; }
    }
}