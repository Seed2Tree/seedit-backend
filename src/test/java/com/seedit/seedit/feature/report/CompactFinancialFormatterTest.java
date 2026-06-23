package com.seedit.seedit.feature.report;

import com.seedit.feature.report.domain.FinancialStatement;
import com.seedit.feature.report.service.CompactFinancialFormatter;
import com.seedit.feature.stock.domain.StockDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 압축 포매터 단위 테스트 (네트워크 불필요).
 * 조원 변환·선계산 지표·이자보상배율·기업명 헤더·주가지표를 검증한다.
 */
class CompactFinancialFormatterTest {

    private static FinancialStatement samsung2024() {
        return FinancialStatement.builder()
                .bsnsYear("2024").reprtCode("11011")
                .revenue(bd("300870903000000"))
                .operatingIncome(bd("32725961000000"))
                .netIncome(bd("34451351000000"))
                .assets(bd("514531948000000"))
                .liabilities(bd("112339878000000"))
                .equity(bd("402192070000000"))
                .currentAssets(bd("227062266000000"))
                .currentLiabilities(bd("93326299000000"))
                .cashFlowOperating(bd("72982621000000"))
                .cashFlowInvesting(bd("-85381702000000"))
                .cashFlowFinancing(bd("-7797243000000"))
                .roe(null)                                   // → 순익/자본으로 보완 계산
                .interestExpense(bd("1000000000000"))        // 영업익/이자비용 = 32.7배
                .cash(bd("50000000000000"))
                .borrowings(bd("10000000000000"))
                .receivables(bd("40000000000000"))
                .inventory(bd("30000000000000"))
                .build();
    }

    @Test
    @DisplayName("기업명 헤더와 조원 변환·지표가 출력에 포함된다")
    void 출력_검증() {
        StockDetail d = mock(StockDetail.class);
        when(d.getCompanyName()).thenReturn("삼성전자");
        when(d.getTicker()).thenReturn("005930");
        when(d.getPer()).thenReturn(new BigDecimal("49.13"));
        when(d.getPbr()).thenReturn(new BigDecimal("5.04"));
        when(d.getEps()).thenReturn(6564);
        when(d.getBps()).thenReturn(63976);

        String out = CompactFinancialFormatter.build(List.of(samsung2024()), d);

        assertThat(out).contains("종목: 삼성전자 (005930)");   // 어떤 기업인지 명시
        assertThat(out).contains("300.9");                    // 매출 조원
        assertThat(out).contains("10.9");                     // 영업이익률 %
        assertThat(out).contains("8.6");                      // ROE 보완계산 %
        assertThat(out).contains("이자보상배율");
        assertThat(out).contains("32.7");                     // 이자보상배율 배
        assertThat(out).contains("PER 49.13").contains("PBR 5.04");
    }

    @Test
    @DisplayName("StockDetail이 null이면 주가지표는 데이터 없음으로 표기")
    void 주가지표_널() {
        String out = CompactFinancialFormatter.build(List.of(samsung2024()), null);
        assertThat(out).contains("PER 데이터 없음");
    }

    private static BigDecimal bd(String s) { return new BigDecimal(s); }
}