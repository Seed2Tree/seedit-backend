-- ====================================================================
-- 0. 데이터베이스 생성 및 초기화
-- ====================================================================
CREATE DATABASE IF NOT EXISTS seedit;
USE seedit;

-- 의존성을 고려한 기존 테이블 삭제 (자식 테이블부터 삭제)
DROP TABLE IF EXISTS study_bookmark;
DROP TABLE IF EXISTS investment_study;
DROP TABLE IF EXISTS ai_report;
DROP TABLE IF EXISTS news_content;
DROP TABLE IF EXISTS watchlist;
DROP TABLE IF EXISTS diary;
DROP TABLE IF EXISTS reason;
DROP TABLE IF EXISTS settlement;
DROP TABLE IF EXISTS trade;
DROP TABLE IF EXISTS portfolio;
DROP TABLE IF EXISTS stock_detail;
DROP TABLE IF EXISTS stock;
DROP TABLE IF EXISTS user_level;
DROP TABLE IF EXISTS level_definition;
DROP TABLE IF EXISTS balance_history;
DROP TABLE IF EXISTS user_account;

-- 테이블 생성
-- ====================================================================
-- 1. 회원 및 자산/레벨 도메인 (1차 스프린트 필수)
-- ====================================================================
CREATE TABLE user_account (
    user_id        BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 고유 ID',
    username       VARCHAR(50) NOT NULL COMMENT '로그인 아이디',
    password_hash  VARCHAR(255) NOT NULL COMMENT '암호화된 비밀번호',
    name           VARCHAR(50) NOT NULL COMMENT '실명',
    birth          DATE NOT NULL COMMENT '생년월일',
    email          VARCHAR(100) UNIQUE NOT NULL COMMENT '이메일',
    balance        BIGINT NOT NULL DEFAULT 5000000 COMMENT '현재 잔액 (모의투자 투자금)',
    total_invested BIGINT NOT NULL DEFAULT 0 COMMENT '총 투자 원금 누계',
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '가입일시',
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정일시',
    role 		   VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER' COMMENT '역할'
) COMMENT '사용자 계정 및 잔액 정보';

CREATE TABLE balance_history (
    bhid            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '이력 고유 ID',
    user_id 		BIGINT NOT NULL COMMENT '사용자 ID',
    amount 			BIGINT NOT NULL COMMENT '변동 금액(양수 = 입금, 음수=출금/매수',
    current_balance BIGINT NOT NULL COMMENT '변동 후 최종 잔액',
    reason_type 	VARCHAR(20) NOT NULL COMMENT '변동 유형:BUY/SELL',
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '변동 일시',
    FOREIGN KEY (user_id) REFERENCES user_account(user_id) ON DELETE CASCADE
) COMMENT '잔액 변동 이력';

CREATE TABLE level_definition (
    level  		   INT PRIMARY KEY COMMENT '레벨 번호',
    level_name     VARCHAR(30) NOT NULL COMMENT '레벨 명칭',
    required_point INT NOT NULL COMMENT '해당 레벨 달성에 필요한 최소 포인트',
    benefits 	   TEXT COMMENT '레벨 혜택 설명'
) COMMENT '레벨 정의';

CREATE TABLE user_level (
    user_id        BIGINT PRIMARY KEY COMMENT '사용자 고유 ID',
    level  		   INT NOT NULL DEFAULT 1 COMMENT '현재 레벨',
    point 		   INT NOT NULL DEFAULT 0 COMMENT '현재 누적 포인트',
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 갱신일시',
    FOREIGN KEY (user_id) REFERENCES user_account(user_id) ON DELETE CASCADE,
    FOREIGN KEY (level) REFERENCES level_definition(level)
) COMMENT '사용자 현재 레벨 및 포인트';

-- ====================================================================
-- 0. 레벨 정의 (씨앗 → 나무 성장 테마, 6단계)
--    이미 존재하는 level 행은 이름/기준점/혜택을 덮어씀
-- ====================================================================
INSERT INTO level_definition (level, level_name, required_point, benefits) VALUES
(1, '씨앗',         0,    '기본 모의투자 자격 부여'),
(2, '새싹 투자자',   100,  '투자 가설 태그 전체 해금'),
(3, '묘목 트레이더', 300,  '일지 회고 통계 보기'),
(4, '든든한 가지',   700,  '관심종목 무제한 등록'),
(5, '큰나무 투자자', 1500, '프로필 뱃지 + AI 리포트 우선 제공'),
(6, '숲의 현자',     3000, '명예 뱃지 + 전체 혜택')
ON DUPLICATE KEY UPDATE
level_name     = VALUES(level_name),
required_point = VALUES(required_point),
benefits       = VALUES(benefits);

-- ====================================================================
-- 2. 주식 종목 및 시세 도메인 (모의투자 필수 부모 테이블)
-- ====================================================================
CREATE TABLE stock (
    sid                   BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '종목 고유 ID',
    company_name          VARCHAR(100) NOT NULL COMMENT '기업명',
    ticker                VARCHAR(20) UNIQUE NOT NULL COMMENT '종목 코드 (예: 005930)',
    description           TEXT COMMENT '기업 설명',
    sector                VARCHAR(50) COMMENT '업종/섹터',
    market                VARCHAR(20) COMMENT '상장 시장 (KOSPI/KOSDAQ)',
    market_cap            BIGINT COMMENT '시가총액 (억원) - KIS: hts_avls',
    foreign_ownership_pct DECIMAL(5,2) COMMENT '외국인 보유 비율 (%) - KIS: frgn_hldn_qty_smtl_pcnt',
    per                   DECIMAL(6,2) COMMENT 'PER - KIS: per',
    eps                   INT COMMENT 'EPS 원 - KIS: eps',
    pbr                   DECIMAL(5,2) COMMENT 'PBR - KIS: pbr',
    bps                   INT COMMENT 'BPS 원 - KIS: bps'
) COMMENT '종목 기본 정보';

-- 종목 디테일 테이블 생성해주기
CREATE TABLE stock_detail (
    sdid             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '시세 고유 ID',
    sid              BIGINT COMMENT '종목 ID',
    open_price       DECIMAL(12,2) NOT NULL COMMENT '시가 - KIS: stck_oprc',
    close_price      DECIMAL(12,2) COMMENT '종가(장중에는 NULL, 마감 후 확정) - KIS: stck_clpr',
    current_price    DECIMAL(12,2) NOT NULL COMMENT '현재가(장중 실시간 갱신) - KIS: stck_prpr',
    high_price       DECIMAL(12,2) COMMENT '고가 - KIS: stck_hgpr',
    low_price        DECIMAL(12,2) COMMENT '저가 - KIS: stck_lwpr',
    prev_close_price DECIMAL(12,2) COMMENT '전일 종가 - KIS: stck_prdy_clpr',
    volume           BIGINT DEFAULT 0 COMMENT '거래량 - KIS: acml_vol',
    trading_value    BIGINT COMMENT '거래대금 원 - KIS: acml_tr_pbmn',
    w52_high_price   DECIMAL(12,2) COMMENT '52주 최고가 - KIS: w52_hgpr',
    w52_low_price    DECIMAL(12,2) COMMENT '52주 최저가 - KIS: w52_lwpr',
    trade_date       DATE NOT NULL COMMENT '거래일 기준',
    FOREIGN KEY (sid) REFERENCES stock(sid) ON DELETE CASCADE,
    UNIQUE KEY uq_sid_trade_date (sid, trade_date) COMMENT '동일 종목 하루 중복 시세 방지'
) COMMENT '종목 일별 시세';

-- ====================================================================
-- 3. 거래 및 포트폴리오 도메인 (1차 스프린트 핵심: 매수/매도 API 연동)
-- ====================================================================
CREATE TABLE portfolio (
    pid            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '보유종목 고유 ID',
    user_id 	   BIGINT NOT NULL COMMENT '사용자 ID',
    sid 		   BIGINT NOT NULL COMMENT '종목 ID',
    quantity 	   INT NOT NULL DEFAULT 0 COMMENT '보유 수량',
    avg_price 	   DECIMAL(12,2) NOT NULL COMMENT '평균 매입 단가(평단가)',
    total_amount   DECIMAL(15,2) NOT NULL COMMENT '총 보유금액(보유수량x평단가)',
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 갱신일시',
    FOREIGN KEY (user_id) REFERENCES user_account(user_id) ON DELETE CASCADE,
    FOREIGN KEY (sid) REFERENCES stock(sid),
	UNIQUE KEY uq_user_id_sid (user_id, sid) COMMENT '동일 사용자 종목 중복 보유 방지'
) COMMENT '사용자 보유 종목';

CREATE TABLE trade (
    tid            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '거래 고유 ID',
    user_id 	   BIGINT NOT NULL COMMENT '사용자 ID',
    sid 		   BIGINT NOT NULL COMMENT '종목 ID',
    sdid 		   BIGINT NOT NULL COMMENT '종목 일별 시세 ID',
    trade_type 		   VARCHAR(10) NOT NULL COMMENT '거래 유형:BUY/SELL',
    trade_price 	   	   DECIMAL(12,2) NOT NULL COMMENT '체결 단가',
    quantity 	   INT NOT NULL COMMENT '거래 수량',
    total_amount   DECIMAL(15,2) NOT NULL COMMENT '거래 총액(거래수량x체결단가)',
    remaining_balance  DECIMAL(15,2) NOT NULL COMMENT '거래 후 잔액',
    trade_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '거래 일시',
    FOREIGN KEY (user_id) REFERENCES user_account(user_id) ON DELETE CASCADE,
    FOREIGN KEY (sid) REFERENCES stock(sid),
    FOREIGN KEY (sdid) REFERENCES stock_detail(sdid)
) COMMENT '매수/매도 거래 내역';

CREATE TABLE settlement (
  settlement_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id    BIGINT NOT NULL,
  tid        BIGINT,                          -- 어떤 매도 거래에서 발생
  amount     BIGINT NOT NULL,                 -- 정산 예정 금액(매도대금)
  trade_date DATE   NOT NULL,
  settle_date DATE  NOT NULL,                 -- 영업일 +2
  status     VARCHAR(10) NOT NULL DEFAULT 'PENDING',  -- PENDING / SETTLED
  settled_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES user_account(user_id) ON DELETE CASCADE,
  INDEX idx_settlement_pending (status, settle_date)   -- 스케줄러 조회용
);

CREATE TABLE reason (
    rid            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '가설 고유 ID',
    user_id 	   BIGINT NOT NULL COMMENT '사용자 ID',
    tid 		   BIGINT NULL COMMENT '연관 거래 ID',
    reason_type 		   VARCHAR(10) NOT NULL COMMENT '가설 유형:BUY/SELL',
    reason_date    DATE NOT NULL COMMENT '가설 작성일',
	reason_tag 		   VARCHAR(50) COMMENT '필터용 태그',
    reason_text    TEXT COMMENT '가설 내용(자유 텍스트)',
    is_verified    BOOLEAN DEFAULT false COMMENT '가설 검증 완료 여부',
    is_deleted 	   BOOLEAN DEFAULT false COMMENT '소프트 삭제 여부',
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
	FOREIGN KEY (user_id) REFERENCES user_account(user_id) ON DELETE CASCADE,
    FOREIGN KEY (tid) REFERENCES trade(tid) ON DELETE SET NULL
) COMMENT '투자 가설 이유';

CREATE TABLE diary (
    did            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '일기 고유 ID',
    user_id 	   BIGINT NOT NULL COMMENT '사용자 ID',
    diary_date     DATE NOT NULL COMMENT '일기 작성 날짜',
	content	 	   TEXT COMMENT '오늘의 일지 내용',
    ai_feedback    TEXT COMMENT 'AI 피드백',
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '생성 일시',
	FOREIGN KEY (user_id) REFERENCES user_account(user_id) ON DELETE CASCADE,
	UNIQUE KEY uq_user_id_diary_date (user_id, diary_date) COMMENT '하루에 일기 1개 제한'
) COMMENT '투자 일기';

-- 관심 종목 테이블 추가하기 
CREATE TABLE watchlist (
    wid            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '즐겨찾기 고유 ID',
    user_id		   BIGINT NOT NULL COMMENT '사용자 ID',
    sid 		   BIGINT NOT NULL COMMENT '종목 ID',
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '등록 일시',
    FOREIGN KEY (user_id) REFERENCES user_account(user_id) ON DELETE CASCADE,
    FOREIGN KEY (sid) REFERENCES stock(sid) ON DELETE CASCADE,
    UNIQUE KEY uq_user_id_sid_watch (user_id, sid) COMMENT '동일 종목 중복 즐겨찾기 방지'
) COMMENT '관심 종목';

-- ====================================================================
-- 4. 콘텐츠 및 학습 도메인
-- ====================================================================
-- 영상 콘텐츠 테이블 추가하기 
CREATE TABLE investment_study (
    isid         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '콘텐츠 고유 ID',
    title        VARCHAR(200) NOT NULL COMMENT '콘텐츠 제목',
    youtube_url  VARCHAR(500) NOT NULL COMMENT '유튜브 링크',
    description  TEXT COMMENT '콘텐츠 설명',
    category     VARCHAR(50) COMMENT '분류 (기초지식 / 기업분석 / 시장분석 등)',
    thumbnail    VARCHAR(500) COMMENT '썸네일 이미지 URL',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시'
) COMMENT '공용 투자 학습 콘텐츠';

-- 콘텐츠 즐겨찾기(북마크) 추가하기 
CREATE TABLE study_bookmark (
    sbid       BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '즐겨찾기 고유 ID',
    user_id     BIGINT NOT NULL COMMENT '사용자 ID',
    isid       BIGINT NOT NULL COMMENT '콘텐츠 ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '즐겨찾기 등록일시',
    FOREIGN KEY (user_id) REFERENCES user_account(user_id) ON DELETE CASCADE,
    FOREIGN KEY (isid) REFERENCES investment_study(isid) ON DELETE CASCADE,
    UNIQUE KEY uq_user_id_isid (user_id, isid) COMMENT '동일 콘텐츠 중복 북마크 방지'
) COMMENT '사용자별 투자 공부 즐겨찾기';

-- 뉴스 콘텐츠 테이블 추가하기 
CREATE TABLE news_content  (
    nid         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '뉴스 콘텐츠 ID',
    news_title   VARCHAR(255) NOT NULL COMMENT '수집한 뉴스 헤드라인',
    news_url     VARCHAR(500) UNIQUE COMMENT '원본 뉴스 링크',
    press 		VARCHAR(50) NOT NULL COMMENT '언론사',
    published_at TIMESTAMP COMMENT '뉴스 발행 일자',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시'
) COMMENT '뉴스 콘텐츠';

-- AI 분석 리포트 캐시 테이블
-- (stock_code, bsns_year, reprt_code) 단위로 1건. 분기별 저장/업데이트.
CREATE TABLE IF NOT EXISTS ai_report (
    arid        BIGINT       NOT NULL AUTO_INCREMENT,
    rcept_no 	VARCHAR(14), 
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


CREATE TABLE IF NOT EXISTS study_comment (
    scid       BIGINT AUTO_INCREMENT PRIMARY KEY,
    isid       BIGINT  NOT NULL,
    user_id    BIGINT  NOT NULL,
    content    TEXT    NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (isid)    REFERENCES investment_study(isid) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user_account(user_id)  ON DELETE CASCADE
);

