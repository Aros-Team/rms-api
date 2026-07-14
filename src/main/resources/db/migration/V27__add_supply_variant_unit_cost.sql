-- V27__add_supply_variant_unit_cost.sql
-- Adds unit_cost column to enable the admin price suggestion tool

ALTER TABLE supply_variants
  ADD COLUMN unit_cost DECIMAL(10,2) NOT NULL DEFAULT 0.00;
