package com.seedit.feature.trade.controller;

import com.seedit.feature.trade.domain.Trade;
import com.seedit.feature.trade.dto.request.TradeRequest;
import com.seedit.feature.trade.dto.response.*;
import com.seedit.feature.trade.service.TradeService;
import com.seedit.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
@Tag(name="모의 주식 거래 API", description = "주문 체결(매수/매도) 및 전체/거래별/주식별 주문 이력 조회")
public class TradeController {

    private final TradeService tradeService;

    /**
     * 주문 체결 (매수)
     * 매수 시 주문 체결 transaction 진행
     * @param
     * @param authentication
     * @return
     */
    @PostMapping("/buy")
    public ApiResponse<BuyResponse> orderStock(
            @Valid @RequestBody TradeRequest request,
            Authentication authentication
            ){
        BuyResponse response = tradeService.processOrder(authentication.getName(), request);
        return ApiResponse.ok(response);
    }

    /**
     * 주문 체결 (매도)
     * 매도 시 주문 체결 transaction 진행
     * @param
     * @param authentication
     * @return
     */
    @PostMapping("/sell")
    public ApiResponse<SellResponse> sellStock(
            @Valid @RequestBody TradeRequest request,
            Authentication authentication
    ){
        SellResponse response = tradeService.processSell(authentication.getName(), request);
        return ApiResponse.ok(response);
    }

    /**
     * 매수 시 주식 종목 데이터 조회
     * @param authentication
     * @return
     */
    @GetMapping("/buy/{ticker}")
    public ApiResponse<BuyPrepareResponse> getBuystock(
            Authentication authentication,
            @PathVariable("ticker") String ticker
    ){
        BuyPrepareResponse response = tradeService.getBuystock(authentication.getName(), ticker);

        return ApiResponse.ok(response);
    }

    /**
     * 매도 시 주식 종목 데이터 조회
     * @param authentication
     * @return
     */
    @GetMapping("/sell/{ticker}")
    public ApiResponse<SellPrepareResponse> getSellstock(
            Authentication authentication,
            @PathVariable("ticker") String ticker
    ){
        SellPrepareResponse response = tradeService.getSellstock(authentication.getName(), ticker);

        return ApiResponse.ok(response);
    }

    /**
     * 전체 거래 이력 조회 (date 파라미터 있으면 날짜 필터)
     */
    @GetMapping
    public ApiResponse<List<TradeHistoryResponse>> getTradeHistory(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (date != null) {
            return ApiResponse.ok(tradeService.getHistoryListByDate(authentication.getName(), date));
        }
        return ApiResponse.ok(tradeService.getHistoryList(authentication.getName(), 1));
    }

    /**
     * 월별 거래 날짜 요약 (캘린더 도트용)
     */
    @GetMapping("/calendar")
    public ApiResponse<List<TradeCalendarEntry>> getTradeCalendar(
            Authentication authentication,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ApiResponse.ok(tradeService.getTradeCalendar(authentication.getName(), year, month));
    }

    /**
     * 특정 주식의 이력 조회
     * @param authentication
     * @param sid
     * @return
     */
    @GetMapping("/stocks/{sid}")
    public ApiResponse<List<TradeResponse>> getTradeHistoryByStockId(
        Authentication authentication,
        @PathVariable Long sid
    ){
        List<TradeResponse> responses = tradeService.getHistoryListByStockId(authentication.getName(), sid);

        return ApiResponse.ok(responses);
    }

    /**
     * 특정 거래 단건 상세 조회
     * @param authentication
     * @return
     */
    @GetMapping("/{tid}")
    public ApiResponse<TradeDetailResponse> getTradeHistoryById(
            Authentication authentication,
            @PathVariable Long tid
    ) {
        TradeDetailResponse response = tradeService.getHistoryListById(authentication.getName(), tid);

        return ApiResponse.ok(response);
    }

}
