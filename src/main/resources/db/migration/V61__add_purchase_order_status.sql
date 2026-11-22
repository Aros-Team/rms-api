-- V61: typed purchase order status (P4-INV-06).
--
-- The deterministic seed and the previous API encoded the lifecycle only as a
-- token at the start of `notes` (`RECIBIDA` / `CONFIRMADA`) and inferred
-- "received" from the item quantities. Promote it to a typed column so the UI
-- can badge and filter purchases without parsing free text.
--
-- Values:
--   CONFIRMED - order placed, no goods received yet
--   RECEIVED  - at least one line arrived and entered stock
-- There is no cancellation flow in the domain, so CANCELLED is not modelled.
--
-- Backfill precedence:
--   1. legacy notes token
--   2. any purchase_order_items.quantity_received > 0
--   3. default CONFIRMED
-- `notes` is kept untouched for compatibility.

ALTER TABLE purchase_orders
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED' AFTER total_amount;

UPDATE purchase_orders po
SET status = 'RECEIVED'
WHERE po.notes LIKE 'RECIBIDA%'
   OR EXISTS (
       SELECT 1
       FROM purchase_order_items i
       WHERE i.purchase_order_id = po.id
         AND i.quantity_received > 0
   );

ALTER TABLE purchase_orders
    ADD CONSTRAINT chk_purchase_orders_status
        CHECK (status IN ('CONFIRMED', 'RECEIVED'));

CREATE INDEX idx_purchase_orders_status ON purchase_orders(status);
