-- Payment aggregate: one immutable money snapshot per charge.
-- active_order_id is NULL for voided rows, so UNIQUE(active_order_id) allows
-- at most one non-voided payment per order while keeping the void history.
CREATE TABLE payments (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id          BIGINT        NOT NULL,
    shift_id          BIGINT        NOT NULL,
    status            VARCHAR(20)   NOT NULL DEFAULT 'COMPLETED',
    currency          VARCHAR(3)    NOT NULL DEFAULT 'COP',
    subtotal          DECIMAL(12,2) NOT NULL,
    discount_type     VARCHAR(20)   NULL,
    discount_value    DECIMAL(12,2) NULL,
    discount_amount   DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    discount_reason   VARCHAR(255)  NULL,
    tip_amount        DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_amount      DECIMAL(12,2) NOT NULL,
    tendered_amount   DECIMAL(12,2) NOT NULL,
    change_amount     DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    receipt_number    VARCHAR(40)   NULL,
    cashier_id        BIGINT        NOT NULL,
    idempotency_key   VARCHAR(100)  NOT NULL,
    created_at        DATETIME      NOT NULL,
    completed_at      DATETIME      NULL,
    voided_at         DATETIME      NULL,
    voided_by         BIGINT        NULL,
    void_reason       VARCHAR(255)  NULL,
    version           BIGINT        NOT NULL DEFAULT 0,
    active_order_id   BIGINT GENERATED ALWAYS AS
                          (CASE WHEN status = 'VOIDED' THEN NULL ELSE order_id END) STORED,
    UNIQUE KEY uk_payments_active_order (active_order_id),
    UNIQUE KEY uk_payments_idempotency (idempotency_key),
    UNIQUE KEY uk_payments_receipt (receipt_number),
    KEY idx_payments_order (order_id),
    KEY idx_payments_shift_status (shift_id, status),
    KEY idx_payments_completed_at (completed_at),
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_payments_shift FOREIGN KEY (shift_id) REFERENCES cash_shifts (id),
    CONSTRAINT fk_payments_cashier FOREIGN KEY (cashier_id) REFERENCES users (id),
    CONSTRAINT fk_payments_voided_by FOREIGN KEY (voided_by) REFERENCES users (id),
    CONSTRAINT chk_payments_status CHECK (status IN ('COMPLETED', 'VOIDED')),
    CONSTRAINT chk_payments_discount_type CHECK (
        discount_type IS NULL OR discount_type IN ('PERCENT', 'AMOUNT')),
    CONSTRAINT chk_payments_amounts CHECK (
        subtotal >= 0
        AND discount_amount >= 0
        AND tip_amount >= 0
        AND total_amount >= 0
        AND tendered_amount >= 0
        AND change_amount >= 0),
    CONSTRAINT chk_payments_discount CHECK (
        discount_amount < subtotal OR (discount_amount = 0 AND subtotal = 0))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
