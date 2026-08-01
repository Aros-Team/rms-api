-- =============================================================================
-- V37: option_categories selection mode + substitution category
-- =============================================================================
--
-- PURPOSE:
--   Allow option categories (the customization buckets like "Proteína",
--   "Queso", "Adición") to declare their selection semantics:
--     - SINGLE_CHOICE : exactly one option may be chosen
--     - MULTI_SELECT  : zero or more options may be chosen
--     - EXTRA         : standalone surcharge (e.g. "Extra queso")
--     - REMOVE        : subtractive option (e.g. "Sin cebolla")
--
--   SINGLE_CHOICE categories MAY reference a supply category via
--   replace_supply_category_id to model a substitution swap (e.g. "Salsa
--   tártara" replacing the base recipe's "Mayonesa"). For EXTRA / REMOVE
--   the replacement column must be NULL by convention.
--
-- MIGRATION SAFETY (forward-only additive):
--   * selection_type is NOT NULL with DEFAULT 'SINGLE_CHOICE' — existing
--     rows get the default on ALTER, so the NOT NULL constraint is safe
--     on populated tables.
--   * replace_supply_category_id is NULL — safe, no constraint violation.
--   * FK is ON DELETE SET NULL — safe even if a referenced supply
--     category is removed later.
--   * No DROP / RENAME / MODIFY of existing columns. No backfill that
--     overwrites user data.
--
-- Reference: progress/current.md (activity 2), task b (Phase B — model).
-- =============================================================================

ALTER TABLE option_categories
    ADD COLUMN selection_type VARCHAR(20) NOT NULL DEFAULT 'SINGLE_CHOICE' AFTER description;

ALTER TABLE option_categories
    ADD COLUMN replace_supply_category_id BIGINT NULL AFTER selection_type;

ALTER TABLE option_categories
    ADD CONSTRAINT fk_option_category_replace_supply
        FOREIGN KEY (replace_supply_category_id)
        REFERENCES supply_categories (id)
        ON DELETE SET NULL;
