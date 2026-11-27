-- =============================================================================
-- V70: products.prep_time_mode
-- =============================================================================
--
-- Both preparation times coexist:
--   * estimated_prep_minutes — editable base estimate (peak/low-demand logic)
--   * real prep time — measured from order lines, shown for calibration
--
-- The costing source is selectable per product:
--   ESTIMATED (default), REAL, AUTO
--
-- ESTIMATED is the default so existing costs do not change silently when real
-- data becomes available.
--
-- Reference: ADR-0004 and ADR-0005.
-- =============================================================================

ALTER TABLE products
    ADD COLUMN prep_time_mode VARCHAR(20) NOT NULL DEFAULT 'ESTIMATED'
        AFTER required_workers;

ALTER TABLE products
    ADD CONSTRAINT chk_products_prep_time_mode
        CHECK (prep_time_mode IN ('ESTIMATED', 'REAL', 'AUTO'));
