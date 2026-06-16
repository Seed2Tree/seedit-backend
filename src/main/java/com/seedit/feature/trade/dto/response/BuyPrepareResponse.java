package com.seedit.feature.trade.dto.response;

import com.seedit.feature.stock.domain.Stock;
import com.seedit.feature.stock.domain.StockDetail;

public record BuyPrepareResponse(
        // 매수 화면 DTO
        Long sdid,
        String companyName,
        String ticker,
//        String market,
        Long balance,
        Long currentPrice
){
    public static BuyPrepareResponse from(Long sdid, Long balance, Stock stock){
        return new BuyPrepareResponse(sdid,
                stock.getCompanyName(),
                stock.getTicker(),
                balance,
                stock.getCurrentPrice()
                );
    }
}
