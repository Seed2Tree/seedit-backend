package com.seedit.feature.trade.dto.response;

import com.seedit.feature.trade.domain.TradeType;

import java.time.LocalDateTime;

// 과거 거래 이력 조회
public record TradeHistoryResponse (
        Long tid,
        Long sid,
        String companyName,
        String ticker,
        String sector,
        String market,
        TradeType tradeType,
        Long tradePrice,
        int quantity,
        Long totalAmount,
        Long remainingBalance,
        LocalDateTime tradeAt,
        String reasonTag,
        String reasonText
){
}