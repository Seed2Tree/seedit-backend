-- AI 분석 리포트 캐시 테이블
-- (stock_code, bsns_year, reprt_code) 단위로 1건. 분기별 저장/업데이트.
CREATE TABLE IF NOT EXISTS ai_report (
                                         arid        BIGINT       NOT NULL AUTO_INCREMENT,
                                         stock_code  VARCHAR(6)   NOT NULL,                 -- 종목코드(=ticker)
    bsns_year   INT          NOT NULL,                 -- 사업연도 (예: 2025)
    reprt_code  VARCHAR(5)   NOT NULL,                 -- 11011 사업/11012 반기/11013 1Q/11014 3Q
    content     JSON         NOT NULL,                 -- 모델이 생성한 구조화 리포트(JSON)
    model       VARCHAR(50)  NULL,                     -- 생성 모델명(감사용)
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (arid),
    UNIQUE KEY uq_report (stock_code, bsns_year, reprt_code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;