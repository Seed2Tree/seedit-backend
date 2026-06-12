package com.seedit.feature.stock.external;

/**
 * 시세 데이터 공급자 추상화.
 * 키움 → 한국투자(KIS) 등으로 증권사를 교체해도
 * 스케줄러/저장 로직은 이 인터페이스만 바라보므로 영향이 없다.
 */
public interface StockPriceProvider {

    /** 종목 코드(ticker)로 오늘의 시세 + 펀더멘털을 조회한다. */
    DailyStockPrice fetchDailyPrice(String ticker);

    /** 과거 일봉을 최신순으로 최대 maxCount건 조회한다. (백필용) */
    java.util.List<DailyCandle> fetchDailyHistory(String ticker, int maxCount);
}
