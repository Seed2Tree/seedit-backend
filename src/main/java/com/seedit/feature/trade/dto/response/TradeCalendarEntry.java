package com.seedit.feature.trade.dto.response;

import java.time.LocalDate;

public record TradeCalendarEntry(
        LocalDate tradeDate,
        boolean hasBuy,
        boolean hasSell
) {}
