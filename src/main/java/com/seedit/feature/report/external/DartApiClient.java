package com.seedit.feature.report.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class DartApiClient {

    private static final Logger log = LoggerFactory.getLogger(DartApiClient.class);

    private final DartProperties props;
    private final ObjectMapper om;
    private final RestClient rc = RestClient.create();

    public DartApiClient(DartProperties props, ObjectMapper om){
        this.props = props;
        this.om = om;
    }

    /** 단일회사 전체 재무제표. fsDiv : CFS(연결)/OFS(개별). list 노드 반환 */
    public JsonNode fetchSingleAcntAll(String corpCode, String bsnsYear,
                                       String reprtCode, String fsDiv){
        JsonNode root = get("/fnlttSinglAcntAll.json",corpCode, bsnsYear, reprtCode, "fs_div",fsDiv);
        return root == null ? null : root.path("list");
    }

    /**
     * 주요 재무지표 idxClCod : M210000~M240000
     */
    public JsonNode fetchSingleIndx(String corpCode, String bsnsYear,
                                    String reprtCode, String idxClCode){
        JsonNode root = get("/fnlttSinglIndx.json", corpCode, bsnsYear, reprtCode,
                "idx_cl_code", idxClCode);
        return root == null ? null : root.path("list");
    }

    /**
     * 기업개황(company.json). corp_code의 실제 stock_code·corp_name 확인용(매핑 검증).
     * 반환 루트에 corp_name / stock_name / stock_code 등이 들어있음. 실패 시 null.
     */
    public JsonNode fetchCompany(String corpCode){
        try{
            String body = rc.get()
                    .uri(props.baseUrl() + "/company.json?crtfc_key={k}&corp_code={c}",
                            props.apiKey(), corpCode)
                    .retrieve()
                    .body(String.class);
            if(body == null || body.isBlank()) return null;
            JsonNode root = om.readTree(body);
            String status = root.path("status").asText("");
            if(!"000".equals(status)){
                log.warn("DART /company.json status={} message='{}' (corp={})",
                        status, root.path("message").asText(""), corpCode);
                return null;
            }
            return root;
        } catch (Exception e){
            log.warn("DART /company.json 호출 예외: {} (corp={})", e.toString(), corpCode);
            return null;
        }
    }

    /** 정기공시 목록(list.json). pblntf_ty=A(정기공시). list 노드 반환, 실패 시 null. */
    public JsonNode fetchDisclosureList(String corpCode, String bgnDe, String endDe){
        try{
            String body = rc.get()
                    .uri(props.baseUrl() + "/list.json"
                                    + "?crtfc_key={k}&corp_code={c}&bgn_de={b}&end_de={e}"
                                    + "&pblntf_ty=A&page_count=100",
                            props.apiKey(), corpCode, bgnDe, endDe)
                    .retrieve()
                    .body(String.class);
            if(body == null || body.isBlank()) return null;
            JsonNode root = om.readTree(body);
            String status = root.path("status").asText("");
            if(!"000".equals(status)){
                log.warn("DART /list.json status={} message='{}' (corp={})",
                        status, root.path("message").asText(""), corpCode);
                return null;
            }
            return root.path("list");
        } catch (Exception e){
            log.warn("DART /list.json 호출 예외: {} (corp={})", e.toString(), corpCode);
            return null;
        }
    }

    private JsonNode get(String path, String corpCode, String bsnsYear,
                         String reprtCode, String extraKey, String extraVal){
        try{
            String body = rc.get()
                    .uri(props.baseUrl() + path
                                    + "?crtfc_key={k}&corp_code={c}&bsns_year={y}"
                                    + "&reprt_code={r}&{ek}={ev}",
                            props.apiKey(), corpCode, bsnsYear, reprtCode, extraKey, extraVal)
                    .retrieve()
                    .body(String.class);
            if(body == null || body.isBlank()){
                log.warn("DART {} 빈 응답 (corp={}, year={}, reprt={}, {}={})",
                        path, corpCode, bsnsYear, reprtCode, extraKey, extraVal);
                return null;
            }
            JsonNode root = om.readTree(body);
            String status = root.path("status").asText("");
            if(!"000".equals(status)){
                // ▼ 실패 원인 노출: 020=사용한도초과, 013=데이터없음, 010/011=키오류, 100=필드오류, 800=점검
                log.warn("DART {} status={} message='{}' (corp={}, year={}, reprt={}, {}={})",
                        path, status, root.path("message").asText(""),
                        corpCode, bsnsYear, reprtCode, extraKey, extraVal);
                return null;
            }
            return root;
        } catch (Exception e){
            log.warn("DART {} 호출 예외: {} (corp={}, year={}, reprt={}, {}={})",
                    path, e.toString(), corpCode, bsnsYear, reprtCode, extraKey, extraVal);
            return null;
        }
    }
}