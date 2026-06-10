package com.seedit.feature.stock.controller;

import com.seedit.feature.stock.service.StockService;
import com.seedit.global.response.ApiResponse;
import com.seedit.feature.stock.dto.StockResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
