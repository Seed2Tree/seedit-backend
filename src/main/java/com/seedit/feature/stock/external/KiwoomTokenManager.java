package com.seedit.feature.stock.external;

import tools.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 키움 REST API 접근토큰 발급/캐싱.
 *
 *  - POST /oauth2/token { grant_type, appkey, secretkey } → { token, token_type, expires_dt }
 *  - 토큰 유효기간 24시간, 발급 횟수 제한이 있으므로 메모리에 캐싱하고
 *    만료 5분 전부터만 재발급한다.
 */
@Component
public class KiwoomTokenManager {

    private static final Logger log = LoggerFactory.getLogger(KiwoomTokenManager.class);
    private static final DateTimeFormatter EXPIRES_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Value("${app.kiwoom.base-url}")
    private String baseUrl;

    @Value("${app.kiwoom.app-key}")
    private String appKey;

    @Value("${app.kiwoom.secret-key}")
    private String secretKey;

    private String cachedToken;
    private LocalDateTime expiresAt;

    /** 유효한 토큰 반환. 없거나 만료 임박이면 재발급. */
    public synchronized String getToken() {
        if (cachedToken != null && expiresAt != null
                && LocalDateTime.now().isBefore(expiresAt.minusMinutes(5))) {
            return cachedToken;
        }
        issueToken();
        return cachedToken;
    }

    private void issueToken() {
        if (appKey == null || appKey.isBlank()) {
            throw new IllegalStateException("KIWOOM_APP_KEY 가 설정되지 않았습니다. (.env / 실행환경 변수 확인)");
        }
        JsonNode res = RestClient.create()
                .post()
                .uri(baseUrl + "/oauth2/token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "grant_type", "client_credentials",
                        "appkey", appKey,
                        "secretkey", secretKey))
                .retrieve()
                .body(JsonNode.class);

        if (res == null || res.path("token").isMissingNode()) {
            throw new IllegalStateException("키움 토큰 발급 실패: " + res);
        }
        cachedToken = res.path("token").asText();
        expiresAt = LocalDateTime.parse(res.path("expires_dt").asText(), EXPIRES_FORMAT);
        log.info("키움 접근토큰 발급 완료 (만료: {})", expiresAt);
    }
}
