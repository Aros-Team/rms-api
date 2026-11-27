-- =============================================================================
-- V73: restaurant floor layout
-- =============================================================================
--
-- The floor layout is a single, restaurant-wide schematic grid. Each region is
-- an independent physical representation of an existing Area. A region stores
-- compact row segments instead of one row per painted cell:
--
--   [{"row":0,"from":0,"to":3},{"row":1,"from":0,"to":1}]
--
-- Coordinates are zero-based, row grows downward and from/to are inclusive
-- column bounds. The API normalizes and validates every submitted document
-- before replacing the stored set inside one transaction.
--
-- Integrity:
--   * Deleting the whole layout removes its regions (internal cascade).
--   * Deleting an Area is RESTRICTED while one of its regions exists. Layout
--     data is never silently deleted when an area disappears.
-- =============================================================================

CREATE TABLE floor_layout (
    id            BIGINT    NOT NULL PRIMARY KEY,
    columns_count INT       NOT NULL,
    rows_count    INT       NOT NULL,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO floor_layout (id, columns_count, rows_count)
VALUES (1, 32, 20);

CREATE TABLE floor_layout_region (
    id              BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    floor_layout_id BIGINT      NOT NULL,
    region_key      VARCHAR(64) NOT NULL,
    area_id         BIGINT      NOT NULL,
    segments_json   LONGTEXT    NOT NULL,
    CONSTRAINT fk_floor_layout_region_layout
        FOREIGN KEY (floor_layout_id) REFERENCES floor_layout (id) ON DELETE CASCADE,
    CONSTRAINT fk_floor_layout_region_area
        FOREIGN KEY (area_id) REFERENCES areas (id) ON DELETE RESTRICT,
    CONSTRAINT uq_floor_layout_region_key
        UNIQUE (floor_layout_id, region_key)
);

CREATE INDEX idx_floor_layout_region_area ON floor_layout_region (area_id);
