package com.seedit.feature.settlement.scheduler;

import com.seedit.feature.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Clock;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {
    private final SettlementService settlementService;
    private final Clock settlementClock;

    @Scheduled(cron = "0 10 0 * * *", zone = "Asia/Seoul")
    public void settle() {
        runSafely(false);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void catchUpOnStartup() {
        runSafely(false);
    }

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void retry() {
        runSafely(true);
    }

    private void runSafely(boolean retryOnly) {
        try {
            LocalDate today = LocalDate.now(settlementClock);
            if (retryOnly) settlementService.retryDue(today);
            else settlementService.settleDue(today);
        } catch (Exception e) {
            log.error("SETTLEMENT_BATCH_FAILED retryOnly={}", retryOnly, e);
        }
    }
}
