package com.seedit.feature.diary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seedit.feature.diary.domain.Diary;
import com.seedit.feature.diary.dto.request.DiaryCreateRequest;
import com.seedit.feature.diary.dto.request.DiaryUpdateRequest;
import com.seedit.feature.diary.dto.response.DiaryDetailResponse;
import com.seedit.feature.diary.dto.response.DiaryListItem;
import com.seedit.feature.diary.repository.DiaryRepository;
import com.seedit.feature.stock.repository.StockRepository;
import com.seedit.feature.trade.dto.response.TradeHistoryResponse;
import com.seedit.feature.trade.repository.TradeRepository;
import com.seedit.feature.user.domain.UserAccount;
import com.seedit.feature.user.repository.UserAccountRepository;
import com.seedit.global.error.BusinessException;
import com.seedit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final TradeRepository tradeRepository;
    private final StockRepository stockRepository;
    private final UserAccountRepository userAccountRepository;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Value("${app.ai.api-key}")
    private String apiKey;

    @Value("${app.ai.api-url}")
    private String apiUrl;

    private static final Map<String, String> REASON_TAG_LABELS = Map.of(
            "earnings", "실적 기대",
            "news",     "호재 뉴스",
            "long",     "장기 투자",
            "rebound",  "단기 반등",
            "chart",    "차트 패턴",
            "etc",      "기타",
            "target",   "목표가 도달",
            "stoploss", "손절",
            "bad-news", "악재",
            "switch",   "다른 종목으로"
    );

    private static final String SYSTEM_PROMPT = """
            당신은 모의투자 입문자를 위한 따뜻한 투자 코치입니다.
            아래 투자 데이터를 분석하고 반드시 아래 JSON 형식으로만 응답하세요.

            {
              "score": (0.0~5.0 사이 소수점 한 자리, 투자 판단 및 복기의 합리성을 고려한 점수),
              "good": (잘한 점 50자 내외, ~요 말투),
              "improve": (생각해볼 점과 보완 방향 60자 내외, ~요 말투),
              "action": (격려와 다음 액션 제안 40자 내외, ~요 말투)
            }

            채점 기준:
            - 매수/매도 근거가 구체적인가
            - 계획이나 기준이 있는가
            - 일지에 자기 성찰이 담겨 있는가

            말투는 친근하고 따뜻하게 (~요, ~이에요).
            """;

    public List<DiaryListItem> getList(String email) {
        Long userId = getUserId(email);
        return diaryRepository.findAllByUserId(userId).stream()
                .map(DiaryListItem::from)
                .toList();
    }

    public List<LocalDate> getCalendar(String email, int year, int month) {
        Long userId = getUserId(email);
        return diaryRepository.findDatesByUserIdAndMonth(userId, year, month);
    }

    public DiaryDetailResponse getByDate(String email, LocalDate date) {
        Long userId = getUserId(email);
        return diaryRepository.findByUserIdAndDate(userId, date)
                .map(DiaryDetailResponse::from)
                .orElse(null);
    }

    public DiaryDetailResponse create(String email, DiaryCreateRequest request) {
        Long userId = getUserId(email);
        if (diaryRepository.countByUserIdAndDate(userId, request.diaryDate()) > 0) {
            throw new BusinessException(ErrorCode.DIARY_ALREADY_WRITTEN_TODAY, "해당 날짜에 이미 일지를 작성했습니다.");
        }
        diaryRepository.insert(userId, request.diaryDate(), request.content());
        return diaryRepository.findByUserIdAndDate(userId, request.diaryDate())
                .map(DiaryDetailResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_INTERNAL, "일지 저장에 실패했습니다."));
    }

    public DiaryDetailResponse update(String email, Long did, DiaryUpdateRequest request) {
        Long userId = getUserId(email);
        diaryRepository.findByDidAndUserId(did, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "일지를 찾을 수 없습니다."));
        diaryRepository.update(did, userId, request.content());
        return diaryRepository.findByDidAndUserId(did, userId)
                .map(DiaryDetailResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_INTERNAL, "일지 수정에 실패했습니다."));
    }

    public void delete(String email, Long did) {
        Long userId = getUserId(email);
        diaryRepository.findByDidAndUserId(did, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "일지를 찾을 수 없습니다."));
        diaryRepository.delete(did, userId);
    }

    public DiaryDetailResponse generateFeedback(String email, LocalDate date) {
        Long userId = getUserId(email);

        List<TradeHistoryResponse> trades = tradeRepository.findAllByUserIdAndDate(userId, date);
        Optional<Diary> diaryOpt = diaryRepository.findByUserIdAndDate(userId, date);

        boolean hasDiaryContent = diaryOpt
                .map(d -> d.getContent() != null && !d.getContent().isBlank())
                .orElse(false);
        if (trades.isEmpty() && !hasDiaryContent) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION, "피드백을 생성할 투자 내용이 없습니다.");
        }

        if (diaryOpt.map(d -> d.getAiFeedback() != null).orElse(false)) {
            throw new BusinessException(ErrorCode.DIARY_FEEDBACK_ALREADY_EXISTS, "이미 AI 피드백이 생성되었습니다.");
        }

        String prompt = buildPrompt(diaryOpt.orElse(null), trades, date);
        String aiFeedback = callGmsApi(prompt);

        if (diaryOpt.isEmpty()) {
            diaryRepository.insert(userId, date, "");
        }

        Diary diary = diaryRepository.findByUserIdAndDate(userId, date)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_INTERNAL, "일지를 찾을 수 없습니다."));

        diaryRepository.updateAiFeedback(diary.getDid(), userId, aiFeedback);

        return diaryRepository.findByDidAndUserId(diary.getDid(), userId)
                .map(DiaryDetailResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_INTERNAL, "피드백 저장에 실패했습니다."));
    }

    private String buildPrompt(Diary diary, List<TradeHistoryResponse> trades, LocalDate date) {
        StringBuilder sb = new StringBuilder();
        sb.append("날짜: ").append(date.getYear()).append("년 ")
          .append(date.getMonthValue()).append("월 ")
          .append(date.getDayOfMonth()).append("일\n\n");

        if (!trades.isEmpty()) {
            sb.append("[거래 내역]\n");
            for (TradeHistoryResponse trade : trades) {
                boolean isBuy = "BUY".equals(trade.tradeType().name());
                sb.append("- ").append(trade.companyName())
                  .append(" ").append(isBuy ? "매수" : "매도")
                  .append(" | ").append(Math.abs(trade.quantity())).append("주")
                  .append(" | 체결가 ").append(String.format("%,d", trade.tradePrice())).append("원");

                if (trade.reasonTag() != null) {
                    String tagLabel = REASON_TAG_LABELS.getOrDefault(trade.reasonTag(), trade.reasonTag());
                    sb.append(" | ").append(isBuy ? "가설: " : "이유: ").append(tagLabel);
                }
                if (trade.reasonText() != null && !trade.reasonText().isBlank()) {
                    sb.append(" | \"").append(trade.reasonText()).append("\"");
                }
                stockRepository.findChangeRateBySidAndDate(trade.sid(), date).ifPresent(rate -> {
                    String sign = rate.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
                    sb.append(" | 당일 등락률: ").append(sign).append(rate).append("%");
                });
                sb.append("\n");
            }
        }

        if (diary != null && diary.getContent() != null && !diary.getContent().isBlank()) {
            sb.append("\n[오늘의 일지]\n").append(diary.getContent());
        }

        return sb.toString();
    }

    private String callGmsApi(String userPrompt) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("system_instruction", Map.of("parts", List.of(Map.of("text", SYSTEM_PROMPT))));
            body.put("contents", List.of(Map.of("parts", List.of(Map.of("text", userPrompt)))));
            body.put("generationConfig", Map.of("response_mime_type", "application/json"));

            String responseStr = RestClient.create()
                    .post()
                    .uri(apiUrl)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = OBJECT_MAPPER.readTree(responseStr);
            return root.path("candidates").get(0)
                       .path("content").path("parts").get(0)
                       .path("text").asText();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_GENERATION_FAILED, "AI 피드백 생성에 실패했습니다.");
        }
    }

    private Long getUserId(String email) {
        return userAccountRepository.findUserByEmail(email)
                .map(UserAccount::getUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
}
