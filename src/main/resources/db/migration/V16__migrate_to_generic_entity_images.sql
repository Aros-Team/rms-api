/* Migrate product_images to generic entity_images table */

CREATE TABLE entity_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    original_size_bytes BIGINT NOT NULL,
    storage_key VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Migrate data from product_images to entity_images
INSERT INTO entity_images (id, entity_type, entity_id, original_filename, content_type, original_size_bytes, storage_key, created_at)
SELECT id, 'PRODUCT', product_id, original_filename, content_type, original_size_bytes, storage_key, created_at
FROM product_images;

-- Create indexes for entity_images
CREATE INDEX idx_entity_images_entity ON entity_images(entity_type, entity_id);
CREATE INDEX idx_entity_images_storage_key ON entity_images(storage_key);

-- Drop old table and foreign key constraint
ALTER TABLE product_images DROP FOREIGN KEY fk_product_images_product;
DROP TABLE product_images;