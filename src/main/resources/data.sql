-- =============================================================================
-- TEST / DEVELOPMENT DATA ONLY
-- Loaded by Spring Boot (not Flyway). Never runs in production.
-- =============================================================================

-- =============================================================================
-- RESTAURANT TABLES
-- =============================================================================
INSERT IGNORE INTO tables (table_number, capacity, status) VALUES
    (1, 4, 'AVAILABLE'),
    (2, 4, 'AVAILABLE'),
    (3, 4, 'AVAILABLE'),
    (4, 4, 'AVAILABLE'),
    (5, 4, 'AVAILABLE');

-- =============================================================================
-- STORAGE LOCATIONS (name is UNIQUE -> INSERT IGNORE)
-- =============================================================================
INSERT IGNORE INTO storage_locations (name) VALUES
    ('Bodega'),
    ('Cocina'),
    ('Cuarto Frío'),
    ('Bar');

-- =============================================================================
-- SUPPLY CATEGORY FOOD_TYPE (V33 only matched by exact name; backfill here)
-- Idempotent: WHERE guards ensure re-runs of data.sql are safe.
-- =============================================================================
UPDATE supply_categories SET food_type = 'FOOD'     WHERE name IN ('Proteínas','Vegetales y Frescos','Lácteos','Harinas y Masas','Salsas y Bases','Condimentos','Embutidos y Curados','Frutas y Pulpas','Endulzantes','Congelados','Granos y Legumbres','Pastas','Aceites y Grasas') AND food_type <> 'FOOD';
UPDATE supply_categories SET food_type = 'BEVERAGE' WHERE name = 'Bebidas e Hielo' AND food_type <> 'BEVERAGE';
UPDATE supply_categories SET food_type = 'OTHER'    WHERE name = 'Desechables' AND food_type <> 'OTHER';

-- =============================================================================
-- SUPPLIES (name is UNIQUE -> INSERT IGNORE)
-- supplies reference supply_categories seeded by V23
-- =============================================================================
INSERT IGNORE INTO supplies (name, supply_category_id) VALUES
    -- Proteínas (cat 1)
    ('Carne de Res Molida',          1),
    ('Pollo Apanado',                1),
    ('Lenteja Preparada',            1),
    ('Pechuga de Pollo',             1),
    ('Carne de Cerdo',               1),
    ('Camarón Tigre',                1),
    ('Baby Beef',                    1),
    ('Churrasco',                    1),
    ('Entrecot',                     1),
    ('Huevo',                        1),
    -- Vegetales y Frescos (cat 2)
    ('Lechuga Crespa',               2),
    ('Tomate',                       2),
    ('Cebolla Blanca',               2),
    ('Pepinillos',                   2),
    ('Pimentón Verde',               2),
    ('Hierbabuena Fresca',           2),
    ('Albahaca Fresca',              2),
    ('Champiñón Laminado',           2),
    ('Repollo',                      2),
    ('Zanahoria',                    2),
    ('Plátano Maduro',               2),
    ('Papa Criolla',                 2),
    ('Yuca',                         2),
    ('Papa Salada',                  2),
    -- Lácteos (cat 3)
    ('Queso Cheddar',                3),
    ('Queso Mozzarella',             3),
    ('Queso Parmesano',              3),
    ('Queso Crema',                  3),
    ('Crema de Leche',               3),
    ('Mantequilla',                  3),
    -- Harinas y Masas (cat 4)
    ('Pan Brioche',                  4),
    ('Pan Francés',                  4),
    ('Harina de Trigo',              4),
    ('Levadura Seca',                4),
    -- Salsas y Bases (cat 5)
    ('Salsa de Tomate Base',         5),
    ('Chimichurri Preparado',        5),
    ('Crema de Coco',                5),
    -- Condimentos (cat 6)
    ('Sal Refinada',                 6),
    ('Sal Parrillera',               6),
    ('Orégano Seco',                 6),
    ('Pimienta Negra',               6),
    -- Embutidos y Curados (cat 7)
    ('Tocineta Ahumada',             7),
    ('Pepperoni',                    7),
    ('Jamón Cocido',                 7),
    ('Bocadillo',                    7),
    -- Frutas y Pulpas (cat 8)
    ('Limón Tahití',                 8),
    ('Pulpa de Mango',               8),
    ('Pulpa de Mora',                8),
    ('Pulpa de Lulo',                8),
    ('Piña en Almíbar',              8),
    -- Endulzantes (cat 9)
    ('Azúcar Blanca',                9),
    ('Stevia',                       9),
    ('Miel de Abejas',               9),
    -- Desechables (cat 10)
    ('Empaque Caja Cartón',         10),
    ('Vaso Plástico',               10),
    ('Vaso de Vidrio',              10),
    ('Pitillo de Papel',            10),
    -- Congelados (cat 11)
    ('Papa a la Francesa Congelada',11),
    ('Papa en Cascos',              11),
    ('Patacón Prefrito',            11),
    -- Granos y Legumbres (cat 12)
    ('Arroz Blanco',                12),
    ('Frijol Cargamanto',           12),
    ('Lenteja Seca',                12),
    ('Maíz Tierno en Lata',         12),
    -- Pastas (cat 13)
    ('Pasta Larga Fettuccine',      13),
    ('Pasta Corta Penne',           13),
    -- Aceites y Grasas (cat 14)
    ('Aceite Vegetal',              14),
    -- Bebidas e Hielo (cat 15)
    ('Hielo en Cubo',               15);

-- =============================================================================
-- SUPPLY VARIANTS (supply_id, unit_id, quantity is UNIQUE -> INSERT IGNORE)
-- units_of_measure seeded by V23: g=1, kg=2, l=3, u=4, lb=5, pz=6, paq=7, bot=8, lat=9
-- unit_cost in COP, added by V27 (DECIMAL(10,2) NOT NULL DEFAULT 0.00)
-- Prices are realistic Colombian (COP) reference values per category range.
-- =============================================================================
INSERT IGNORE INTO supply_variants (supply_id, unit_id, quantity, unit_cost) VALUES
    -- Carne de Res Molida: 150g / 100g (~35,000 COP/kg)
    (1,  1, 150.000,  5250.00), (1,  1, 100.000,  3500.00),
    -- Pollo Apanado: 180g (~28,000 COP/kg)
    (2,  1, 180.000,  5040.00),
    -- Lenteja Preparada: 150g (~12,000 COP/kg)
    (3,  1, 150.000,  1800.00),
    -- Pechuga de Pollo: 200g / 100g (~25,000 COP/kg)
    (4,  1, 200.000,  5000.00), (4,  1, 100.000,  2500.00),
    -- Carne de Cerdo: 200g (~30,000 COP/kg)
    (5,  1, 200.000,  6000.00),
    -- Camarón Tigre: 6u (~120g, ~80,000 COP/kg)
    (6,  4,   6.000,  9600.00),
    -- Baby Beef: 300g (~45,000 COP/kg)
    (7,  1, 300.000, 13500.00),
    -- Churrasco: 300g (~42,000 COP/kg)
    (8,  1, 300.000, 12600.00),
    -- Entrecot: 300g (~48,000 COP/kg)
    (9,  1, 300.000, 14400.00),
    -- Huevo: 1u / 2u (~800 COP/u)
    (10, 4,   1.000,   800.00), (10, 4,   2.000,  1600.00),
    -- Lechuga Crespa: 20g (~3,500 COP/kg)
    (11, 1,  20.000,    70.00),
    -- Tomate: 2u (~120g, ~4,000 COP/kg)
    (12, 4,   2.000,   480.00),
    -- Cebolla Blanca: 10g (~3,000 COP/kg)
    (13, 1,  10.000,    30.00),
    -- Pepinillos: 3u (~60g, ~8,000 COP/kg)
    (14, 4,   3.000,   480.00),
    -- Pimentón Verde: 30g (~5,000 COP/kg)
    (15, 1,  30.000,   150.00),
    -- Hierbabuena Fresca: 2u (~10g, ~12,000 COP/kg)
    (16, 4,   2.000,   120.00),
    -- Albahaca Fresca: 1u (~5g, ~15,000 COP/kg)
    (17, 4,   1.000,    75.00),
    -- Champiñón Laminado: 80g (~9,000 COP/kg)
    (18, 1,  80.000,   720.00),
    -- Repollo: 50g (~2,500 COP/kg)
    (19, 1,  50.000,   125.00),
    -- Zanahoria: 30g (~3,000 COP/kg)
    (20, 1,  30.000,    90.00),
    -- Plátano Maduro: 0.5u (~150g, ~3,500 COP/kg)
    (21, 4,   0.500,   525.00),
    -- Papa Criolla: 150g (~5,000 COP/kg)
    (22, 1, 150.000,   750.00),
    -- Yuca: 150g (~3,500 COP/kg)
    (23, 1, 150.000,   525.00),
    -- Papa Salada: 1u (~200g, ~4,500 COP/kg)
    (24, 4,   1.000,   900.00),
    -- Queso Cheddar: 2u (~60g, ~35,000 COP/kg)
    (25, 4,   2.000,  2100.00),
    -- Queso Mozzarella: 2 láminas / 200g rallado (~28,000 COP/kg)
    (26, 4,   2.000,  1680.00), (26, 1, 200.000,  5600.00),
    -- Queso Parmesano: 15g (~65,000 COP/kg)
    (27, 1,  15.000,   975.00),
    -- Queso Crema: 80g (~20,000 COP/kg)
    (28, 1,  80.000,  1600.00),
    -- Crema de Leche: 100ml / 0.100 l (~22,000 COP/l)
    (29, 3,   0.100,  2200.00),
    -- Mantequilla: 30g (~30,000 COP/kg)
    (30, 1,  30.000,   900.00),
    -- Pan Brioche: 1u (~2,500 COP/u)
    (31, 4,   1.000,  2500.00),
    -- Pan Francés: 1u (~1,500 COP/u)
    (32, 4,   1.000,  1500.00),
    -- Harina de Trigo: 400g (~4,500 COP/kg)
    (33, 1, 400.000,  1800.00),
    -- Levadura Seca: 10g (~18,000 COP/kg)
    (34, 1,  10.000,   180.00),
    -- Salsa de Tomate Base: 100ml / 0.100 l (~12,000 COP/l)
    (35, 3,   0.100,  1200.00),
    -- Chimichurri Preparado: 10g (~25,000 COP/kg)
    (36, 1,  10.000,   250.00),
    -- Crema de Coco: 30ml / 0.030 l (~18,000 COP/l)
    (37, 3,   0.030,   540.00),
    -- Sal Refinada: 5g (~1,200 COP/kg)
    (38, 1,   5.000,     6.00),
    -- Sal Parrillera: 5g (~2,500 COP/kg)
    (39, 1,   5.000,    12.50),
    -- Orégano Seco: 1u (~2g, ~25,000 COP/kg)
    (40, 4,   1.000,    50.00),
    -- Pimienta Negra: 1u (~2g, ~45,000 COP/kg)
    (41, 4,   1.000,    90.00),
    -- Tocineta Ahumada: 2 tiras / 50g (~35,000 COP/kg)
    (42, 4,   2.000,  1400.00), (42, 1,  50.000,  1750.00),
    -- Pepperoni: 100g (~32,000 COP/kg)
    (43, 1, 100.000,  3200.00),
    -- Jamón Cocido: 100g (~22,000 COP/kg)
    (44, 1, 100.000,  2200.00),
    -- Bocadillo: 80g (~18,000 COP/kg)
    (45, 1,  80.000,  1440.00),
    -- Limón Tahití: 2u (~120g, ~5,000 COP/kg)
    (46, 4,   2.000,   600.00),
    -- Pulpa de Mango: 100g (~12,000 COP/kg)
    (47, 1, 100.000,  1200.00),
    -- Pulpa de Mora: 100g (~14,000 COP/kg)
    (48, 1, 100.000,  1400.00),
    -- Pulpa de Lulo: 100g (~13,000 COP/kg)
    (49, 1, 100.000,  1300.00),
    -- Piña en Almíbar: 60g (~9,000 COP/kg)
    (50, 1,  60.000,   540.00),
    -- Azúcar Blanca: 20g (~4,000 COP/kg)
    (51, 1,  20.000,    80.00),
    -- Stevia: 1u (~1g, ~25,000 COP/kg)
    (52, 4,   1.000,    25.00),
    -- Miel de Abejas: 15ml / 0.015 l (~22,000 COP/l)
    (53, 3,   0.015,   330.00),
    -- Empaque Caja Cartón: 1u (~1,200 COP/u)
    (54, 4,   1.000,  1200.00),
    -- Vaso Plástico: 1u (~350 COP/u)
    (55, 4,   1.000,   350.00),
    -- Vaso de Vidrio: 1u (~1,500 COP/u)
    (56, 4,   1.000,  1500.00),
    -- Pitillo de Papel: 2u (~300 COP/u)
    (57, 4,   2.000,   600.00),
    -- Papa a la Francesa Congelada: 150g (~10,000 COP/kg)
    (58, 1, 150.000,  1500.00),
    -- Papa en Cascos: 150g (~11,000 COP/kg)
    (59, 1, 150.000,  1650.00),
    -- Patacón Prefrito: 1u (~120g, ~12,000 COP/kg)
    (60, 4,   1.000,  1440.00),
    -- Arroz Blanco: 100g (~4,500 COP/kg)
    (61, 1, 100.000,   450.00),
    -- Frijol Cargamanto: 80g (~12,000 COP/kg)
    (62, 1,  80.000,   960.00),
    -- Lenteja Seca: 80g (~7,000 COP/kg)
    (63, 1,  80.000,   560.00),
    -- Maíz Tierno en Lata: 40g (~8,000 COP/kg)
    (64, 1,  40.000,   320.00),
    -- Pasta Larga Fettuccine: 200g (~7,500 COP/kg)
    (65, 1, 200.000,  1500.00),
    -- Pasta Corta Penne: 200g / 100g (~7,000 COP/kg)
    (66, 1, 200.000,  1400.00), (66, 1, 100.000,   700.00),
    -- Aceite Vegetal: 10ml / 0.010 l (~15,000 COP/l)
    (67, 3,   0.010,   150.00),
    -- Hielo en Cubo: 150g (~2,500 COP/kg)
    (68, 1, 150.000,   375.00);

-- =============================================================================
-- INVENTORY STOCK (supply_variant_id, storage_location_id is UNIQUE -> INSERT IGNORE)
-- storage_locations: Bodega=1, Cocina=2, Cuarto Frío=3, Bar=4
-- =============================================================================
INSERT IGNORE INTO inventory_stock (supply_variant_id, storage_location_id, current_quantity) VALUES
    (1,  3, 40.000), (1,  2, 15.000),
    (2,  3, 30.000), (2,  2, 10.000),
    (3,  3, 35.000), (3,  2, 12.000),
    (4,  3, 20.000), (4,  2,  8.000),
    (5,  3, 30.000), (5,  2, 10.000),
    (6,  3, 25.000), (6,  2,  8.000),
    (7,  3, 25.000), (7,  2,  8.000),
    (8,  3, 20.000), (8,  2,  5.000),
    (9,  3, 15.000), (9,  2,  5.000),
    (10, 3, 15.000), (10, 2,  5.000),
    (11, 3, 10.000), (11, 2,  3.000),
    (12, 3, 60.000), (12, 2, 20.000),
    (13, 3, 30.000), (13, 2, 10.000),
    (14, 1, 50.000), (14, 2, 20.000),
    (15, 1, 40.000), (15, 2, 15.000),
    (16, 1, 60.000), (16, 2, 20.000),
    (17, 1, 30.000), (17, 2, 10.000),
    (18, 1, 25.000), (18, 2,  8.000),
    (19, 1, 20.000), (19, 4, 10.000),
    (20, 1, 20.000), (20, 2,  8.000),
    (21, 1, 20.000), (21, 2,  6.000),
    (22, 1, 40.000), (22, 2, 15.000),
    (23, 1, 40.000), (23, 2, 15.000),
    (24, 1, 30.000), (24, 2, 10.000),
    (25, 1, 30.000), (25, 2, 10.000),
    (26, 1, 25.000), (26, 2,  8.000),
    (27, 1, 40.000), (27, 2, 15.000),
    (28, 3, 50.000), (28, 2, 20.000),
    (29, 3, 50.000), (29, 2, 20.000),
    (30, 3, 20.000), (30, 2,  8.000),
    (31, 3, 30.000), (31, 2, 10.000),
    (32, 3, 20.000), (32, 2,  6.000),
    (33, 3, 20.000), (33, 2,  8.000),
    (34, 3, 30.000), (34, 2, 10.000),
    (35, 1, 60.000), (35, 2, 20.000),
    (36, 1, 40.000), (36, 2, 15.000),
    (37, 1, 20.000), (37, 2,  8.000),
    (38, 1, 30.000), (38, 2, 10.000),
    (39, 1, 25.000), (39, 2, 10.000),
    (40, 1, 30.000), (40, 2, 10.000),
    (41, 1, 20.000), (41, 4, 10.000),
    (42, 1, 80.000), (42, 2, 30.000),
    (43, 1, 50.000), (43, 2, 15.000),
    (44, 1, 40.000), (44, 2, 15.000),
    (45, 1, 40.000), (45, 2, 15.000),
    (46, 3, 30.000), (46, 2, 10.000),
    (47, 3, 20.000), (47, 2,  8.000),
    (48, 3, 20.000), (48, 2,  8.000),
    (49, 3, 20.000), (49, 2,  8.000),
    (50, 1, 15.000), (50, 2,  5.000),
    (51, 3, 50.000), (51, 4, 20.000),
    (52, 3, 20.000), (52, 4, 10.000),
    (53, 3, 20.000), (53, 4, 10.000),
    (54, 3, 20.000), (54, 4, 10.000),
    (55, 1, 15.000), (55, 2,  5.000),
    (56, 1, 80.000), (56, 4, 30.000),
    (57, 1, 50.000), (57, 4, 20.000),
    (58, 1, 30.000), (58, 4, 15.000),
    (59, 1, 100.000),(59, 2, 30.000),
    (60, 1, 100.000),(60, 4, 40.000),
    (61, 1,  50.000),(61, 4, 20.000),
    (62, 1, 100.000),(62, 4, 40.000),
    (63, 3,  40.000),(63, 2, 15.000),
    (64, 3,  30.000),(64, 2, 10.000),
    (65, 3,  30.000),(65, 2, 10.000),
    (66, 1,  60.000),(66, 2, 20.000),
    (67, 1,  30.000),(67, 2, 10.000),
    (68, 1,  30.000),(68, 2, 10.000),
    (69, 1,  20.000),(69, 2,  8.000),
    (70, 1,  30.000),(70, 2, 10.000),
    (71, 1,  30.000),(71, 2, 10.000),
    (72, 1,  60.000),(72, 2, 20.000),
    (73, 1,  50.000),(73, 4, 30.000),
    (74, 1,  20.000),(74, 2,  8.000);

-- =============================================================================
-- PRODUCT CATEGORIES (only insert if table is empty)
-- =============================================================================
SET @cat_count = (SELECT COUNT(*) FROM categories);

INSERT INTO categories (name, description, enabled)
SELECT 'Hamburguesas', 'Hamburguesas artesanales con pan brioche', TRUE
FROM dual WHERE @cat_count = 0;

INSERT INTO categories (name, description, enabled)
SELECT 'Platos Típicos', 'Platos típicos colombianos', TRUE
FROM dual WHERE @cat_count = 0;

INSERT INTO categories (name, description, enabled)
SELECT 'Pizzas', 'Pizzas al horno, tamaño familiar', TRUE
FROM dual WHERE @cat_count = 0;

INSERT INTO categories (name, description, enabled)
SELECT 'Pasta / Italiana', 'Pastas de la casa con salsas artesanales', TRUE
FROM dual WHERE @cat_count = 0;

INSERT INTO categories (name, description, enabled)
SELECT 'Parrilla / Carnes', 'Cortes de res a la parrilla con guarnición', TRUE
FROM dual WHERE @cat_count = 0;

INSERT INTO categories (name, description, enabled)
SELECT 'Bebidas Naturales', 'Jugos naturales y limonadas con fruta fresca', TRUE
FROM dual WHERE @cat_count = 0;

-- =============================================================================
-- OPTION CATEGORIES (only insert if table is empty)
-- =============================================================================
SET @ocat_count = (SELECT COUNT(*) FROM option_categories);

INSERT INTO option_categories (name, description)
SELECT 'Proteína Hamburguesa', 'Elección de proteína para hamburguesa'
FROM dual WHERE @ocat_count = 0;

INSERT INTO option_categories (name, description)
SELECT 'Queso Hamburguesa', 'Elección de queso para hamburguesa'
FROM dual WHERE @ocat_count = 0;

INSERT INTO option_categories (name, description)
SELECT 'Vegetales Hamburguesa', 'Vegetales opcionales para hamburguesa'
FROM dual WHERE @ocat_count = 0;

INSERT INTO option_categories (name, description)
SELECT 'Acompañamiento Hamburguesa', 'Acompañamiento para hamburguesa'
FROM dual WHERE @ocat_count = 0;

INSERT INTO option_categories (name, description)
SELECT 'Adición Extra Hamburguesa', 'Ingredientes extra para hamburguesa'
FROM dual WHERE @ocat_count = 0;

INSERT INTO option_categories (name, description)
SELECT 'Sabor Pizza', 'Elección de sabor/topping para pizza'
FROM dual WHERE @ocat_count = 0;

INSERT INTO option_categories (name, description)
SELECT 'Vegetal Extra Pizza', 'Vegetales adicionales para pizza'
FROM dual WHERE @ocat_count = 0;

INSERT INTO option_categories (name, description)
SELECT 'Borde Pizza', 'Tipo de borde para pizza'
FROM dual WHERE @ocat_count = 0;

INSERT INTO option_categories (name, description)
SELECT 'Salsa Pasta', 'Elección de salsa para pasta'
FROM dual WHERE @ocat_count = 0;

INSERT INTO option_categories (name, description)
SELECT 'Proteína Extra Pasta', 'Proteína adicional para pasta'
FROM dual WHERE @ocat_count = 0;

INSERT INTO option_categories (name, description)
SELECT 'Especia Pasta', 'Especia para pasta'
FROM dual WHERE @ocat_count = 0;

INSERT INTO option_categories (name, description)
SELECT 'Corte Parrilla', 'Elección de corte de res'
FROM dual WHERE @ocat_count = 0;

INSERT INTO option_categories (name, description)
SELECT 'Término Parrilla', 'Punto de cocción del corte'
FROM dual WHERE @ocat_count = 0;

INSERT INTO option_categories (name, description)
SELECT 'Guarnición Parrilla', 'Guarnición para el corte de res'
FROM dual WHERE @ocat_count = 0;

INSERT INTO option_categories (name, description)
SELECT 'Base Fruta Bebida', 'Fruta o pulpa base para la bebida'
FROM dual WHERE @ocat_count = 0;

INSERT INTO option_categories (name, description)
SELECT 'Endulzante Bebida', 'Tipo de endulzante para la bebida'
FROM dual WHERE @ocat_count = 0;

INSERT INTO option_categories (name, description)
SELECT 'Sabor Especial Bebida', 'Toque especial para la bebida'
FROM dual WHERE @ocat_count = 0;

-- =============================================================================
-- OPTION CATEGORIES selection_mode (V37 added selection_type + replace_supply_category_id)
-- Idempotent: ensures every seeded category is SINGLE_CHOICE with no substitution.
-- Re-runs of data.sql are safe: the WHERE guard skips rows already in that mode.
-- =============================================================================
UPDATE option_categories SET selection_type = 'SINGLE_CHOICE'
WHERE selection_type IS NULL OR selection_type <> 'SINGLE_CHOICE';

UPDATE option_categories SET replace_supply_category_id = NULL
WHERE replace_supply_category_id IS NOT NULL;

-- =============================================================================
-- PRODUCTS (only insert if table is empty)
-- categories: Hamburguesas=1, Platos Típicos=2, Pizzas=3,
--             Pasta/Italiana=4, Parrilla/Carnes=5, Bebidas Naturales=6
-- areas: Cocina=1, Bar=3
-- =============================================================================
SET @prod_count = (SELECT COUNT(*) FROM products);

INSERT INTO products (name, description, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Hamburguesa Double Bacon Cheese', 'Doble carne de res a la parrilla, queso cheddar fundido, tocineta ahumada crujiente y cebolla caramelizada en pan brioche dorado.', 28000.00, TRUE, 1, 1, 10
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, description, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Pizza Especial Familiar', 'Pizza familiar con pepperoni, jamón cocido, champiñones y pimentón sobre mozzarella y base de tomate de la casa.', 38000.00, TRUE, 3, 1, 25
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, description, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Pasta de la Casa', 'Pasta larga fetuccine con salsa a elección (boloñesa, carbonara o alfredo), terminada con parmesano rallado y hierbas frescas.', 22000.00, TRUE, 4, 1, 18
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, description, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Corte de Res Premium', 'Corte de res premium a la parrilla (Baby Beef, Churrasco o Entrecot) al término que prefieras, con guarnición de papa criolla, yuca o ensalada.', 55000.00, TRUE, 5, 1, 22
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, description, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Limonada / Jugo Natural', 'Limonada o jugo natural con fruta fresca a elección (mango, mora o lulo) y endulzante a tu gusto (azúcar, stevia o miel).', 8000.00, TRUE, 6, 3, 5
FROM dual WHERE @prod_count = 0;

-- V20 extra products
INSERT INTO products (name, description, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Hamburguesa Clásica', 'Hamburguesa clásica con carne de res a la parrilla, queso cheddar, vegetales frescos y pan brioche tostado.', 18000.00, TRUE, 1, 1, 8
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, description, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Hamburguesa BBQ', 'Hamburguesa con doble carne, salsa BBQ ahumada de la casa, tocineta, queso cheddar y cebolla crocante en pan brioche.', 32000.00, TRUE, 1, 1, 10
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, description, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Hamburguesa Veggie', 'Hamburguesa vegetariana con lenteja preparada, champiñones salteados, vegetales frescos y queso crema sobre pan brioche.', 22000.00, TRUE, 1, 1, 12
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, description, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Bandeja Paisa', 'Bandeja paisa tradicional con frijol, arroz, carne molida, chicharrón, huevo frito, plátano maduro y aguacate.', 25000.00, TRUE, 2, 1, 30
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, description, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Sancocho de Gallina', 'Sancocho de gallina criolla con papa, yuca, plátano, mazorca y guacamole, servido con arroz blanco y ají.', 22000.00, TRUE, 2, 1, 35
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, description, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Ajiaco Santafereño', 'Ajiaco santafereño con pollo, tres tipos de papa (criolla, sabanera y pastusa), mazorca, guascas y alcaparras, con crema y alcaparras aparte.', 20000.00, TRUE, 2, 1, 35
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, description, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Pizza Personal', 'Pizza personal con un topping a elección, borde tradicional y base de mozzarella con salsa de tomate de la casa.', 22000.00, TRUE, 3, 1, 20
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, description, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Pizza Vegetariana', 'Pizza vegetariana con pimentón, champiñones, maíz tierno y aceitunas sobre mozzarella y base de tomate.', 32000.00, TRUE, 3, 1, 22
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, description, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Pizza Hawaiana', 'Pizza hawaiana con jamón cocido, piña en almíbar y mozzarella sobre base de tomate ligeramente dulce.', 30000.00, TRUE, 3, 1, 20
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, description, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Lasagna Clásica', 'Lasagna clásica con capas de pasta, salsa boloñesa, queso mozzarella y parmesano gratinado al horno.', 25000.00, TRUE, 4, 1, 25
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, description, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Spaghetti Carbonara', 'Spaghetti con salsa carbonara cremosa a base de tocineta ahumada, huevo, queso parmesano y pimienta negra.', 24000.00, TRUE, 4, 1, 18
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, description, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Costillas BBQ', 'Costillas de cerdo glaseadas con salsa BBQ ahumada, servidas con papa a la francesa y ensalada de la casa.', 42000.00, TRUE, 5, 1, 25
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, description, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Lomo de Cerdo', 'Lomo de cerdo a la parrilla con chimichurri, papa criolla y ensalada fresca.', 38000.00, TRUE, 5, 1, 20
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, description, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Malteada', 'Malteada cremosa con helado, leche y un sabor a elección, servida bien fría.', 12000.00, TRUE, 6, 3, 5
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, description, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Agua de Coco', 'Agua de coco natural servida bien fría, ideal para hidratarse.', 6000.00, TRUE, 6, 3, 3
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, description, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Té Helado', 'Té negro helado con limón, endulzado al gusto y servido con hielo.', 7000.00, TRUE, 6, 3, 3
FROM dual WHERE @prod_count = 0;

-- =============================================================================
-- PRODUCTS description backfill (V17 added description TEXT nullable)
-- Idempotent: only fills rows where description IS NULL, preserves any
-- description set by the INSERTs above or by future manual edits.
-- Safe to re-run: case expression targets known product ids 1..21 only.
-- =============================================================================
UPDATE products SET description = CASE id
    WHEN  1 THEN 'Doble carne de res a la parrilla, queso cheddar fundido, tocineta ahumada crujiente y cebolla caramelizada en pan brioche dorado.'
    WHEN  2 THEN 'Pizza familiar con pepperoni, jamón cocido, champiñones y pimentón sobre mozzarella y base de tomate de la casa.'
    WHEN  3 THEN 'Pasta larga fetuccine con salsa a elección (boloñesa, carbonara o alfredo), terminada con parmesano rallado y hierbas frescas.'
    WHEN  4 THEN 'Corte de res premium a la parrilla (Baby Beef, Churrasco o Entrecot) al término que prefieras, con guarnición de papa criolla, yuca o ensalada.'
    WHEN  5 THEN 'Limonada o jugo natural con fruta fresca a elección (mango, mora o lulo) y endulzante a tu gusto (azúcar, stevia o miel).'
    WHEN  6 THEN 'Hamburguesa clásica con carne de res a la parrilla, queso cheddar, vegetales frescos y pan brioche tostado.'
    WHEN  7 THEN 'Hamburguesa con doble carne, salsa BBQ ahumada de la casa, tocineta, queso cheddar y cebolla crocante en pan brioche.'
    WHEN  8 THEN 'Hamburguesa vegetariana con lenteja preparada, champiñones salteados, vegetales frescos y queso crema sobre pan brioche.'
    WHEN  9 THEN 'Bandeja paisa tradicional con frijol, arroz, carne molida, chicharrón, huevo frito, plátano maduro y aguacate.'
    WHEN 10 THEN 'Sancocho de gallina criolla con papa, yuca, plátano, mazorca y guacamole, servido con arroz blanco y ají.'
    WHEN 11 THEN 'Ajiaco santafereño con pollo, tres tipos de papa (criolla, sabanera y pastusa), mazorca, guascas y alcaparras, con crema y alcaparras aparte.'
    WHEN 12 THEN 'Pizza personal con un topping a elección, borde tradicional y base de mozzarella con salsa de tomate de la casa.'
    WHEN 13 THEN 'Pizza vegetariana con pimentón, champiñones, maíz tierno y aceitunas sobre mozzarella y base de tomate.'
    WHEN 14 THEN 'Pizza hawaiana con jamón cocido, piña en almíbar y mozzarella sobre base de tomate ligeramente dulce.'
    WHEN 15 THEN 'Lasagna clásica con capas de pasta, salsa boloñesa, queso mozzarella y parmesano gratinado al horno.'
    WHEN 16 THEN 'Spaghetti con salsa carbonara cremosa a base de tocineta ahumada, huevo, queso parmesano y pimienta negra.'
    WHEN 17 THEN 'Costillas de cerdo glaseadas con salsa BBQ ahumada, servidas con papa a la francesa y ensalada de la casa.'
    WHEN 18 THEN 'Lomo de cerdo a la parrilla con chimichurri, papa criolla y ensalada fresca.'
    WHEN 19 THEN 'Malteada cremosa con helado, leche y un sabor a elección, servida bien fría.'
    WHEN 20 THEN 'Agua de coco natural servida bien fría, ideal para hidratarse.'
    WHEN 21 THEN 'Té negro helado con limón, endulzado al gusto y servido con hielo.'
END
WHERE description IS NULL;

-- =============================================================================
-- PRODUCT OPTIONS (only insert if table is empty)
-- =============================================================================
SET @opt_count = (SELECT COUNT(*) FROM product_options);

INSERT INTO product_options (name, option_category_id)
SELECT 'Carne de Res 150g', 1 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Pollo Apanado 180g', 1 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Lenteja Preparada 150g', 1 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Queso Cheddar x2', 2 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Queso Mozzarella x2', 2 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Tomate 2 rodajas', 3 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Cebolla Blanca 10g', 3 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Papa a la Francesa 150g', 4 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Papa en Cascos 150g', 4 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Sin Acompañamiento', 4 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Tocineta Ahumada x2', 5 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Huevo 1 unidad', 5 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Pepinillos x3', 5 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Pepperoni 100g', 6 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Jamón Cocido 100g', 6 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Champiñón Laminado 80g', 6 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Pimentón Verde 30g', 7 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Maíz Tierno 40g', 7 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Piña en Almíbar 60g', 7 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Borde Queso Crema 80g', 8 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Borde Bocadillo 80g', 8 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Salsa Alfredo', 9 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Salsa Boloñesa', 9 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Salsa Carbonara', 9 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Camarón Tigre x6', 10 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Pechuga en Cubos 100g', 10 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Sin Proteína Extra', 10 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Orégano Seco', 11 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Albahaca Fresca', 11 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Pimienta Negra', 11 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Baby Beef 300g', 12 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Churrasco 300g', 12 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Entrecot 300g', 12 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Azul', 13 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Medio', 13 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT '3/4', 13 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Bien Asado', 13 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Papa Criolla 150g', 14 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Yuca Frita 150g', 14 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Ensalada de Papa 150g', 14 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Limón Tahití x2', 15 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Pulpa de Mango 100g', 15 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Pulpa de Mora 100g', 15 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Pulpa de Lulo 100g', 15 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Azúcar Blanca 20g', 16 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Stevia 1 sobre', 16 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Miel de Abejas 15ml', 16 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Crema de Coco 30ml', 17 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Hierbabuena Fresca x2', 17 FROM dual WHERE @opt_count = 0;

-- =============================================================================
-- PRODUCT ↔ OPTIONS (PRIMARY KEY (product_id, option_id) -> INSERT IGNORE)
-- All 21 products
-- =============================================================================
INSERT IGNORE INTO product_product_options (product_id, option_id) VALUES
    -- 1. Hamburguesa Double Bacon Cheese
    (1,  1), (1,  2), (1,  3),
    (1,  4), (1,  5),
    (1,  6), (1,  7),
    (1,  8), (1,  9), (1, 10),
    (1, 11), (1, 12), (1, 13),

    -- 2. Pizza Especial Familiar
    (2, 14), (2, 15), (2, 16),
    (2, 17), (2, 18), (2, 19),
    (2, 20), (2, 21),

    -- 3. Pasta de la Casa
    (3, 22), (3, 23), (3, 24),
    (3, 25), (3, 26), (3, 27),
    (3, 28), (3, 29), (3, 30),

    -- 4. Corte de Res Premium
    (4, 31), (4, 32), (4, 33),
    (4, 34), (4, 35), (4, 36), (4, 37),
    (4, 38), (4, 39), (4, 40),

    -- 5. Limonada / Jugo Natural
    (5, 41), (5, 42), (5, 43), (5, 44),
    (5, 45), (5, 46), (5, 47),
    (5, 48), (5, 49),

    -- 6. Hamburguesa Clásica
    (6,  1), (6,  2), (6,  3),
    (6,  4), (6,  5),
    (6,  6), (6,  7),
    (6,  8), (6,  9), (6, 10),
    (6, 11), (6, 12), (6, 13),

    -- 7. Hamburguesa BBQ
    (7,  1), (7,  2), (7,  3),
    (7,  4), (7,  5),
    (7,  6), (7,  7),
    (7,  8), (7,  9), (7, 10),
    (7, 11), (7, 12), (7, 13),

    -- 8. Hamburguesa Veggie
    (8,  1), (8,  2), (8,  3),
    (8,  4), (8,  5),
    (8,  6), (8,  7),
    (8,  8), (8,  9), (8, 10),
    (8, 11), (8, 12), (8, 13),

    -- 9. Bandeja Paisa (Platos Típicos — guarnición)
    (9, 38), (9, 39), (9, 40),

    -- 10. Sancocho de Gallina (Platos Típicos — guarnición)
    (10, 38), (10, 39), (10, 40),

    -- 11. Ajiaco Santafereño (Platos Típicos — guarnición)
    (11, 38), (11, 39), (11, 40),

    -- 12. Pizza Personal
    (12, 14), (12, 15), (12, 16),
    (12, 17), (12, 18), (12, 19),
    (12, 20), (12, 21),

    -- 13. Pizza Vegetariana
    (13, 14), (13, 15), (13, 16),
    (13, 17), (13, 18), (13, 19),
    (13, 20), (13, 21),

    -- 14. Pizza Hawaiana
    (14, 14), (14, 15), (14, 16),
    (14, 17), (14, 18), (14, 19),
    (14, 20), (14, 21),

    -- 15. Lasagna Clásica
    (15, 22), (15, 23), (15, 24),
    (15, 25), (15, 26), (15, 27),
    (15, 28), (15, 29), (15, 30),

    -- 16. Spaghetti Carbonara
    (16, 22), (16, 23), (16, 24),
    (16, 25), (16, 26), (16, 27),
    (16, 28), (16, 29), (16, 30),

    -- 17. Costillas BBQ
    (17, 31), (17, 32), (17, 33),
    (17, 34), (17, 35), (17, 36), (17, 37),
    (17, 38), (17, 39), (17, 40),

    -- 18. Lomo de Cerdo
    (18, 31), (18, 32), (18, 33),
    (18, 34), (18, 35), (18, 36), (18, 37),
    (18, 38), (18, 39), (18, 40),

    -- 19. Malteada
    (19, 41), (19, 42), (19, 43), (19, 44),
    (19, 45), (19, 46), (19, 47),
    (19, 48), (19, 49),

    -- 20. Agua de Coco
    (20, 41), (20, 42), (20, 43), (20, 44),
    (20, 45), (20, 46), (20, 47),
    (20, 48), (20, 49),

    -- 21. Té Helado
    (21, 41), (21, 42), (21, 43), (21, 44),
    (21, 45), (21, 46), (21, 47),
    (21, 48), (21, 49);

-- =============================================================================
-- PRODUCT RECIPES (product_id, supply_variant_id is UNIQUE -> INSERT IGNORE)
-- All 21 products
-- =============================================================================
INSERT IGNORE INTO product_recipes (product_id, supply_variant_id, required_quantity) VALUES
    -- 1. Hamburguesa Double Bacon Cheese (28000)
    (1,  35, 1.000), (1,   1, 1.000), (1,  28, 1.000), (1,  46, 1.000),
    (1,  15, 1.000), (1,  16, 1.000), (1,  14, 1.000), (1,  63, 1.000),

    -- 2. Pizza Especial Familiar (38000) — large
    (2,  37, 0.625), (2,  39, 1.500), (2,  30, 1.000), (2,  48, 0.500),
    (2,  21, 0.500), (2,  44, 1.000), (2,  73, 1.000),

    -- 3. Pasta de la Casa (22000)
    (3,  70, 0.500), (3,   2, 1.000), (3,  39, 0.500), (3,  31, 1.000),
    (3,  36, 0.500),

    -- 4. Corte de Res Premium (55000)
    (4,  11, 1.000), (4,  43, 1.000), (4,  34, 1.000), (4,  73, 1.000),
    (4,  25, 1.000),

    -- 5. Limonada / Jugo Natural (8000)
    (5,  52, 0.500), (5,  56, 1.000), (5,  74, 0.500), (5,  60, 1.000),
    (5,  62, 1.000),

    -- 6. Hamburguesa Clásica (18000)
    (6,  35, 1.000), (6,   2, 1.000), (6,  15, 1.000), (6,  63, 1.000),

    -- 7. Hamburguesa BBQ (32000)
    (7,  35, 1.000), (7,   1, 1.000), (7,  28, 1.000), (7,  46, 1.000),
    (7,  16, 1.000), (7,  64, 1.000), (7,  40, 1.000), (7,  43, 1.000),

    -- 8. Hamburguesa Veggie (22000)
    (8,  35, 1.000), (8,   4, 1.000), (8,  21, 1.000), (8,  18, 1.000),
    (8,  29, 1.000), (8,  14, 1.000), (8,  64, 1.000),

    -- 9. Bandeja Paisa (25000)
    (9,  66, 1.000), (9,  67, 1.000), (9,   6, 1.000), (9,  12, 1.000),
    (9,  24, 1.000), (9,  46, 1.000), (9,  73, 1.000), (9,  42, 1.000),

    -- 10. Sancocho de Gallina (22000)
    (10,  5, 1.000), (10, 25, 1.000), (10, 26, 1.000), (10, 23, 1.000),
    (10, 16, 1.000), (10, 19, 1.000), (10, 66, 1.000), (10, 42, 1.000),

    -- 11. Ajiaco Santafereño (20000)
    (11,  5, 1.000), (11, 25, 1.000), (11, 27, 1.000), (11, 23, 1.000),
    (11, 19, 1.000), (11, 33, 1.000), (11, 66, 1.000), (11, 42, 1.000),

    -- 12. Pizza Personal (22000)
    (12, 37, 0.250), (12, 39, 0.300), (12, 30, 0.250), (12, 48, 0.300),
    (12, 44, 1.000), (12, 73, 0.500),

    -- 13. Pizza Vegetariana (32000)
    (13, 37, 0.375), (13, 39, 0.400), (13, 30, 0.400), (13, 21, 1.000),
    (13, 18, 1.000), (13, 69, 0.500), (13, 17, 0.500), (13, 44, 1.000),
    (13, 73, 0.500),

    -- 14. Pizza Hawaiana (30000)
    (14, 37, 0.375), (14, 39, 0.400), (14, 30, 0.400), (14, 49, 0.500),
    (14, 55, 1.000), (14, 44, 1.000), (14, 73, 0.500),

    -- 15. Lasagna Clásica (25000)
    (15, 70, 0.500), (15,  2, 1.000), (15, 30, 0.500), (15, 31, 1.000),
    (15, 39, 1.000),

    -- 16. Spaghetti Carbonara (24000)
    (16, 70, 0.500), (16, 47, 1.000), (16, 31, 1.000), (16, 12, 1.000),
    (16, 45, 1.000),

    -- 17. Costillas BBQ (42000) — pork ribs
    (17,  7, 1.500), (17, 39, 0.500), (17, 47, 0.500), (17, 34, 1.000),
    (17, 43, 1.000), (17, 25, 1.000),

    -- 18. Lomo de Cerdo (38000)
    (18,  7, 1.000), (18, 64, 1.000), (18, 23, 1.000), (18, 34, 1.000),
    (18, 43, 1.000),

    -- 19. Malteada (12000)
    (19, 33, 0.500), (19, 56, 1.000), (19, 74, 0.333), (19, 60, 1.000),
    (19, 62, 1.000),

    -- 20. Agua de Coco (6000)
    (20, 41, 1.000), (20, 74, 0.200), (20, 60, 1.000), (20, 62, 1.000),

    -- 21. Té Helado (7000)
    (21, 74, 0.500), (21, 56, 1.000), (21, 51, 1.000), (21, 61, 1.000),
    (21, 62, 1.000);

-- =============================================================================
-- OPTION RECIPES (option_id, supply_variant_id is UNIQUE -> INSERT IGNORE)
-- All 49 options
-- =============================================================================
INSERT IGNORE INTO option_recipes (option_id, supply_variant_id, required_quantity) VALUES
    -- 1. Proteína Hamburguesa
    (1,  1, 1.000), (2,  3, 1.000), (3,  4, 1.000),
    -- 2. Queso Hamburguesa
    (4, 28, 1.000), (5, 29, 1.000),
    -- 3. Vegetales Hamburguesa
    (6, 15, 1.000), (7, 16, 1.000),
    -- 4. Acompañamiento Hamburguesa
    (8, 63, 1.000), (9, 64, 1.000),
    -- 5. Adición Extra Hamburguesa
    (11, 46, 1.000), (12, 12, 1.000), (13, 17, 1.000),
    -- 6. Sabor Pizza
    (14, 48, 1.000), (15, 49, 1.000), (16, 21, 1.000),
    -- 7. Vegetal Extra Pizza
    (17, 18, 1.000), (18, 69, 1.000), (19, 55, 1.000),
    -- 8. Borde Pizza
    (20, 32, 1.000), (21, 50, 1.000),
    -- 9. Salsa Pasta (Boloñesa = carne + tomate; Carbonara = tocineta + huevo)
    (22, 33, 1.000), (23,  2, 1.000), (23, 39, 0.500),
    (24, 47, 1.000), (24, 12, 1.000),
    -- 10. Proteína Extra Pasta
    (25,  8, 1.000), (26,  6, 1.000),
    -- 11. Especia Pasta
    (28, 44, 1.000), (29, 20, 1.000), (30, 45, 1.000),
    -- 12. Corte Parrilla
    (31,  9, 1.000), (32, 10, 1.000), (33, 11, 1.000),
    -- 13. Término Parrilla (no recipe — cooking terms)
    -- 14. Guarnición Parrilla
    (38, 25, 1.000), (39, 26, 1.000), (40, 25, 1.000),
    -- 15. Base Fruta Bebida
    (41, 51, 1.000), (42, 52, 1.000), (43, 53, 1.000), (44, 54, 1.000),
    -- 16. Endulzante Bebida
    (45, 56, 1.000), (46, 57, 1.000), (47, 58, 1.000),
    -- 17. Sabor Especial Bebida
    (48, 41, 1.000), (49, 19, 1.000);

-- =============================================================================
-- Q2 2026 STATISTICS SEED (Apr + May + Jun, idempotent, append-only)
-- =============================================================================
-- Simulates three months (Apr/May/Jun 2026) of restaurant operations.
-- Re-runnable: cleanup runs first in FK order. Varies order volume and
-- product mix per month via different CTE seed constants.
--   * users document 'TEST-%' / email '*.test@rms.local' / user_id 1001-1006
--   * schedules name 'Turno%' / schedule_id 1 / shift_id 1..14
--   * orders id 100001+ / order_details id 200001+ / inventory_movements id auto
--   * Apr: baseline (slot filter 3, key 7/31)
--   * May: busier    (slot filter 4, key 11/37)
--   * Jun: quieter   (slot filter 2, key 13/41)
-- =============================================================================

-- Clean any prior Q2 2026 seeded data (idempotent re-run safety).
-- Order matters: clear child tables first because of FK constraints.
DELETE FROM order_detail_options
  WHERE order_detail_id IN (
    SELECT id FROM order_details
    WHERE order_id IN (SELECT id FROM orders WHERE date >= '2026-04-01' AND date < '2026-07-01'));
DELETE FROM order_details
  WHERE order_id IN (SELECT id FROM orders WHERE date >= '2026-04-01' AND date < '2026-07-01');
DELETE FROM order_preparation_areas
  WHERE order_id IN (SELECT id FROM orders WHERE date >= '2026-04-01' AND date < '2026-07-01');
DELETE FROM inventory_movements
  WHERE created_at >= '2026-04-01' AND created_at < '2026-07-01';
DELETE FROM time_logs
  WHERE timestamp >= '2026-04-01' AND timestamp < '2026-07-01'
     OR related_shift_id IN (SELECT id FROM schedule_shifts WHERE schedule_id IN (SELECT id FROM schedules WHERE name LIKE 'Turno%'));
DELETE FROM orders WHERE date >= '2026-04-01' AND date < '2026-07-01';
DELETE FROM worker_schedule_assignments
  WHERE schedule_id IN (SELECT id FROM schedules WHERE name LIKE 'Turno%');
DELETE FROM schedule_shifts
  WHERE schedule_id IN (SELECT id FROM schedules WHERE name LIKE 'Turno%');
DELETE FROM schedules WHERE name LIKE 'Turno%';
DELETE FROM user_assigned_areas
  WHERE user_id IN (SELECT id FROM users WHERE document LIKE 'TEST-%');
DELETE FROM users WHERE document LIKE 'TEST-%';

-- =============================================================================
-- TEST USERS (6 workers only; admin is auto-created by AdminInitializer)
-- 1001-1002 = FOH (Servicio + Caja) at 2,500,000 COP/month
-- 1003-1006 = BOH (Cocina) at 2,800,000 COP/month
-- =============================================================================
INSERT INTO users (id, document, name, email, password, address, phone, role, status, salary) VALUES
  (1001, 'TEST-WK-1',    'Trabajador 1', 'wk1.test@rms.local',     NULL, 'Calle 1 #1-01', '3000000001', 'WORKER', 'ACTIVE', 2500000.00),
  (1002, 'TEST-WK-2',    'Trabajador 2', 'wk2.test@rms.local',     NULL, 'Calle 1 #1-02', '3000000002', 'WORKER', 'ACTIVE', 2500000.00),
  (1003, 'TEST-WK-3',    'Trabajador 3', 'wk3.test@rms.local',     NULL, 'Calle 1 #1-03', '3000000003', 'WORKER', 'ACTIVE', 2800000.00),
  (1004, 'TEST-WK-4',    'Trabajador 4', 'wk4.test@rms.local',     NULL, 'Calle 1 #1-04', '3000000004', 'WORKER', 'ACTIVE', 2800000.00),
  (1005, 'TEST-WK-5',    'Trabajador 5', 'wk5.test@rms.local',     NULL, 'Calle 1 #1-05', '3000000005', 'WORKER', 'ACTIVE', 2800000.00),
  (1006, 'TEST-WK-6',    'Trabajador 6', 'wk6.test@rms.local',     NULL, 'Calle 1 #1-06', '3000000006', 'WORKER', 'ACTIVE', 2800000.00);

-- =============================================================================
-- USER ASSIGNED AREAS (PK user_id + area_id -> INSERT IGNORE)
-- Areas: 1=Cocina(KITCHEN), 2=Servicio(SERVICE), 3=Bar(BAR), 4=Caja(CASH)
-- =============================================================================
INSERT IGNORE INTO user_assigned_areas (user_id, area_id) VALUES
  (1001, 2), (1001, 4), (1002, 2), (1002, 4),
  (1003, 1), (1004, 1), (1005, 1), (1006, 1);

-- =============================================================================
-- SCHEDULE + SHIFTS (deterministic IDs)
-- 1 schedule ("Turno Estándar") + 14 shifts (2 per day-of-week).
-- Lunch 11:00-15:00 (4h) ids 1..7, Dinner 18:00-23:00 (5h) ids 8..14.
-- DAYOFWEEK returns 1=Sun..7=Sat; (DAYOFWEEK + 5) % 7 + 1 maps to 1..7.
-- =============================================================================
INSERT INTO schedules (id, name, description) VALUES (1, 'Turno Estándar', 'Turno de operación 11:00-23:00');

INSERT INTO schedule_shifts (id, schedule_id, day_of_week, start_time, end_time) VALUES
  (1, 1, 'MONDAY', '11:00:00', '15:00:00'), (2, 1, 'TUESDAY', '11:00:00', '15:00:00'),
  (3, 1, 'WEDNESDAY', '11:00:00', '15:00:00'), (4, 1, 'THURSDAY', '11:00:00', '15:00:00'),
  (5, 1, 'FRIDAY', '11:00:00', '15:00:00'), (6, 1, 'SATURDAY', '11:00:00', '15:00:00'),
  (7, 1, 'SUNDAY', '11:00:00', '15:00:00'),
  (8, 1, 'MONDAY', '18:00:00', '23:00:00'), (9, 1, 'TUESDAY', '18:00:00', '23:00:00'),
  (10, 1, 'WEDNESDAY', '18:00:00', '23:00:00'), (11, 1, 'THURSDAY', '18:00:00', '23:00:00'),
  (12, 1, 'FRIDAY', '18:00:00', '23:00:00'), (13, 1, 'SATURDAY', '18:00:00', '23:00:00'),
  (14, 1, 'SUNDAY', '18:00:00', '23:00:00');

-- =============================================================================
-- WORKER SCHEDULE ASSIGNMENTS (UNIQUE worker_id + schedule_id -> INSERT IGNORE)
-- =============================================================================
INSERT IGNORE INTO worker_schedule_assignments (worker_id, schedule_id) VALUES
  (1001, 1), (1002, 1), (1003, 1), (1004, 1), (1005, 1), (1006, 1);

-- =============================================================================
-- TIME LOGS (6 workers x 91 days = 546 rows)
-- Slots 0/2/4 => lunch shift (ids 1..7), slots 1/3/5 => dinner shift (ids 8..14).
-- =============================================================================
INSERT INTO time_logs (worker_id, timestamp, type, within_shift, related_shift_id)
WITH RECURSIVE
  days  AS (SELECT 0 AS d UNION ALL SELECT d + 1 FROM days WHERE d < 91),
  slots AS (SELECT 0 AS slot UNION ALL SELECT slot + 1 FROM slots WHERE slot < 5)
SELECT
  1001 + slots.slot,
  CASE WHEN slots.slot IN (0, 2, 4)
       THEN TIMESTAMP('2026-04-01 11:30:00') + INTERVAL days.d DAY
       ELSE TIMESTAMP('2026-04-01 18:30:00') + INTERVAL days.d DAY
  END,
  'IN', TRUE,
  CASE WHEN slots.slot IN (0, 2, 4)
       THEN ((DAYOFWEEK('2026-04-01' + INTERVAL days.d DAY) + 5) % 7) + 1
       ELSE ((DAYOFWEEK('2026-04-01' + INTERVAL days.d DAY) + 5) % 7) + 8
  END
FROM days CROSS JOIN slots;

-- =============================================================================
-- ORDERS (3 months, ~6500 total, varying volume per month)
-- 8-min slots base 12:00. Each month uses a different weekday filter:
--   Apr: baseline   — NOT (dow in 2,3,4 AND n % 3 = 0)
--   May: busier     — NOT (dow in 2,3,4 AND n % 4 = 0)  (75% kept → ~2600 orders)
--   Jun: quieter    — NOT (dow in 2,3,4 AND n % 2 = 0)  (50% kept → ~1700 orders)
-- =============================================================================
INSERT INTO orders (id, date, status, table_id, party_size, open_time, close_time)
WITH RECURSIVE
  days AS (SELECT 0 AS d UNION ALL SELECT d + 1 FROM days WHERE d < 91),
  slots AS (SELECT 0 AS n UNION ALL SELECT n + 1 FROM slots WHERE n < 75),
  base_ts AS (
    SELECT
      days.d, slots.n,
      TIMESTAMP('2026-04-01 12:00:00') + INTERVAL days.d DAY + INTERVAL (slots.n * 8) MINUTE AS bt,
      HOUR(TIMESTAMP('2026-04-01 12:00:00') + INTERVAL (slots.n * 8) MINUTE) AS hr,
      DAYOFWEEK('2026-04-01' + INTERVAL days.d DAY) AS dow
    FROM days CROSS JOIN slots
  )
SELECT
  ROW_NUMBER() OVER (ORDER BY bt.d, bt.n) + 100000,
  bt.bt,
  'DELIVERED',
  (bt.n % 5) + 1,
  2 + ((bt.n + bt.d) % 3),
  bt.bt,
  bt.bt + INTERVAL 45 MINUTE
FROM base_ts bt
WHERE bt.hr BETWEEN 11 AND 22
  AND CASE
    WHEN bt.d < 30  THEN NOT (bt.dow IN (2,3,4) AND (bt.n % 3) = 0)  -- Apr
    WHEN bt.d < 61  THEN NOT (bt.dow IN (2,3,4) AND (bt.n % 4) = 0)  -- May
    ELSE                 NOT (bt.dow IN (2,3,4) AND (bt.n % 2) = 0)  -- Jun
  END;

-- =============================================================================
-- ORDER DETAILS (2-4 per order, weighted toward top sellers)
-- Product selection key varies by month: Apr=7/31, May=11/37, Jun=13/41.
-- product_list weight ranges stay the same but the different key produces
-- a different distribution each month (different products dominate).
-- =============================================================================
INSERT INTO order_details (id, order_id, product_id, unit_price, instructions)
WITH RECURSIVE
  detail_idx AS (SELECT 0 AS k UNION ALL SELECT k + 1 FROM detail_idx WHERE k < 3),
  product_list AS (
    SELECT  0 AS lo, 14 AS hi,  6 AS pid, 18000.00 AS price
    UNION ALL SELECT 15, 28, 12, 22000.00
    UNION ALL SELECT 29, 40,  5,  8000.00
    UNION ALL SELECT 51, 58,  1, 28000.00
    UNION ALL SELECT 59, 64, 11, 20000.00
    UNION ALL SELECT 65, 70,  9, 25000.00
    UNION ALL SELECT 71, 75,  4, 55000.00
    UNION ALL SELECT 76, 80, 16, 24000.00
    UNION ALL SELECT 81, 85, 15, 25000.00
    UNION ALL SELECT 86, 89, 19, 12000.00
    UNION ALL SELECT 90, 92, 17, 42000.00
    UNION ALL SELECT 93, 95, 18, 38000.00
    UNION ALL SELECT 96, 97, 21,  7000.00
    UNION ALL SELECT 98, 99, 20,  6000.00
  )
SELECT
  ROW_NUMBER() OVER (ORDER BY o.id, d.k) + 200000,
  o.id,
  p.pid,
  p.price,
  NULL
FROM orders o
CROSS JOIN detail_idx d
CROSS JOIN product_list p
WHERE o.date >= '2026-04-01' AND o.date < '2026-07-01'
  AND d.k < 2 + ((o.id + d.k) % 3)
  AND CASE
    WHEN o.date < '2026-05-01' THEN ((o.id * 7 + d.k * 31) % 100)
    WHEN o.date < '2026-06-01' THEN ((o.id * 11 + d.k * 37) % 100)
    ELSE                            ((o.id * 13 + d.k * 41) % 100)
  END BETWEEN p.lo AND p.hi;

-- =============================================================================
-- ORDER DETAIL OPTIONS (PK -> INSERT IGNORE)
-- =============================================================================
INSERT IGNORE INTO order_detail_options (order_detail_id, option_id)
SELECT od.id, ((od.id * 11) % 10) + 1
FROM order_details od
JOIN orders o ON o.id = od.order_id
WHERE o.date >= '2026-04-01' AND o.date < '2026-07-01'
  AND od.product_id IN (1, 6) AND ((od.id * 13) % 4) = 0
UNION ALL
SELECT od.id, (((od.id * 23) % 3) + 23)
FROM order_details od
JOIN orders o ON o.id = od.order_id
WHERE o.date >= '2026-04-01' AND o.date < '2026-07-01'
  AND od.product_id IN (2, 12, 13, 14) AND ((od.id * 29) % 5) = 0;

-- =============================================================================
-- ORDER PREPARATION AREAS (PK -> INSERT IGNORE)
-- =============================================================================
INSERT IGNORE INTO order_preparation_areas (order_id, area_id)
SELECT DISTINCT o.id, 1 FROM orders o
WHERE o.date >= '2026-04-01' AND o.date < '2026-07-01';

INSERT IGNORE INTO order_preparation_areas (order_id, area_id)
SELECT DISTINCT o.id, 3 FROM orders o
JOIN order_details od ON od.order_id = o.id
WHERE o.date >= '2026-04-01' AND o.date < '2026-07-01'
  AND od.product_id IN (5, 19, 20, 21);

-- =============================================================================
-- INVENTORY MOVEMENTS — DEDUCTION (COGS source for prime cost)
-- =============================================================================
INSERT INTO inventory_movements (supply_variant_id, from_storage_location_id, to_storage_location_id, quantity, movement_type, reference_order_id, created_at)
SELECT pr.supply_variant_id, 2, NULL, pr.required_quantity, 'DEDUCTION', od.order_id, o.date + INTERVAL 5 MINUTE
FROM order_details od
JOIN orders o ON o.id = od.order_id
JOIN product_recipes pr ON pr.product_id = od.product_id
WHERE o.date >= '2026-04-01' AND o.date < '2026-07-01';

-- =============================================================================
-- INVENTORY MOVEMENTS — TRANSFER (Bodega -> Cocina, daily restock)
-- =============================================================================
INSERT INTO inventory_movements (supply_variant_id, from_storage_location_id, to_storage_location_id, quantity, movement_type, reference_order_id, created_at)
WITH RECURSIVE
  days AS (SELECT 0 AS d UNION ALL SELECT d + 1 FROM days WHERE d < 91),
  variants AS (SELECT 35 AS vid UNION ALL SELECT 36 UNION ALL SELECT 37 UNION ALL SELECT 38)
SELECT variants.vid, 1, 2, 10.000, 'TRANSFER', NULL, TIMESTAMP('2026-04-01 09:00:00') + INTERVAL days.d DAY
FROM days CROSS JOIN variants;

-- =============================================================================
-- MONTHLY FINANCIAL SUMMARY (for Prime Cost report)
-- Computes net_sales and cogs from actual seeded data, labor from known
-- worker/shift structure (2 FOH + 4 BOH, lunch 4h + dinner 5h).
-- UNIQUE (period_key, bucket) -> INSERT ON DUPLICATE KEY UPDATE for idempotency.
-- =============================================================================
INSERT INTO monthly_financial_summary
    (period_key, bucket, net_sales, gross_sales, discounts, comped,
     cogs_food, cogs_beverage, cogs_alcohol, cogs_other, food_cogs_pct,
     labor_foh, labor_boh, labor_total, labor_pct,
     prime_cost, prime_cost_pct, gross_profit_pct, net_profit_pct, data_completeness)
WITH
monthly_revenue AS (
  SELECT DATE_FORMAT(o.date, '%Y-%m') AS m, SUM(od.unit_price) AS sales
  FROM orders o JOIN order_details od ON od.order_id = o.id
  WHERE o.date >= '2026-04-01' AND o.date < '2026-07-01'
  GROUP BY m
),
monthly_cogs AS (
  SELECT DATE_FORMAT(o.date, '%Y-%m') AS m,
         COALESCE(SUM(CASE WHEN sc.food_type = 'FOOD' THEN im.quantity * sv.unit_cost END), 0) AS food,
         COALESCE(SUM(CASE WHEN sc.food_type = 'BEVERAGE' THEN im.quantity * sv.unit_cost END), 0) AS bev,
         COALESCE(SUM(CASE WHEN sc.food_type = 'ALCOHOL' THEN im.quantity * sv.unit_cost END), 0) AS alc,
         COALESCE(SUM(CASE WHEN sc.food_type = 'OTHER' THEN im.quantity * sv.unit_cost END), 0) AS oth
  FROM inventory_movements im
  JOIN supply_variants sv ON sv.id = im.supply_variant_id
  JOIN supplies s ON s.id = sv.supply_id
  JOIN supply_categories sc ON sc.id = s.supply_category_id
  JOIN orders o ON o.id = im.reference_order_id
  WHERE im.movement_type = 'DEDUCTION' AND o.date >= '2026-04-01' AND o.date < '2026-07-01'
  GROUP BY m
)
SELECT
  r.m, 'monthly',
  r.sales, r.sales, 0, 0,
  c.food, c.bev, c.alc, c.oth,
  ROUND(c.food / NULLIF(c.food + c.bev + c.alc + c.oth, 0) * 100, 2),
  5000000,
  11200000,
  16200000,
  ROUND(16200000 / NULLIF(r.sales, 0) * 100, 2),
  ROUND(c.food + c.bev + c.alc + c.oth + 16200000, 2),
  ROUND((c.food + c.bev + c.alc + c.oth + 16200000) / NULLIF(r.sales, 0) * 100, 2),
  ROUND((1 - (c.food + c.bev + c.alc + c.oth + 16200000) / NULLIF(r.sales, 0)) * 100, 2),
  ROUND((1 - (c.food + c.bev + c.alc + c.oth + 16200000) / NULLIF(r.sales, 0)) * 100, 2),
  'FULL'
FROM monthly_revenue r
LEFT JOIN monthly_cogs c ON c.m = r.m
ON DUPLICATE KEY UPDATE
  net_sales = VALUES(net_sales), gross_sales = VALUES(gross_sales),
  cogs_food = VALUES(cogs_food), cogs_beverage = VALUES(cogs_beverage),
  cogs_alcohol = VALUES(cogs_alcohol), cogs_other = VALUES(cogs_other);

-- =============================================================================
-- JULY 2026 STATISTICS SEED (Jul 1-27, idempotent, append-only)
-- =============================================================================
-- Simulates 27 days of restaurant operations for the current month.
-- Runs after Q2 cleanup so it won't delete Q2 data.
--   * same users 1001-1006, same schedule/shifts, reuses ids 100001+
--   * orders id 200001+ / order_details id 400001+ / inventory_movements id auto
-- =============================================================================

-- Clean any prior July 2026 seeded data (idempotent re-run safety).
DELETE FROM order_detail_options
  WHERE order_detail_id IN (
    SELECT id FROM order_details
    WHERE order_id IN (SELECT id FROM orders WHERE date >= '2026-07-01' AND date < '2026-07-28'));
DELETE FROM order_details
  WHERE order_id IN (SELECT id FROM orders WHERE date >= '2026-07-01' AND date < '2026-07-28');
DELETE FROM order_preparation_areas
  WHERE order_id IN (SELECT id FROM orders WHERE date >= '2026-07-01' AND date < '2026-07-28');
DELETE FROM inventory_movements
  WHERE created_at >= '2026-07-01' AND created_at < '2026-07-28';
DELETE FROM time_logs
  WHERE timestamp >= '2026-07-01' AND timestamp < '2026-07-28';
DELETE FROM orders WHERE date >= '2026-07-01' AND date < '2026-07-28';

-- Reuse users 1001-1006 (already inserted by Q2 block), no DELETE of users needed.

-- =============================================================================
-- TIME LOGS (6 workers x 27 days = 162 rows)
-- =============================================================================
INSERT INTO time_logs (worker_id, timestamp, type, within_shift, related_shift_id)
WITH RECURSIVE
  days  AS (SELECT 0 AS d UNION ALL SELECT d + 1 FROM days WHERE d < 26),
  slots AS (SELECT 0 AS slot UNION ALL SELECT slot + 1 FROM slots WHERE slot < 5)
SELECT
  1001 + slots.slot,
  CASE WHEN slots.slot IN (0, 2, 4)
       THEN TIMESTAMP('2026-07-01 11:30:00') + INTERVAL days.d DAY
       ELSE TIMESTAMP('2026-07-01 18:30:00') + INTERVAL days.d DAY
  END,
  'IN', TRUE,
  CASE WHEN slots.slot IN (0, 2, 4)
       THEN ((DAYOFWEEK('2026-07-01' + INTERVAL days.d DAY) + 5) % 7) + 1
       ELSE ((DAYOFWEEK('2026-07-01' + INTERVAL days.d DAY) + 5) % 7) + 8
  END
FROM days CROSS JOIN slots;

-- =============================================================================
-- ORDERS (27 days, ~1400 total)
-- =============================================================================
INSERT INTO orders (id, date, status, table_id, party_size, open_time, close_time)
WITH RECURSIVE
  days AS (SELECT 0 AS d UNION ALL SELECT d + 1 FROM days WHERE d < 26),
  slots AS (SELECT 0 AS n UNION ALL SELECT n + 1 FROM slots WHERE n < 75),
  base_ts AS (
    SELECT
      days.d, slots.n,
      TIMESTAMP('2026-07-01 12:00:00') + INTERVAL days.d DAY + INTERVAL (slots.n * 8) MINUTE AS bt,
      HOUR(TIMESTAMP('2026-07-01 12:00:00') + INTERVAL (slots.n * 8) MINUTE) AS hr,
      DAYOFWEEK('2026-07-01' + INTERVAL days.d DAY) AS dow
    FROM days CROSS JOIN slots
  )
SELECT
  ROW_NUMBER() OVER (ORDER BY bt.d, bt.n) + 200000,
  bt.bt,
  'DELIVERED',
  (bt.n % 5) + 1,
  2 + ((bt.n + bt.d) % 3),
  bt.bt,
  bt.bt + INTERVAL 45 MINUTE
FROM base_ts bt
WHERE bt.hr BETWEEN 11 AND 22
  AND NOT (bt.dow IN (2,3,4) AND (bt.n % 3) = 0);

-- =============================================================================
-- ORDER DETAILS
-- =============================================================================
INSERT INTO order_details (id, order_id, product_id, unit_price, instructions)
WITH RECURSIVE
  detail_idx AS (SELECT 0 AS k UNION ALL SELECT k + 1 FROM detail_idx WHERE k < 3),
  product_list AS (
    SELECT  0 AS lo, 14 AS hi,  6 AS pid, 18000.00 AS price
    UNION ALL SELECT 15, 28, 12, 22000.00
    UNION ALL SELECT 29, 40,  5,  8000.00
    UNION ALL SELECT 51, 58,  1, 28000.00
    UNION ALL SELECT 59, 64, 11, 20000.00
    UNION ALL SELECT 65, 70,  9, 25000.00
    UNION ALL SELECT 71, 75,  4, 55000.00
    UNION ALL SELECT 76, 80, 16, 24000.00
    UNION ALL SELECT 81, 85, 15, 25000.00
    UNION ALL SELECT 86, 89, 19, 12000.00
    UNION ALL SELECT 90, 92, 17, 42000.00
    UNION ALL SELECT 93, 95, 18, 38000.00
    UNION ALL SELECT 96, 97, 21,  7000.00
    UNION ALL SELECT 98, 99, 20,  6000.00
  )
SELECT
  ROW_NUMBER() OVER (ORDER BY o.id, d.k) + 400000,
  o.id,
  p.pid,
  p.price,
  NULL
FROM orders o
CROSS JOIN detail_idx d
CROSS JOIN product_list p
WHERE o.date >= '2026-07-01' AND o.date < '2026-07-28'
  AND d.k < 2 + ((o.id + d.k) % 3)
  AND ((o.id * 7 + d.k * 31) % 100) BETWEEN p.lo AND p.hi;

-- =============================================================================
-- ORDER DETAIL OPTIONS
-- =============================================================================
INSERT IGNORE INTO order_detail_options (order_detail_id, option_id)
SELECT od.id, ((od.id * 11) % 10) + 1
FROM order_details od
JOIN orders o ON o.id = od.order_id
WHERE o.date >= '2026-07-01' AND o.date < '2026-07-28'
  AND od.product_id IN (1, 6) AND ((od.id * 13) % 4) = 0
UNION ALL
SELECT od.id, (((od.id * 23) % 3) + 23)
FROM order_details od
JOIN orders o ON o.id = od.order_id
WHERE o.date >= '2026-07-01' AND o.date < '2026-07-28'
  AND od.product_id IN (2, 12, 13, 14) AND ((od.id * 29) % 5) = 0;

-- =============================================================================
-- ORDER PREPARATION AREAS
-- =============================================================================
INSERT IGNORE INTO order_preparation_areas (order_id, area_id)
SELECT DISTINCT o.id, 1 FROM orders o
WHERE o.date >= '2026-07-01' AND o.date < '2026-07-28';

INSERT IGNORE INTO order_preparation_areas (order_id, area_id)
SELECT DISTINCT o.id, 3 FROM orders o
JOIN order_details od ON od.order_id = o.id
WHERE o.date >= '2026-07-01' AND o.date < '2026-07-28'
  AND od.product_id IN (5, 19, 20, 21);

-- =============================================================================
-- INVENTORY MOVEMENTS — DEDUCTION
-- =============================================================================
INSERT INTO inventory_movements (supply_variant_id, from_storage_location_id, to_storage_location_id, quantity, movement_type, reference_order_id, created_at)
SELECT pr.supply_variant_id, 2, NULL, pr.required_quantity, 'DEDUCTION', od.order_id, o.date + INTERVAL 5 MINUTE
FROM order_details od
JOIN orders o ON o.id = od.order_id
JOIN product_recipes pr ON pr.product_id = od.product_id
WHERE o.date >= '2026-07-01' AND o.date < '2026-07-28';

-- =============================================================================
-- INVENTORY MOVEMENTS — TRANSFER (Bodega -> Cocina, daily restock)
-- =============================================================================
INSERT INTO inventory_movements (supply_variant_id, from_storage_location_id, to_storage_location_id, quantity, movement_type, reference_order_id, created_at)
WITH RECURSIVE
  days AS (SELECT 0 AS d UNION ALL SELECT d + 1 FROM days WHERE d < 26),
  variants AS (SELECT 35 AS vid UNION ALL SELECT 36 UNION ALL SELECT 37 UNION ALL SELECT 38)
SELECT variants.vid, 1, 2, 10.000, 'TRANSFER', NULL, TIMESTAMP('2026-07-01 09:00:00') + INTERVAL days.d DAY
FROM days CROSS JOIN variants;

-- =============================================================================
-- MONTHLY FINANCIAL SUMMARY — July
-- Labor prorated 27/31 for 27 days of operation.
-- =============================================================================
INSERT INTO monthly_financial_summary
    (period_key, bucket, net_sales, gross_sales, discounts, comped,
     cogs_food, cogs_beverage, cogs_alcohol, cogs_other, food_cogs_pct,
     labor_foh, labor_boh, labor_total, labor_pct,
     prime_cost, prime_cost_pct, gross_profit_pct, net_profit_pct, data_completeness)
WITH
monthly_revenue AS (
  SELECT DATE_FORMAT(o.date, '%Y-%m') AS m, SUM(od.unit_price) AS sales
  FROM orders o JOIN order_details od ON od.order_id = o.id
  WHERE o.date >= '2026-07-01' AND o.date < '2026-07-28'
  GROUP BY m
),
monthly_cogs AS (
  SELECT DATE_FORMAT(o.date, '%Y-%m') AS m,
         COALESCE(SUM(CASE WHEN sc.food_type = 'FOOD' THEN im.quantity * sv.unit_cost END), 0) AS food,
         COALESCE(SUM(CASE WHEN sc.food_type = 'BEVERAGE' THEN im.quantity * sv.unit_cost END), 0) AS bev,
         COALESCE(SUM(CASE WHEN sc.food_type = 'ALCOHOL' THEN im.quantity * sv.unit_cost END), 0) AS alc,
         COALESCE(SUM(CASE WHEN sc.food_type = 'OTHER' THEN im.quantity * sv.unit_cost END), 0) AS oth
  FROM inventory_movements im
  JOIN supply_variants sv ON sv.id = im.supply_variant_id
  JOIN supplies s ON s.id = sv.supply_id
  JOIN supply_categories sc ON sc.id = s.supply_category_id
  JOIN orders o ON o.id = im.reference_order_id
  WHERE im.movement_type = 'DEDUCTION' AND o.date >= '2026-07-01' AND o.date < '2026-07-28'
  GROUP BY m
)
SELECT
  r.m, 'monthly',
  r.sales, r.sales, 0, 0,
  c.food, c.bev, c.alc, c.oth,
  ROUND(c.food / NULLIF(c.food + c.bev + c.alc + c.oth, 0) * 100, 2),
  4354839,
  9754839,
  14109677,
  ROUND(14109677 / NULLIF(r.sales, 0) * 100, 2),
  ROUND(c.food + c.bev + c.alc + c.oth + 14109677, 2),
  ROUND((c.food + c.bev + c.alc + c.oth + 14109677) / NULLIF(r.sales, 0) * 100, 2),
  ROUND((1 - (c.food + c.bev + c.alc + c.oth + 14109677) / NULLIF(r.sales, 0)) * 100, 2),
  ROUND((1 - (c.food + c.bev + c.alc + c.oth + 14109677) / NULLIF(r.sales, 0)) * 100, 2),
  'PARTIAL'
FROM monthly_revenue r
LEFT JOIN monthly_cogs c ON c.m = r.m
ON DUPLICATE KEY UPDATE
  net_sales = VALUES(net_sales), gross_sales = VALUES(gross_sales),
  cogs_food = VALUES(cogs_food), cogs_beverage = VALUES(cogs_beverage),
  cogs_alcohol = VALUES(cogs_alcohol), cogs_other = VALUES(cogs_other);
