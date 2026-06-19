package com.seedit.feature.trade.dto.response;

import com.seedit.feature.reason.domain.Reason;
import com.seedit.feature.trade.domain.Trade;
import com.seedit.feature.trade.domain.TradeType;

public record SellResponse(
        Long sdid,
        int quantity,
        TradeType tradeType,
        String reasonTag,
        String reasonText
) {
    public static SellResponse from(Trade trade, Reason reason){
        return new SellResponse(
                trade.getSdid(),
                trade.getQuantity(),
                TradeType.SELL,
                reason.getReasonTag(),
                reason.getReasonText());
    }
}
