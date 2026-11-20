package com.seedit.feature.stock.service;

import com.seedit.feature.stock.external.DailyCandle;
import com.seedit.feature.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 한 종목의 시세 히스토리 교체.
 * StockPriceSyncService 안에서 self-invocation으로 부르면 @Transactional이 적용되지 않으므로
 * 별도 빈으로 분리해 삭제 + 전체 청크 삽입을 종목 단위로 원자화한다. (SEED-111)
 */
@Service
@RequiredArgsConstructor
public class StockHistoryReplacer {

    private static final int CHUNK_SIZE = 500;

    private final StockRepository stockMapper;

    /** 한 종목의 히스토리를 통째로 교체 (삭제+삽입이 하나의 트랜잭션) */
    @Transactional
    public int replace(Long sid, List<DailyCandle> candles) {
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
        for (int i = 0; i < rows.size(); i += CHUNK_SIZE) {
            stockMapper.insertHistoryBatch(sid, rows.subList(i, Math.min(i + CHUNK_SIZE, rows.size())));
        }
        return rows.size();
    }
}
