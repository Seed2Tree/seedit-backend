package com.seedit.feature.stock.service;

import com.seedit.feature.stock.domain.StockDetail;
import com.seedit.feature.stock.dto.StockCandleResponse;
import com.seedit.feature.stock.dto.StockDetailResponse;
import com.seedit.feature.stock.dto.StockResponse;
import com.seedit.feature.stock.repository.StockRepository;
import com.seedit.global.error.BusinessException;
import com.seedit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockService {

    /** 기간 파라미터 → (조회 영업일 수, 캔들 집계 단위) */
    private record PeriodSpec(int days, String groupFormat) {}

    private static final String DAILY   = "%Y%m%d";  // 일봉
    private static final String WEEKLY  = "%x%v";    // 주봉 (ISO 연도+주차)
    private static final String MONTHLY = "%Y%m";    // 월봉

    private static final Map<String, PeriodSpec> PERIODS = Map.of(
            "1d", new PeriodSpec(1, DAILY),
            "1w", new PeriodSpec(7, DAILY),
            "1m", new PeriodSpec(30, DAILY),
            "3m", new PeriodSpec(90, DAILY),
            "1y", new PeriodSpec(260, WEEKLY),
            "5y", new PeriodSpec(1300, MONTHLY)
    );

    private final StockRepository stockMapper;

    public List<StockResponse> getStocks() {
        return stockMapper.findAll().stream()
                .map(StockResponse::new)
                .toList();
    }

    public StockDetailResponse getStockDetail(String ticker) {
        StockDetail detail = stockMapper.findDetailByTicker(ticker);
        if (detail == null) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "존재하지 않는 종목입니다.");
        }
        return new StockDetailResponse(detail);
    }

    public List<StockCandleResponse> getStockPrices(String ticker, String period) {
        PeriodSpec spec = PERIODS.get(period);
        if (spec == null) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION,
                    "지원하지 않는 기간입니다. (1d/1w/1m/3m/1y/5y)");
        }
        List<StockCandleResponse> candles =
                stockMapper.findCandlesByTicker(ticker, spec.days(), spec.groupFormat()).stream()
                        .map(StockCandleResponse::new)
                        .toList();
        if (candles.isEmpty()) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "존재하지 않는 종목입니다.");
        }
        return candles;
    }
}
