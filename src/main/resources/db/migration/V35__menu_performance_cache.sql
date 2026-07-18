-- 1. Menu performance cache table for BCG quadrant analysis
CREATE TABLE menu_performance_cache (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id          BIGINT NOT NULL,
    product_name        VARCHAR(255) NOT NULL,
    category_id         BIGINT,
    category_name       VARCHAR(255),
    period_key          VARCHAR(10) NOT NULL,
    bucket              VARCHAR(10) NOT NULL,
    units_sold          INT NOT NULL DEFAULT 0,
    revenue             DECIMAL(14,2) NOT NULL DEFAULT 0,
    recipe_cost         DECIMAL(14,2) NOT NULL DEFAULT 0,
    gross_profit_per_unit DECIMAL(14,2) NOT NULL DEFAULT 0,
    total_contribution  DECIMAL(14,2) NOT NULL DEFAULT 0,
    quadrant            VARCHAR(20) NOT NULL,
    source_version      VARCHAR(20) NOT NULL,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_mpc_product_period (product_id, period_key, bucket),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX idx_mpc_period ON menu_performance_cache (bucket, period_key);
CREATE INDEX idx_mpc_quadrant ON menu_performance_cache (quadrant);
CREATE INDEX idx_mpc_category ON menu_performance_cache (category_id);

-- DOWN (for operations reference, not auto-run)
-- DROP INDEX idx_mpc_category ON menu_performance_cache;
-- DROP INDEX idx_mpc_quadrant ON menu_performance_cache;
-- DROP INDEX idx_mpc_period ON menu_performance_cache;
-- DROP TABLE menu_performance_cache;
