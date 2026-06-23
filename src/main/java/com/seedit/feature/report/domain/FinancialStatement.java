package com.seedit.feature.report.domain;

import lombok.Builder;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Builder
public record FinancialStatement(
        String bsnsYear, String reprtCode, String rceptNo,
        BigDecimal revenue, BigDecimal operatingIncome, BigDecimal netIncome,
        BigDecimal assets, BigDecimal liabilities, BigDecimal equity,
        BigDecimal currentAssets, BigDecimal currentLiabilities,
        BigDecimal cashFlowOperating, BigDecimal cashFlowInvesting, BigDecimal cashFlowFinancing,
        BigDecimal roe,
        BigDecimal interestExpense,    // 이자비용/금융비용 → 이자보상배율
        BigDecimal cash,               // 현금및현금성자산
        BigDecimal borrowings,         // 차입금+사채 합산
        BigDecimal receivables,        // 매출채권
        BigDecimal inventory           // 재고자산
) {
    public BigDecimal operatingMargin() {           // 영업이익률 = 영업이익/매출
        if (revenue == null || operatingIncome == null || revenue.signum() == 0) return null;
        return operatingIncome.divide(revenue, 4, RoundingMode.HALF_UP);
    }
    public BigDecimal debtRatio() {                 // 부채비율 = 부채/자본
        if (liabilities == null || equity == null || equity.signum() == 0) return null;
        return liabilities.divide(equity, 4, RoundingMode.HALF_UP);
    }
    public BigDecimal currentRatio() {              // 유동비율 = 유동자산/유동부채
        if (currentAssets == null || currentLiabilities == null
                || currentLiabilities.signum() == 0) return null;
        return currentAssets.divide(currentLiabilities, 4, RoundingMode.HALF_UP);
    }
    public BigDecimal interestCoverage() {          // 이자보상배율 = 영업이익/이자비용 (배)
        if (operatingIncome == null || interestExpense == null
                || interestExpense.signum() == 0) return null;
        return operatingIncome.divide(interestExpense, 1, RoundingMode.HALF_UP);
    }
}