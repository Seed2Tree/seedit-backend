package com.seedit.feature.trade.dto.response;

import java.util.List;

public record TradeDetailResponse(
        TradeHistoryResponse trade,
        List<VerifiedReasonResponse> verifiedReasons
){}