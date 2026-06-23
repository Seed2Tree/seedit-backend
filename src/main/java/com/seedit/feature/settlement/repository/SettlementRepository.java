package com.seedit.feature.settlement.repository;

import com.seedit.feature.settlement.domain.Settlement;
import com.seedit.feature.settlement.domain.SettlementStatus;
import com.seedit.feature.settlement.dto.response.SettlementResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SettlementRepository {
    void save(Settlement settlement);
    List<Settlement> findPendingDue(LocalDate today); // status=PENDING AND settle_date<=today
    void markSettled(@Param("settlementId") Long settlementId, @Param("now") LocalDateTime now);
    long sumPendingByUser(@Param("userId") Long userId);
    List<SettlementResponse> findByUserId(@Param("userId") Long userId,
                                          @Param("status") SettlementStatus status); // status null이면 전체
}
