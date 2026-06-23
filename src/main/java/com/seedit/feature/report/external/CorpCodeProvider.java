package com.seedit.feature.report.external;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

/**
 * DART 고유번호(corp_code) <-> 종목코드(ticker) 매핑.
 * corpCode.xml 을 받아 stock_code 가 있는 상장사만 캐시(기업명 포함).
 */
@Component
public class CorpCodeProvider {

    /** 매핑 1건: 고유번호 + 기업명 */
    public record CorpInfo(String corpCode, String corpName) {}

    private final DartProperties props;
    private final AtomicReference<Map<String, CorpInfo>> cache = new AtomicReference<>(Map.of());

    // <list> 블록 단위로 끊은 뒤 블록 안에서 각각 추출 (블록 경계 넘어가는 오매칭 방지)
    private static final Pattern LIST  = Pattern.compile("<list>(.*?)</list>", Pattern.DOTALL);
    private static final Pattern CORP  = Pattern.compile("<corp_code>\\s*(\\d+)\\s*</corp_code>");
    private static final Pattern NAME  = Pattern.compile("<corp_name>(.*?)</corp_name>", Pattern.DOTALL);
    private static final Pattern STOCK = Pattern.compile("<stock_code>\\s*(\\d{6})\\s*</stock_code>");

    public CorpCodeProvider(DartProperties props) {
        this.props = props;
    }

    /** ticker -> corp_code (없으면 null) */
    public String toCorpCode(String ticker) {
        CorpInfo i = lookup(ticker);
        return i == null ? null : i.corpCode();
    }

    /** ticker -> DART 기업명 (없으면 null). corp_code 매핑 검증용. */
    public String toCorpName(String ticker) {
        CorpInfo i = lookup(ticker);
        return i == null ? null : i.corpName();
    }

    private CorpInfo lookup(String ticker) {
        Map<String, CorpInfo> map = cache.get();
        if (map.isEmpty()) refresh();
        return cache.get().get(ticker);
    }

    /** 기동 후 1회 + 스케줄로 갱신 권장 */
    public synchronized void refresh() {
        if (props.apiKey() == null || props.apiKey().isBlank()) {
            throw new IllegalStateException("DART_API_KEY 미설정 — application.yml의 app.dart.api-key 확인");
        }
        byte[] zip = RestClient.create()
                .get()
                .uri(props.baseUrl() + "/corpCode.xml?crtfc_key={key}", props.apiKey())
                .retrieve()
                .body(byte[].class);
        if (zip == null) return;

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip))) {
            if (zis.getNextEntry() == null) return;
            String xml = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, CorpInfo> map = parse(xml);
            if (!map.isEmpty()) cache.set(map);
        } catch (Exception e) {
            // 파싱 실패 시 기존 캐시 유지
        }
    }

    /**
     * corpCode.xml 문자열을 stock_code -> CorpInfo 맵으로 파싱.
     * 네트워크와 분리되어 단위 테스트 가능. (각 &lt;list&gt; 블록 내에서만 짝지어 오매칭 방지)
     */
    public static Map<String, CorpInfo> parse(String xml) {
        Map<String, CorpInfo> map = new HashMap<>();
        Matcher blocks = LIST.matcher(xml);
        while (blocks.find()) {
            String block = blocks.group(1);
            Matcher sm = STOCK.matcher(block);
            if (!sm.find()) continue;            // 종목코드 없는 비상장사는 skip
            Matcher cm = CORP.matcher(block);
            if (!cm.find()) continue;
            Matcher nm = NAME.matcher(block);
            String name = nm.find() ? nm.group(1).trim() : "";
            map.put(sm.group(1), new CorpInfo(cm.group(1), name));
        }
        return map;
    }
}