DROP TABLE IF EXISTS stock_detail;
DROP TABLE IF EXISTS stock;

CREATE TABLE stock (
    sid                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_name          VARCHAR(100) NOT NULL,
    ticker                VARCHAR(20) UNIQUE NOT NULL,
    description           TEXT,
    sector                VARCHAR(50),
    market                VARCHAR(20),
    market_cap            BIGINT,
    foreign_ownership_pct DECIMAL(5,2),
    per                   DECIMAL(6,2),
    eps                   INT,
    pbr                   DECIMAL(5,2),
    bps                   INT
);

CREATE TABLE stock_detail (
    sdid             BIGINT AUTO_INCREMENT PRIMARY KEY,
    sid              BIGINT,
    open_price       DECIMAL(12,2) NOT NULL,
    close_price      DECIMAL(12,2),
    current_price    DECIMAL(12,2) NOT NULL,
    high_price       DECIMAL(12,2),
    low_price        DECIMAL(12,2),
    prev_close_price DECIMAL(12,2),
    volume           BIGINT DEFAULT 0,
    trading_value    BIGINT,
    w52_high_price   DECIMAL(12,2),
    w52_low_price    DECIMAL(12,2),
    trade_date       DATE NOT NULL,
    FOREIGN KEY (sid) REFERENCES stock(sid) ON DELETE CASCADE,
    UNIQUE KEY uq_sid_trade_date (sid, trade_date)
);
