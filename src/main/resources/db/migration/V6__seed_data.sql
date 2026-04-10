-- V6__seed_data.sql
-- Seed data for local development. NOT for production.

-- =============================================================================
-- AREAS (preparación)
-- =============================================================================
INSERT INTO areas (name, type, enabled) VALUES
    ('Cocina', 'KITCHEN',   TRUE),
    ('Bar',    'BARTENDER', TRUE);

-- =============================================================================
-- INVENTARIO: unidades de medida
-- =============================================================================
INSERT INTO units_of_measure (name, abbreviation) VALUES
    ('Gramos',       'g'),
    ('Kilogramos',   'kg'),
    ('Litros',       'l'),
    ('Unidades',     'u'),
    ('Libras',       'lb'),
    ('Piezas',       'pz'),
    ('Paquetes',     'paq'),
    ('Botellas',     'bot'),
    ('Latas',        'lat');

INSERT INTO storage_locations (name) VALUES
    ('Bodega'),   -- id 1
    ('Cocina');             -- id 2
