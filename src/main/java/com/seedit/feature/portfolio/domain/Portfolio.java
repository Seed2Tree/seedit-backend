package com.seedit.feature.portfolio.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @Builder
public class Portfolio {
    private Long pid;
    private Long userId;
    private Long sid;
    private int quantity;
    private Long avgPrice;
    private Long totalAmount;
    private LocalDateTime updatedAt;
}
