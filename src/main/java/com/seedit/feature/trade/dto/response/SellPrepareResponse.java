package com.seedit.feature.trade.dto.response;

import com.seedit.feature.portfolio.domain.Portfolio;
import com.seedit.feature.reason.domain.Reason;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record SellPrepareResponse(
        // 매도 화면 DTO
        Long sid,
        int currentQuantity,
        Long avgPrice,
        Long currentPrice,
        List<String> reasonTags,
        List<String> reasonTexts,
        LocalDateTime updatedAt
) {
    public static SellPrepareResponse from(Portfolio portfolio,List<Reason> reasons,Long currentPrice){
        List<String> tags = reasons.stream().distinct().map(Reason::getReasonTag).filter(Objects::nonNull).toList();
        List<String> texts = reasons.stream().distinct().map(Reason::getReasonText).filter(Objects::nonNull).toList();
        return new SellPrepareResponse(
                portfolio.getSid(),
                portfolio.getQuantity(),
                portfolio.getAvgPrice(),
                currentPrice,
                tags,
                texts,
                portfolio.getUpdatedAt());
    }
}
