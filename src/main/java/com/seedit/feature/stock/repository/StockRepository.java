package com.seedit.feature.stock.repository;

import com.seedit.feature.stock.domain.Stock;
import com.seedit.feature.stock.domain.StockCandle;
import com.seedit.feature.stock.domain.StockDetail;
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
}
