package com.seedit.feature.trade.dto.response;

import com.seedit.feature.reason.domain.Reason;
import com.seedit.feature.trade.domain.Trade;
import com.seedit.feature.trade.domain.TradeType;

public record BuyResponse(
        // 매수 결과 DTO
        Long sdid,
        int quantity,
        TradeType tradeType,
        String reasonTag,
        String reasonText
){
    public static BuyResponse from(Trade trade, Reason reason){
        return new BuyResponse(trade.getSdid(),
                trade.getQuantity(),
                TradeType.BUY,
                reason.getReasonTag(),
                reason.getReasonText());
    }
}
