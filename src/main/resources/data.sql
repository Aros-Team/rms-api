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
SELECT 'Almuerzo Ejecutivo', 'Menú del día con proteína, principio y acompañamiento', TRUE
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
SELECT 'Proteína Almuerzo', 'Elección de proteína para menú del día'
FROM dual WHERE @ocat_count = 0;

INSERT INTO option_categories (name, description)
SELECT 'Principio Almuerzo', 'Elección de principio para menú del día'
FROM dual WHERE @ocat_count = 0;

INSERT INTO option_categories (name, description)
SELECT 'Acompañamiento Almuerzo', 'Acompañamiento para menú del día'
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
-- PRODUCTS (only insert if table is empty)
-- categories: Hamburguesas=1, Almuerzo Ejecutivo=2, Pizzas=3,
--             Pasta/Italiana=4, Parrilla/Carnes=5, Bebidas Naturales=6
-- areas: Cocina=1, Bar=3
-- =============================================================================
SET @prod_count = (SELECT COUNT(*) FROM products);

INSERT INTO products (name, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Hamburguesa Double Bacon Cheese', 28000.00, TRUE, 1, 1, 10
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Menú del Día', 18000.00, TRUE, 2, 1, 25
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Pizza Especial Familiar', 38000.00, TRUE, 3, 1, 25
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Pasta de la Casa', 22000.00, TRUE, 4, 1, 18
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Corte de Res Premium', 55000.00, TRUE, 5, 1, 22
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Limonada / Jugo Natural', 8000.00, TRUE, 6, 3, 5
FROM dual WHERE @prod_count = 0;

-- V20 extra products
INSERT INTO products (name, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Hamburguesa Clásica', 18000.00, TRUE, 1, 1, 8
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Hamburguesa BBQ', 32000.00, TRUE, 1, 1, 10
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Hamburguesa Veggie', 22000.00, TRUE, 1, 1, 12
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Bandeja Paisa', 25000.00, TRUE, 2, 1, 30
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Sancocho de Gallina', 22000.00, TRUE, 2, 1, 35
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Ajiaco Santafereño', 20000.00, TRUE, 2, 1, 35
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Pizza Personal', 22000.00, TRUE, 3, 1, 20
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Pizza Vegetariana', 32000.00, TRUE, 3, 1, 22
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Pizza Hawaiana', 30000.00, TRUE, 3, 1, 20
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Lasagna Clásica', 25000.00, TRUE, 4, 1, 25
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Spaghetti Carbonara', 24000.00, TRUE, 4, 1, 18
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Costillas BBQ', 42000.00, TRUE, 5, 1, 25
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Lomo de Cerdo', 38000.00, TRUE, 5, 1, 20
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Malteada', 12000.00, TRUE, 6, 3, 5
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Agua de Coco', 6000.00, TRUE, 6, 3, 3
FROM dual WHERE @prod_count = 0;

INSERT INTO products (name, base_price, active, category_id, area_id, estimated_prep_minutes)
SELECT 'Té Helado', 7000.00, TRUE, 6, 3, 3
FROM dual WHERE @prod_count = 0;

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
SELECT 'Pechuga de Pollo 200g', 6 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Carne de Cerdo 200g', 6 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Huevo Perico x2', 6 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Frijol Cargamanto 80g', 7 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Lenteja Seca 80g', 7 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Pasta Corta 100g', 7 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Plátano Maduro 1/2', 8 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Patacón Prefrito 1u', 8 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Papa Salada 1u', 8 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Pepperoni 100g', 9 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Jamón Cocido 100g', 9 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Champiñón Laminado 80g', 9 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Pimentón Verde 30g', 10 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Maíz Tierno 40g', 10 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Piña en Almíbar 60g', 10 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Borde Queso Crema 80g', 11 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Borde Bocadillo 80g', 11 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Salsa Alfredo', 12 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Salsa Boloñesa', 12 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Salsa Carbonara', 12 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Camarón Tigre x6', 13 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Pechuga en Cubos 100g', 13 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Sin Proteína Extra', 13 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Orégano Seco', 14 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Albahaca Fresca', 14 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Pimienta Negra', 14 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Baby Beef 300g', 15 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Churrasco 300g', 15 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Entrecot 300g', 15 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Azul', 16 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Medio', 16 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT '3/4', 16 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Bien Asado', 16 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Papa Criolla 150g', 17 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Yuca Frita 150g', 17 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Ensalada de Papa 150g', 17 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Limón Tahití x2', 18 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Pulpa de Mango 100g', 18 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Pulpa de Mora 100g', 18 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Pulpa de Lulo 100g', 18 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Azúcar Blanca 20g', 19 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Stevia 1 sobre', 19 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Miel de Abejas 15ml', 19 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Crema de Coco 30ml', 20 FROM dual WHERE @opt_count = 0;

INSERT INTO product_options (name, option_category_id)
SELECT 'Hierbabuena Fresca x2', 20 FROM dual WHERE @opt_count = 0;

-- =============================================================================
-- PRODUCT ↔ OPTIONS (PRIMARY KEY (product_id, option_id) -> INSERT IGNORE)
-- =============================================================================
INSERT IGNORE INTO product_product_options (product_id, option_id) VALUES
    -- Hamburguesa Double Bacon Cheese (id=1)
    (1,  1), (1,  2), (1,  3),
    (1,  4), (1,  5),
    (1,  6), (1,  7),
    (1,  8), (1,  9), (1, 10),
    (1, 11), (1, 12), (1, 13),

    -- Menú del Día (id=2)
    (2, 14), (2, 15), (2, 16),
    (2, 17), (2, 18), (2, 19),
    (2, 20), (2, 21), (2, 22),

    -- Pizza Especial Familiar (id=3)
    (3, 23), (3, 24), (3, 25),
    (3, 26), (3, 27), (3, 28),
    (3, 29), (3, 30),

    -- Pasta de la Casa (id=4)
    (4, 31), (4, 32), (4, 33),
    (4, 34), (4, 35), (4, 36),
    (4, 37), (4, 38), (4, 39),

    -- Corte de Res Premium (id=5)
    (5, 40), (5, 41), (5, 42),
    (5, 43), (5, 44), (5, 45), (5, 46),
    (5, 47), (5, 48), (5, 49),

    -- Limonada / Jugo Natural (id=6)
    (6, 50), (6, 51), (6, 52), (6, 53),
    (6, 54), (6, 55), (6, 56),
    (6, 57), (6, 58);

-- =============================================================================
-- PRODUCT RECIPES (product_id, supply_variant_id is UNIQUE -> INSERT IGNORE)
-- =============================================================================
INSERT IGNORE INTO product_recipes (product_id, supply_variant_id, required_quantity) VALUES
    -- Hamburguesa Double Bacon Cheese (id=1)
    (1, 35, 1.000), (1, 34, 1.000), (1, 14, 1.000), (1, 59, 1.000),
    -- Menú del Día (id=2)
    (2, 66, 1.000), (2, 72, 1.000), (2, 42, 1.000), (2, 22, 1.000), (2, 23, 1.000),
    -- Pizza Especial Familiar (id=3)
    (3, 37, 1.000), (3, 38, 1.000), (3, 39, 1.000), (3, 30, 1.000),
    -- Pasta de la Casa (id=4)
    (4, 70, 1.000), (4, 36, 1.000), (4, 31, 1.000),
    -- Corte de Res Premium (id=5)
    (5, 43, 1.000), (5, 40, 1.000), (5, 22, 1.000),
    -- Limonada / Jugo Natural (id=6)
    (6, 60, 1.000), (6, 62, 1.000), (6, 73, 1.000);

-- =============================================================================
-- OPTION RECIPES (option_id, supply_variant_id is UNIQUE -> INSERT IGNORE)
-- =============================================================================
INSERT IGNORE INTO option_recipes (option_id, supply_variant_id, required_quantity) VALUES
    -- Proteína Hamburguesa
    (1,  1, 1.000), (2,  3, 1.000), (3,  4, 1.000),
    -- Queso Hamburguesa
    (4, 28, 1.000), (5, 29, 1.000),
    -- Vegetales Hamburguesa
    (6, 15, 1.000), (7, 16, 1.000),
    -- Acompañamiento Hamburguesa
    (8, 63, 1.000), (9, 64, 1.000),
    -- Extras Hamburguesa
    (11, 46, 1.000), (12, 12, 1.000), (13, 17, 1.000),
    -- Proteína Almuerzo
    (14,  5, 1.000), (15,  7, 1.000), (16, 13, 1.000),
    -- Principio Almuerzo
    (17, 67, 1.000), (18, 68, 1.000), (19, 74, 1.000),
    -- Acompañamiento Almuerzo
    (20, 24, 1.000), (21, 65, 1.000), (22, 27, 1.000),
    -- Sabor Pizza
    (23, 48, 1.000), (24, 49, 1.000), (25, 21, 1.000),
    -- Vegetal Extra Pizza
    (26, 18, 1.000), (27, 69, 1.000), (28, 55, 1.000),
    -- Borde Pizza
    (29, 32, 1.000), (30, 50, 1.000),
    -- Salsa Pasta
    (31, 33, 1.000), (32,  2, 1.000), (33, 47, 1.000),
    -- Proteína Extra Pasta
    (34,  8, 1.000), (35,  6, 1.000),
    -- Especia Pasta
    (37, 44, 1.000), (38, 20, 1.000), (39, 45, 1.000),
    -- Corte Parrilla
    (40,  9, 1.000), (41, 10, 1.000), (42, 11, 1.000),
    -- Guarnición Parrilla
    (47, 25, 1.000), (48, 26, 1.000), (49, 25, 1.000),
    -- Base Fruta Bebida
    (50, 51, 1.000), (51, 52, 1.000), (52, 53, 1.000), (53, 54, 1.000),
    -- Endulzante Bebida
    (54, 56, 1.000), (55, 57, 1.000), (56, 58, 1.000),
    -- Sabor Especial Bebida
    (57, 41, 1.000), (58, 19, 1.000);
