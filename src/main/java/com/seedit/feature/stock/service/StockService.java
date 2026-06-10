package com.seedit.feature.stock.service;

import com.seedit.feature.stock.dto.StockResponse;
import com.seedit.feature.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockMapper;

    public List<StockResponse> getStocks() {
        return stockMapper.findAll().stream()
                .map(StockResponse::new)
                .toList();
    }
}
