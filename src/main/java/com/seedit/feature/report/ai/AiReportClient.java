package com.seedit.feature.report.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class AiReportClient {

    private final RestClient rc = RestClient.create();
    private final ObjectMapper om;

    @Value("${app.gms.key}")
    private String apiKey;

    @Value("${app.gms.base-url}")
    private String baseUrl;

    // 모델명은 설정으로 분리 (gemini-3.5-flash 등으로 교체 가능)
    @Value("${app.gms.model:gemini-3.5-flash}")
    private String model;

    // 출력 JSON Schema (inner schema object) — classpath 리소스
    @Value("classpath:prompts/report-schema.json")
    private Resource schemaResource;

    @Value("${app.gms.thinking-level:MINIMAL}")
    private String thinkingLevel;

    public AiReportClient(ObjectMapper om) { this.om = om; }

    public String generate(String systemPrompt, String userData) {
        // response_format = { type: json_schema, json_schema: { name, strict, schema } }
        Map<String, Object> responseFormat = Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "company_report",
                        "strict", true,
                        "schema", readSchema()));

        Map<String, Object> body = Map.of(
                // 캐시 적중을 위해 고정 콘텐츠는 systemInstruction에, 가변은 contents에
                "systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
                "contents", List.of(Map.of("parts", List.of(Map.of("text", userData)))),
                "generationConfig", Map.of(
                        "temperature", 0,
                        "responseMimeType", "application/json",   // JSON 출력 강제
                        "responseSchema", readSchema(),            // 구조 강제
                        "thinkingConfig", Map.of(                  // 추론 토큰 최소화
                                "thinkingLevel", thinkingLevel)));

        JsonNode res = rc.post()
                .uri(baseUrl + "/models/" + model + ":generateContent")
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-goog-api-key", apiKey)                  // ← Bearer 아님
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        return res == null ? "" :
                res.path("candidates").path(0)
                        .path("content").path("parts").path(0)
                        .path("text").asText("");
    }

    /** report-schema.json(=inner schema object)을 JsonNode로 로드. */
    private JsonNode readSchema() {
        try {
            return om.readTree(schemaResource.getContentAsString(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("report-schema.json 로드 실패", e);
        }
    }
}