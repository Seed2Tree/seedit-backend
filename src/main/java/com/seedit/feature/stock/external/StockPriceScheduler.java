package com.seedit.feature.stock.external;

import com.seedit.feature.stock.service.StockPriceSyncService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 시세 수집 스케줄러 (평일 10:00 / 16:00, 한국 시간)
 *  - 10시: 장중 시세 INSERT (종가 NULL)
 *  - 16시: 장 마감(15:30) 이후 → 같은 행 UPDATE로 종가/고저가 확정
 */
@Component
@RequiredArgsConstructor
public class StockPriceScheduler {

    private static final Logger log = LoggerFactory.getLogger(StockPriceScheduler.class);

    private final StockPriceSyncService syncService;

    @Scheduled(cron = "0 0 10 * * MON-FRI", zone = "Asia/Seoul")
    public void syncMorning() {
        log.info("[배치] 10시 장중 시세 수집 시작");
        syncService.syncAll(false);
    }

    @Scheduled(cron = "0 0 16 * * MON-FRI", zone = "Asia/Seoul")
    public void syncAfterClose() {
        log.info("[배치] 16시 종가 확정 수집 시작");
        syncService.syncAll(true);
    }
}
