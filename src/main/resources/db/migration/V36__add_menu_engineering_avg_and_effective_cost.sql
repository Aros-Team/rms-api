-- =============================================================================
-- V36: Add avg_option_cost and effective_cost to menu_performance_cache
-- =============================================================================
--
-- PURPOSE:
--   Menu engineering currently uses only recipeCost (base ingredients) for
--   gross profit per unit and BCG quadrant. This ignores options (proteins,
--   sides, etc.) actually chosen by customers per order.
--
--   This migration adds:
--     - avg_option_cost : historical average cost of chosen options per order
--                         within the bucket (default monthly)
--     - effective_cost  : = recipeCost + avgOptionCost
--                         Used by RefreshMenuEngineeringService to compute
--                         grossProfitPerUnit and assign BCG quadrants.
--
--   recipeCost is kept untouched and stays exposed in the response DTO for
--   kitchen/production visibility (it answers "how much raw inventory do I
--   need to make this dish?").
--
-- PERFORMANCE NOTE:
--   The aggregation query (loadAvgOptionCostByProduct) joins
--     order_details × order_detail_options × option_recipes × supply_variants
--   On large datasets (12+ months of history) this may exceed the 2-3s
--   threshold acceptable for the nightly RefreshMenuEngineeringJob.
--
--   If production/staging shows degraded latency, apply the conditional
--   V37__add_menu_engineering_indexes.sql with:
--     - idx_od_order_product   on order_details(order_id, product_id)
--     - idx_odo_option         on order_detail_options(option_id)
--     - idx_oreq_option_supply on option_recipes(option_id, supply_variant_id)
--
--   Indexes are NOT created here on purpose: avoid premature optimization.
--
-- Reference: progress/current.md (activity id 2), discussion in orchestrator
-- session 2026-07-29.
-- =============================================================================

ALTER TABLE menu_performance_cache
    ADD COLUMN avg_option_cost DECIMAL(14,2) NOT NULL DEFAULT 0,
    ADD COLUMN effective_cost   DECIMAL(14,2) NOT NULL DEFAULT 0;

-- Backfill: existing rows had no avgOptionCost computed. Setting it to 0 and
-- effective_cost = recipe_cost keeps the existing cache consistent with the
-- pre-V36 behaviour until the next scheduled refresh (RefreshMenuEngineeringJob)
-- recomputes everything. This avoids any period where the dashboard would
-- display broken data right after deploy.
UPDATE menu_performance_cache
SET avg_option_cost = 0,
    effective_cost  = recipe_cost;
