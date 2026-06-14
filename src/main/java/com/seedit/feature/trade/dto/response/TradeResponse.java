package com.seedit.feature.trade.dto.response;

import com.seedit.feature.balance.domain.BalanceHistory;
import com.seedit.feature.balance.dto.response.BalanceHistoryResponse;
import com.seedit.feature.trade.domain.Trade;
import com.seedit.feature.trade.domain.TradeType;

import java.time.LocalDateTime;

// 거래 체결 직후 응답
public record TradeResponse (
        Long tid,
        Long sid,
        Long sdid,
        TradeType tradeType,
        Long tradePrice,
        int quantity,
        Long totalAmount,
        Long remainingBalance,
        LocalDateTime tradeAt
){

    public static TradeResponse from(Trade trade){
        return new TradeResponse(
                trade.getTid(),
                trade.getSid(),
                trade.getSdid(),
                trade.getTradeType(),
                trade.getTradePrice(),
                trade.getQuantity(),
                trade.getTotalAmount(),
                trade.getRemainingBalance(),
                trade.getTradeAt()
        );
    }
}