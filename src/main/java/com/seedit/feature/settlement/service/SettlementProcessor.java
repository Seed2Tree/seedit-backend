package com.seedit.feature.settlement.service;

import com.seedit.feature.balance.domain.BalanceHistory;
import com.seedit.feature.balance.repository.BalanceHistoryRepository;
import com.seedit.feature.settlement.domain.Settlement;
import com.seedit.feature.settlement.domain.SettlementStatus;
import com.seedit.feature.settlement.repository.SettlementRepository;
import com.seedit.feature.trade.domain.TradeType;
import com.seedit.feature.user.domain.UserAccount;
import com.seedit.feature.user.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementProcessor {
    private final SettlementRepository settlements;
    private final UserAccountRepository accounts;
    private final BalanceHistoryRepository histories;
    private final Clock settlementClock;

    public record Attempt(Long settlementId, Long userId, String token) {}

    // 별도 커밋된 선점으로 중단/재시작과 동시 실행에도 최대 시도 횟수를 유지한다.
    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 30)
    public Attempt claim(Long id, Long userId, LocalDate today) {
        accounts.findUserByIdForUpdate(userId);
        Settlement s = settlements.findByIdForUpdate(id);
        LocalDateTime now = LocalDateTime.now(settlementClock);
        if (s == null || !userId.equals(s.getUserId()) || s.getStatus() != SettlementStatus.PENDING
                || s.getSettleDate().isAfter(today)
                || (s.getNextRetryAt() != null && s.getNextRetryAt().isAfter(now))) return null;
        if (s.getAttemptToken() != null) {
            if (s.getLeaseUntil() != null && !s.getLeaseUntil().isAfter(now)) {
                fail(s, true, "ATTEMPT_LEASE_EXPIRED", now);
            }
            return null;
        }
        String token = UUID.randomUUID().toString();
        if (settlements.claim(id, token, now.plusMinutes(5)) != 1) return null;
        return new Attempt(id, userId, token);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 30)
    public boolean settleOne(Attempt attempt) {
        UserAccount user = accounts.findUserByIdForUpdate(attempt.userId()).orElse(null);
        Settlement s = settlements.findByIdForUpdate(attempt.settlementId());
        if (!owns(s, attempt)) return false;
        if (user == null || user.getBalance() == null || s.getAmount() == null || s.getAmount() <= 0) {
            throw new IllegalStateException("Invalid settlement account or amount");
        }
        long balance = Math.addExact(user.getBalance(), s.getAmount());
        LocalDateTime now = LocalDateTime.now(settlementClock);
        if (accounts.updateBalance(user.getUserId(), balance) != 1
                || settlements.markSettled(s.getSettlementId(), attempt.token(), now) != 1) {
            throw new IllegalStateException("Settlement update count mismatch");
        }
        if (histories.save(BalanceHistory.builder().userId(user.getUserId()).amount(s.getAmount())
                .currentBalance(balance).reasonType(TradeType.SELL).createdAt(now).build()) != 1) {
            throw new IllegalStateException("Settlement history insert count mismatch");
        }
        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 30)
    public void recordFailure(Attempt attempt, boolean retryable, String errorCode) {
        accounts.findUserByIdForUpdate(attempt.userId());
        Settlement s = settlements.findByIdForUpdate(attempt.settlementId());
        if (owns(s, attempt)) fail(s, retryable, errorCode, LocalDateTime.now(settlementClock));
    }

    private boolean owns(Settlement s, Attempt attempt) {
        return s != null && s.getStatus() == SettlementStatus.PENDING
                && attempt.userId().equals(s.getUserId()) && attempt.token().equals(s.getAttemptToken());
    }

    private void fail(Settlement s, boolean retryable, String errorCode, LocalDateTime now) {
        LocalDateTime next = retryable ? SettlementRetryPolicy.nextRetryAt(s.getAttemptCount(), now) : null;
        SettlementStatus status = next == null ? SettlementStatus.FAILED : SettlementStatus.PENDING;
        if (settlements.recordFailure(s.getSettlementId(), s.getAttemptToken(), status, next, errorCode, now) != 1) {
            throw new IllegalStateException("Settlement failure update count mismatch");
        }
        if (status == SettlementStatus.FAILED) {
            log.error("SETTLEMENT_FAILED settlementId={}, attempts={}, code={}",
                    s.getSettlementId(), s.getAttemptCount(), errorCode);
        } else {
            log.warn("SETTLEMENT_RETRY settlementId={}, attempts={}, nextRetryAt={}, code={}",
                    s.getSettlementId(), s.getAttemptCount(), next, errorCode);
        }
    }
}
