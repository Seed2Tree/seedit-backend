package com.seedit.feature.report.service;

import com.seedit.feature.report.ai.AiReportClient;
import com.seedit.feature.report.domain.AiReport;
import com.seedit.feature.report.domain.FinancialStatement;
import com.seedit.feature.report.dto.response.CompanyReportPeriod;
import com.seedit.feature.report.dto.response.CompanyReportResponse;
import com.seedit.feature.report.external.CorpCodeProvider;
import com.seedit.feature.report.external.DartApiClient;
import com.seedit.feature.report.planner.ReportPlanner;
import com.seedit.feature.report.repository.AiReportRepository;
import com.seedit.feature.stock.domain.StockDetail;
import com.seedit.feature.stock.repository.StockRepository;
import com.seedit.global.error.BusinessException;
import com.seedit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;   // ← lombok.Value 아님
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import com.seedit.feature.report.dto.response.AvailableReport;
import com.seedit.feature.report.external.CorpCodeProvider;
import com.seedit.feature.report.external.DartApiClient;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CompanyReportService {

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper;
    private final ReportPlanner planner;
    private final AiReportClient ai;
    private final StockRepository stockRepository;
    private final AiReportRepository aiReportRepository;
    private final CorpCodeProvider corpCodes;
    private final DartApiClient dart;

    @Value("classpath:prompts/report-system-slim.txt")
    private Resource systemPromptResource;

    @Value("${app.dart.api-key:}")
    private String apiKey;

    @Value("${app.gms.model:gemini-3.5-flash}")
    private String model;

    // ───────────────────────── AI 리포트 캐시 API ─────────────────────────

    /** 저장된 리포트 조회 (없으면 null → 컨트롤러가 생성 유도). */
    public CompanyReportResponse getSavedReport(String ticker, Integer bsnsYear, String reprtCode) {
        AiReport r = aiReportRepository.findOne(ticker, bsnsYear, reprtCode);
        return r == null ? null : toResponse(r);
    }

    /** 생성 + DB 저장(upsert) 후 반환. 이미 있으면 갱신. */
    @Transactional
    public CompanyReportResponse generateAndSave(String ticker, Integer bsnsYear, String reprtCode) {
        List<Integer> years = List.of(bsnsYear, bsnsYear - 1, bsnsYear - 2);
        List<FinancialStatement> stmts = planner.collect(ticker, years, reprtCode);

        // 요청 연도에 해당하는 DART 실데이터가 있을 때만 생성 (폴백 라벨 방지 + 증표)
        FinancialStatement primary = stmts.stream()
                .filter(s -> String.valueOf(bsnsYear).equals(s.bsnsYear()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NO_DATA));


        StockDetail d = stockRepository.findDetailByTicker(ticker);
        String userData = CompactFinancialFormatter.build(stmts, d);
        String content = ai.generate(readPrompt(), userData);

        AiReport saved = AiReport.builder()
                .stockCode(ticker).bsnsYear(Integer.valueOf(primary.bsnsYear()))
                .rceptNo(primary.rceptNo())
                .reprtCode(primary.reprtCode())
                .content(content).model(model)
                .createdAt(LocalDateTime.now())
                .build();
        aiReportRepository.upsert(saved);
        return toResponse(saved);
    }

    /** 저장된 분기 목록(최신순) — 프론트 필터용. */
    public List<CompanyReportPeriod> getPeriods(String ticker) {
        return aiReportRepository.findPeriods(ticker).stream()
                .map(r -> new CompanyReportPeriod(
                        r.getBsnsYear(), r.getReprtCode(), str(r.getUpdatedAt())))
                .toList();
    }

    private CompanyReportResponse toResponse(AiReport r) {
        JsonNode node;
        try {
            node = objectMapper.readTree(r.getContent());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL, "리포트 JSON 파싱 실패");
        }
        return new CompanyReportResponse(
                r.getStockCode(), r.getBsnsYear(), r.getReprtCode(),
                node, r.getModel(), str(r.getCreatedAt()));
    }

    private static String str(LocalDateTime t) { return t == null ? null : t.toString(); }

    public String getDartReportByCode(String companyCode) {
        String result = "";
        try {
            result = callOpenDart(companyCode);
        } catch (Exception e) {
            result = "Dart 조회 실패: " + e.getMessage();
        }
        return result;
    }

    public String callOpenDart(String companyCode) {
        try {
            String url = UriComponentsBuilder
                    .fromUriString("https://opendart.fss.or.kr/api/list.json")
                    .queryParam("crtfc_key", apiKey)
                    .build(true)
                    .toUriString();
            String body = restClient.get().uri(url).retrieve().body(String.class);

            if (body == null || body.isBlank()) return "Dart 조회 실패: 빈 응답";

            JsonNode root = objectMapper.readTree(body);
            String status = root.path("status").asText("");
            if (!status.isBlank() && !"000".equals(status)) {
                return "Dart 조회 실패: status=" + status
                        + ", message=" + root.path("message").asText("unknown");
            }
            return root.path("list").path(0).path("report_nm").asText("-");
        } catch (Exception e) {
            return "Dart 조회 실패 : " + e.getClass().getSimpleName() + " - " + e.getMessage();
        }
        // ← 도달 불가 return "" 제거
    }

    /** 분기/반기 리포트 생성. reprtCode: 11011/11012/11013/11014 */
    public String generateReport(String ticker, List<Integer> years, String reprtCode) {
        // 1) 서버 주도 수집
        List<FinancialStatement> stmts = planner.collect(ticker, years, reprtCode);
        if (stmts.isEmpty()) throw new BusinessException(ErrorCode.REPORT_NO_DATA);
        // 2) PER/PBR 등은 우리 DB
        StockDetail d = stockRepository.findDetailByTicker(ticker);

        // 3) 압축 데이터 블록 조립 (조원 단위 + 선계산 지표 + 주가지표)
        String userData = CompactFinancialFormatter.build(stmts, d);

        // 4) LLM 호출
        String systemPrompt = readPrompt();
        return ai.generate(systemPrompt, userData);
    }

    // 입력 JSON 조립 (AI 호출 X) — 여기까지만 구현
    public String buildUserData(String ticker, List<Integer> years, String reprtCode) {
        List<FinancialStatement> fs = planner.collect(ticker, years, reprtCode);
        if (fs.isEmpty()) throw new BusinessException(ErrorCode.REPORT_NO_DATA);
        StockDetail d = stockRepository.findDetailByTicker(ticker);
        return CompactFinancialFormatter.build(fs, d);
    }

    // 시스템 프롬프트 원문 읽기 (classpath 리소스 → String)
    public String getSystemPrompt() {
        return readPrompt();
    }

    private String buildDataBlock(String ticker, StockDetail d, List<FinancialStatement> stmts) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 종목: ").append(d != null ? d.getCompanyName() : ticker)
                .append(" (").append(ticker).append(")\n");
        sb.append("## 주가지표 (출처: 자사 DB)\n")
                .append("- PER: ").append(nv(d == null ? null : d.getPer())).append("\n")
                .append("- PBR: ").append(nv(d == null ? null : d.getPbr())).append("\n")
                .append("- EPS: ").append(d == null ? "데이터 없음" : d.getEps()).append("\n")
                .append("- BPS: ").append(d == null ? "데이터 없음" : d.getBps()).append("\n\n");

        sb.append("## 기간별 재무 (출처: DART 전자공시)\n");
        for (FinancialStatement s : stmts) {
            sb.append("### ").append(s.bsnsYear()).append("년 (").append(s.reprtCode()).append(")\n")
                    .append("- 매출액: ").append(nv(s.revenue())).append("\n")
                    .append("- 영업이익: ").append(nv(s.operatingIncome())).append("\n")
                    .append("- 당기순이익: ").append(nv(s.netIncome())).append("\n")
                    .append("- 영업이익률: ").append(nv(s.operatingMargin())).append("\n")
                    .append("- 부채비율: ").append(nv(s.debtRatio())).append("\n")
                    .append("- 유동비율: ").append(nv(s.currentRatio())).append("\n")
                    .append("- 영업활동현금흐름: ").append(nv(s.cashFlowOperating())).append("\n")
                    .append("- 투자활동현금흐름: ").append(nv(s.cashFlowInvesting())).append("\n")
                    .append("- 재무활동현금흐름: ").append(nv(s.cashFlowFinancing())).append("\n")
                    .append("- ROE: ").append(nv(s.roe())).append("\n\n");
        }
        sb.append("## 매출 증감 이유: 데이터 없음 (사업보고서 본문 미수집)\n");
        return sb.toString();
    }

    private static String nv(BigDecimal v) { return v == null ? "데이터 없음" : v.toPlainString(); }

    private String readPrompt() {
        try {
            return systemPromptResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL, "프롬프트 로드 실패");
        }
    }

    private static final Pattern PERIOD_PAT = Pattern.compile("\\((\\d{4})[.\\-/](\\d{2})\\)");

    /** DART에 실제 존재하는 정기보고서 목록 (최근 6개년). */
    public List<AvailableReport> getAvailablePeriods(String ticker) {
        String corp = corpCodes.toCorpCode(ticker);
        if (corp == null) throw new BusinessException(ErrorCode.REPORT_NO_DATA);

        int thisYear = Year.now().getValue();
        String bgnDe = (thisYear - 6) + "0101";
        String endDe = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        JsonNode list = dart.fetchDisclosureList(corp, bgnDe, endDe);
        if (list == null || !list.isArray()) return List.of();

        Map<String, AvailableReport> uniq = new LinkedHashMap<>();
        for (JsonNode n : list) {
            String nm = n.path("report_nm").asText("");
            Matcher m = PERIOD_PAT.matcher(nm);
            if (!m.find()) continue;
            int year = Integer.parseInt(m.group(1));
            String reprt = switch (m.group(2)) {
                case "03" -> "11013"; // 1분기
                case "06" -> "11012"; // 반기
                case "09" -> "11014"; // 3분기
                case "12" -> "11011"; // 사업보고서(연간)
                default   -> null;
            };
            if (reprt == null) continue;
            uniq.putIfAbsent(year + "-" + reprt,
                    new AvailableReport(year, reprt, label(reprt), n.path("rcept_dt").asText("")));
        }
        return uniq.values().stream()
                .sorted(Comparator.comparing(AvailableReport::bsnsYear).reversed()
                        .thenComparing(AvailableReport::reprtCode))
                .toList();
    }

    private static String label(String reprtCode) {
        return switch (reprtCode) {
            case "11011" -> "사업보고서(연간)";
            case "11014" -> "3분기";
            case "11012" -> "반기";
            case "11013" -> "1분기";
            default -> reprtCode;
        };
    }
}