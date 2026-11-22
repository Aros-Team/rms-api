-- G1 — prime cost golden fixture (period 2027-01).
-- Load into a fresh schema after Flyway migrations. Expected response is in expected.json.
DELETE FROM monthly_financial_summary WHERE bucket = 'monthly' AND period_key = '2027-01';
INSERT INTO monthly_financial_summary
  (period_key, bucket, net_sales, gross_sales, discounts, comped,
   cogs_food, cogs_beverage, cogs_alcohol, cogs_other,
   food_cogs_pct, labor_foh, labor_boh, labor_total, labor_pct,
   prime_cost, prime_cost_pct, gross_profit_pct, net_profit_pct, data_completeness)
VALUES
  ('2027-01', 'monthly', 950000.00, 1000000.00, 40000.00, 10000.00,
   300000.00, 80000.00, 50000.00, 20000.00,
   31.58, 120000.00, 180000.00, 300000.00, 31.58,
   750000.00, 78.95, 52.63, 21.05, 'FULL');
