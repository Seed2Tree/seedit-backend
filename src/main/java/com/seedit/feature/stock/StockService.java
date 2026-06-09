package com.seedit.feature.stock;

import com.seedit.feature.stock.dto.StockResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockMapper stockMapper;

    public List<StockResponse> getStocks() {
        return stockMapper.findAll().stream()
                .map(StockResponse::new)
                .toList();
    }
}
