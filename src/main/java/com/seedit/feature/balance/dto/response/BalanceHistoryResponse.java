package com.seedit.feature.balance.dto.response;

import com.seedit.feature.balance.domain.BalanceHistory;
import com.seedit.feature.trade.domain.TradeType;
import java.time.LocalDateTime;


/**
 * 잔액 조회 응답
 */
public record BalanceHistoryResponse (
        Long bhId,
        Long amount,
        Long currentBalance,
        TradeType reasonType,
        LocalDateTime createdAt,
        TradeDetail tradeDetail
){
    public record TradeDetail(
            String stockName,
            Long price,
            int quantity
    ) {}

    public static BalanceHistoryResponse from(BalanceHistory history, TradeDetail tradeDetail) {
        return new BalanceHistoryResponse(
                history.getBhId(),
                history.getAmount(),
                history.getCurrentBalance(),
                history.getReasonType(),
                history.getCreatedAt(),
                tradeDetail
        );
    }

    public static BalanceHistoryResponse from(BalanceHistory history) {
        return from(history, null);
    }
}