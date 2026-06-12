package com.seedit.feature.stock.dto;

import com.seedit.feature.stock.domain.StockDetail;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class StockDetailResponse {
    private final Long sid;
    private final String companyName;
    private final String ticker;
    private final String description;
    private final String sector;
    private final String market;
    private final Long marketCap;
    private final BigDecimal foreignOwnershipPct;
    private final BigDecimal per;
    private final Integer eps;
    private final BigDecimal pbr;
    private final Integer bps;
    private final Long currentPrice;
    private final Long openPrice;
    private final Long highPrice;
    private final Long lowPrice;
    private final Long prevClosePrice;
    private final Long volume;
    private final Long tradingValue;
    private final Long w52HighPrice;
    private final Long w52LowPrice;
    private final LocalDate tradeDate;
    private final Double changeRate;
    private final Long changePrice;

    public StockDetailResponse(StockDetail s) {
        this.sid = s.getSid();
        this.companyName = s.getCompanyName();
        this.ticker = s.getTicker();
        this.description = s.getDescription();
        this.sector = s.getSector();
        this.market = s.getMarket();
        this.marketCap = s.getMarketCap();
        this.foreignOwnershipPct = s.getForeignOwnershipPct();
        this.per = s.getPer();
        this.eps = s.getEps();
        this.pbr = s.getPbr();
        this.bps = s.getBps();
        this.currentPrice = s.getCurrentPrice();
        this.openPrice = s.getOpenPrice();
        this.highPrice = s.getHighPrice();
        this.lowPrice = s.getLowPrice();
        this.prevClosePrice = s.getPrevClosePrice();
        this.volume = s.getVolume();
        this.tradingValue = s.getTradingValue();
        this.w52HighPrice = s.getW52HighPrice();
        this.w52LowPrice = s.getW52LowPrice();
        this.tradeDate = s.getTradeDate();
        this.changeRate = s.getChangeRate();
        this.changePrice = s.getChangePrice();
    }
}
