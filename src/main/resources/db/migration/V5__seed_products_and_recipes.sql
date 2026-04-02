-- V5__seed_products_and_recipes.sql
-- Seed data: categories, areas, products, inventory and recipes

-- Categories
INSERT INTO categories (name, description, enabled) VALUES
('Platos Principales', 'Platos fuertes del menú', TRUE),
('Bebidas', 'Bebidas frías y calientes', TRUE),
('Entradas', 'Aperitivos y entradas', TRUE);

-- Areas
INSERT INTO areas (name, type, enabled) VALUES
('Cocina', 'KITCHEN', TRUE),
('Bar', 'BAR', TRUE);

-- Products
INSERT INTO products (name, base_price, has_options, active, category_id, area_id) VALUES
('Bandeja Paisa',     28000.00, FALSE, TRUE, 1, 1),
('Pollo a la Plancha',22000.00, TRUE,  TRUE, 1, 1),
('Limonada Natural',   6000.00, FALSE, TRUE, 2, 2),
('Ensalada Verde',    12000.00, FALSE, TRUE, 3, 1);

-- Supply categories
INSERT INTO supply_categories (name) VALUES
('Proteínas'),
('Granos'),
('Vegetales'),
('Lácteos'),
('Bebidas Base');

-- Units of measure
INSERT INTO units_of_measure (name, abbreviation) VALUES
('Gramos',      'g'),
('Mililitros',  'ml'),
('Unidades',    'und');

-- Supplies
INSERT INTO supplies (name, supply_category_id) VALUES
('Carne de Res',   1),
('Pechuga de Pollo', 1),
('Frijoles',       2),
('Arroz',          2),
('Lechuga',        3),
('Tomate',         3),
('Limón',          3),
('Chicharrón',     1);

-- Supply variants (supply_id, unit_id, quantity)
-- unit 1=g, 2=ml, 3=und
INSERT INTO supply_variants (supply_id, unit_id, quantity) VALUES
(1, 1, 200),   -- Carne de Res 200g
(2, 1, 180),   -- Pechuga de Pollo 180g
(3, 1, 150),   -- Frijoles 150g
(4, 1, 150),   -- Arroz 150g
(5, 1, 80),    -- Lechuga 80g
(6, 3, 1),     -- Tomate 1und
(7, 3, 2),     -- Limón 2und
(8, 1, 80);    -- Chicharrón 80g

-- Recipes: product_recipes (product_id, supply_variant_id, required_quantity)
-- Bandeja Paisa (id=1): carne, frijoles, arroz, chicharrón
INSERT INTO product_recipes (product_id, supply_variant_id, required_quantity) VALUES
(1, 1, 1),   -- 1 porción carne 200g
(1, 3, 1),   -- 1 porción frijoles 150g
(1, 4, 1),   -- 1 porción arroz 150g
(1, 8, 1);   -- 1 porción chicharrón 80g

-- Pollo a la Plancha (id=2): pechuga, arroz
INSERT INTO product_recipes (product_id, supply_variant_id, required_quantity) VALUES
(2, 2, 1),   -- 1 porción pechuga 180g
(2, 4, 1);   -- 1 porción arroz 150g

-- Ensalada Verde (id=4): lechuga, tomate
INSERT INTO product_recipes (product_id, supply_variant_id, required_quantity) VALUES
(4, 5, 1),   -- 1 porción lechuga 80g
(4, 6, 1);   -- 1 tomate

-- Limonada Natural (id=3): limón
INSERT INTO product_recipes (product_id, supply_variant_id, required_quantity) VALUES
(3, 7, 1);   -- 2 limones
