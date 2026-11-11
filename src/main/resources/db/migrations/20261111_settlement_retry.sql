-- 수동 적용 마이그레이션: 기존 서버의 배치를 중지한 뒤 1회 실행한다.
-- schema.sql로 새로 생성한 DB에는 실행하지 않는다. 자동 마이그레이션 도구는 사용하지 않는다.
ALTER TABLE settlement
    ADD COLUMN attempt_count INT NOT NULL DEFAULT 0,
    ADD COLUMN next_retry_at DATETIME NULL,
    ADD COLUMN attempt_token VARCHAR(36) NULL,
    ADD COLUMN lease_until DATETIME NULL,
    ADD COLUMN last_error_code VARCHAR(64) NULL,
    ADD COLUMN last_failed_at DATETIME NULL,
    ADD INDEX idx_settlement_retry (status, next_retry_at, settlement_id);
