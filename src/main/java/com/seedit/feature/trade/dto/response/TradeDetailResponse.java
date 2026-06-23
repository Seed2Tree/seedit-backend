package com.seedit.feature.trade.dto.response;

import com.seedit.feature.settlement.domain.SettlementStatus;
import com.seedit.feature.trade.domain.TradeType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TradeDetailResponse(
        Long tid, String companyName, String ticker,
        TradeType tradeType, Long tradePrice, int quantity, Long totalAmount,
        Long remainingBalance, LocalDateTime tradeAt,
        String reasonTag, String reasonText,            // 투자 가설
        SettlementStatus settlementStatus, LocalDate settleDate  // 매도면 정산 상태
){}