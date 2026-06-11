package com.seedit.feature.trade.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Transaction {
    private Long tid;
    private Long userId;
    private Long sid;
    private TradeType tradeType;
    private Long price;
    private int quantity;
    private Long totalAmount;
    private LocalDateTime tradeAt;
}
