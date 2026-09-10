CREATE TABLE payroll_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_date DATE NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL DEFAULT 1,
    unit_rate DECIMAL(12,2) NOT NULL DEFAULT 0,
    amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    notes VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    CONSTRAINT fk_payroll_events_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_payroll_events_event_type CHECK (event_type IN ('OVERTIME', 'NIGHT_SURCHARGE', 'ABSENCE', 'BONUS_ATTENDANCE', 'BONUS_PERFORMANCE', 'DEDUCTION'))
);

CREATE INDEX idx_payroll_events_user_date ON payroll_events(user_id, event_date);
CREATE INDEX idx_payroll_events_type ON payroll_events(event_type);
