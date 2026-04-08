-- V8: Create purchase/supplier tables and alter inventory_movements

-- Distribuidores / Proveedores
CREATE TABLE suppliers (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    contact VARCHAR(255),
    active  BOOLEAN NOT NULL DEFAULT TRUE
);

-- Orden de compra (cabecera)
CREATE TABLE purchase_orders (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_id   BIGINT        NOT NULL,
    registered_by BIGINT        NOT NULL,
    purchased_at  TIMESTAMP     NOT NULL,
    total_amount  DECIMAL(12,2) NOT NULL,
    notes         VARCHAR(500),
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_po_supplier     FOREIGN KEY (supplier_id)   REFERENCES suppliers(id),
    CONSTRAINT fk_po_registered_by FOREIGN KEY (registered_by) REFERENCES users(id)
);

-- Ítems de la orden de compra
CREATE TABLE purchase_order_items (
    id                BIGINT        AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id BIGINT        NOT NULL,
    supply_variant_id BIGINT        NOT NULL,
    quantity_ordered  DECIMAL(10,3) NOT NULL,
    quantity_received DECIMAL(10,3) NOT NULL,
    unit_price        DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_poi_purchase_order  FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_poi_supply_variant  FOREIGN KEY (supply_variant_id) REFERENCES supply_variants(id)
);

-- Agregar referencia a orden de compra en movimientos de inventario
ALTER TABLE inventory_movements
    ADD COLUMN reference_purchase_order_id BIGINT NULL,
    ADD CONSTRAINT fk_movement_purchase_order
        FOREIGN KEY (reference_purchase_order_id)
        REFERENCES purchase_orders(id) ON DELETE SET NULL;
