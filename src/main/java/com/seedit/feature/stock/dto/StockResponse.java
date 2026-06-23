package com.seedit.feature.stock.dto;

import com.seedit.feature.stock.domain.Stock;
import lombok.Getter;

@Getter
public class StockResponse {
    private final Long sid;
    private final String companyName;
    private final String ticker;
    private final String sector;
    private final String searchKeywords;
    private final Long currentPrice;
    private final Double changeRate;
    private final Long changePrice;

    public StockResponse(Stock s) {
        this.sid = s.getSid();
        this.companyName = s.getCompanyName();
        this.ticker = s.getTicker();
        this.sector = s.getSector();
        this.searchKeywords = s.getSearchKeywords();
        this.currentPrice = s.getCurrentPrice();
        this.changeRate = s.getChangeRate();
        this.changePrice = s.getChangePrice();
    }
}
