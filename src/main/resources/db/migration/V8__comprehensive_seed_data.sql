-- V8__comprehensive_seed_data.sql
-- Comprehensive seed data for local development. NOT for production.
-- V6 already seeds: areas, units_of_measure, storage_locations
-- IDs reference: storage_locations → Bodega=1, Cocina=2

-- =============================================================================
-- SUPPLY CATEGORIES
-- =============================================================================
INSERT INTO supply_categories (name) VALUES
    ('Proteínas'),      -- id 1
    ('Vegetales'),      -- id 2
    ('Lácteos'),        -- id 3
    ('Harinas'),        -- id 4
    ('Salsas'),         -- id 5
    ('Condimentos'),    -- id 6
    ('Embutidos'),      -- id 7
    ('Bebidas');        -- id 8

-- =============================================================================
-- SUPPLIES (insumos base)
-- =============================================================================
-- units_of_measure: g=1, kg=2, l=3, u=4, lb=5, pz=6, paq=7, bot=8, lat=9
INSERT INTO supplies (name, supply_category_id) VALUES
    ('Carne de Res',        1),   -- id 1
    ('Pechuga de Pollo',    1),   -- id 2
    ('Pepperoni',           7),   -- id 3
    ('Bacon',               7),   -- id 4
    ('Queso Mozzarella',    3),   -- id 5
    ('Queso Cheddar',       3),   -- id 6
    ('Lechuga',             2),   -- id 7
    ('Tomate',              2),   -- id 8
    ('Cebolla',             2),   -- id 9
    ('Papa',                2),   -- id 10
    ('Harina de Trigo',     4),   -- id 11
    ('Masa de Pizza',       4),   -- id 12
    ('Pan de Hamburguesa',  4),   -- id 13
    ('Arroz',               4),   -- id 14
    ('Salsa de Tomate',     5),   -- id 15
    ('Mayonesa',            5),   -- id 16
    ('Salsa BBQ',           5),   -- id 17
    ('Aceite Vegetal',      6),   -- id 18
    ('Sal',                 6),   -- id 19
    ('Limonada Concentrada',8);   -- id 20

-- =============================================================================
-- SUPPLY VARIANTS (presentaciones físicas)
-- unit_of_measure IDs: g=1, kg=2, l=3, u=4, lb=5, pz=6, paq=7, bot=8, lat=9
-- =============================================================================
INSERT INTO supply_variants (supply_id, unit_id, quantity) VALUES
    -- Carne de Res
    (1,  1, 100.000),   -- id 1  → Carne de Res 100g
    (1,  1, 200.000),   -- id 2  → Carne de Res 200g
    -- Pechuga de Pollo
    (2,  1, 150.000),   -- id 3  → Pechuga de Pollo 150g
    (2,  1, 250.000),   -- id 4  → Pechuga de Pollo 250g
    -- Pepperoni
    (3,  1,  60.000),   -- id 5  → Pepperoni 60g
    -- Bacon
    (4,  1,  40.000),   -- id 6  → Bacon 40g
    -- Queso Mozzarella
    (5,  1,  50.000),   -- id 7  → Queso Mozzarella 50g
    (5,  1, 100.000),   -- id 8  → Queso Mozzarella 100g
    -- Queso Cheddar
    (6,  1,  30.000),   -- id 9  → Queso Cheddar 30g
    -- Lechuga
    (7,  1,  40.000),   -- id 10 → Lechuga 40g
    -- Tomate
    (8,  1,  50.000),   -- id 11 → Tomate 50g
    -- Cebolla
    (9,  1,  30.000),   -- id 12 → Cebolla 30g
    -- Papa
    (10, 1, 200.000),   -- id 13 → Papa 200g
    -- Harina de Trigo
    (11, 1, 100.000),   -- id 14 → Harina de Trigo 100g
    -- Masa de Pizza
    (12, 1, 200.000),   -- id 15 → Masa de Pizza 200g (personal)
    (12, 1, 400.000),   -- id 16 → Masa de Pizza 400g (familiar)
    -- Pan de Hamburguesa
    (13, 4,   1.000),   -- id 17 → Pan de Hamburguesa 1u
    -- Arroz
    (14, 1, 150.000),   -- id 18 → Arroz 150g
    -- Salsa de Tomate
    (15, 1,  30.000),   -- id 19 → Salsa de Tomate 30g
    (15, 1,  60.000),   -- id 20 → Salsa de Tomate 60g
    -- Mayonesa
    (16, 1,  20.000),   -- id 21 → Mayonesa 20g
    -- Salsa BBQ
    (17, 1,  25.000),   -- id 22 → Salsa BBQ 25g
    -- Aceite Vegetal
    (18, 1,  15.000),   -- id 23 → Aceite Vegetal 15g
    -- Sal
    (19, 1,   5.000),   -- id 24 → Sal 5g
    -- Limonada Concentrada
    (20, 1, 100.000);   -- id 25 → Limonada Concentrada 100ml


-- =============================================================================
-- INVENTORY STOCK (stock inicial por variante en Bodega y Cocina)
-- storage_locations: Bodega=1, Cocina=2
-- =============================================================================
INSERT INTO inventory_stock (supply_variant_id, storage_location_id, current_quantity) VALUES
    -- Carne de Res 100g
    (1,  1, 50.000),  (1,  2, 20.000),
    -- Carne de Res 200g
    (2,  1, 30.000),  (2,  2, 10.000),
    -- Pechuga de Pollo 150g
    (3,  1, 40.000),  (3,  2, 15.000),
    -- Pechuga de Pollo 250g
    (4,  1, 25.000),  (4,  2,  8.000),
    -- Pepperoni 60g
    (5,  1, 30.000),  (5,  2, 10.000),
    -- Bacon 40g
    (6,  1, 20.000),  (6,  2,  8.000),
    -- Queso Mozzarella 50g
    (7,  1, 60.000),  (7,  2, 20.000),
    -- Queso Mozzarella 100g
    (8,  1, 40.000),  (8,  2, 15.000),
    -- Queso Cheddar 30g
    (9,  1, 50.000),  (9,  2, 20.000),
    -- Lechuga 40g
    (10, 1, 30.000),  (10, 2, 15.000),
    -- Tomate 50g
    (11, 1, 40.000),  (11, 2, 20.000),
    -- Cebolla 30g
    (12, 1, 35.000),  (12, 2, 15.000),
    -- Papa 200g
    (13, 1, 50.000),  (13, 2, 20.000),
    -- Harina de Trigo 100g
    (14, 1, 40.000),  (14, 2, 10.000),
    -- Masa de Pizza 200g
    (15, 1, 30.000),  (15, 2, 10.000),
    -- Masa de Pizza 400g
    (16, 1, 20.000),  (16, 2,  5.000),
    -- Pan de Hamburguesa 1u
    (17, 1, 60.000),  (17, 2, 20.000),
    -- Arroz 150g
    (18, 1, 50.000),  (18, 2, 20.000),
    -- Salsa de Tomate 30g
    (19, 1, 40.000),  (19, 2, 15.000),
    -- Salsa de Tomate 60g
    (20, 1, 25.000),  (20, 2, 10.000),
    -- Mayonesa 20g
    (21, 1, 50.000),  (21, 2, 20.000),
    -- Salsa BBQ 25g
    (22, 1, 40.000),  (22, 2, 15.000),
    -- Aceite Vegetal 15g
    (23, 1, 60.000),  (23, 2, 20.000),
    -- Sal 5g
    (24, 1, 80.000),  (24, 2, 30.000),
    -- Limonada Concentrada 100ml
    (25, 1, 30.000),  (25, 2, 10.000);

-- =============================================================================
-- PRODUCT CATEGORIES
-- =============================================================================
INSERT INTO categories (name, description, enabled) VALUES
    ('Hamburguesas',     'Hamburguesas artesanales',          TRUE),  -- id 1
    ('Pizzas',           'Pizzas al horno de leña',           TRUE),  -- id 2
    ('Platos Ejecutivos','Platos del día con acompañamiento', TRUE),  -- id 3
    ('Entradas',         'Entradas y aperitivos',             TRUE),  -- id 4
    ('Bebidas',          'Bebidas frías y calientes',         TRUE);  -- id 5

-- =============================================================================
-- OPTION CATEGORIES
-- =============================================================================
INSERT INTO option_categories (name, description) VALUES
    ('Término de Cocción', 'Punto de cocción de la carne'),   -- id 1
    ('Extras',             'Ingredientes adicionales'),        -- id 2
    ('Tamaño',             'Tamaño de la porción');            -- id 3


-- =============================================================================
-- PRODUCTS
-- areas: Cocina=1, Bar=2  (seeded in V6)
-- categories: Hamburguesas=1, Pizzas=2, Platos Ejecutivos=3, Entradas=4, Bebidas=5
-- =============================================================================
INSERT INTO products (name, base_price, has_options, active, category_id, area_id) VALUES
    -- Hamburguesas (has_options = TRUE → el cliente elige término y extras)
    ('Hamburguesa Clásica',   12500.00, TRUE,  TRUE, 1, 1),  -- id 1
    ('Hamburguesa BBQ',       14000.00, TRUE,  TRUE, 1, 1),  -- id 2
    ('Hamburguesa Doble',     16000.00, TRUE,  TRUE, 1, 1),  -- id 3

    -- Pizzas (Margarita con opciones de tamaño/extras; Pepperoni sin opciones)
    ('Pizza Margarita',       18000.00, TRUE,  TRUE, 2, 1),  -- id 4
    ('Pizza Pepperoni',       20000.00, FALSE, TRUE, 2, 1),  -- id 5
    ('Pizza BBQ Pollo',       22000.00, FALSE, TRUE, 2, 1),  -- id 6

    -- Platos Ejecutivos (sin opciones, receta fija)
    ('Plato Ejecutivo Pollo', 15000.00, FALSE, TRUE, 3, 1),  -- id 7
    ('Plato Ejecutivo Res',   17000.00, FALSE, TRUE, 3, 1),  -- id 8

    -- Entradas (sin opciones)
    ('Papas Fritas',           6000.00, FALSE, TRUE, 4, 1),  -- id 9
    ('Aros de Cebolla',        7000.00, FALSE, TRUE, 4, 1),  -- id 10

    -- Bebidas (sin opciones)
    ('Limonada Natural',       4500.00, FALSE, TRUE, 5, 2);  -- id 11

-- =============================================================================
-- PRODUCT OPTIONS
-- option_categories: Término=1, Extras=2, Tamaño=3
-- =============================================================================
INSERT INTO product_options (name, option_category_id) VALUES
    -- Término de cocción (para hamburguesas)
    ('Bien Cocida',      1),   -- id 1
    ('Término Medio',    1),   -- id 2
    ('Poco Cocida',      1),   -- id 3
    -- Extras hamburguesa
    ('Queso Extra',      2),   -- id 4
    ('Con Bacon',        2),   -- id 5
    ('Doble Carne',      2),   -- id 6
    -- Tamaño pizza
    ('Personal',         3),   -- id 7
    ('Familiar',         3),   -- id 8
    -- Extras pizza
    ('Orilla Rellena',   2),   -- id 9
    ('Extra Queso',      2);   -- id 10

-- =============================================================================
-- PRODUCT ↔ OPTIONS (many-to-many via product_product_options)
-- Productos con has_options=TRUE: Hamburguesa Clásica=1, BBQ=2, Doble=3, Pizza Margarita=4
-- =============================================================================
INSERT INTO product_product_options (product_id, option_id) VALUES
    -- Hamburguesa Clásica → término + extras
    (1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
    -- Hamburguesa BBQ → término + extras
    (2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6),
    -- Hamburguesa Doble → término + extras
    (3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6),
    -- Pizza Margarita → tamaño + extras
    (4, 7), (4, 8), (4, 9), (4, 10);


-- =============================================================================
-- PRODUCT RECIPES (todos los productos tienen receta, con o sin opciones)
-- supply_variant IDs referencia al bloque de supply_variants arriba
-- =============================================================================
INSERT INTO product_recipes (product_id, supply_variant_id, required_quantity) VALUES
    -- Hamburguesa Clásica (id=1): pan + carne 100g + lechuga + tomate + mayonesa
    (1, 17, 1.000),   -- Pan de Hamburguesa 1u
    (1,  1, 1.000),   -- Carne de Res 100g
    (1, 10, 1.000),   -- Lechuga 40g
    (1, 11, 1.000),   -- Tomate 50g
    (1, 21, 1.000),   -- Mayonesa 20g

    -- Hamburguesa BBQ (id=2): pan + carne 100g + cebolla + salsa BBQ + queso cheddar
    (2, 17, 1.000),   -- Pan de Hamburguesa 1u
    (2,  1, 1.000),   -- Carne de Res 100g
    (2, 12, 1.000),   -- Cebolla 30g
    (2, 22, 1.000),   -- Salsa BBQ 25g
    (2,  9, 1.000),   -- Queso Cheddar 30g

    -- Hamburguesa Doble (id=3): pan + 2x carne 100g + lechuga + tomate + queso cheddar
    (3, 17, 1.000),   -- Pan de Hamburguesa 1u
    (3,  1, 2.000),   -- Carne de Res 100g × 2
    (3, 10, 1.000),   -- Lechuga 40g
    (3, 11, 1.000),   -- Tomate 50g
    (3,  9, 1.000),   -- Queso Cheddar 30g

    -- Pizza Margarita (id=4): masa 200g + salsa tomate 60g + queso mozzarella 100g
    (4, 15, 1.000),   -- Masa de Pizza 200g
    (4, 20, 1.000),   -- Salsa de Tomate 60g
    (4,  8, 1.000),   -- Queso Mozzarella 100g

    -- Pizza Pepperoni (id=5): masa 200g + salsa tomate 60g + queso mozzarella 100g + pepperoni 60g
    (5, 15, 1.000),   -- Masa de Pizza 200g
    (5, 20, 1.000),   -- Salsa de Tomate 60g
    (5,  8, 1.000),   -- Queso Mozzarella 100g
    (5,  5, 1.000),   -- Pepperoni 60g

    -- Pizza BBQ Pollo (id=6): masa 200g + salsa BBQ + queso mozzarella 100g + pollo 150g
    (6, 15, 1.000),   -- Masa de Pizza 200g
    (6, 22, 1.000),   -- Salsa BBQ 25g
    (6,  8, 1.000),   -- Queso Mozzarella 100g
    (6,  3, 1.000),   -- Pechuga de Pollo 150g

    -- Plato Ejecutivo Pollo (id=7): pollo 250g + arroz 150g + aceite + sal
    (7,  4, 1.000),   -- Pechuga de Pollo 250g
    (7, 18, 1.000),   -- Arroz 150g
    (7, 23, 1.000),   -- Aceite Vegetal 15g
    (7, 24, 1.000),   -- Sal 5g

    -- Plato Ejecutivo Res (id=8): carne 200g + arroz 150g + aceite + sal
    (8,  2, 1.000),   -- Carne de Res 200g
    (8, 18, 1.000),   -- Arroz 150g
    (8, 23, 1.000),   -- Aceite Vegetal 15g
    (8, 24, 1.000),   -- Sal 5g

    -- Papas Fritas (id=9): papa 200g + aceite + sal
    (9, 13, 1.000),   -- Papa 200g
    (9, 23, 1.000),   -- Aceite Vegetal 15g
    (9, 24, 1.000),   -- Sal 5g

    -- Aros de Cebolla (id=10): cebolla 30g + harina 100g + aceite + sal
    (10, 12, 1.000),  -- Cebolla 30g
    (10, 14, 1.000),  -- Harina de Trigo 100g
    (10, 23, 1.000),  -- Aceite Vegetal 15g
    (10, 24, 1.000),  -- Sal 5g

    -- Limonada Natural (id=11): limonada concentrada 100ml
    (11, 25, 1.000);  -- Limonada Concentrada 100ml

-- =============================================================================
-- OPTION RECIPES (recetas de las opciones de producto)
-- Solo opciones que agregan insumos: Queso Extra, Con Bacon, Doble Carne,
--   Orilla Rellena, Extra Queso. Las de término de cocción y tamaño no consumen
--   insumos adicionales (solo cambian el proceso/presentación).
-- =============================================================================
INSERT INTO option_recipes (option_id, supply_variant_id, required_quantity) VALUES
    -- Queso Extra (id=4) → queso cheddar 30g
    (4,  9, 1.000),
    -- Con Bacon (id=5) → bacon 40g
    (5,  6, 1.000),
    -- Doble Carne (id=6) → carne de res 100g adicional
    (6,  1, 1.000),
    -- Orilla Rellena (id=9) → queso mozzarella 50g
    (9,  7, 1.000),
    -- Extra Queso (id=10) → queso mozzarella 100g
    (10, 8, 1.000);
