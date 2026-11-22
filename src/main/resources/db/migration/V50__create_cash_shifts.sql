-- Minimal cash shift (drawer session) for the payments v1 slice.
-- One shift may be OPEN at a time; the generated open_flag enforces it in the database.
CREATE TABLE cash_shifts (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    status            VARCHAR(20)   NOT NULL DEFAULT 'OPEN',
    currency          VARCHAR(3)    NOT NULL DEFAULT 'COP',
    opened_by         BIGINT        NOT NULL,
    opened_at         DATETIME      NOT NULL,
    closed_by         BIGINT        NULL,
    closed_at         DATETIME      NULL,
    opening_float     DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    expected_cash     DECIMAL(12,2) NULL,
    counted_cash      DECIMAL(12,2) NULL,
    difference_amount DECIMAL(12,2) NULL,
    notes             VARCHAR(500)  NULL,
    open_flag         TINYINT GENERATED ALWAYS AS
                          (CASE WHEN status = 'OPEN' THEN 1 ELSE NULL END) STORED,
    UNIQUE KEY uk_cash_shifts_open (open_flag),
    KEY idx_cash_shifts_opened_at (opened_at),
    CONSTRAINT fk_cash_shifts_opened_by FOREIGN KEY (opened_by) REFERENCES users (id),
    CONSTRAINT fk_cash_shifts_closed_by FOREIGN KEY (closed_by) REFERENCES users (id),
    CONSTRAINT chk_cash_shifts_status CHECK (status IN ('OPEN', 'CLOSED')),
    CONSTRAINT chk_cash_shifts_float CHECK (opening_float >= 0),
    CONSTRAINT chk_cash_shifts_counted CHECK (counted_cash IS NULL OR counted_cash >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
