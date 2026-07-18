-- 1. Add food_type to supply_categories for COGS breakdown
ALTER TABLE supply_categories
    ADD COLUMN food_type VARCHAR(20) NOT NULL DEFAULT 'OTHER';

-- 2. Seed existing categories
UPDATE supply_categories
    SET food_type = 'FOOD'
    WHERE name IN ('Proteínas', 'Vegetales', 'Lácteos', 'Frutas');
UPDATE supply_categories
    SET food_type = 'BEVERAGE'
    WHERE name LIKE '%Bebida%' AND name NOT LIKE '%Alcohol%' AND name NOT LIKE '%Licor%';
UPDATE supply_categories
    SET food_type = 'ALCOHOL'
    WHERE name LIKE '%Alcohol%' OR name LIKE '%Licor%';

-- 3. Monthly financial summary table for prime-cost rollups
CREATE TABLE monthly_financial_summary (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    period_key      VARCHAR(10) NOT NULL,
    bucket          VARCHAR(10) NOT NULL,
    net_sales       DECIMAL(14,2) NOT NULL DEFAULT 0,
    gross_sales     DECIMAL(14,2) NOT NULL DEFAULT 0,
    discounts       DECIMAL(14,2) NOT NULL DEFAULT 0,
    comped          DECIMAL(14,2) NOT NULL DEFAULT 0,
    cogs_food       DECIMAL(14,2) NOT NULL DEFAULT 0,
    cogs_beverage   DECIMAL(14,2) NOT NULL DEFAULT 0,
    cogs_alcohol    DECIMAL(14,2) NOT NULL DEFAULT 0,
    cogs_other      DECIMAL(14,2) NOT NULL DEFAULT 0,
    food_cogs_pct   DECIMAL(5,2) NOT NULL DEFAULT 0,
    labor_foh       DECIMAL(14,2) NOT NULL DEFAULT 0,
    labor_boh       DECIMAL(14,2) NOT NULL DEFAULT 0,
    labor_total     DECIMAL(14,2) NOT NULL DEFAULT 0,
    labor_pct       DECIMAL(5,2) NOT NULL DEFAULT 0,
    prime_cost      DECIMAL(14,2) NOT NULL DEFAULT 0,
    prime_cost_pct  DECIMAL(5,2) NOT NULL DEFAULT 0,
    gross_profit_pct DECIMAL(5,2) NOT NULL DEFAULT 0,
    net_profit_pct  DECIMAL(5,2) NOT NULL DEFAULT 0,
    data_completeness VARCHAR(10) NOT NULL DEFAULT 'FULL',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_mfs_period_bucket (period_key, bucket)
);

CREATE INDEX idx_mfs_bucket ON monthly_financial_summary (bucket, period_key);

-- DOWN (commented, not auto-run; for ops reference)
-- DROP INDEX idx_mfs_bucket ON monthly_financial_summary;
-- DROP TABLE monthly_financial_summary;
-- ALTER TABLE supply_categories DROP COLUMN food_type;
