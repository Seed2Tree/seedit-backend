package com.seedit.feature.trade.domain;

import com.seedit.feature.trade.dto.request.TradeRequest;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@Builder
public class Trade {
    private Long tid;
    private Long userId;
    private Long sid;
    private Long sdid;
    private TradeType tradeType;
    private Long tradePrice;
    private int quantity;
    private Long totalAmount;
    private Long remainingBalance;
    private LocalDateTime tradeAt;

}
