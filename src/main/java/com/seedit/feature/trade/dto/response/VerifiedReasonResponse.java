package com.seedit.feature.trade.dto.response;

import java.time.LocalDate;

public record VerifiedReasonResponse(
        Long rid,
        String reasonTag,
        String reasonText,
        LocalDate reasonDate
){}