-- =============================================================================
-- V63: user_notifications — persistent global feed for the admin header.
-- =============================================================================
-- Producers: order lifecycle events and analytics variance alerts. The feed is
-- global (single-tenant deployment), deduplicated per producer event so retries
-- and scheduled jobs can run again without duplicating rows.

CREATE TABLE user_notifications (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL,
    severity    VARCHAR(20)  NOT NULL DEFAULT 'INFO',
    title       VARCHAR(120) NOT NULL,
    message     VARCHAR(500) NOT NULL,
    route       VARCHAR(200) NULL,
    dedupe_key  VARCHAR(200) NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at     TIMESTAMP    NULL,
    CONSTRAINT uk_user_notifications_dedupe UNIQUE (dedupe_key)
);

CREATE INDEX idx_user_notifications_created ON user_notifications (created_at);
CREATE INDEX idx_user_notifications_read ON user_notifications (read_at);

-- Backfill the alerts that already exist so the feed is useful before the next
-- nightly generation run. Later alerts arrive through NotificationUseCase.
INSERT IGNORE INTO user_notifications
    (code, severity, title, message, route, dedupe_key, created_at, read_at)
SELECT
    'ANALYTICS_ALERT',
    CASE severity
        WHEN 'RED' THEN 'CRITICAL'
        WHEN 'YELLOW' THEN 'WARNING'
        ELSE 'INFO'
    END,
    CASE type
        WHEN 'FOOD_COST_DEVIATION' THEN 'Costo de alimentos'
        WHEN 'LABOR_COST_DEVIATION' THEN 'Costo de nómina'
        WHEN 'SALES_DROP_YOY' THEN 'Caída de ventas'
        WHEN 'LOW_MARGIN_SKU_SPIKE' THEN 'Producto de bajo margen'
        WHEN 'REVPASH_DROP' THEN 'Caída de RevPASH'
        ELSE 'Alerta de estadísticas'
    END,
    message,
    '/admin/analytics',
    CONCAT('ALERT:', type, ':', metric, ':', period_key),
    detected_at,
    CASE WHEN status = 'OPEN' THEN NULL ELSE COALESCE(read_at, detected_at) END
FROM system_alerts;

-- DOWN (for operations reference, not auto-run)
-- DROP TABLE user_notifications;
