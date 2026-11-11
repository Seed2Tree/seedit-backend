DROP TABLE IF EXISTS balance_history;
DROP TABLE IF EXISTS settlement;
DROP TABLE IF EXISTS user_account;
CREATE TABLE user_account (
  user_id BIGINT PRIMARY KEY,
  username VARCHAR(50), password_hash VARCHAR(255), name VARCHAR(50), birth DATE,
  email VARCHAR(100) UNIQUE, balance BIGINT, total_invested BIGINT DEFAULT 0,
  role VARCHAR(50) DEFAULT 'ROLE_USER', created_at DATETIME, updated_at DATETIME
) ENGINE=InnoDB;
CREATE TABLE balance_history (
  bhid BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL,
  amount BIGINT NOT NULL, current_balance BIGINT NOT NULL, reason_type VARCHAR(20),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES user_account(user_id) ON DELETE CASCADE,
  CONSTRAINT reject_test_amount CHECK (amount <> 13)
) ENGINE=InnoDB;
-- 기존 스키마에 운영 마이그레이션을 적용하는 경로도 함께 검증한다.
CREATE TABLE settlement (
  settlement_id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL, tid BIGINT,
  amount BIGINT NOT NULL, trade_date DATE NOT NULL, settle_date DATE NOT NULL,
  status VARCHAR(10) NOT NULL DEFAULT 'PENDING', settled_at DATETIME, created_at DATETIME,
  FOREIGN KEY (user_id) REFERENCES user_account(user_id) ON DELETE CASCADE,
  INDEX idx_settlement_pending (status, settle_date)
) ENGINE=InnoDB;
