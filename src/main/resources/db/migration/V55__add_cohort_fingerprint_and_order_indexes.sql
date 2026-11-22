-- =============================================================================
-- V55: cohort fingerprint fields + order date indexes.
-- =============================================================================
-- Owned by the Analytics BE lane. Cohort identity has no customer table, so an
-- optional phone (normalized digits) and its SHA-256 fingerprint are stored on
-- the order. When both are null the query falls back to table_id and flags the
-- report as PARTIAL.

ALTER TABLE orders
    ADD COLUMN customer_phone       VARCHAR(32) NULL AFTER close_time,
    ADD COLUMN customer_fingerprint VARCHAR(64) NULL AFTER customer_phone;

-- Date-range analytics and order lists currently scan the table (S0 F-019).
CREATE INDEX idx_orders_date ON orders (date);
CREATE INDEX idx_orders_status_date ON orders (status, date);
CREATE INDEX idx_orders_customer_fingerprint ON orders (customer_fingerprint);

-- DOWN (for operations reference, not auto-run)
-- DROP INDEX idx_orders_customer_fingerprint ON orders;
-- DROP INDEX idx_orders_status_date ON orders;
-- DROP INDEX idx_orders_date ON orders;
-- ALTER TABLE orders DROP COLUMN customer_fingerprint, DROP COLUMN customer_phone;
