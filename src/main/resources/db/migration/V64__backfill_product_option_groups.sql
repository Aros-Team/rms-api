-- =============================================================================
-- V64: backfill product_option_groups from existing product-option associations
-- =============================================================================
--
-- PURPOSE:
--   V40 introduced `product_option_groups` as the high-level Product -> OptionGroup
--   association, but existing seeded products only have `product_product_options`
--   rows. As a result the admin product wizard/receipt page cannot see which
--   option groups a product already exposes.
--
--   This migration makes the existing associations visible by inserting one
--   (product_id, option_group_id, required=false) row per distinct pair found
--   in `product_product_options` + `product_options`.
--
-- MIGRATION SAFETY (forward-only additive):
--   * INSERT IGNORE keeps the migration idempotent and preserves any V40 rows
--     that already exist (including their `required` flag).
--   * `required` defaults to FALSE: we must not turn existing products into
--     mandatory-choice products without an explicit admin decision.
--   * No UPDATE / DELETE of existing data.
--
-- Reference: ADR-0001 (product-scoped option groups) in docs/adrs.
-- =============================================================================

INSERT IGNORE INTO product_option_groups (product_id, option_group_id, required)
SELECT DISTINCT ppo.product_id, po.option_category_id, FALSE
FROM product_product_options ppo
JOIN product_options po ON po.id = ppo.option_id
WHERE po.option_category_id IS NOT NULL;
