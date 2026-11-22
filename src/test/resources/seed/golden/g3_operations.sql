-- G3 — operations golden fixture: 2027-01-15, 5 tables × 4 seats, 10 orders.
-- This file documents the inputs; expected.json holds the exact response.
-- Load order: tables, then orders with party_size/open_time/close_time.
INSERT IGNORE INTO tables (id, table_number, capacity, status) VALUES
  (1,1,4,'AVAILABLE'),(2,2,4,'AVAILABLE'),(3,3,4,'AVAILABLE'),(4,4,4,'AVAILABLE'),(5,5,4,'AVAILABLE');
-- 10 orders, 28 covers, 500 occupied minutes, 1,390 seat-minutes, net sales 1,700,000.
-- Order 1..4 lunch; 5..10 dinner (see seed-spec.md section 10.3).
