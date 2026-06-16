package com.seedit.feature.portfolio.dto.response;

public record PortfolioResponse (
    Long sid,
    int quantity,
    Long avgPrice,
    Long evalProfit,
    Long currentPrice,
    double profitRate
){}