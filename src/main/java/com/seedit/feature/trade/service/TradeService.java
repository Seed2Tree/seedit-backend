package com.seedit.feature.trade.service;

import com.seedit.feature.trade.domain.Trade;
import com.seedit.feature.trade.dto.request.TradeRequest;
import com.seedit.feature.trade.dto.response.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface TradeService {

    BuyResponse processOrder(String email, TradeRequest request);

    SellResponse processSell(String email, TradeRequest request);

    BuyPrepareResponse getBuystock(String email, Long sdid);

    SellPrepareResponse getSellstock(String email, Long sdid);

    List<TradeHistoryResponse> getHistoryList(String email);

    List<TradeResponse> getHistoryListByStockId(String email, Long sid);

    TradeHistoryResponse getHistoryListById(String email, Long tid);

}
