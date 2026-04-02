-- V7__seed_preparing_orders.sql
-- 5 test orders in PREPARING status (depends on V5 products)

-- Tables
INSERT INTO tables (table_number, capacity, status) VALUES
(1, 4, 'OCCUPIED'),
(2, 2, 'OCCUPIED'),
(3, 6, 'OCCUPIED'),
(4, 4, 'OCCUPIED'),
(5, 2, 'OCCUPIED');

-- Orders in PREPARING status, one per table
INSERT INTO orders (date, status, table_id)
SELECT NOW(), 'PREPARING', id FROM tables WHERE table_number = 1;
SET @o1 = LAST_INSERT_ID();

INSERT INTO orders (date, status, table_id)
SELECT NOW(), 'PREPARING', id FROM tables WHERE table_number = 2;
SET @o2 = LAST_INSERT_ID();

INSERT INTO orders (date, status, table_id)
SELECT NOW(), 'PREPARING', id FROM tables WHERE table_number = 3;
SET @o3 = LAST_INSERT_ID();

INSERT INTO orders (date, status, table_id)
SELECT NOW(), 'PREPARING', id FROM tables WHERE table_number = 4;
SET @o4 = LAST_INSERT_ID();

INSERT INTO orders (date, status, table_id)
SELECT NOW(), 'PREPARING', id FROM tables WHERE table_number = 5;
SET @o5 = LAST_INSERT_ID();

-- Order details using captured IDs
-- products: 1=Bandeja Paisa, 2=Pollo a la Plancha, 3=Limonada, 4=Ensalada Verde
INSERT INTO order_details (order_id, product_id, unit_price, instructions) VALUES
(@o1, 1, 28000.00, NULL),
(@o1, 3,  6000.00, 'Sin azúcar'),
(@o2, 2, 22000.00, 'Término medio'),
(@o3, 1, 28000.00, NULL),
(@o3, 4, 12000.00, NULL),
(@o4, 2, 22000.00, NULL),
(@o4, 3,  6000.00, NULL),
(@o5, 4, 12000.00, 'Sin tomate'),
(@o5, 1, 28000.00, NULL);
