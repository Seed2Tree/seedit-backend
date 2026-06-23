CREATE TABLE IF NOT EXISTS study_bookmark (
    sbid    BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT   NOT NULL,
    isid    BIGINT   NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_user_isid (user_id, isid),
    FOREIGN KEY (user_id) REFERENCES user_account(user_id),
    FOREIGN KEY (isid)    REFERENCES investment_study(isid)
);
