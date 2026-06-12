package com.seedit.feature.stock.external;

import tools.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 키움 REST API 시세 조회 클라이언트.
 *
 *  주식기본정보요청 (TR: ka10001)
 *   - POST {base}/api/dostk/stkinfo
 *   - 헤더: authorization(Bearer 토큰), api-id(ka10001)
 *   - 바디: { "stk_cd": "005930" }
 *
 *  ※ 응답 필드명은 아래 FIELD_* 상수에 모아두었다.
 *    첫 실행 시 INFO 로그로 원본 응답을 출력하므로,
 *    실제 응답과 다른 필드명이 있으면 상수만 고치면 된다.
 */
@Component
public class KiwoomApiClient implements StockPriceProvider {

    private static final Logger log = LoggerFactory.getLogger(KiwoomApiClient.class);

    // ===== ka10001 응답 필드명 (실제 응답으로 검증 후 확정) =====
    private static final String FIELD_CURRENT   = "cur_prc";      // 현재가 (부호 포함 문자열)
    private static final String FIELD_OPEN      = "open_pric";    // 시가
    private static final String FIELD_HIGH      = "high_pric";    // 고가
    private static final String FIELD_LOW       = "low_pric";     // 저가
    private static final String FIELD_BASE      = "base_pric";    // 기준가(전일 종가)
    private static final String FIELD_VOLUME    = "trde_qty";     // 거래량
    private static final String FIELD_W52_HIGH  = "250hgst";      // 250일(≈52주) 최고가
    private static final String FIELD_W52_LOW   = "250lwst";      // 250일(≈52주) 최저가
    private static final String FIELD_MARKETCAP = "mac";          // 시가총액 (억원)
    private static final String FIELD_FOREIGN   = "for_exh_rt";   // 외인 소진율 (%)
    private static final String FIELD_PER       = "per";
    private static final String FIELD_EPS       = "eps";
    private static final String FIELD_PBR       = "pbr";
    private static final String FIELD_BPS       = "bps";

    private final KiwoomTokenManager tokenManager;

    @Value("${app.kiwoom.base-url}")
    private String baseUrl;

    /** 디버깅용: 첫 호출의 원본 응답을 한 번만 로그로 출력 */
    private volatile boolean rawLogged = false;

    public KiwoomApiClient(KiwoomTokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public DailyStockPrice fetchDailyPrice(String ticker) {
        JsonNode res = RestClient.create()
                .post()
                .uri(baseUrl + "/api/dostk/stkinfo")
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer " + tokenManager.getToken())
                .header("api-id", "ka10001")
                .body(Map.of("stk_cd", ticker))
                .retrieve()
                .body(JsonNode.class);

        if (res == null) {
            throw new IllegalStateException("키움 응답 없음: " + ticker);
        }
        if (!rawLogged) {
            log.info("[키움 ka10001 원본 응답 - 필드명 검증용] {}", res.toPrettyString());
            rawLogged = true;
        }
        // return_code 가 있고 0이 아니면 실패 (휴장일/오류 등)
        if (res.has("return_code") && res.path("return_code").asInt(0) != 0) {
            throw new IllegalStateException(
                    "키움 조회 실패 [" + ticker + "]: " + res.path("return_msg").asText());
        }

        Long current = asLong(res, FIELD_CURRENT);
        if (current == null) {
            throw new IllegalStateException("현재가 파싱 실패 [" + ticker + "] - 응답 필드명 확인 필요");
        }

        Long volume = asLong(res, FIELD_VOLUME);

        return DailyStockPrice.builder()
                .ticker(ticker)
                .tradeDate(LocalDate.now())
                .currentPrice(current)
                .openPrice(asLong(res, FIELD_OPEN))
                .highPrice(asLong(res, FIELD_HIGH))
                .lowPrice(asLong(res, FIELD_LOW))
                .prevClosePrice(asLong(res, FIELD_BASE))
                .volume(volume)
                // ka10001엔 거래대금이 없어 현재가x거래량으로 근사 (표시용으로 충분)
                .tradingValue(volume == null ? null : current * volume)
                .w52HighPrice(asLong(res, FIELD_W52_HIGH))
                .w52LowPrice(asLong(res, FIELD_W52_LOW))
                .marketCap(asLong(res, FIELD_MARKETCAP))
                .foreignOwnershipPct(asDecimal(res, FIELD_FOREIGN))
                .per(asDecimal(res, FIELD_PER))
                .eps(asInteger(res, FIELD_EPS))
                .pbr(asDecimal(res, FIELD_PBR))
                .bps(asInteger(res, FIELD_BPS))
                .build();
    }

    // ===== ka10081 주식일봉차트 (백필용) =====

    private static final DateTimeFormatter CHART_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String CHART_LIST_FIELD = "stk_dt_pole_chart_qry";  // 일봉 배열 (원본 로그로 검증)

    private volatile boolean chartRawLogged = false;

    /**
     * 과거 일봉 조회 (최신 → 과거 순).
     * 한 번에 다 안 오면 응답 헤더 cont-yn/next-key 로 연속조회.
     */
    @Override
    public List<DailyCandle> fetchDailyHistory(String ticker, int maxCount) {
        List<DailyCandle> candles = new ArrayList<>();
        String contYn = "N";
        String nextKey = "";

        while (candles.size() < maxCount) {
            ResponseEntity<JsonNode> entity = RestClient.create()
                    .post()
                    .uri(baseUrl + "/api/dostk/chart")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("authorization", "Bearer " + tokenManager.getToken())
                    .header("api-id", "ka10081")
                    .header("cont-yn", contYn)
                    .header("next-key", nextKey)
                    .body(Map.of(
                            "stk_cd", ticker,
                            "base_dt", LocalDate.now().format(CHART_DATE),
                            "upd_stkpc_tp", "1"))   // 수정주가 적용
                    .retrieve()
                    .toEntity(JsonNode.class);

            JsonNode res = entity.getBody();
            if (res == null) break;
            if (!chartRawLogged) {
                JsonNode first = res.path(CHART_LIST_FIELD).path(0);
                log.info("[키움 ka10081 원본 응답 - 루트 필드: {} / 첫 캔들: {}]",
                        res.propertyNames(), first.toString());
                chartRawLogged = true;
            }
            if (res.has("return_code") && res.path("return_code").asInt(0) != 0) {
                throw new IllegalStateException(
                        "키움 일봉 조회 실패 [" + ticker + "]: " + res.path("return_msg").asText());
            }

            JsonNode list = res.path(CHART_LIST_FIELD);
            if (!list.isArray() || list.isEmpty()) break;

            for (JsonNode item : list) {
                String dt = item.path("dt").asText("");
                Long close = asLong(item, "cur_prc");   // 일봉의 종가
                if (dt.isEmpty() || close == null) continue;
                Long volume = asLong(item, "trde_qty");
                candles.add(DailyCandle.builder()
                        .tradeDate(LocalDate.parse(dt, CHART_DATE))
                        .open(asLong(item, "open_pric"))
                        .close(close)
                        .high(asLong(item, "high_pric"))
                        .low(asLong(item, "low_pric"))
                        .volume(volume)
                        .tradingValue(volume == null ? null : close * volume)
                        .build());
                if (candles.size() >= maxCount) break;
            }

            // 연속조회: 응답 헤더에 cont-yn=Y 면 next-key 로 이어서 요청
            String resContYn = entity.getHeaders().getFirst("cont-yn");
            nextKey = entity.getHeaders().getFirst("next-key");
            if (!"Y".equalsIgnoreCase(resContYn) || nextKey == null || nextKey.isEmpty()) break;
            contYn = "Y";
            sleep(250);   // 연속조회도 속도 제한 대응
        }
        return candles;
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ===== 파싱 헬퍼: 키움 숫자는 "+71300", "-1.34", "12,480" 같은 문자열로 옴 =====

    private static String clean(JsonNode node, String field) {
        JsonNode v = node.path(field);
        if (v.isMissingNode() || v.isNull()) return null;
        String s = v.asText().trim().replace(",", "");
        // 등락 방향 부호(+/-) 제거 — 가격 자체는 항상 양수
        if (s.startsWith("+") || s.startsWith("-")) s = s.substring(1);
        return s.isEmpty() ? null : s;
    }

    private static Long asLong(JsonNode node, String field) {
        String s = clean(node, field);
        if (s == null) return null;
        try {
            return new BigDecimal(s).longValue();   // "71300.00" 형태도 허용
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer asInteger(JsonNode node, String field) {
        Long v = asLong(node, field);
        return v == null ? null : v.intValue();
    }

    private static BigDecimal asDecimal(JsonNode node, String field) {
        String s = clean(node, field);
        if (s == null) return null;
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
