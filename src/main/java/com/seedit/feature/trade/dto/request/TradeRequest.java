package com.seedit.feature.trade.dto.request;

import com.seedit.feature.trade.domain.TradeType;

public record TradeRequest(
    Long sdid,
    int quantity,
    TradeType tradeType
){}

