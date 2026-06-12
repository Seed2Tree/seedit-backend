package com.seedit.feature.stock.external;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 외부 시세 API에서 받아온 "오늘의 시세 + 펀더멘털" 1건 (증권사 중립 내부 표현).
 * 어느 증권사 API를 쓰든 이 형태로 변환해서 DB에 저장한다.
 *
 * @Builder: 필드가 많아 생성자 대신 빌더로 생성
 *   DailyStockPrice.builder().ticker("005930").currentPrice(71300L)...build()
 */
@Getter
@Builder
public class DailyStockPrice {

    private Long sid;              // 저장 직전에 채움 (DB 종목 ID)
    private String ticker;

    private LocalDate tradeDate;
    private Long openPrice;
    private Long currentPrice;
    private Long closePrice;       // 장 마감 후에만 값 존재, 장중엔 null
    private Long highPrice;
    private Long lowPrice;
    private Long prevClosePrice;
    private Long volume;
    private Long tradingValue;     // 원 단위
    private Long w52HighPrice;
    private Long w52LowPrice;

    // 펀더멘털 (응답에 없으면 null → 기존 값 유지)
    private Long marketCap;        // 억원 단위
    private BigDecimal foreignOwnershipPct;
    private BigDecimal per;
    private Integer eps;
    private BigDecimal pbr;
    private Integer bps;

    /** 장 마감 후 호출 시 종가를 확정한다. */
    public DailyStockPrice withClosePrice() {
        return DailyStockPrice.builder()
                .sid(sid).ticker(ticker).tradeDate(tradeDate)
                .openPrice(openPrice).currentPrice(currentPrice)
                .closePrice(currentPrice)   // 마감 후엔 현재가 = 종가
                .highPrice(highPrice).lowPrice(lowPrice)
                .prevClosePrice(prevClosePrice)
                .volume(volume).tradingValue(tradingValue)
                .w52HighPrice(w52HighPrice).w52LowPrice(w52LowPrice)
                .marketCap(marketCap).foreignOwnershipPct(foreignOwnershipPct)
                .per(per).eps(eps).pbr(pbr).bps(bps)
                .build();
    }

    /** 저장 직전 종목 ID를 채운 사본을 만든다. */
    public DailyStockPrice withSid(Long newSid) {
        return DailyStockPrice.builder()
                .sid(newSid).ticker(ticker).tradeDate(tradeDate)
                .openPrice(openPrice).currentPrice(currentPrice)
                .closePrice(closePrice)
                .highPrice(highPrice).lowPrice(lowPrice)
                .prevClosePrice(prevClosePrice)
                .volume(volume).tradingValue(tradingValue)
                .w52HighPrice(w52HighPrice).w52LowPrice(w52LowPrice)
                .marketCap(marketCap).foreignOwnershipPct(foreignOwnershipPct)
                .per(per).eps(eps).pbr(pbr).bps(bps)
                .build();
    }
}
