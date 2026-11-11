package com.seedit.feature.settlement.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Settlement {
    private Long settlementId;
    private Long userId;
    private Long tid;
    private Long amount;
    private LocalDate tradeDate;
    private LocalDate settleDate;
    private SettlementStatus status;
    private LocalDateTime settledAt;
    private LocalDateTime createdAt;
    private int attemptCount;
    private LocalDateTime nextRetryAt;
    private String attemptToken;
    private LocalDateTime leaseUntil;
    private String lastErrorCode;
    private LocalDateTime lastFailedAt;
}
