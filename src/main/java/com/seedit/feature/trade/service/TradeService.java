package com.seedit.feature.trade.service;

import com.seedit.feature.trade.dto.request.TradeRequest;
import com.seedit.feature.trade.dto.response.*;

import java.time.LocalDate;
import java.util.List;

public interface TradeService {

    BuyResponse processOrder(String email, TradeRequest request);

    SellResponse processSell(String email, TradeRequest request);

    BuyPrepareResponse getBuystock(String email, String ticker);

    SellPrepareResponse getSellstock(String email, String ticker);

    List<TradeHistoryResponse> getHistoryList(String email, int month);

    List<TradeResponse> getHistoryListByStockId(String email, Long sid);

    TradeDetailResponse getHistoryListById(String email, Long tid);

    List<TradeHistoryResponse> getHistoryListByDate(String email, LocalDate date);

    List<TradeCalendarEntry> getTradeCalendar(String email, int year, int month);

}
