-- V1__create_tables.sql
-- Creates the initial database schema for RMS

-- Tabla areas
CREATE TABLE areas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabla users
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(50),
    role VARCHAR(50) NOT NULL
);

-- Relación usuarios-áreas
CREATE TABLE user_assigned_areas (
    user_id BIGINT NOT NULL,
    area_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, area_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (area_id) REFERENCES areas(id) ON DELETE CASCADE
);

-- Tabla refresh_tokens
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabla two_factor_codes
CREATE TABLE two_factor_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE);

-- Tabla devices
CREATE TABLE devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    hash VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabla categories
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabla option_categories
CREATE TABLE option_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500)
);

-- Tabla tables
CREATE TABLE tables (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_number INTEGER NOT NULL UNIQUE,
    capacity INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE'
);

-- Tabla products
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    has_options BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    category_id BIGINT,
    area_id BIGINT,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (area_id) REFERENCES areas(id) ON DELETE SET NULL
);

-- Tabla product_options
CREATE TABLE product_options (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    option_category_id BIGINT,
    FOREIGN KEY (option_category_id) REFERENCES option_categories(id) ON DELETE SET NULL);

-- Tabla orders
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'QUEUE',
    table_id BIGINT,
    FOREIGN KEY (table_id) REFERENCES tables(id) ON DELETE SET NULL
);

-- Tabla order_details
CREATE TABLE order_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    instructions VARCHAR(500),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
);

-- Tabla relación orders-areas (order_preparation_areas)
CREATE TABLE order_preparation_areas (
    order_id BIGINT NOT NULL,
    area_id BIGINT NOT NULL,
    PRIMARY KEY (order_id, area_id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (area_id) REFERENCES areas(id) ON DELETE CASCADE
);

-- Tabla relación order_details-product_options (order_detail_options)
CREATE TABLE order_detail_options (
    order_detail_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,
    PRIMARY KEY (order_detail_id, option_id),
    FOREIGN KEY (order_detail_id) REFERENCES order_details(id) ON DELETE CASCADE,
    FOREIGN KEY (option_id) REFERENCES product_options(id) ON DELETE CASCADE
);

-- =============================================================================
-- INVENTORY TABLES
-- =============================================================================

-- Categorías de insumos (ej: Proteínas, Vegetales, Lácteos)
CREATE TABLE supply_categories (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Unidades de medida estandarizadas (ej: Gramos, Mililitros, Unidades)
CREATE TABLE units_of_measure (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL UNIQUE,
    abbreviation VARCHAR(20)  NOT NULL UNIQUE
);

-- Insumo base (ej: "Carne de Res", "Arroz")
CREATE TABLE supplies (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(255) NOT NULL UNIQUE,
    supply_category_id  BIGINT NOT NULL,
    FOREIGN KEY (supply_category_id) REFERENCES supply_categories(id)
);

-- Presentaciones físicas del insumo (ej: "Carne 250g", "Carne 100g")
-- El nombre NO se repite; se deriva de supply + quantity + unit
CREATE TABLE supply_variants (
    id      BIGINT         AUTO_INCREMENT PRIMARY KEY,
    supply_id BIGINT       NOT NULL,
    unit_id   BIGINT       NOT NULL,
    quantity  DECIMAL(10,3) NOT NULL,   -- valor numérico (ej: 250)
    FOREIGN KEY (supply_id) REFERENCES supplies(id),
    FOREIGN KEY (unit_id)   REFERENCES units_of_measure(id),
    UNIQUE (supply_id, unit_id, quantity)
);

-- Áreas físicas de almacenamiento (ej: "Bodega Principal", "Cocina")
CREATE TABLE storage_locations (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Saldo actual de cada variante en cada ubicación
CREATE TABLE inventory_stock (
    id                  BIGINT         AUTO_INCREMENT PRIMARY KEY,
    supply_variant_id   BIGINT         NOT NULL,
    storage_location_id BIGINT         NOT NULL,
    current_quantity    DECIMAL(10,3)  NOT NULL DEFAULT 0,
    UNIQUE (supply_variant_id, storage_location_id),
    FOREIGN KEY (supply_variant_id)   REFERENCES supply_variants(id),
    FOREIGN KEY (storage_location_id) REFERENCES storage_locations(id)
);

-- Receta de un producto: qué variantes y en qué cantidad se necesitan
CREATE TABLE product_recipes (
    id                BIGINT         AUTO_INCREMENT PRIMARY KEY,
    product_id        BIGINT         NOT NULL,
    supply_variant_id BIGINT         NOT NULL,
    required_quantity DECIMAL(10,3)  NOT NULL,
    UNIQUE (product_id, supply_variant_id),
    FOREIGN KEY (product_id)        REFERENCES products(id)        ON DELETE CASCADE,
    FOREIGN KEY (supply_variant_id) REFERENCES supply_variants(id)
);

-- Receta de una opción de producto
CREATE TABLE option_recipes (
    id                BIGINT         AUTO_INCREMENT PRIMARY KEY,
    option_id         BIGINT         NOT NULL,
    supply_variant_id BIGINT         NOT NULL,
    required_quantity DECIMAL(10,3)  NOT NULL,
    UNIQUE (option_id, supply_variant_id),
    FOREIGN KEY (option_id)         REFERENCES product_options(id) ON DELETE CASCADE,
    FOREIGN KEY (supply_variant_id) REFERENCES supply_variants(id)
);

-- Historial de movimientos de inventario (traslados y deducciones)
CREATE TABLE inventory_movements (
    id                       BIGINT         AUTO_INCREMENT PRIMARY KEY,
    supply_variant_id        BIGINT         NOT NULL,
    from_storage_location_id BIGINT,                          -- NULL si es ingreso inicial
    to_storage_location_id   BIGINT,                          -- NULL si es baja/consumo
    quantity                 DECIMAL(10,3)  NOT NULL,
    movement_type            VARCHAR(50)    NOT NULL,          -- TRANSFER | DEDUCTION | ENTRY
    reference_order_id       BIGINT,                          -- FK a orders (nullable)
    created_at               TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (supply_variant_id)        REFERENCES supply_variants(id),
    FOREIGN KEY (from_storage_location_id) REFERENCES storage_locations(id),
    FOREIGN KEY (to_storage_location_id)   REFERENCES storage_locations(id),
    FOREIGN KEY (reference_order_id)       REFERENCES orders(id) ON DELETE SET NULL
);
