-- V24__add_selection_columns_to_products.sql
-- Adds columns to products table for special selection support

ALTER TABLE products
  ADD COLUMN selection_type VARCHAR(20) NOT NULL DEFAULT 'STANDARD',
  ADD COLUMN selection_base_recipe_enabled BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN scheduling_required BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_products_selection_type ON products(selection_type);
