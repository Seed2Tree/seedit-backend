package com.seedit.feature.balance.service;

import com.seedit.feature.balance.domain.BalanceHistory;
import com.seedit.feature.balance.repository.BalanceHistoryRepository;
import com.seedit.global.error.BusinessException;
import com.seedit.global.error.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BalanceHistoryServiceImpl implements BalanceHistoryService{

    @Autowired
    private BalanceHistoryRepository balanceHistoryRepository;


    @Override
    @Transactional
    public boolean saveBalanceHistory(BalanceHistory history) {
        return balanceHistoryRepository.save(history) == 1;
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceHistory findBalanceHistory(Long userId, Long bhid) {
        BalanceHistory balanceHistory = balanceHistoryRepository.findById(bhid)
                .orElseThrow(()->new BusinessException(ErrorCode.COMMON_NOT_FOUND,"해당 이력이 존재하지 않았습니다. bhid="+bhid));
        if(!balanceHistory.getUserId().equals(userId)){
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN,"잘못된 접근입니다.");
        }
        return balanceHistory;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BalanceHistory> findBalanceHistories(Long userId) {
        List<BalanceHistory> histories = balanceHistoryRepository.findByUserId(userId);
        return histories;
    }
}
