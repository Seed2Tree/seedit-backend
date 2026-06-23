package com.seedit.feature.settlement.scheduler;

import com.seedit.feature.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class SettlementScheduler {
    private final SettlementService settlementService;

    // 매일 00:10 (주말에 돌아도 settle_date<=today 조건이라 안전, 놓친 날도 자동 보충)
    @Scheduled(cron = "0 10 0 * * *", zone = "Asia/Seoul")
    public void settle() {
        settlementService.settleDue(LocalDate.now(ZoneId.of("Asia/Seoul")));
    }
}