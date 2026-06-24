package com.seedit.feature.trade.repository;


import com.seedit.feature.trade.domain.Trade;
import com.seedit.feature.trade.dto.response.TradeCalendarEntry;
import com.seedit.feature.trade.dto.response.TradeHistoryResponse;
import com.seedit.feature.trade.dto.response.TradeResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface TradeRepository {

    int save(Trade trade); // 매수, 매도 거래 저장

    List<TradeHistoryResponse> findAllByUserId(@Param("userId") Long userId, @Param("month") int month); // 사용자별 거래 내역 전체 조회

    List<TradeResponse> findAllByUserIdAndStockId(@Param("userId") Long userId, @Param("sid") Long sid); // 기업별 거래 내역 전체 조회

    Optional<TradeHistoryResponse> findByIdAndUserId(@Param("userId") Long userId, @Param("tid") Long tid); // 거래 내역 단건 조회

    int countByUserId(@Param("userId") Long userId); // INIT 트랜잭션 확인 및 거래 횟수 조회

    int deleteByUserId(@Param("userId") Long userId);

    int findTotalQuantityByStockId(@Param("userId") Long userId, @Param("sid") Long sid);

    List<TradeHistoryResponse> findAllByUserIdAndDate(@Param("userId") Long userId,
                                                      @Param("date") LocalDate date);

    List<TradeCalendarEntry> findCalendarByUserIdAndMonth(@Param("userId") Long userId,
                                                          @Param("year") int year,
                                                          @Param("month") int month);
}
