package com.seedit.feature.trade.dto.request;

import com.seedit.feature.trade.domain.TradeType;

public record TradeRequest(
    String ticker,
    int quantity,
    TradeType tradeType,
    String reasonTag,
    String reasonText
){}

