package com.seedit.feature.stock.dto;

import lombok.Getter;

import java.util.List;

/** 시세 동기화 실행 결과 요약 */
@Getter
public class StockSyncResponse {
    private final int total;
    private final int success;
    private final int failed;
    private final List<String> failedTickers;

    public StockSyncResponse(int total, int success, List<String> failedTickers) {
        this.total = total;
        this.success = success;
        this.failed = failedTickers.size();
        this.failedTickers = failedTickers;
    }
}
