package com.seedit.feature.stock.service;

import com.seedit.feature.stock.domain.Stock;
import com.seedit.feature.stock.dto.StockSyncResponse;
import com.seedit.feature.stock.external.DailyCandle;
import com.seedit.feature.stock.external.DailyStockPrice;
import com.seedit.feature.stock.external.StockPriceProvider;
import com.seedit.feature.stock.repository.StockRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 전체 종목 시세 동기화.
 *  - DB의 모든 종목을 순회하며 외부 API(StockPriceProvider)에서 시세를 받아 저장
 *  - 한 종목이 실패해도 나머지는 계속 진행
 *  - 증권사 API 호출 속도 제한을 피하기 위해 종목 간 간격을 둠
 */
@Service
@RequiredArgsConstructor
public class StockPriceSyncService {

    private static final Logger log = LoggerFactory.getLogger(StockPriceSyncService.class);
    private static final long CALL_INTERVAL_MS = 250;   // 키움 호출 속도 제한 대응

    private final StockRepository stockMapper;
    private final StockPriceProvider priceProvider;

    /**
     * @param marketClosed true면 장 마감 후 호출 → 현재가를 종가로 확정 저장
     */
    public StockSyncResponse syncAll(boolean marketClosed) {
        List<Stock> stocks = stockMapper.findAll();
        List<String> failed = new ArrayList<>();
        int success = 0;

        for (Stock stock : stocks) {
            try {
                DailyStockPrice price = priceProvider.fetchDailyPrice(stock.getTicker());
                if (marketClosed) {
                    price = price.withClosePrice();
                }
                price = price.withSid(stock.getSid());

                stockMapper.upsertDailyPrice(price);
                if (hasFundamentals(price)) {
                    stockMapper.updateFundamentals(price);
                }
                success++;
            } catch (Exception e) {
                log.warn("시세 동기화 실패 [{}]: {}", stock.getTicker(), e.getMessage());
                failed.add(stock.getTicker() + " : " + e.getMessage());
            }
            sleep(CALL_INTERVAL_MS);
        }

        log.info("시세 동기화 완료: 성공 {} / 실패 {} (marketClosed={})", success, failed.size(), marketClosed);
        return new StockSyncResponse(stocks.size(), success, failed);
    }

    /**
     * 과거 일봉 백필 (1회성).
     *  종목별로: 기존 히스토리 전체 삭제(mock 제거) → 실제 일봉 INSERT.
     *  오늘 데이터는 백필에서 제외하고 syncAll()이 담당 (52주 고저/펀더멘털 포함).
     *
     * @param days 종목당 가져올 최대 영업일 수 (예: 1300 ≈ 5년)
     */
    public StockSyncResponse backfillAll(int days) {
        List<Stock> stocks = stockMapper.findAll();
        List<String> failed = new ArrayList<>();
        int success = 0;

        for (Stock stock : stocks) {
            try {
                List<DailyCandle> candles = priceProvider.fetchDailyHistory(stock.getTicker(), days);
                int inserted = replaceHistory(stock.getSid(), candles);
                log.info("백필 완료 [{}]: {}건", stock.getTicker(), inserted);
                success++;
            } catch (Exception e) {
                log.warn("백필 실패 [{}]: {}", stock.getTicker(), e.getMessage());
                failed.add(stock.getTicker() + " : " + e.getMessage());
            }
            sleep(CALL_INTERVAL_MS);
        }

        log.info("백필 전체 완료: 성공 {} / 실패 {}", success, failed.size());
        return new StockSyncResponse(stocks.size(), success, failed);
    }

    /** 한 종목의 히스토리를 통째로 교체 (삭제+삽입이 하나의 트랜잭션) */
    @Transactional
    protected int replaceHistory(Long sid, List<DailyCandle> candles) {
        LocalDate today = LocalDate.now();
        // 과거 → 최신 순으로 정렬 후, 직전 캔들 종가로 prev_close 채움
        List<DailyCandle> sorted = candles.stream()
                .filter(c -> c.getTradeDate().isBefore(today))   // 오늘은 syncAll 담당
                .sorted(Comparator.comparing(DailyCandle::getTradeDate))
                .toList();

        List<DailyCandle> rows = new ArrayList<>(sorted.size());
        Long prevClose = null;
        for (DailyCandle c : sorted) {
            rows.add(c.toBuilder().prevClose(prevClose).build());
            prevClose = c.getClose();
        }

        stockMapper.deleteHistoryBySid(sid);
        // 한 번에 너무 큰 INSERT 를 피하기 위해 500건씩 나눠 저장
        for (int i = 0; i < rows.size(); i += 500) {
            stockMapper.insertHistoryBatch(sid, rows.subList(i, Math.min(i + 500, rows.size())));
        }
        return rows.size();
    }

    /** 전부 null이면 UPDATE 자체를 건너뜀 (빈 SET 절 방지) */
    private static boolean hasFundamentals(DailyStockPrice p) {
        return p.getMarketCap() != null || p.getForeignOwnershipPct() != null
                || p.getPer() != null || p.getEps() != null
                || p.getPbr() != null || p.getBps() != null;
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
