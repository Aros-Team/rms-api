-- Settings lane (P4-SET-07). Owns V57 per docs/PLAN.md.
-- Global notification preferences singleton.
CREATE TABLE notification_preferences (
    id                       BIGINT    NOT NULL PRIMARY KEY,
    kitchen_sound_enabled    BOOLEAN   NOT NULL DEFAULT TRUE,
    kitchen_haptics_enabled  BOOLEAN   NOT NULL DEFAULT FALSE,
    low_stock_alerts_enabled BOOLEAN   NOT NULL DEFAULT TRUE,
    order_alerts_enabled     BOOLEAN   NOT NULL DEFAULT TRUE,
    daily_summary_enabled    BOOLEAN   NOT NULL DEFAULT FALSE,
    updated_at               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO notification_preferences (id) VALUES (1);
