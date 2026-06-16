package com.seedit.feature.portfolio.service;

import com.seedit.feature.portfolio.domain.Portfolio;
import com.seedit.feature.portfolio.dto.response.PortfolioResponse;
import com.seedit.feature.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService{

    private final PortfolioRepository portfolioRepository;

    @Override
    public boolean savePortfolio(Portfolio portfolio) {

        return portfolioRepository.save(portfolio) == 1;
    }

    @Override
    public boolean updatePortfolio(Portfolio portfolio) {
        return portfolioRepository.updateOneByuserIdAndsid(portfolio) == 1;
    }

    @Override
    public List<PortfolioResponse> findAllByUserId(Long userId) {
        return portfolioRepository.findAllByUserId(userId);
    }
}
