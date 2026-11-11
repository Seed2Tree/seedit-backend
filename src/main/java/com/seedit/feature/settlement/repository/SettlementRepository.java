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
    List<Settlement> findDuePage(@Param("today") LocalDate today, @Param("now") LocalDateTime now,
                                @Param("afterId") long afterId, @Param("limit") int limit,
                                @Param("retryOnly") boolean retryOnly);
    Settlement findByIdForUpdate(@Param("settlementId") Long settlementId);
    int claim(@Param("settlementId") Long settlementId, @Param("token") String token,
              @Param("leaseUntil") LocalDateTime leaseUntil);
    int markSettled(@Param("settlementId") Long settlementId, @Param("token") String token,
                    @Param("now") LocalDateTime now);
    int recordFailure(@Param("settlementId") Long settlementId, @Param("token") String token,
                      @Param("status") SettlementStatus status, @Param("nextRetryAt") LocalDateTime nextRetryAt,
                      @Param("errorCode") String errorCode, @Param("now") LocalDateTime now);
    long sumPendingByUser(@Param("userId") Long userId);
    List<SettlementResponse> findByUserId(@Param("userId") Long userId,
                                          @Param("status") SettlementStatus status); // status null이면 전체
    int deleteByUserId(@Param("userId") Long userId);
}
