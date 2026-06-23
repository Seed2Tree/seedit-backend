package com.seedit.feature.settlement.dto.response;

import com.seedit.feature.settlement.domain.SettlementStatus;

import java.time.LocalDate;

public record SettlementResponse(
        Long settlementId,
        String ticker,
        String companyName,
        Long amount,
        LocalDate tradeDate,
        LocalDate settleDate,
        SettlementStatus status
){}