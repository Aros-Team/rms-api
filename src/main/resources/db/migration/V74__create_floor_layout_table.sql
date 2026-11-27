-- =============================================================================
-- V74: table placements on the floor layout
-- =============================================================================
--
-- A table is placed on one cell of the floor grid. Placements are independent
-- from area regions so editing the area map does not delete the tables. The
-- service validates that every cell belongs to an enabled area with the SERVE
-- activity. Deleting a table or the layout removes its placement.

CREATE TABLE floor_layout_table (
    id              BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    floor_layout_id BIGINT NOT NULL,
    table_id        BIGINT NOT NULL,
    row_index       INT    NOT NULL,
    column_index    INT    NOT NULL,
    CONSTRAINT fk_floor_layout_table_layout
        FOREIGN KEY (floor_layout_id) REFERENCES floor_layout (id) ON DELETE CASCADE,
    CONSTRAINT fk_floor_layout_table_table
        FOREIGN KEY (table_id) REFERENCES tables (id) ON DELETE CASCADE,
    CONSTRAINT uq_floor_layout_table_table
        UNIQUE (floor_layout_id, table_id),
    CONSTRAINT uq_floor_layout_table_cell
        UNIQUE (floor_layout_id, row_index, column_index)
);
