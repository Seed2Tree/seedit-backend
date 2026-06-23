package com.seedit.seedit.feature.report;

import com.seedit.feature.report.external.CorpCodeProvider;
import com.seedit.feature.report.external.CorpCodeProvider.CorpInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * corpCode.xml 파싱 단위 테스트 (네트워크 불필요).
 * 핵심: 비상장사(공란 stock_code)를 건너뛰며 '블록 경계를 넘어' 엉뚱한 corp_code와
 *      짝지어지던 버그가 재발하지 않는지 검증한다.
 */
class CorpCodeParseTest {

    // 비상장사 2건을 사이에 끼운 모의 corpCode.xml
    private static final String XML = """
        <result>
          <list><corp_code>00111111</corp_code><corp_name>비상장에이</corp_name><stock_code> </stock_code></list>
          <list><corp_code>00222222</corp_code><corp_name>비상장비</corp_name><stock_code> </stock_code></list>
          <list><corp_code>00356370</corp_code><corp_name>POSCO홀딩스</corp_name><stock_code>005490</stock_code></list>
          <list><corp_code>00126380</corp_code><corp_name>삼성전자</corp_name><stock_code>005930</stock_code></list>
          <list><corp_code>00999999</corp_code><corp_name>비상장씨</corp_name><stock_code> </stock_code></list>
        </result>
        """;

    @Test
    @DisplayName("같은 <list> 블록의 corp_code/stock_code/corp_name 만 짝짓는다")
    void 블록_내_정상_매핑() {
        Map<String, CorpInfo> map = CorpCodeProvider.parse(XML);

        // 005490 이 앞쪽 비상장사 corp_code(00111111 등)로 새지 않아야 함 ← 버그 회귀 방지
        assertThat(map.get("005490").corpCode()).isEqualTo("00356370");
        assertThat(map.get("005490").corpName()).contains("POSCO");
        assertThat(map.get("005930").corpCode()).isEqualTo("00126380");
        assertThat(map.get("005930").corpName()).isEqualTo("삼성전자");
    }

    @Test
    @DisplayName("종목코드 없는 비상장사는 매핑에서 제외한다")
    void 비상장사_제외() {
        Map<String, CorpInfo> map = CorpCodeProvider.parse(XML);

        assertThat(map).hasSize(2);                 // 상장 2건만
        assertThat(map).containsOnlyKeys("005490", "005930");
        assertThat(map.values())
                .extracting(CorpInfo::corpCode)
                .doesNotContain("00111111", "00222222", "00999999");
    }
}