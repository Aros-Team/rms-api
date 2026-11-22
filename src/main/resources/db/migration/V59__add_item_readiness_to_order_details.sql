-- V59 — Kitchen: item-level readiness on order details.
--
-- A line can be marked ready before the whole ticket is done so a station can
-- clear its own work. The order still moves to READY when every line is ready
-- (enforced in the application service); the existing order-level endpoint
-- remains available and marks all lines ready at once.

ALTER TABLE order_details
    ADD COLUMN ready BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN ready_at TIMESTAMP NULL;

-- Keep historical tickets coherent: lines belonging to an order that is already
-- READY or DELIVERED are considered ready, using the closest available timestamp.
UPDATE order_details d
JOIN orders o ON o.id = d.order_id
SET d.ready = TRUE,
    d.ready_at = COALESCE(o.close_time, o.date)
WHERE o.status IN ('READY', 'DELIVERED');
