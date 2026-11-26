-- =============================================================================
-- V69: per-line preparation/delivery timing and order ticket timestamps
-- =============================================================================
--
-- Real preparation time is measured per order line, because a multi-area order
-- prepares its drink at the bar and its hamburger in the kitchen in parallel.
-- Order-level start timestamps would contaminate the other station's time.
--
-- Adds:
--   * order_details.preparation_started_at — when that line's station took it
--   * order_details.delivered / delivered_at — partial delivery by the waiter
--   * orders.prepared_at — when every line became ready
--   * orders.delivered_at — when every line was delivered
--
-- Historical rows do not get a synthetic preparation_started_at: inventing one
-- from the order date would reintroduce the multi-area contamination. Existing
-- delivered orders are marked delivered from their close_time so history stays
-- coherent for delivery counts.
--
-- Reference: ADR-0005 (per-line preparation and partial delivery).
-- =============================================================================

ALTER TABLE order_details
    ADD COLUMN preparation_started_at TIMESTAMP NULL AFTER instructions,
    ADD COLUMN delivered BOOLEAN NOT NULL DEFAULT FALSE AFTER ready_at,
    ADD COLUMN delivered_at TIMESTAMP NULL AFTER delivered;

ALTER TABLE orders
    ADD COLUMN prepared_at TIMESTAMP NULL AFTER close_time,
    ADD COLUMN delivered_at TIMESTAMP NULL AFTER prepared_at;

UPDATE order_details d
JOIN orders o ON o.id = d.order_id
SET d.delivered = TRUE,
    d.delivered_at = COALESCE(o.close_time, o.date)
WHERE o.status = 'DELIVERED';

UPDATE orders o
SET o.delivered_at = COALESCE(o.close_time, o.date)
WHERE o.status = 'DELIVERED';

UPDATE orders o
SET o.prepared_at = (
    SELECT MAX(d.ready_at) FROM order_details d WHERE d.order_id = o.id
)
WHERE o.status IN ('READY', 'DELIVERED')
  AND o.prepared_at IS NULL;

CREATE INDEX idx_order_details_product_ready
    ON order_details (product_id, ready_at);
