package com.seedit.feature.stock;

import com.seedit.feature.stock.domain.Stock;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StockMapper {
    List<Stock> findAll();
}
