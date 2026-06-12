package com.seedit.feature.balance.domain;

import com.seedit.feature.trade.domain.TradeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceHistory {
    private Long bhId;
    private Long userId;
    private Long amount;
    private Long currentBalance;
    private TradeType reasonType;
    private LocalDateTime createdAt;
}
