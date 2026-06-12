package com.seedit.feature.stock.dto;

import com.seedit.feature.stock.domain.StockCandle;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class StockCandleResponse {
    private final LocalDate date;
    private final Long open;
    private final Long close;
    private final Long high;
    private final Long low;

    public StockCandleResponse(StockCandle c) {
        this.date = c.getDate();
        this.open = c.getOpen();
        this.close = c.getClose();
        this.high = c.getHigh();
        this.low = c.getLow();
    }
}
