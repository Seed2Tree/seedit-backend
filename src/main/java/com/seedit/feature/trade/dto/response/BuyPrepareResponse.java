package com.seedit.feature.trade.dto.response;

import com.seedit.feature.stock.domain.StockDetail;

public record BuyPrepareResponse(
        // 매수 화면 DTO
        Long balance,
        StockSummary stock
){
    public record StockSummary(
            String companyName,
            String ticker,
            String sector,
            String marker,
            Long currentPrice
    ){}

    public static BuyPrepareResponse from(Long balance, StockDetail stock){
        StockSummary stockSummary = new StockSummary(
                stock.getCompanyName(),
                stock.getTicker(),
                stock.getSector(),
                stock.getMarket(),
                stock.getCurrentPrice()
        );

        return new BuyPrepareResponse(
                balance,
                stockSummary
        );
    }
}
