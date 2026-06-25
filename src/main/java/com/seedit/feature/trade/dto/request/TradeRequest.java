package com.seedit.feature.trade.dto.request;

import com.seedit.feature.trade.domain.TradeType;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record TradeRequest(
    String ticker,
    int quantity,
    TradeType tradeType,
    @NotBlank(message="최소 1개 이상의 태그를 선택해주세요.")
    String reasonTag,
    String reasonText,
    List<Long> verifiedReasonIds
){}

