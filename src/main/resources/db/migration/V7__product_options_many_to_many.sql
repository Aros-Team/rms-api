-- V7__product_options_many_to_many.sql
-- Refactor product_options to support many-to-many relationship with products.
-- A product_option now exists independently and can be associated with multiple products.

-- 1. Create the join table
CREATE TABLE product_product_options (
    product_id BIGINT NOT NULL,
    option_id  BIGINT NOT NULL,
    PRIMARY KEY (product_id, option_id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (option_id)  REFERENCES product_options(id) ON DELETE CASCADE
);

-- 2. Migrate existing associations from product_options.product_id into the join table
INSERT INTO product_product_options (product_id, option_id)
SELECT product_id, id
FROM product_options
WHERE product_id IS NOT NULL;

-- 3. Drop the now-redundant product_id column from product_options
ALTER TABLE product_options DROP FOREIGN KEY product_options_ibfk_2;
ALTER TABLE product_options DROP COLUMN product_id;
