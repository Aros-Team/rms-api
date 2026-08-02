-- =============================================================================
-- V40: product_option_groups (Product M:N OptionGroup, with required flag)
-- =============================================================================
--
-- PURPOSE:
--   Add a direct Product-OptionGroup M:N relationship. Previously a Product
--   reached its OptionGroups only via the chain
--     Product -> ProductOption (product_product_options) -> OptionGroup
--   which made the FE / API callers do their own joining. This new table
--   exposes the high-level "which option groups apply to this product"
--   relationship as a first-class association.
--
--   `required` lets a product mark a group as mandatory at order-taking
--   time (e.g. "Tipo de proteína" must always be chosen for a hamburger).
--   Defaults to FALSE; nullable only for back-compat with existing rows.
--
--   `product_product_options` is intentionally NOT removed: it remains the
--   per-option detail table (extra_price, display_order) for options that
--   belong to a group's product.
--
-- BUSINESS RULE (enforced at service layer):
--   An OptionGroup can only exist if at least one Product is associated
--   with it. Enforced by OptionGroupRequiresProductException -> HTTP 400
--   on the create / update endpoints. This forces the workflow:
--     1) Create / select a product.
--     2) Create or attach an option group to that product.
--     3) Add options to the group.
--   rather than the previous reverse order, where option groups could
--   float without any product attached.
--
-- MIGRATION SAFETY (forward-only additive):
--   * New table; no existing columns changed.
--   * Composite PK (product_id, option_group_id) — natural unique key.
--   * FKs ON DELETE CASCADE — deleting a product or option group removes
--     the association rows (the underlying option_group / product rows
--     themselves are not deleted by this migration).
--   * No backfill: existing seeded groups in data.sql gain their
--     associations via a follow-up idempotent INSERT IGNORE in data.sql.
--
-- Reference: progress/current.md (activity 6),
--            chore/option-group-product-association.
-- =============================================================================

CREATE TABLE product_option_groups (
    product_id BIGINT NOT NULL,
    option_group_id BIGINT NOT NULL,
    required BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (product_id, option_group_id),
    CONSTRAINT fk_product_option_groups_product
        FOREIGN KEY (product_id)
        REFERENCES products (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_product_option_groups_option_group
        FOREIGN KEY (option_group_id)
        REFERENCES option_group (id)
        ON DELETE CASCADE
);