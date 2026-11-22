-- Explicit drawer ledger (petty cash in/out). The table ships with v1 so the
-- cash-shift totals already understand movements; the endpoint is v1.1.
CREATE TABLE cash_movements (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    shift_id   BIGINT        NOT NULL,
    type       VARCHAR(20)   NOT NULL,
    amount     DECIMAL(12,2) NOT NULL,
    reason     VARCHAR(255)  NOT NULL,
    created_by BIGINT        NOT NULL,
    created_at DATETIME      NOT NULL,
    KEY idx_cash_movements_shift (shift_id),
    CONSTRAINT fk_cash_movements_shift FOREIGN KEY (shift_id) REFERENCES cash_shifts (id),
    CONSTRAINT fk_cash_movements_user FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT chk_cash_movements_type CHECK (type IN ('PAID_IN', 'PAID_OUT')),
    CONSTRAINT chk_cash_movements_amount CHECK (amount > 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
