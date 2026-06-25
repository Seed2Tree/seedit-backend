package com.seedit.feature.settlement.scheduler;

import com.seedit.feature.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {
    private final SettlementService settlementService;

    @Scheduled(cron = "0 10 0 * * *", zone = "Asia/Seoul")
    public void settle() {
        runSafely();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void catchUpOnStartup() {
        runSafely();
    }

    private void runSafely() {
        try {
            settlementService.settleDue(LocalDate.now(ZoneId.of("Asia/Seoul")));
        } catch (Exception e) {
            log.error("정산 배치 실패", e);
        }
    }
}