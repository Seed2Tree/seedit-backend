package com.seedit.feature.portfolio.service;

import com.seedit.feature.portfolio.domain.Portfolio;
import com.seedit.feature.portfolio.dto.response.PortfolioResponse;
import com.seedit.feature.portfolio.dto.response.PortfolioSummaryResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PortfolioService {
    boolean savePortfolio(Portfolio portfolio); // 보유 종목 추가

    boolean updatePortfolio(Portfolio portfolio); // 보유 종목 정보 수정

    PortfolioSummaryResponse findAllByUserId(@Param("userId") Long userId, Long balance); //  사용자의 보유 종목 전체 조회


}
