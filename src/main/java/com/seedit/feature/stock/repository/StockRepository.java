package com.seedit.feature.stock.repository;

import com.seedit.feature.stock.domain.Stock;
import com.seedit.feature.stock.domain.StockCandle;
import com.seedit.feature.stock.domain.StockDetail;
import com.seedit.feature.stock.external.DailyCandle;
import com.seedit.feature.stock.external.DailyStockPrice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StockRepository {
    List<Stock> findAll();

    StockDetail findDetailByTicker(@Param("ticker") String ticker);

    /**
     * 기간별 캔들(시가/고가/저가/종가) 조회.
     * groupFormat: DATE_FORMAT 패턴으로 집계 단위 결정
     *   '%Y%m%d' = 일봉, '%x%v' = 주봉(ISO 주), '%Y%m' = 월봉
     */
    List<StockCandle> findCandlesByTicker(@Param("ticker") String ticker,
                                          @Param("limit") int limit,
                                          @Param("groupFormat") String groupFormat);

    /** 오늘 시세 저장. (sid, trade_date) 중복 시 UPDATE (uq_sid_trade_date) */
    void upsertDailyPrice(DailyStockPrice price);

    /** 종목 펀더멘털(시총/PER/EPS/PBR/BPS) 갱신. null 필드는 기존 값 유지 */
    void updateFundamentals(DailyStockPrice price);

    /** 백필용: 한 종목의 시세 히스토리 전체 삭제 */
    void deleteHistoryBySid(@Param("sid") Long sid);

    /** 백필용: 과거 일봉 일괄 INSERT */
    void insertHistoryBatch(@Param("sid") Long sid, @Param("candles") List<DailyCandle> candles);
}
