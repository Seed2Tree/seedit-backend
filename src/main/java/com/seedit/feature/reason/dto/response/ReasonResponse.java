package com.seedit.feature.reason.dto.response;

import com.seedit.feature.trade.domain.TradeType;

public record ReasonResponse (
        Long rid,
        Long tid,
        TradeType reasonType,
        String reasonTag,
        String reasonText,
        boolean isVerified,
        boolean isDeleted,
        Long sid,
        Long tradePrice,
        int quantity,
        Long totalAmount
){}