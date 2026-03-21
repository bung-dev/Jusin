-- 1. 기업 테이블
CREATE TABLE companies (
    id              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    company_id      VARCHAR(8)   UNIQUE NOT NULL COMMENT 'DART 고유 ID',
    company_name    VARCHAR(100) NOT NULL,
    stock_code      VARCHAR(6)   UNIQUE NOT NULL,
    sector          VARCHAR(50),
    list_date       DATE,
    representative  VARCHAR(100),
    address         VARCHAR(255),
    website         VARCHAR(255),
    phone_number    VARCHAR(20),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_stock_code   (stock_code),
    INDEX idx_company_name (company_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 재무제표 테이블
CREATE TABLE financial_statements (
    id                    BIGINT      PRIMARY KEY AUTO_INCREMENT,
    company_id            VARCHAR(8)  NOT NULL,
    period                VARCHAR(10) NOT NULL COMMENT '예: 2025-12 또는 2025-Q4',
    statement_type        VARCHAR(20) COMMENT 'INCOME | BALANCE | CASHFLOW',

    -- 손익계산서
    revenue               DECIMAL(20,0),
    cost_of_sales         DECIMAL(20,0),
    gross_profit          DECIMAL(20,0),
    operating_expenses    DECIMAL(20,0),
    operating_income      DECIMAL(20,0),
    non_operating_income  DECIMAL(20,0),
    non_operating_expense DECIMAL(20,0),
    income_tax_expense    DECIMAL(20,0),
    net_income            DECIMAL(20,0),
    share_count           BIGINT,

    -- 재무상태표
    current_assets        DECIMAL(20,0),
    total_assets          DECIMAL(20,0),
    current_liabilities   DECIMAL(20,0),
    total_liabilities     DECIMAL(20,0),
    equity                DECIMAL(20,0),

    -- 현금흐름표
    operating_cash_flow   DECIMAL(20,0),
    investing_cash_flow   DECIMAL(20,0),
    financing_cash_flow   DECIMAL(20,0),

    dart_report_code      VARCHAR(50),
    reported_date         DATE,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (company_id) REFERENCES companies(company_id),
    UNIQUE KEY unique_period (company_id, period, statement_type),
    INDEX idx_company_period (company_id, period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 재무 지표 테이블
CREATE TABLE financial_indicators (
    id                 BIGINT      PRIMARY KEY AUTO_INCREMENT,
    company_id         VARCHAR(8)  NOT NULL,
    period             VARCHAR(10) NOT NULL,

    per                DECIMAL(8,2) COMMENT 'NULL 허용 - 주가 미확보 시',
    roe                DECIMAL(8,2),
    debt_ratio         DECIMAL(8,2),
    eps                DECIMAL(18,0),
    eps_growth         DECIMAL(8,2),
    pbr                DECIMAL(8,2) COMMENT 'NULL 허용 - 주가 미확보 시',
    operating_margin   DECIMAL(8,2),
    current_ratio      DECIMAL(8,2),

    calculated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (company_id) REFERENCES companies(company_id),
    UNIQUE KEY unique_period (company_id, period),
    INDEX idx_company_period (company_id, period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 예측 결과 테이블
CREATE TABLE prediction_results (
    id                     BIGINT      PRIMARY KEY AUTO_INCREMENT,
    company_id             VARCHAR(8)  NOT NULL,
    period                 VARCHAR(10) NOT NULL,

    total_score            INT         COMMENT '0~100',
    signal                 VARCHAR(20) COMMENT 'UP | DOWN | NEUTRAL',
    signal_level           VARCHAR(20) COMMENT 'STRONG | WEAK',
    emoji                  VARCHAR(10),

    per_score              INT,
    roe_score              INT,
    debt_ratio_score       INT,
    eps_growth_score       INT,
    pbr_score              INT,
    operating_margin_score INT,
    current_ratio_score    INT,

    calculated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (company_id) REFERENCES companies(company_id),
    UNIQUE KEY unique_prediction (company_id, period),
    INDEX idx_company_date (company_id, calculated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. API 호출 로그 테이블
CREATE TABLE data_sync_log (
    id             BIGINT      PRIMARY KEY AUTO_INCREMENT,
    company_id     VARCHAR(8),
    sync_type      VARCHAR(20) COMMENT 'COMPANY | FINANCIAL | INDICATOR',
    status         VARCHAR(20) COMMENT 'SUCCESS | FAILED | PARTIAL',
    message        VARCHAR(255),
    synced_records INT,
    failed_records INT,
    synced_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    next_sync_at   TIMESTAMP,

    FOREIGN KEY (company_id) REFERENCES companies(company_id),
    INDEX idx_sync_time     (synced_at),
    INDEX idx_company_sync  (company_id, synced_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
