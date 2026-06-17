package com.seedit.feature.portfolio.dto.response;

import java.util.List;

public record PortfolioSummaryResponse (
    Long totalCost, // 총 매입금 avgPrice * quantity
    Long totalEval, // 총 평가금액 currentPrice * quantity
    Long totalProfit, // 평가 손익 totalEval - totalCost
    double totalProfitRate, // 총 수익률
    Long balance, // 예수금(현금)
    List<PortfolioResponse> holdings
){

}
