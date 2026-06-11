package com.seedit.feature.stock.domain;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class StockCandle {
    private LocalDate date;
    private Long open;
    private Long close;
    private Long high;
    private Long low;
}
