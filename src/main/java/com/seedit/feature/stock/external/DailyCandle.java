package com.seedit.feature.stock.external;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 과거 일봉 1건 (백필용).
 * prevClose는 API 응답엔 없고, 저장 직전에 직전 캔들의 종가로 채운다.
 */
@Getter
@Builder(toBuilder = true)
public class DailyCandle {
    private LocalDate tradeDate;
    private Long open;
    private Long close;
    private Long high;
    private Long low;
    private Long volume;
    private Long tradingValue;
    private Long prevClose;
}
