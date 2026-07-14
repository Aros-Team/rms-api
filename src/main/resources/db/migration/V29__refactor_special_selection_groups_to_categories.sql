/* V29: Refactor special selection groups from option-based to category+product-based */
/* Groups now reference a product category and contain products (not product options) */

ALTER TABLE special_selection_groups
  ADD COLUMN category_id BIGINT NULL AFTER product_id,
  DROP COLUMN name;

ALTER TABLE special_selection_groups
  ADD CONSTRAINT fk_group_category FOREIGN KEY (category_id) REFERENCES categories(id);

CREATE TABLE special_selection_group_products (
    group_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (group_id, product_id),
    FOREIGN KEY (group_id) REFERENCES special_selection_groups(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

ALTER TABLE product_product_options DROP FOREIGN KEY fk_ppo_group;
ALTER TABLE product_product_options DROP COLUMN selection_group_id;
