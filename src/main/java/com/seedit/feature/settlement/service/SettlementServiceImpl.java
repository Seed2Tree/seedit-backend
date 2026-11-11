package com.seedit.feature.settlement.service;

import com.seedit.feature.settlement.domain.Settlement;
import com.seedit.feature.settlement.domain.SettlementStatus;
import com.seedit.feature.settlement.dto.response.SettlementResponse;
import com.seedit.feature.settlement.repository.SettlementRepository;
import com.seedit.feature.user.domain.UserAccount;
import com.seedit.feature.user.repository.UserAccountRepository;
import com.seedit.global.error.BusinessException;
import com.seedit.global.error.ErrorCode;
import com.seedit.global.util.BusinessDayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Clock;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final UserAccountRepository userAccountRepository;
    private final SettlementProcessor processor;
    private final Clock settlementClock;
    @Override
    public void settleDue(LocalDate today) {
        runDue(today, false);
    }

    @Override
    public void retryDue(LocalDate today) {
        runDue(today, true);
    }

    private void runDue(LocalDate today, boolean retryOnly) {
        LocalDateTime cutoff = LocalDateTime.now(settlementClock);
        long afterId = 0;
        int succeeded = 0, failed = 0, skipped = 0;
        while (true) {
            List<Settlement> page = settlementRepository.findDuePage(today, cutoff, afterId, 200, retryOnly);
            if (page.isEmpty()) break;
            for (Settlement s : page) {
                afterId = s.getSettlementId();
                SettlementProcessor.Attempt attempt = null;
                try {
                    attempt = processor.claim(s.getSettlementId(), s.getUserId(), today);
                    if (attempt != null && processor.settleOne(attempt)) succeeded++;
                    else skipped++;
                } catch (RuntimeException error) {
                    if (SettlementRetryPolicy.isInfrastructureFailure(error)) {
                        // 선점된 한 건은 lease 만료로 복구한다. 나머지 건의 횟수는 소진하지 않는다.
                        log.error("SETTLEMENT_BATCH_UNAVAILABLE settlementId={}", s.getSettlementId(), error);
                        throw error;
                    }
                    failed++;
                    log.error("정산 건 처리 실패 settlementId={}", s.getSettlementId(), error);
                    if (attempt != null) {
                        // 이 기록마저 실패하면 배치를 중단하고 만료 복구에 맡긴다.
                        processor.recordFailure(attempt, SettlementRetryPolicy.isRetryable(error),
                                SettlementRetryPolicy.errorCode(error));
                    }
                }
            }
        }
        log.info("정산 배치 종료 retryOnly={}, succeeded={}, failed={}, skipped={}",
                retryOnly, succeeded, failed, skipped);
    }

    @Override @Transactional
    public void reserve(Long userId, Long tid, long amount, LocalDate tradeDate) {
        Settlement s = Settlement.builder()
                .userId(userId).tid(tid).amount(amount)
                .tradeDate(tradeDate)
                .settleDate(BusinessDayUtil.plusBusinessDays(tradeDate, 2))
                .status(SettlementStatus.PENDING).createdAt(LocalDateTime.now(settlementClock)).build();
        settlementRepository.save(s);
    }

    @Override
    public List<SettlementResponse> getMySettlements(String email, SettlementStatus status) {
        UserAccount u = userAccountRepository.findUserByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        return settlementRepository.findByUserId(u.getUserId(), status);
    }
}
