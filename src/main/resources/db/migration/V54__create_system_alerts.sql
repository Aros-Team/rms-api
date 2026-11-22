-- =============================================================================
-- V54: system_alerts — persistent variance alerts for the analytics module.
-- =============================================================================
-- Owned by the Analytics BE lane. V50-V53 belong to payments, V56+ to settings
-- and roles, so this is the only alert migration.
--
-- Dedupe key: (type, metric, period_key). Re-running the generation rules can
-- never duplicate an alert for the same period.

CREATE TABLE system_alerts (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    severity       VARCHAR(10)  NOT NULL,
    type           VARCHAR(50)  NOT NULL,
    metric         VARCHAR(100) NOT NULL,
    baseline       DECIMAL(14,4) NULL,
    current        DECIMAL(14,4) NULL,
    deviation_pct  DECIMAL(10,2) NULL,
    period_key     VARCHAR(10)  NOT NULL,
    period_bucket  VARCHAR(10)  NOT NULL DEFAULT 'monthly',
    message        VARCHAR(500) NOT NULL,
    detected_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status         VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    read_at        TIMESTAMP    NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_system_alerts_rule UNIQUE (type, metric, period_key)
);

CREATE INDEX idx_alerts_status_detected ON system_alerts (status, detected_at);
CREATE INDEX idx_alerts_severity ON system_alerts (severity);
CREATE INDEX idx_alerts_period ON system_alerts (period_bucket, period_key);

-- Demo rows so the alerts module is never empty before the first nightly job.
-- The Seed lane rebuilds these three rows on reset (seed-spec §7.5).
INSERT IGNORE INTO system_alerts
    (severity, type, metric, baseline, current, deviation_pct, period_key, period_bucket,
     message, detected_at, status, read_at)
VALUES
    ('RED', 'FOOD_COST_DEVIATION', 'food_cost_pct', 30.6200, 32.7000, 6.79, '2026-10', 'monthly',
     'Food cost is 2.08 pp above the 12-month moving average', '2026-11-20 04:00:00', 'OPEN', NULL),
    ('RED', 'REVPASH_DROP', 'revpash', 12000.0000, 9000.0000, -25.00, '2026-W45', 'weekly',
     'RevPASH dropped 25.00% versus the 4-week moving average', '2026-11-19 04:00:00', 'OPEN', NULL),
    ('YELLOW', 'LOW_MARGIN_SKU_SPIKE', 'dog_units', 50.0000, 80.0000, 60.00, '2026-W45', 'weekly',
     'Low-margin SKU units grew 60.00% week over week', '2026-11-18 04:00:00', 'READ',
     '2026-11-18 09:15:00');

-- DOWN (for operations reference, not auto-run)
-- DROP TABLE system_alerts;
