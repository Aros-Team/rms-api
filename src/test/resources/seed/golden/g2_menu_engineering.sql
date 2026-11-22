-- G2 — menu engineering golden fixture (period 2027-01, 8 active SKUs, H never sold).
-- Product ids 1..8 are local to the golden schema; expected response is in expected.json.
DELETE FROM menu_performance_cache WHERE bucket = 'monthly' AND period_key = '2027-01';
INSERT INTO menu_performance_cache
  (product_id, product_name, category_id, category_name, period_key, bucket,
   units_sold, revenue, recipe_cost, gross_profit_per_unit, total_contribution,
   quadrant, source_version, updated_at, avg_option_cost, effective_cost)
VALUES
  (1, 'A', 1, 'Golden', '2027-01', 'monthly', 300, 9000000.00, 18000.00, 12000.00, 3600000.00, 'PLOWHORSE', 'golden-v1', '2027-01-31 23:59:00', 0.00, 18000.00),
  (2, 'B', 1, 'Golden', '2027-01', 'monthly', 260, 8840000.00, 19000.00, 15000.00, 3900000.00, 'STAR',      'golden-v1', '2027-01-31 23:59:00', 0.00, 19000.00),
  (3, 'C', 1, 'Golden', '2027-01', 'monthly', 200, 5200000.00, 18000.00,  8000.00, 1600000.00, 'PLOWHORSE', 'golden-v1', '2027-01-31 23:59:00', 0.00, 18000.00),
  (4, 'D', 1, 'Golden', '2027-01', 'monthly', 120, 5040000.00, 22000.00, 20000.00, 2400000.00, 'STAR',      'golden-v1', '2027-01-31 23:59:00', 0.00, 22000.00),
  (5, 'E', 1, 'Golden', '2027-01', 'monthly',  80, 1600000.00, 15000.00,  5000.00,  400000.00, 'DOG',       'golden-v1', '2027-01-31 23:59:00', 0.00, 15000.00),
  (6, 'F', 1, 'Golden', '2027-01', 'monthly',  40, 1520000.00, 20000.00, 18000.00,  720000.00, 'PUZZLE',    'golden-v1', '2027-01-31 23:59:00', 0.00, 20000.00),
  (7, 'G', 1, 'Golden', '2027-01', 'monthly',  30, 1440000.00, 26000.00, 22000.00,  660000.00, 'PUZZLE',    'golden-v1', '2027-01-31 23:59:00', 0.00, 26000.00);
