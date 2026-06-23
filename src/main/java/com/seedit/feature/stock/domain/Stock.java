package com.seedit.feature.stock.domain;

import lombok.Getter;

@Getter
public class Stock {
    private Long sid;
    private String companyName;
    private String ticker;
    private String sector;
    private String searchKeywords;
    private Long volume;
    private Long currentPrice;
    private Double changeRate;
    private Long changePrice;
}
