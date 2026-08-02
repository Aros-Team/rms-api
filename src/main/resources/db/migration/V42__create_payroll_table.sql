CREATE TABLE payroll (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT        NOT NULL,
    period_year     SMALLINT      NOT NULL,
    period_month    TINYINT       NOT NULL,
    period_start    DATE          NOT NULL,
    period_end      DATE          NOT NULL,
    base_salary     DECIMAL(12,2) NOT NULL DEFAULT 0,
    bonuses         DECIMAL(12,2) NOT NULL DEFAULT 0,
    deductions      DECIMAL(12,2) NOT NULL DEFAULT 0,
    net_amount      DECIMAL(12,2) NOT NULL DEFAULT 0,
    hours_worked    DECIMAL(6,1)  NOT NULL DEFAULT 0,
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    notes           VARCHAR(500),
    registered_by   BIGINT        NOT NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_payroll_period (user_id, period_year, period_month),
    CONSTRAINT fk_payroll_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_payroll_registrar FOREIGN KEY (registered_by) REFERENCES users(id),
    CONSTRAINT chk_payroll_month CHECK (period_month BETWEEN 1 AND 12),
    CONSTRAINT chk_payroll_year  CHECK (period_year BETWEEN 2000 AND 2100),
    CONSTRAINT chk_payroll_nonneg CHECK (
        base_salary >= 0 AND bonuses >= 0 AND deductions >= 0
        AND net_amount >= 0 AND hours_worked >= 0
    )
);

CREATE INDEX idx_payroll_period ON payroll(period_year, period_month);
CREATE INDEX idx_payroll_user ON payroll(user_id);
