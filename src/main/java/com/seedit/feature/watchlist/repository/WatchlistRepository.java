package com.seedit.feature.watchlist.repository;

import com.seedit.feature.watchlist.domain.Watchlist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WatchlistRepository {
    List<Watchlist> findByUserId(@Param("userId") Long userId);
    int countByUserIdAndSid(@Param("userId") Long userId, @Param("sid") Long sid);
    int countStockBySid(@Param("sid") Long sid);
    int countStockNumByUserId(@Param("userId") Long userId);
    int insert(@Param("userId") Long userId, @Param("sid") Long sid);
    int deleteByUserIdAndSid(@Param("userId") Long userId, @Param("sid") Long sid);
    int deleteByUserId(@Param("userId") Long userId);
}
