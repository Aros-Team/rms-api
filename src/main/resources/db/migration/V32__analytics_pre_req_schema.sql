-- 1. ALTER orders: add party_size, open_time, close_time
ALTER TABLE orders
    ADD COLUMN party_size INT NULL,
    ADD COLUMN open_time  TIMESTAMP NULL,
    ADD COLUMN close_time TIMESTAMP NULL;

-- 2. Composite index for RevPASH range scans (analytics §5.3)
CREATE INDEX idx_orders_open_close ON orders (open_time, close_time);

-- 3. Singleton table for operating hours + alert thresholds
CREATE TABLE analytics_config (
    id                          INT PRIMARY KEY,
    default_open                TIME NOT NULL,
    default_close               TIME NOT NULL,
    lunch_start                 TIME NOT NULL,
    lunch_end                   TIME NOT NULL,
    dinner_start                TIME NOT NULL,
    dinner_end                  TIME NOT NULL,
    food_cost_deviation_pp      DECIMAL(5,2) NOT NULL DEFAULT 2.00,
    labor_cost_deviation_pp     DECIMAL(5,2) NOT NULL DEFAULT 3.00,
    sales_drop_yoy_pct          DECIMAL(5,2) NOT NULL DEFAULT 10.00,
    updated_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by                  BIGINT NULL,
    CONSTRAINT fk_analytics_config_updated_by
        FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
);

-- 4. Seed row 1 with defaults (11-23h, lunch 11-15, dinner 18-23, thresholds 2/3/10)
INSERT INTO analytics_config
    (id, default_open, default_close, lunch_start, lunch_end, dinner_start, dinner_end,
     food_cost_deviation_pp, labor_cost_deviation_pp, sales_drop_yoy_pct, updated_by)
VALUES
    (1, '11:00:00', '23:00:00', '11:00:00', '15:00:00', '18:00:00', '23:00:00',
     2.00, 3.00, 10.00, NULL);

-- DOWN (commented, not auto-run; for ops reference)
-- DROP INDEX idx_orders_open_close ON orders;
-- ALTER TABLE orders DROP COLUMN close_time, DROP COLUMN open_time, DROP COLUMN party_size;
-- DROP TABLE analytics_config;
