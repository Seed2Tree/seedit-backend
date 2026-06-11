package com.seedit.feature.stock.controller;

import com.seedit.feature.stock.service.StockService;
import com.seedit.global.response.ApiResponse;
import com.seedit.feature.stock.dto.StockCandleResponse;
import com.seedit.feature.stock.dto.StockDetailResponse;
import com.seedit.feature.stock.dto.StockResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /** F01: 종목 전체 목록 조회 */
    @GetMapping
    public ApiResponse<List<StockResponse>> getStocks() {
        return ApiResponse.ok(stockService.getStocks());
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
