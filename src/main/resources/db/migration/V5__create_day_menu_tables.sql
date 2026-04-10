-- V5__create_day_menu_tables.sql
-- Creates day_menu (active record) and day_menu_history (archived records) tables

CREATE TABLE day_menu (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    product_id  BIGINT       NOT NULL,
    valid_from  TIMESTAMP    NOT NULL,
    created_by  VARCHAR(255) NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
);

CREATE TABLE day_menu_history (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    product_id  BIGINT       NOT NULL,
    valid_from  TIMESTAMP    NOT NULL,
    valid_until TIMESTAMP    NOT NULL,
    created_by  VARCHAR(255) NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
);

CREATE INDEX idx_day_menu_history_valid_until
    ON day_menu_history (valid_until DESC);
