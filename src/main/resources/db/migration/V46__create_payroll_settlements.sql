CREATE TABLE payroll_settlements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payroll_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    settlement_type VARCHAR(20) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    notes VARCHAR(500) NULL,
    settled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    settled_by VARCHAR(100) NULL,
    CONSTRAINT fk_payroll_settlements_payroll FOREIGN KEY (payroll_id) REFERENCES payroll(id) ON DELETE CASCADE,
    CONSTRAINT fk_payroll_settlements_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_payroll_settlements_type CHECK (settlement_type IN ('DAILY', 'WEEKLY', 'BIWEEKLY', 'MONTHLY'))
);

CREATE INDEX idx_payroll_settlements_payroll ON payroll_settlements(payroll_id);
CREATE INDEX idx_payroll_settlements_user_date ON payroll_settlements(user_id, period_start, period_end);
