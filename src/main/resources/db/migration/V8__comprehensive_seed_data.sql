-- V8__comprehensive_seed_data.sql
-- Seed data realista para desarrollo local. NO para producción.
-- V6 ya siembra: areas, units_of_measure, storage_locations
-- IDs de referencia:
--   areas             → Cocina=1, Bar=2
--   units_of_measure  → g=1, kg=2, l=3, u=4, lb=5, pz=6, paq=7, bot=8, lat=9
--   storage_locations → Bodega=1, Cocina=2

-- =============================================================================
-- SUPPLY CATEGORIES
-- =============================================================================
INSERT INTO supply_categories (name) VALUES
    ('Proteínas'),           -- id 1
    ('Vegetales y Frescos'), -- id 2
    ('Lácteos'),             -- id 3
    ('Harinas y Masas'),     -- id 4
    ('Salsas y Bases'),      -- id 5
    ('Condimentos'),         -- id 6
    ('Embutidos y Curados'), -- id 7
    ('Frutas y Pulpas'),     -- id 8
    ('Endulzantes'),         -- id 9
    ('Desechables'),         -- id 10
    ('Congelados'),          -- id 11
    ('Granos y Legumbres'),  -- id 12
    ('Pastas'),              -- id 13
    ('Aceites y Grasas'),    -- id 14
    ('Bebidas e Hielo');     -- id 15

-- =============================================================================
-- SUPPLIES
-- =============================================================================
INSERT INTO supplies (name, supply_category_id) VALUES
    -- Proteínas (cat 1)
    ('Carne de Res Molida',          1),  -- id 1
    ('Pollo Apanado',                1),  -- id 2
    ('Lenteja Preparada',            1),  -- id 3
    ('Pechuga de Pollo',             1),  -- id 4
    ('Carne de Cerdo',               1),  -- id 5
    ('Camarón Tigre',                1),  -- id 6
    ('Baby Beef',                    1),  -- id 7
    ('Churrasco',                    1),  -- id 8
    ('Entrecot',                     1),  -- id 9
    ('Huevo',                        1),  -- id 10

    -- Vegetales y Frescos (cat 2)
    ('Lechuga Crespa',               2),  -- id 11
    ('Tomate',                       2),  -- id 12
    ('Cebolla Blanca',               2),  -- id 13
    ('Pepinillos',                   2),  -- id 14
    ('Pimentón Verde',               2),  -- id 15
    ('Hierbabuena Fresca',           2),  -- id 16
    ('Albahaca Fresca',              2),  -- id 17
    ('Champiñón Laminado',           2),  -- id 18
    ('Repollo',                      2),  -- id 19
    ('Zanahoria',                    2),  -- id 20
    ('Plátano Maduro',               2),  -- id 21
    ('Papa Criolla',                 2),  -- id 22
    ('Yuca',                         2),  -- id 23
    ('Papa Salada',                  2),  -- id 24

    -- Lácteos (cat 3)
    ('Queso Cheddar',                3),  -- id 25
    ('Queso Mozzarella',             3),  -- id 26
    ('Queso Parmesano',              3),  -- id 27
    ('Queso Crema',                  3),  -- id 28
    ('Crema de Leche',               3),  -- id 29
    ('Mantequilla',                  3),  -- id 30

    -- Harinas y Masas (cat 4)
    ('Pan Brioche',                  4),  -- id 31
    ('Pan Francés',                  4),  -- id 32
    ('Harina de Trigo',              4),  -- id 33
    ('Levadura Seca',                4),  -- id 34

    -- Salsas y Bases (cat 5)
    ('Salsa de Tomate Base',         5),  -- id 35
    ('Chimichurri Preparado',        5),  -- id 36
    ('Crema de Coco',                5),  -- id 37

    -- Condimentos (cat 6)
    ('Sal Refinada',                 6),  -- id 38
    ('Sal Parrillera',               6),  -- id 39
    ('Orégano Seco',                 6),  -- id 40
    ('Pimienta Negra',               6),  -- id 41

    -- Embutidos y Curados (cat 7)
    ('Tocineta Ahumada',             7),  -- id 42
    ('Pepperoni',                    7),  -- id 43
    ('Jamón Cocido',                 7),  -- id 44
    ('Bocadillo',                    7),  -- id 45

    -- Frutas y Pulpas (cat 8)
    ('Limón Tahití',                 8),  -- id 46
    ('Pulpa de Mango',               8),  -- id 47
    ('Pulpa de Mora',                8),  -- id 48
    ('Pulpa de Lulo',                8),  -- id 49
    ('Piña en Almíbar',              8),  -- id 50

    -- Endulzantes (cat 9)
    ('Azúcar Blanca',                9),  -- id 51
    ('Stevia',                       9),  -- id 52
    ('Miel de Abejas',               9),  -- id 53

    -- Desechables (cat 10)
    ('Empaque Caja Cartón',         10),  -- id 54
    ('Vaso Plástico',               10),  -- id 55
    ('Vaso de Vidrio',              10),  -- id 56
    ('Pitillo de Papel',            10),  -- id 57

    -- Congelados (cat 11)
    ('Papa a la Francesa Congelada',11),  -- id 58
    ('Papa en Cascos',              11),  -- id 59
    ('Patacón Prefrito',            11),  -- id 60

    -- Granos y Legumbres (cat 12)
    ('Arroz Blanco',                12),  -- id 61
    ('Frijol Cargamanto',           12),  -- id 62
    ('Lenteja Seca',                12),  -- id 63
    ('Maíz Tierno en Lata',         12),  -- id 64

    -- Pastas (cat 13)
    ('Pasta Larga Fettuccine',      13),  -- id 65
    ('Pasta Corta Penne',           13),  -- id 66

    -- Aceites y Grasas (cat 14)
    ('Aceite Vegetal',              14),  -- id 67

    -- Bebidas e Hielo (cat 15)
    ('Hielo en Cubo',               15);  -- id 68

-- =============================================================================
-- SUPPLY VARIANTS
-- Cada fila es una presentación física única (supply_id + unit_id + quantity UNIQUE)
-- unit_of_measure: g=1, kg=2, l=3, u=4, lb=5, pz=6, paq=7, bot=8, lat=9
-- =============================================================================
INSERT INTO supply_variants (supply_id, unit_id, quantity) VALUES
    -- Carne de Res Molida: 150g (hamburguesa) y 100g (pasta boloñesa)
    (1,  1, 150.000),   -- id 1
    (1,  1, 100.000),   -- id 2
    -- Pollo Apanado: 180g
    (2,  1, 180.000),   -- id 3
    -- Lenteja Preparada: 150g
    (3,  1, 150.000),   -- id 4
    -- Pechuga de Pollo: 200g (almuerzo) y 100g (pasta extra)
    (4,  1, 200.000),   -- id 5
    (4,  1, 100.000),   -- id 6
    -- Carne de Cerdo: 200g
    (5,  1, 200.000),   -- id 7
    -- Camarón Tigre: 6 unidades
    (6,  4,   6.000),   -- id 8
    -- Baby Beef: 300g
    (7,  1, 300.000),   -- id 9
    -- Churrasco: 300g
    (8,  1, 300.000),   -- id 10
    -- Entrecot: 300g
    (9,  1, 300.000),   -- id 11
    -- Huevo: 1u (hamburguesa extra) y 2u (almuerzo perico)
    (10, 4,   1.000),   -- id 12
    (10, 4,   2.000),   -- id 13
    -- Lechuga Crespa: 20g
    (11, 1,  20.000),   -- id 14
    -- Tomate: 2 rodajas (u)
    (12, 4,   2.000),   -- id 15
    -- Cebolla Blanca: 10g
    (13, 1,  10.000),   -- id 16
    -- Pepinillos: 3 rodajas (u)
    (14, 4,   3.000),   -- id 17
    -- Pimentón Verde: 30g
    (15, 1,  30.000),   -- id 18
    -- Hierbabuena Fresca: 2 ramas (u)
    (16, 4,   2.000),   -- id 19
    -- Albahaca Fresca: 1 porción (u)
    (17, 4,   1.000),   -- id 20
    -- Champiñón Laminado: 80g
    (18, 1,  80.000),   -- id 21
    -- Repollo: 50g
    (19, 1,  50.000),   -- id 22
    -- Zanahoria: 30g
    (20, 1,  30.000),   -- id 23
    -- Plátano Maduro: 0.5u
    (21, 4,   0.500),   -- id 24
    -- Papa Criolla: 150g
    (22, 1, 150.000),   -- id 25
    -- Yuca: 150g (frita)
    (23, 1, 150.000),   -- id 26
    -- Papa Salada: 1u
    (24, 4,   1.000),   -- id 27
    -- Queso Cheddar: 2 láminas (u)
    (25, 4,   2.000),   -- id 28
    -- Queso Mozzarella: 2 láminas (u) y 200g rallado
    (26, 4,   2.000),   -- id 29
    (26, 1, 200.000),   -- id 30
    -- Queso Parmesano: 15g
    (27, 1,  15.000),   -- id 31
    -- Queso Crema: 80g (borde pizza)
    (28, 1,  80.000),   -- id 32
    -- Crema de Leche: 100ml
    (29, 3,   0.100),   -- id 33
    -- Mantequilla: 30g
    (30, 1,  30.000),   -- id 34
    -- Pan Brioche: 1u
    (31, 4,   1.000),   -- id 35
    -- Pan Francés: 1u
    (32, 4,   1.000),   -- id 36
    -- Harina de Trigo: 400g
    (33, 1, 400.000),   -- id 37
    -- Levadura Seca: 10g
    (34, 1,  10.000),   -- id 38
    -- Salsa de Tomate Base: 100ml
    (35, 3,   0.100),   -- id 39
    -- Chimichurri Preparado: 10g
    (36, 1,  10.000),   -- id 40
    -- Crema de Coco: 30ml
    (37, 3,   0.030),   -- id 41
    -- Sal Refinada: 5g
    (38, 1,   5.000),   -- id 42
    -- Sal Parrillera: 5g
    (39, 1,   5.000),   -- id 43
    -- Orégano Seco: 1 porción (u)
    (40, 4,   1.000),   -- id 44
    -- Pimienta Negra: 1 porción (u)
    (41, 4,   1.000),   -- id 45
    -- Tocineta Ahumada: 2 tiras (u) y 50g (pasta carbonara)
    (42, 4,   2.000),   -- id 46
    (42, 1,  50.000),   -- id 47
    -- Pepperoni: 100g
    (43, 1, 100.000),   -- id 48
    -- Jamón Cocido: 100g
    (44, 1, 100.000),   -- id 49
    -- Bocadillo: 80g (borde pizza)
    (45, 1,  80.000),   -- id 50
    -- Limón Tahití: 2u
    (46, 4,   2.000),   -- id 51
    -- Pulpa de Mango: 100g
    (47, 1, 100.000),   -- id 52
    -- Pulpa de Mora: 100g
    (48, 1, 100.000),   -- id 53
    -- Pulpa de Lulo: 100g
    (49, 1, 100.000),   -- id 54
    -- Piña en Almíbar: 60g
    (50, 1,  60.000),   -- id 55
    -- Azúcar Blanca: 20g
    (51, 1,  20.000),   -- id 56
    -- Stevia: 1 sobre (u)
    (52, 4,   1.000),   -- id 57
    -- Miel de Abejas: 15ml
    (53, 3,   0.015),   -- id 58
    -- Empaque Caja Cartón: 1u
    (54, 4,   1.000),   -- id 59
    -- Vaso Plástico: 1u
    (55, 4,   1.000),   -- id 60
    -- Vaso de Vidrio: 1u
    (56, 4,   1.000),   -- id 61
    -- Pitillo de Papel: 2u
    (57, 4,   2.000),   -- id 62
    -- Papa a la Francesa Congelada: 150g
    (58, 1, 150.000),   -- id 63
    -- Papa en Cascos: 150g
    (59, 1, 150.000),   -- id 64
    -- Patacón Prefrito: 1u
    (60, 4,   1.000),   -- id 65
    -- Arroz Blanco: 100g crudo
    (61, 1, 100.000),   -- id 66
    -- Frijol Cargamanto: 80g seco
    (62, 1,  80.000),   -- id 67
    -- Lenteja Seca: 80g seca
    (63, 1,  80.000),   -- id 68
    -- Maíz Tierno en Lata: 40g
    (64, 1,  40.000),   -- id 69
    -- Pasta Larga Fettuccine: 200g
    (65, 1, 200.000),   -- id 70
    -- Pasta Corta Penne: 200g
    (66, 1, 200.000),   -- id 71
    -- Aceite Vegetal: 10ml
    (67, 3,   0.010),   -- id 72
    -- Hielo en Cubo: 150g
    (68, 1, 150.000),   -- id 73
    -- Ensalada de la Huerta (parrilla base): repollo 50g ya existe id 22
    -- Papa Salada entera: 1u ya existe id 27
    -- Pasta corta para almuerzo principio: 100g
    (66, 1, 100.000);   -- id 74

-- =============================================================================
-- STORAGE LOCATIONS adicionales
-- =============================================================================
INSERT INTO storage_locations (name) VALUES
    ('Cuarto Frío'),   -- id 3
    ('Bar');           -- id 4

-- =============================================================================
-- INVENTORY STOCK (stock inicial por variante)
-- storage_locations: Bodega=1, Cocina=2, Cuarto Frío=3, Bar=4
-- =============================================================================
INSERT INTO inventory_stock (supply_variant_id, storage_location_id, current_quantity) VALUES
    -- Carne de Res Molida 150g
    (1,  1, 40.000), (1,  2, 15.000),
    -- Carne de Res Molida 100g
    (2,  1, 30.000), (2,  2, 10.000),
    -- Pollo Apanado 180g
    (3,  1, 35.000), (3,  2, 12.000),
    -- Lenteja Preparada 150g
    (4,  1, 20.000), (4,  2,  8.000),
    -- Pechuga de Pollo 200g
    (5,  1, 30.000), (5,  2, 10.000),
    -- Pechuga de Pollo 100g
    (6,  1, 25.000), (6,  2,  8.000),
    -- Carne de Cerdo 200g
    (7,  1, 25.000), (7,  2,  8.000),
    -- Camarón Tigre 6u
    (8,  3, 20.000), (8,  2,  5.000),
    -- Baby Beef 300g
    (9,  3, 15.000), (9,  2,  5.000),
    -- Churrasco 300g
    (10, 3, 15.000), (10, 2,  5.000),
    -- Entrecot 300g
    (11, 3, 10.000), (11, 2,  3.000),
    -- Huevo 1u
    (12, 1, 60.000), (12, 2, 20.000),
    -- Huevo 2u
    (13, 1, 30.000), (13, 2, 10.000),
    -- Lechuga Crespa 20g
    (14, 1, 50.000), (14, 2, 20.000),
    -- Tomate 2 rodajas
    (15, 1, 40.000), (15, 2, 15.000),
    -- Cebolla Blanca 10g
    (16, 1, 60.000), (16, 2, 20.000),
    -- Pepinillos 3 rodajas
    (17, 1, 30.000), (17, 2, 10.000),
    -- Pimentón Verde 30g
    (18, 1, 25.000), (18, 2,  8.000),
    -- Hierbabuena Fresca 2 ramas
    (19, 1, 20.000), (19, 4, 10.000),
    -- Albahaca Fresca 1 porción
    (20, 1, 20.000), (20, 2,  8.000),
    -- Champiñón Laminado 80g
    (21, 1, 20.000), (21, 2,  6.000),
    -- Repollo 50g
    (22, 1, 40.000), (22, 2, 15.000),
    -- Zanahoria 30g
    (23, 1, 40.000), (23, 2, 15.000),
    -- Plátano Maduro 0.5u
    (24, 1, 30.000), (24, 2, 10.000),
    -- Papa Criolla 150g
    (25, 1, 30.000), (25, 2, 10.000),
    -- Yuca 150g
    (26, 1, 25.000), (26, 2,  8.000),
    -- Papa Salada 1u
    (27, 1, 40.000), (27, 2, 15.000),
    -- Queso Cheddar 2 láminas
    (28, 3, 50.000), (28, 2, 20.000),
    -- Queso Mozzarella 2 láminas
    (29, 3, 50.000), (29, 2, 20.000),
    -- Queso Mozzarella 200g rallado
    (30, 3, 20.000), (30, 2,  8.000),
    -- Queso Parmesano 15g
    (31, 1, 30.000), (31, 2, 10.000),
    -- Queso Crema 80g
    (32, 3, 20.000), (32, 2,  6.000),
    -- Crema de Leche 100ml
    (33, 3, 20.000), (33, 2,  8.000),
    -- Mantequilla 30g
    (34, 3, 30.000), (34, 2, 10.000),
    -- Pan Brioche 1u
    (35, 1, 60.000), (35, 2, 20.000),
    -- Pan Francés 1u
    (36, 1, 40.000), (36, 2, 15.000),
    -- Harina de Trigo 400g
    (37, 1, 20.000), (37, 2,  8.000),
    -- Levadura Seca 10g
    (38, 1, 30.000), (38, 2, 10.000),
    -- Salsa de Tomate Base 100ml
    (39, 1, 25.000), (39, 2, 10.000),
    -- Chimichurri Preparado 10g
    (40, 1, 30.000), (40, 2, 10.000),
    -- Crema de Coco 30ml
    (41, 1, 20.000), (41, 4, 10.000),
    -- Sal Refinada 5g
    (42, 1, 80.000), (42, 2, 30.000),
    -- Sal Parrillera 5g
    (43, 1, 50.000), (43, 2, 15.000),
    -- Orégano Seco 1 porción
    (44, 1, 40.000), (44, 2, 15.000),
    -- Pimienta Negra 1 porción
    (45, 1, 40.000), (45, 2, 15.000),
    -- Tocineta Ahumada 2 tiras
    (46, 3, 30.000), (46, 2, 10.000),
    -- Tocineta Ahumada 50g
    (47, 3, 20.000), (47, 2,  8.000),
    -- Pepperoni 100g
    (48, 3, 20.000), (48, 2,  8.000),
    -- Jamón Cocido 100g
    (49, 3, 20.000), (49, 2,  8.000),
    -- Bocadillo 80g
    (50, 1, 15.000), (50, 2,  5.000),
    -- Limón Tahití 2u
    (51, 1, 50.000), (51, 4, 20.000),
    -- Pulpa de Mango 100g
    (52, 3, 20.000), (52, 4, 10.000),
    -- Pulpa de Mora 100g
    (53, 3, 20.000), (53, 4, 10.000),
    -- Pulpa de Lulo 100g
    (54, 3, 20.000), (54, 4, 10.000),
    -- Piña en Almíbar 60g
    (55, 1, 15.000), (55, 2,  5.000),
    -- Azúcar Blanca 20g
    (56, 1, 80.000), (56, 4, 30.000),
    -- Stevia 1 sobre
    (57, 1, 50.000), (57, 4, 20.000),
    -- Miel de Abejas 15ml
    (58, 1, 30.000), (58, 4, 15.000),
    -- Empaque Caja Cartón 1u
    (59, 1, 100.000),(59, 2, 30.000),
    -- Vaso Plástico 1u
    (60, 1, 100.000),(60, 4, 40.000),
    -- Vaso de Vidrio 1u
    (61, 1,  50.000),(61, 4, 20.000),
    -- Pitillo de Papel 2u
    (62, 1, 100.000),(62, 4, 40.000),
    -- Papa a la Francesa Congelada 150g
    (63, 3,  40.000),(63, 2, 15.000),
    -- Papa en Cascos 150g
    (64, 3,  30.000),(64, 2, 10.000),
    -- Patacón Prefrito 1u
    (65, 3,  30.000),(65, 2, 10.000),
    -- Arroz Blanco 100g
    (66, 1,  60.000),(66, 2, 20.000),
    -- Frijol Cargamanto 80g
    (67, 1,  30.000),(67, 2, 10.000),
    -- Lenteja Seca 80g
    (68, 1,  30.000),(68, 2, 10.000),
    -- Maíz Tierno en Lata 40g
    (69, 1,  20.000),(69, 2,  8.000),
    -- Pasta Larga Fettuccine 200g
    (70, 1,  30.000),(70, 2, 10.000),
    -- Pasta Corta Penne 200g
    (71, 1,  30.000),(71, 2, 10.000),
    -- Aceite Vegetal 10ml
    (72, 1,  60.000),(72, 2, 20.000),
    -- Hielo en Cubo 150g
    (73, 1,  50.000),(73, 4, 30.000),
    -- Pasta Corta Penne 100g (principio almuerzo)
    (74, 1,  20.000),(74, 2,  8.000);

-- =============================================================================
-- PRODUCT CATEGORIES
-- =============================================================================
INSERT INTO categories (name, description, enabled) VALUES
    ('Hamburguesas',      'Hamburguesas artesanales con pan brioche',          TRUE),  -- id 1
    ('Almuerzo Ejecutivo','Menú del día con proteína, principio y acompañamiento', TRUE), -- id 2
    ('Pizzas',            'Pizzas al horno, tamaño familiar',                  TRUE),  -- id 3
    ('Pasta / Italiana',  'Pastas de la casa con salsas artesanales',          TRUE),  -- id 4
    ('Parrilla / Carnes', 'Cortes de res a la parrilla con guarnición',        TRUE),  -- id 5
    ('Bebidas Naturales', 'Jugos naturales y limonadas con fruta fresca',      TRUE);  -- id 6

-- =============================================================================
-- OPTION CATEGORIES
-- Agrupan las selecciones de construcción de cada producto
-- =============================================================================
INSERT INTO option_categories (name, description) VALUES
    ('Proteína Hamburguesa',  'Elección de proteína para hamburguesa'),         -- id 1
    ('Queso Hamburguesa',     'Elección de queso para hamburguesa'),            -- id 2
    ('Vegetales Hamburguesa', 'Vegetales opcionales para hamburguesa'),         -- id 3
    ('Acompañamiento Hamburguesa', 'Acompañamiento para hamburguesa'),          -- id 4
    ('Adición Extra Hamburguesa',  'Ingredientes extra para hamburguesa'),      -- id 5
    ('Proteína Almuerzo',     'Elección de proteína para menú del día'),        -- id 6
    ('Principio Almuerzo',    'Elección de principio para menú del día'),       -- id 7
    ('Acompañamiento Almuerzo','Acompañamiento para menú del día'),             -- id 8
    ('Sabor Pizza',           'Elección de sabor/topping para pizza'),          -- id 9
    ('Vegetal Extra Pizza',   'Vegetales adicionales para pizza'),              -- id 10
    ('Borde Pizza',           'Tipo de borde para pizza'),                      -- id 11
    ('Salsa Pasta',           'Elección de salsa para pasta'),                  -- id 12
    ('Proteína Extra Pasta',  'Proteína adicional para pasta'),                 -- id 13
    ('Especia Pasta',         'Especia para pasta'),                            -- id 14
    ('Corte Parrilla',        'Elección de corte de res'),                      -- id 15
    ('Término Parrilla',      'Punto de cocción del corte'),                    -- id 16
    ('Guarnición Parrilla',   'Guarnición para el corte de res'),               -- id 17
    ('Base Fruta Bebida',     'Fruta o pulpa base para la bebida'),             -- id 18
    ('Endulzante Bebida',     'Tipo de endulzante para la bebida'),             -- id 19
    ('Sabor Especial Bebida', 'Toque especial para la bebida');                 -- id 20

-- =============================================================================
-- PRODUCTS
-- areas: Cocina=1, Bar=2
-- categories: Hamburguesas=1, Almuerzo Ejecutivo=2, Pizzas=3,
--             Pasta/Italiana=4, Parrilla/Carnes=5, Bebidas Naturales=6
-- =============================================================================
INSERT INTO products (name, base_price, active, category_id, area_id) VALUES
    ('Hamburguesa Double Bacon Cheese', 28000.00, TRUE, 1, 1),  -- id 1
    ('Menú del Día',                    18000.00, TRUE, 2, 1),  -- id 2
    ('Pizza Especial Familiar',         38000.00, TRUE, 3, 1),  -- id 3
    ('Pasta de la Casa',                22000.00, TRUE, 4, 1),  -- id 4
    ('Corte de Res Premium',            55000.00, TRUE, 5, 1),  -- id 5
    ('Limonada / Jugo Natural',          8000.00, TRUE, 6, 2);  -- id 6

-- =============================================================================
-- PRODUCT OPTIONS
-- =============================================================================
INSERT INTO product_options (name, option_category_id) VALUES
    -- Proteína Hamburguesa (cat 1)
    ('Carne de Res 150g',        1),   -- id 1
    ('Pollo Apanado 180g',       1),   -- id 2
    ('Lenteja Preparada 150g',   1),   -- id 3
    -- Queso Hamburguesa (cat 2)
    ('Queso Cheddar x2',         2),   -- id 4
    ('Queso Mozzarella x2',      2),   -- id 5
    -- Vegetales Hamburguesa (cat 3)
    ('Tomate 2 rodajas',         3),   -- id 6
    ('Cebolla Blanca 10g',       3),   -- id 7
    -- Acompañamiento Hamburguesa (cat 4)
    ('Papa a la Francesa 150g',  4),   -- id 8
    ('Papa en Cascos 150g',      4),   -- id 9
    ('Sin Acompañamiento',       4),   -- id 10
    -- Adición Extra Hamburguesa (cat 5)
    ('Tocineta Ahumada x2',      5),   -- id 11
    ('Huevo 1 unidad',           5),   -- id 12
    ('Pepinillos x3',            5),   -- id 13
    -- Proteína Almuerzo (cat 6)
    ('Pechuga de Pollo 200g',    6),   -- id 14
    ('Carne de Cerdo 200g',      6),   -- id 15
    ('Huevo Perico x2',          6),   -- id 16
    -- Principio Almuerzo (cat 7)
    ('Frijol Cargamanto 80g',    7),   -- id 17
    ('Lenteja Seca 80g',         7),   -- id 18
    ('Pasta Corta 100g',         7),   -- id 19
    -- Acompañamiento Almuerzo (cat 8)
    ('Plátano Maduro 1/2',       8),   -- id 20
    ('Patacón Prefrito 1u',      8),   -- id 21
    ('Papa Salada 1u',           8),   -- id 22
    -- Sabor Pizza (cat 9)
    ('Pepperoni 100g',           9),   -- id 23
    ('Jamón Cocido 100g',        9),   -- id 24
    ('Champiñón Laminado 80g',   9),   -- id 25
    -- Vegetal Extra Pizza (cat 10)
    ('Pimentón Verde 30g',      10),   -- id 26
    ('Maíz Tierno 40g',         10),   -- id 27
    ('Piña en Almíbar 60g',     10),   -- id 28
    -- Borde Pizza (cat 11)
    ('Borde Queso Crema 80g',   11),   -- id 29
    ('Borde Bocadillo 80g',     11),   -- id 30
    -- Salsa Pasta (cat 12)
    ('Salsa Alfredo',           12),   -- id 31
    ('Salsa Boloñesa',          12),   -- id 32
    ('Salsa Carbonara',         12),   -- id 33
    -- Proteína Extra Pasta (cat 13)
    ('Camarón Tigre x6',        13),   -- id 34
    ('Pechuga en Cubos 100g',   13),   -- id 35
    ('Sin Proteína Extra',      13),   -- id 36
    -- Especia Pasta (cat 14)
    ('Orégano Seco',            14),   -- id 37
    ('Albahaca Fresca',         14),   -- id 38
    ('Pimienta Negra',          14),   -- id 39
    -- Corte Parrilla (cat 15)
    ('Baby Beef 300g',          15),   -- id 40
    ('Churrasco 300g',          15),   -- id 41
    ('Entrecot 300g',           15),   -- id 42
    -- Término Parrilla (cat 16)
    ('Azul',                    16),   -- id 43
    ('Medio',                   16),   -- id 44
    ('3/4',                     16),   -- id 45
    ('Bien Asado',              16),   -- id 46
    -- Guarnición Parrilla (cat 17)
    ('Papa Criolla 150g',       17),   -- id 47
    ('Yuca Frita 150g',         17),   -- id 48
    ('Ensalada de Papa 150g',   17),   -- id 49
    -- Base Fruta Bebida (cat 18)
    ('Limón Tahití x2',         18),   -- id 50
    ('Pulpa de Mango 100g',     18),   -- id 51
    ('Pulpa de Mora 100g',      18),   -- id 52
    ('Pulpa de Lulo 100g',      18),   -- id 53
    -- Endulzante Bebida (cat 19)
    ('Azúcar Blanca 20g',       19),   -- id 54
    ('Stevia 1 sobre',          19),   -- id 55
    ('Miel de Abejas 15ml',     19),   -- id 56
    -- Sabor Especial Bebida (cat 20)
    ('Crema de Coco 30ml',      20),   -- id 57
    ('Hierbabuena Fresca x2',   20);   -- id 58

-- =============================================================================
-- PRODUCT ↔ OPTIONS (many-to-many)
-- =============================================================================
INSERT INTO product_product_options (product_id, option_id) VALUES
    -- Hamburguesa Double Bacon Cheese (id=1)
    -- Proteína
    (1,  1), (1,  2), (1,  3),
    -- Queso
    (1,  4), (1,  5),
    -- Vegetales
    (1,  6), (1,  7),
    -- Acompañamiento
    (1,  8), (1,  9), (1, 10),
    -- Extras
    (1, 11), (1, 12), (1, 13),

    -- Menú del Día (id=2)
    -- Proteína
    (2, 14), (2, 15), (2, 16),
    -- Principio
    (2, 17), (2, 18), (2, 19),
    -- Acompañamiento
    (2, 20), (2, 21), (2, 22),

    -- Pizza Especial Familiar (id=3)
    -- Sabor
    (3, 23), (3, 24), (3, 25),
    -- Vegetal Extra
    (3, 26), (3, 27), (3, 28),
    -- Borde
    (3, 29), (3, 30),

    -- Pasta de la Casa (id=4)
    -- Salsa
    (4, 31), (4, 32), (4, 33),
    -- Proteína Extra
    (4, 34), (4, 35), (4, 36),
    -- Especia
    (4, 37), (4, 38), (4, 39),

    -- Corte de Res Premium (id=5)
    -- Corte
    (5, 40), (5, 41), (5, 42),
    -- Término
    (5, 43), (5, 44), (5, 45), (5, 46),
    -- Guarnición
    (5, 47), (5, 48), (5, 49),

    -- Limonada / Jugo Natural (id=6)
    -- Base Fruta
    (6, 50), (6, 51), (6, 52), (6, 53),
    -- Endulzante
    (6, 54), (6, 55), (6, 56),
    -- Sabor Especial
    (6, 57), (6, 58);

-- =============================================================================
-- PRODUCT RECIPES (receta base fija — insumos que siempre van en el plato)
-- supply_variant IDs según el bloque de supply_variants arriba
-- =============================================================================
INSERT INTO product_recipes (product_id, supply_variant_id, required_quantity) VALUES
    -- -------------------------------------------------------------------------
    -- Hamburguesa Double Bacon Cheese (id=1)
    -- Base fija: Pan Brioche, Mantequilla, Lechuga Crespa, Empaque Caja Cartón
    -- -------------------------------------------------------------------------
    (1, 35, 1.000),   -- Pan Brioche 1u
    (1, 34, 1.000),   -- Mantequilla 30g
    (1, 14, 1.000),   -- Lechuga Crespa 20g
    (1, 59, 1.000),   -- Empaque Caja Cartón 1u

    -- -------------------------------------------------------------------------
    -- Menú del Día (id=2)
    -- Base fija: Arroz Blanco, Aceite Vegetal, Sal Refinada, Ensalada (Repollo+Zanahoria)
    -- -------------------------------------------------------------------------
    (2, 66, 1.000),   -- Arroz Blanco 100g
    (2, 72, 1.000),   -- Aceite Vegetal 10ml
    (2, 42, 1.000),   -- Sal Refinada 5g
    (2, 22, 1.000),   -- Repollo 50g
    (2, 23, 1.000),   -- Zanahoria 30g

    -- -------------------------------------------------------------------------
    -- Pizza Especial Familiar (id=3)
    -- Base fija: Harina de Trigo, Levadura Seca, Salsa de Tomate Base, Queso Mozzarella 200g
    -- -------------------------------------------------------------------------
    (3, 37, 1.000),   -- Harina de Trigo 400g
    (3, 38, 1.000),   -- Levadura Seca 10g
    (3, 39, 1.000),   -- Salsa de Tomate Base 100ml
    (3, 30, 1.000),   -- Queso Mozzarella 200g rallado

    -- -------------------------------------------------------------------------
    -- Pasta de la Casa (id=4)
    -- Base fija: Pan Francés, Queso Parmesano
    -- (La pasta en sí se elige entre Fettuccine o Penne — se pone Fettuccine como base,
    --  Penne se puede agregar como opción de construcción si se desea en el futuro)
    -- -------------------------------------------------------------------------
    (4, 70, 1.000),   -- Pasta Larga Fettuccine 200g
    (4, 36, 1.000),   -- Pan Francés 1u
    (4, 31, 1.000),   -- Queso Parmesano 15g

    -- -------------------------------------------------------------------------
    -- Corte de Res Premium (id=5)
    -- Base fija: Sal Parrillera, Chimichurri, Ensalada de la Huerta (Repollo)
    -- -------------------------------------------------------------------------
    (5, 43, 1.000),   -- Sal Parrillera 5g
    (5, 40, 1.000),   -- Chimichurri Preparado 10g
    (5, 22, 1.000),   -- Repollo 50g (ensalada de la huerta)

    -- -------------------------------------------------------------------------
    -- Limonada / Jugo Natural (id=6)
    -- Base fija: Vaso Plástico, Pitillo de Papel, Hielo en Cubo
    -- -------------------------------------------------------------------------
    (6, 60, 1.000),   -- Vaso Plástico 1u
    (6, 62, 1.000),   -- Pitillo de Papel 2u
    (6, 73, 1.000);   -- Hielo en Cubo 150g

-- =============================================================================
-- OPTION RECIPES (insumos que consume cada opción seleccionable)
-- Las opciones de término (Azul/Medio/3/4/Bien Asado) y "Sin X" no consumen insumos.
-- =============================================================================
INSERT INTO option_recipes (option_id, supply_variant_id, required_quantity) VALUES
    -- Proteína Hamburguesa
    (1,  1, 1.000),   -- Carne de Res 150g
    (2,  3, 1.000),   -- Pollo Apanado 180g
    (3,  4, 1.000),   -- Lenteja Preparada 150g
    -- Queso Hamburguesa
    (4, 28, 1.000),   -- Queso Cheddar 2 láminas
    (5, 29, 1.000),   -- Queso Mozzarella 2 láminas
    -- Vegetales Hamburguesa
    (6, 15, 1.000),   -- Tomate 2 rodajas
    (7, 16, 1.000),   -- Cebolla Blanca 10g
    -- Acompañamiento Hamburguesa
    (8, 63, 1.000),   -- Papa a la Francesa Congelada 150g
    (9, 64, 1.000),   -- Papa en Cascos 150g
    -- Extras Hamburguesa
    (11, 46, 1.000),  -- Tocineta Ahumada 2 tiras
    (12, 12, 1.000),  -- Huevo 1u
    (13, 17, 1.000),  -- Pepinillos 3 rodajas
    -- Proteína Almuerzo
    (14,  5, 1.000),  -- Pechuga de Pollo 200g
    (15,  7, 1.000),  -- Carne de Cerdo 200g
    (16, 13, 1.000),  -- Huevo 2u (perico)
    -- Principio Almuerzo
    (17, 67, 1.000),  -- Frijol Cargamanto 80g
    (18, 68, 1.000),  -- Lenteja Seca 80g
    (19, 74, 1.000),  -- Pasta Corta Penne 100g
    -- Acompañamiento Almuerzo
    (20, 24, 1.000),  -- Plátano Maduro 0.5u
    (21, 65, 1.000),  -- Patacón Prefrito 1u
    (22, 27, 1.000),  -- Papa Salada 1u
    -- Sabor Pizza
    (23, 48, 1.000),  -- Pepperoni 100g
    (24, 49, 1.000),  -- Jamón Cocido 100g
    (25, 21, 1.000),  -- Champiñón Laminado 80g
    -- Vegetal Extra Pizza
    (26, 18, 1.000),  -- Pimentón Verde 30g
    (27, 69, 1.000),  -- Maíz Tierno en Lata 40g
    (28, 55, 1.000),  -- Piña en Almíbar 60g
    -- Borde Pizza
    (29, 32, 1.000),  -- Queso Crema 80g
    (30, 50, 1.000),  -- Bocadillo 80g
    -- Salsa Pasta
    (31, 33, 1.000),  -- Crema de Leche 100ml (Alfredo)
    (32,  2, 1.000),  -- Carne de Res Molida 100g (Boloñesa)
    (33, 47, 1.000),  -- Tocineta Ahumada 50g (Carbonara)
    -- Proteína Extra Pasta
    (34,  8, 1.000),  -- Camarón Tigre 6u
    (35,  6, 1.000),  -- Pechuga en Cubos 100g
    -- Especia Pasta
    (37, 44, 1.000),  -- Orégano Seco 1 porción
    (38, 20, 1.000),  -- Albahaca Fresca 1 porción
    (39, 45, 1.000),  -- Pimienta Negra 1 porción
    -- Corte Parrilla
    (40,  9, 1.000),  -- Baby Beef 300g
    (41, 10, 1.000),  -- Churrasco 300g
    (42, 11, 1.000),  -- Entrecot 300g
    -- Guarnición Parrilla
    (47, 25, 1.000),  -- Papa Criolla 150g
    (48, 26, 1.000),  -- Yuca Frita 150g
    (49, 25, 1.000),  -- Ensalada de Papa 150g (usa papa criolla)
    -- Base Fruta Bebida
    (50, 51, 1.000),  -- Limón Tahití 2u
    (51, 52, 1.000),  -- Pulpa de Mango 100g
    (52, 53, 1.000),  -- Pulpa de Mora 100g
    (53, 54, 1.000),  -- Pulpa de Lulo 100g
    -- Endulzante Bebida
    (54, 56, 1.000),  -- Azúcar Blanca 20g
    (55, 57, 1.000),  -- Stevia 1 sobre
    (56, 58, 1.000),  -- Miel de Abejas 15ml
    -- Sabor Especial Bebida
    (57, 41, 1.000),  -- Crema de Coco 30ml
    (58, 19, 1.000);  -- Hierbabuena Fresca 2 ramas
