package com.seedit.feature.portfolio.dto.response;

import com.seedit.feature.balance.domain.BalanceHistory;
import com.seedit.feature.portfolio.domain.Portfolio;

public record PortfolioResponse (
    Long sid,
    String companyName,
    String ticker,
    int quantity,
    Long avgPrice,
    Long evalProfit,
    Long currentPrice,
    double profitRate
){
}