package com.seedit.feature.settlement.service;

import com.seedit.feature.settlement.domain.SettlementStatus;
import com.seedit.feature.settlement.dto.response.SettlementResponse;

import java.time.LocalDate;
import java.util.List;

public interface SettlementService {
    void settleDue(LocalDate today);
    void reserve(Long userId, Long tid, long amount, LocalDate tradeDate);
    List<SettlementResponse> getMySettlements(String email, SettlementStatus status);
}