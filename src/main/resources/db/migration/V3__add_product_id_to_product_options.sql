-- V3__add_product_id_to_product_options.sql
-- Add product_id foreign key to product_options table to establish relationship between products and their options

ALTER TABLE product_options
ADD COLUMN product_id BIGINT,
ADD FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;

