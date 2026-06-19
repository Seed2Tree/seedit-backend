package com.seedit.feature.trade.dto.response;

import com.seedit.feature.portfolio.domain.Portfolio;
import com.seedit.feature.reason.domain.Reason;
import com.seedit.feature.stock.domain.StockDetail;
import com.seedit.feature.trade.domain.TradeType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record SellPrepareResponse(
        // 매도 화면 DTO
        PortfolioResponse portfolio,
        List<ReasonPrepareResponse> reasons,
        StockSummary stock
) {

    public record PortfolioResponse (
            Long sid,
            int quantity,
            Long avgPrice,
            Long totalAmount,
            LocalDateTime updateAt
    ){}

    public record ReasonPrepareResponse(
        TradeType reasonType, LocalDate reasonDate,
        String reasonTag, String reasonText
    ){}

    public record StockSummary(
        String companyName,
        String ticker,
        String sector,
        String market,
        Long currentPrice
    ){}

    public static SellPrepareResponse from(Portfolio portfolio, List<Reason> reasons, StockDetail stockDetail){
        PortfolioResponse portfolioResponse = new PortfolioResponse(
                portfolio.getSid(),
                portfolio.getQuantity(),
                portfolio.getAvgPrice(),
                portfolio.getTotalAmount(),
                portfolio.getUpdatedAt());
        List<ReasonPrepareResponse> reasonPrepareResponses = reasons.stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                .map(r -> new ReasonPrepareResponse(
                        r.getReasonType(),
                        r.getReasonDate(),
                        r.getReasonTag(),
                        r.getReasonText()))
                .toList();

        StockSummary stockSummary = new StockSummary(
                stockDetail.getCompanyName(),
                stockDetail.getTicker(),
                stockDetail.getSector(),
                stockDetail.getMarket(),
                stockDetail.getCurrentPrice()
        );

        return new SellPrepareResponse(
                portfolioResponse,
                reasonPrepareResponses,
                stockSummary);
    }
}