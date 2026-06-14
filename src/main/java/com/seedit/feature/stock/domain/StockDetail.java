package com.seedit.feature.stock.domain;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class StockDetail {
    private Long sid;
    private Long sdid;
    private String companyName;
    private String ticker;
    private String description;
    private String sector;
    private String market;
    private Long marketCap;
    private BigDecimal foreignOwnershipPct;
    private BigDecimal per;
    private Integer eps;
    private BigDecimal pbr;
    private Integer bps;
    private Long currentPrice;
    private Long openPrice;
    private Long highPrice;
    private Long lowPrice;
    private Long prevClosePrice;
    private Long volume;
    private Long tradingValue;
    private Long w52HighPrice;
    private Long w52LowPrice;
    private LocalDate tradeDate;
    private Double changeRate;
    private Long changePrice;
}
