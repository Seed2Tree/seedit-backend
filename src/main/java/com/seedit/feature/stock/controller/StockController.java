package com.seedit.feature.stock.controller;

import com.seedit.feature.stock.service.StockPriceSyncService;
import com.seedit.feature.stock.service.StockService;
import com.seedit.global.response.ApiResponse;
import com.seedit.feature.stock.dto.StockCandleResponse;
import com.seedit.feature.stock.dto.StockDetailResponse;
import com.seedit.feature.stock.dto.StockResponse;
import com.seedit.feature.stock.dto.StockSyncResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.time.ZoneId;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final StockPriceSyncService syncService;

    /** F01: 종목 전체 목록 조회 */
    @GetMapping
    public ApiResponse<List<StockResponse>> getStocks() {
        return ApiResponse.ok(stockService.getStocks());
    }

    /** F01-4: 시세 수동 동기화 (개발/시연용, SEED-72)
     *  장 마감(15:30) 이후 호출이면 종가 확정으로 저장 */
    @PostMapping("/sync")
    public ApiResponse<StockSyncResponse> syncPrices() {
        boolean marketClosed = LocalTime.now(ZoneId.of("Asia/Seoul"))
                .isAfter(LocalTime.of(15, 30));
        return ApiResponse.ok(syncService.syncAll(marketClosed));
    }

    /** F01-4: 과거 일봉 백필 (1회성, SEED-72)
     *  기존 mock 히스토리를 지우고 실제 과거 시세로 교체한 뒤, 오늘 시세도 동기화 */
    @PostMapping("/backfill")
    public ApiResponse<StockSyncResponse> backfillPrices(
            @RequestParam(defaultValue = "1300") int days) {
        StockSyncResponse result = syncService.backfillAll(days);
        // 백필은 어제까지만 채우므로 오늘 행은 sync로 보충
        boolean marketClosed = LocalTime.now(ZoneId.of("Asia/Seoul"))
                .isAfter(LocalTime.of(15, 30));
        syncService.syncAll(marketClosed);
        return ApiResponse.ok(result);
    }

    /** F01-2: 종목 상세 조회 (SEED-70) */
    @GetMapping("/{ticker}")
    public ApiResponse<StockDetailResponse> getStockDetail(@PathVariable String ticker) {
        return ApiResponse.ok(stockService.getStockDetail(ticker));
    }

    /** F01-3: 종목 기간별 시세(캔들) 조회 (SEED-71) */
    @GetMapping("/{ticker}/prices")
    public ApiResponse<List<StockCandleResponse>> getStockPrices(
            @PathVariable String ticker,
            @RequestParam(defaultValue = "1w") String period) {
        return ApiResponse.ok(stockService.getStockPrices(ticker, period));
    }
}
