package com.seedit.feature.reason.domain;

import com.seedit.feature.trade.domain.TradeType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @Builder
public class Reason {
    private Long rid;
    private Long userId;
    private Long tid;
    private TradeType reasonType;
    private LocalDate reasonDate;
    private String reasonTag;
    private String reasonText;
    private Boolean isVerified;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
}
