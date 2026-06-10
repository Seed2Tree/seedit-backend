package com.seedit.feature.stock.repository;

import com.seedit.feature.stock.domain.Stock;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StockRepository {
    List<Stock> findAll();
}
