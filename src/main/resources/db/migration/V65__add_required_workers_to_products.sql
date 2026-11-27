-- =============================================================================
-- V65: products.required_workers
-- =============================================================================
--
-- PURPOSE:
--   A dish is not always prepared by one person. The labor-cost formula
--   multiplies person-hours by the area hourly rate, so the product needs an
--   estimate of how many people work on it during its preparation time.
--
--   labor cost = (estimated_prep_minutes / 60) * area_hourly_rate * required_workers
--
--   Defaults to 1 so existing products keep their previous calculated cost.
--
-- MIGRATION SAFETY (forward-only additive):
--   * New column with a NOT NULL DEFAULT 1; existing rows get the default.
--   * CHECK constraint only allows positive values.
--   * No DROP / RENAME / MODIFY of existing columns.
--
-- Reference: ADR-0004 (labor cost includes required workers).
-- =============================================================================

ALTER TABLE products
    ADD COLUMN required_workers INT NOT NULL DEFAULT 1 AFTER estimated_prep_minutes;

ALTER TABLE products
    ADD CONSTRAINT chk_products_required_workers CHECK (required_workers >= 1);
