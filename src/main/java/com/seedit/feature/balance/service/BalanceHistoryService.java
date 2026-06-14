package com.seedit.feature.balance.service;

import com.seedit.feature.balance.domain.BalanceHistory;
import org.springframework.stereotype.Service;

import java.util.List;


public interface BalanceHistoryService {

    boolean saveBalanceHistory(BalanceHistory history);

    BalanceHistory findBalanceHistory(Long userId, Long bhid);

    List<BalanceHistory> findBalanceHistories(Long userId);
}
