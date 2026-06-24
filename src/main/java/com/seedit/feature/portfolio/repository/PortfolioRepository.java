package com.seedit.feature.portfolio.repository;

import com.seedit.feature.portfolio.domain.Portfolio;
import com.seedit.feature.portfolio.dto.response.PortfolioResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PortfolioRepository {
    int save(Portfolio portfolio); // 보유 종목 추가

    int updateOneByuserIdAndsid(Portfolio portfolio); // 보유 종목 정보 수정

    List<PortfolioResponse> findAllByUserId(@Param("userId") Long userId); //  사용자의 보유 종목 전체 조회

    Portfolio findByUserIdAndSid(@Param("userId") Long userId, @Param("sid") Long sid); //  사용자의 특정 보유 주식 조회

    int deleteByUserId(@Param("userId") Long userId);
}