package com.seedit.seedit.feature.report;

import com.seedit.feature.report.domain.FinancialStatement;
import com.seedit.feature.report.external.CorpCodeProvider;
import com.seedit.feature.report.external.DartApiClient;
import com.seedit.feature.report.external.DartProperties;
import com.seedit.feature.report.planner.ReportPlanner;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실제 DART 호출 통합 테스트.
 * application-local.yml 의 app.dart.api-key 를 그대로 사용한다(= local 프로파일).
 *
 * 실행: ./mvnw test -Dtest=ReportPipelineIT
 *   (키가 비어 있으면 assumeTrue 로 자동 skip)
 *
 * 핵심 검증: ticker -> corp_code 매핑이 '실제로 그 기업'을 가리키는지
 *   (1) DART 기업개황의 stock_code 가 요청 ticker 와 '동일'한가
 *   (2) DART corp_name 이 기대 기업명과 '유사'한가
 *   (3) 그 종목의 재무데이터가 실제로 수집되는가
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("local")
class ReportPipelineIT {

    private static final String TICKER = "005490";          // POSCO홀딩스
    private static final String[] NAME_HINTS = {"포스코", "POSCO"};

    @Autowired DartProperties props;
    @Autowired CorpCodeProvider corp;
    @Autowired DartApiClient dart;
    @Autowired ReportPlanner planner;

    @BeforeEach
    void requireKey() {
        Assumptions.assumeTrue(
                props.apiKey() != null && !props.apiKey().isBlank(),
                "app.dart.api-key 미설정 — 통합 테스트 skip");
    }

    @Test
    @DisplayName("ticker→corp_code 매핑이 실제 그 기업(종목코드 동일·이름 유사)을 가리킨다")
    void 매핑_무결성_검증() {
        String corpCode = corp.toCorpCode(TICKER);
        assertThat(corpCode).as("corp_code 매핑").isNotBlank();

        // corpCode.xml 측 기업명도 기대 이름과 유사한지
        assertThat(containsHint(corp.toCorpName(TICKER)))
                .as("corpCode.xml 기업명: %s", corp.toCorpName(TICKER))
                .isTrue();

        // DART 기업개황으로 교차 검증
        JsonNode company = dart.fetchCompany(corpCode);
        assertThat(company).as("기업개황 응답").isNotNull();

        String dartStock = company.path("stock_code").asText();
        String dartName  = company.path("corp_name").asText();

        assertThat(dartStock).as("DART stock_code 동일성").isEqualTo(TICKER);       // (1) 동일?
        assertThat(containsHint(dartName)).as("DART corp_name: %s", dartName).isTrue(); // (2) 유사?
    }

    @Test
    @DisplayName("해당 종목의 재무데이터가 실제로 수집된다")
    void 재무수집_검증() {
        List<FinancialStatement> stmts =
                planner.collect(TICKER, List.of(2024, 2023, 2022), "11011");

        assertThat(stmts).as("수집된 기간").isNotEmpty();
        FinancialStatement latest = stmts.get(0);
        assertThat(latest.revenue()).as("매출").isNotNull();
        assertThat(latest.revenue().signum()).as("매출 > 0").isPositive();
        assertThat(latest.equity()).as("자본").isNotNull();
    }

    private static boolean containsHint(String name) {
        if (name == null) return false;
        String upper = name.toUpperCase();
        for (String h : NAME_HINTS) {
            if (name.contains(h) || upper.contains(h.toUpperCase())) return true;
        }
        return false;
    }
}