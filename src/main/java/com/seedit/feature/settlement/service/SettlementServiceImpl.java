package com.seedit.feature.settlement.service;

import com.seedit.feature.balance.domain.BalanceHistory;
import com.seedit.feature.balance.repository.BalanceHistoryRepository;
import com.seedit.feature.settlement.domain.Settlement;
import com.seedit.feature.settlement.domain.SettlementStatus;
import com.seedit.feature.settlement.dto.response.SettlementResponse;
import com.seedit.feature.settlement.repository.SettlementRepository;
import com.seedit.feature.trade.domain.TradeType;
import com.seedit.feature.user.domain.UserAccount;
import com.seedit.feature.user.repository.UserAccountRepository;
import com.seedit.global.error.BusinessException;
import com.seedit.global.error.ErrorCode;
import com.seedit.global.util.BusinessDayUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final UserAccountRepository userAccountRepository;
    private final BalanceHistoryRepository balanceHistoryRepository;

    @Override
    @Transactional
    public void settleDue(LocalDate today) {
        List<Settlement> dues = settlementRepository.findPendingDue(today); // status=PENDING AND settle_date<=today
        for (Settlement s : dues) {
            UserAccount u = userAccountRepository.findUserById(s.getUserId()).orElseThrow();
            long newBalance = u.getBalance() + s.getAmount();
            userAccountRepository.updateBalance(u.getUserId(), newBalance);

            settlementRepository.markSettled(s.getSettlementId(), LocalDateTime.now()); // status=SETTLED

            balanceHistoryRepository.save(BalanceHistory.builder()
                    .userId(u.getUserId())
                    .amount(s.getAmount())
                    .currentBalance(newBalance)
                    .reasonType(TradeType.SELL)   // 또는 SETTLEMENT 타입 신설
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }

    @Override @Transactional
    public void reserve(Long userId, Long tid, long amount, LocalDate tradeDate) {
        Settlement s = Settlement.builder()
                .userId(userId).tid(tid).amount(amount)
                .tradeDate(tradeDate)
                .settleDate(BusinessDayUtil.plusBusinessDays(tradeDate, 2))
                .status(SettlementStatus.PENDING).createdAt(LocalDateTime.now()).build();
        settlementRepository.save(s);
    }

    @Override
    public List<SettlementResponse> getMySettlements(String email, SettlementStatus status) {
        UserAccount u = userAccountRepository.findUserByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        return settlementRepository.findByUserId(u.getUserId(), status);
    }
}
