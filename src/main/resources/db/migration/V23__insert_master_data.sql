-- V23__insert_master_data.sql
-- Master data: business-critical reference data required in ALL environments.
-- All inserts are idempotent.

-- =============================================================================
-- AREAS
-- No UNIQUE constraint on name, so use WHERE NOT EXISTS
-- =============================================================================
INSERT INTO areas (name, type, enabled, for_user, for_product)
SELECT 'Cocina', 'KITCHEN', TRUE, FALSE, TRUE FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM areas WHERE name = 'Cocina');

INSERT INTO areas (name, type, enabled, for_user, for_product)
SELECT 'Servicio', 'SERVICE', TRUE, FALSE, TRUE FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM areas WHERE name = 'Servicio');

INSERT INTO areas (name, type, enabled, for_user, for_product)
SELECT 'Bar', 'BAR', TRUE, FALSE, TRUE FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM areas WHERE name = 'Bar');

INSERT INTO areas (name, type, enabled, for_user, for_product)
SELECT 'Caja', 'CASH', TRUE, TRUE, FALSE FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM areas WHERE name = 'Caja');

-- =============================================================================
-- UNITS OF MEASURE (name + abbreviation are UNIQUE -> INSERT IGNORE)
-- =============================================================================
INSERT IGNORE INTO units_of_measure (name, abbreviation) VALUES
    ('Gramos',       'g'),
    ('Kilogramos',   'kg'),
    ('Litros',       'l'),
    ('Unidades',     'u'),
    ('Libras',       'lb'),
    ('Piezas',       'pz'),
    ('Paquetes',     'paq'),
    ('Botellas',     'bot'),
    ('Latas',        'lat');

-- =============================================================================
-- SUPPLY CATEGORIES (name is UNIQUE -> INSERT IGNORE)
-- =============================================================================
INSERT IGNORE INTO supply_categories (name) VALUES
    ('Proteínas'),
    ('Vegetales y Frescos'),
    ('Lácteos'),
    ('Harinas y Masas'),
    ('Salsas y Bases'),
    ('Condimentos'),
    ('Embutidos y Curados'),
    ('Frutas y Pulpas'),
    ('Endulzantes'),
    ('Desechables'),
    ('Congelados'),
    ('Granos y Legumbres'),
    ('Pastas'),
    ('Aceites y Grasas'),
    ('Bebidas e Hielo');
