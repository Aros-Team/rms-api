-- Tenders are a child table because split payments are the normal cashier flow.
-- Change is stored on the payment header, never as a negative tender.
CREATE TABLE payment_tenders (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT        NOT NULL,
    method     VARCHAR(20)   NOT NULL,
    amount     DECIMAL(12,2) NOT NULL,
    reference  VARCHAR(100)  NULL,
    sort_order INT           NOT NULL DEFAULT 0,
    UNIQUE KEY uk_payment_tenders_order (payment_id, sort_order),
    KEY idx_payment_tenders_method (method),
    CONSTRAINT fk_payment_tenders_payment FOREIGN KEY (payment_id)
        REFERENCES payments (id) ON DELETE CASCADE,
    CONSTRAINT chk_payment_tenders_method CHECK (method IN ('CASH', 'CARD', 'TRANSFER')),
    CONSTRAINT chk_payment_tenders_amount CHECK (amount > 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
