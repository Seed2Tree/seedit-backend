package com.seedit.feature.trade.controller;

import com.seedit.feature.trade.domain.Trade;
import com.seedit.feature.trade.dto.request.TradeRequest;
import com.seedit.feature.trade.dto.response.*;
import com.seedit.feature.trade.service.TradeService;
import com.seedit.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody TradeRequest request,
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
            @RequestBody TradeRequest request,
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
    @GetMapping("/buy/{sdid}")
    public ApiResponse<BuyPrepareResponse> getBuystock(
            Authentication authentication,
            @PathVariable("sdid") Long sdid
    ){
        BuyPrepareResponse response = tradeService.getBuystock(authentication.getName(), sdid);

        return ApiResponse.ok(response);
    }

    /**
     * 매도 시 주식 종목 데이터 조회
     * @param authentication
     * @return
     */
    @GetMapping("/sell/{sdid}")
    public ApiResponse<SellPrepareResponse> getSellstock(
            Authentication authentication,
            @PathVariable("sdid") Long sdid
    ){
        SellPrepareResponse response = tradeService.getSellstock(authentication.getName(), sdid);

        return ApiResponse.ok(response);
    }

    /**
     * 전체 거래 이력 조회
     * @param authentication
     * @return
     */
    @GetMapping
    public ApiResponse<List<TradeHistoryResponse>> getTradeHistory(
            Authentication authentication
    ){
        List<TradeHistoryResponse> response = tradeService.getHistoryList(authentication.getName());

        return ApiResponse.ok(response);
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
    public ApiResponse<TradeHistoryResponse> getTradeHistoryById(
            Authentication authentication,
            @PathVariable Long tid
    ) {
        TradeHistoryResponse response = tradeService.getHistoryListById(authentication.getName(), tid);

        return ApiResponse.ok(response);
    }

}
