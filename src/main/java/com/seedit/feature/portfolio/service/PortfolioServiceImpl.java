package com.seedit.feature.portfolio.service;

import com.seedit.feature.portfolio.domain.Portfolio;
import com.seedit.feature.portfolio.dto.response.PortfolioResponse;
import com.seedit.feature.portfolio.dto.response.PortfolioSummaryResponse;
import com.seedit.feature.portfolio.repository.PortfolioRepository;
import com.seedit.feature.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService{

    private final PortfolioRepository portfolioRepository;

    private final StockRepository stockRepository;

    @Override
    public boolean savePortfolio(Portfolio portfolio) {

        return portfolioRepository.save(portfolio) == 1;
    }

    @Override
    public boolean updatePortfolio(Portfolio portfolio) {
        return portfolioRepository.updateOneByuserIdAndsid(portfolio) == 1;
    }

    @Override
    public PortfolioSummaryResponse findAllByUserId(Long userId, Long balance) {
        List<PortfolioResponse> holdings =  portfolioRepository.findAllByUserId(userId);

        // 총 매입금
        Long totalCost = holdings.stream().mapToLong(h -> h.avgPrice() * h.quantity()).sum();
        // 총 평가금액
        Long totalEval = holdings.stream().mapToLong(h -> h.currentPrice() * h.quantity()).sum();
        // 평가 손익
        Long totalProfit = totalEval - totalCost;
        // 총 수익률
        double totalProfitRate = totalCost > 0 ? (double) totalProfit / totalCost * 100 : 0;

        return new PortfolioSummaryResponse(
                totalCost, totalEval, totalProfit,
                Math.round(totalProfitRate * 100) / 100.0,
                balance, holdings);
    }
}
