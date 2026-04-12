-- V8__comprehensive_seed_data.sql
-- Complete seed data for testing RMS (excludes login-related tables)
-- Uses INSERT IGNORE to handle duplicate entries safely
-- Run after V6__seed_data.sql

-- =============================================================================
-- PRODUCT CATEGORIES
-- =============================================================================
INSERT IGNORE INTO categories (name, description, enabled) VALUES
    ('Entradas', 'Aperitivos y entradas ligeras', TRUE),
    ('Principios', 'Arroz, pastas y platos base', TRUE),
    ('Carnes', 'Cortes de carne a la parrilla', TRUE),
    ('Pollo', 'Preparaciones con pollo', TRUE),
    ('Pescados', 'Mariscos y pescados frescos', TRUE),
    ('Ensaladas', 'Ensaladas frescas y ligeras', TRUE),
    ('Bebidas', 'Refrescos y jugos naturales', TRUE),
    ('Cocktails', 'Bebidas con alcohol y cócteles', TRUE),
    ('Postres', 'Dulces y postres', TRUE),
    ('Menú del Día', 'Productos del menú diario', TRUE);

-- =============================================================================
-- OPTION CATEGORIES (for customizable products)
-- =============================================================================
INSERT IGNORE INTO option_categories (name, description) VALUES
    ('Tipo de Cocción', 'Punto de cocción de la carne'),
    ('Accompañamiento', 'Guarnición para el plato principal'),
    ('Tamaño', 'Tamaños disponibles'),
    ('Nivel de Picante', 'Intensidad del picante'),
    ('Tipo de Leche', 'Leche para bebidas calientes'),
    ('Tipo de Pan', 'Variedad de panes disponibles'),
    ('Salsa', 'Salsas adicionales'),
    ('Extras', 'Ingredientes extra');

-- =============================================================================
-- RESTAURANT TABLES
-- =============================================================================
INSERT IGNORE INTO tables (table_number, capacity, status) VALUES
    (1, 2, 'AVAILABLE'),
    (2, 2, 'AVAILABLE'),
    (3, 4, 'AVAILABLE'),
    (4, 4, 'AVAILABLE'),
    (5, 6, 'AVAILABLE'),
    (6, 6, 'AVAILABLE'),
    (7, 8, 'AVAILABLE'),
    (8, 8, 'AVAILABLE'),
    (9, 10, 'AVAILABLE'),
    (10, 12, 'AVAILABLE'),
    (11, 2, 'AVAILABLE'),
    (12, 4, 'AVAILABLE');

-- =============================================================================
-- SUPPLY CATEGORIES (for inventory)
-- =============================================================================
INSERT IGNORE INTO supply_categories (name) VALUES
    ('Proteínas'),
    ('Vegetales'),
    ('Lácteos'),
    ('Granos y Pastas'),
    ('Frutas'),
    ('Bebidas'),
    ('Condimentos'),
    ('Aceites y Grasas'),
    ('Embutidos'),
    ('Panadería'),
    ('Mariscos'),
    ('Hierbas y Especias');

-- =============================================================================
-- SUPPLIES (base ingredients)
-- Unit ids: 1=g, 2=kg, 3=l, 4=u, 5=lb, 6=pz, 7=paq, 8=bot, 9=lat
-- =============================================================================
INSERT IGNORE INTO supplies (name, supply_category_id) VALUES
    ('Carne de Res', 1),
    ('Pechuga de Pollo', 1),
    ('Cerdo', 1),
    ('Lomo de Cerdo', 1),
    ('Salmon', 1),
    ('Tilapia', 1),
    ('Camaron', 1),
    ('Tomate', 2),
    ('Cebolla', 2),
    ('Ajo', 2),
    ('Lechuga', 2),
    ('Zanahoria', 2),
    ('Pimenton', 2),
    ('Aguacate', 2),
    ('Papa', 2),
    ('Yuca', 2),
    ('Platano', 2),
    ('Leche', 3),
    ('Queso Mozarella', 3),
    ('Queso Parmesano', 3),
    ('Crema de Leche', 3),
    ('Mantequilla', 3),
    ('Huevo', 3),
    ('Arroz', 4),
    ('Pasta Espagueti', 4),
    ('Fideos', 4),
    ('Panela', 4),
    ('Limon', 5),
    ('Naranja', 5),
    ('Fresa', 5),
    ('Mango', 5),
    ('Gaseosa', 6),
    ('Jugo de Naranja', 6),
    ('Agua', 6),
    ('Cerveza', 6),
    ('Vino Tinto', 6),
    ('Sal', 7),
    ('Pimienta', 7),
    ('Azeite de Oliva', 7),
    ('Vinagre', 7),
    ('Salsa Barbecue', 7),
    ('Salsa Picante', 7),
    ('Aceite Vegetal', 8),
    ('Manteca', 8),
    ('Tocino', 9),
    ('Jamón', 9),
    ('Pan Frances', 10),
    ('Pan de Hamburguesa', 10),
    ('Arepas', 10),
    ('Calamar', 11),
    ('Cilantro', 12),
    ('Perejil', 12),
    ('Orégano', 12);

-- =============================================================================
-- SUPPLY VARIANTS (specific presentations)
-- =============================================================================
INSERT IGNORE INTO supply_variants (supply_id, unit_id, quantity) VALUES
    (1, 1, 500), (1, 1, 1000),
    (2, 1, 500), (2, 1, 1000),
    (3, 1, 500), (4, 1, 800),
    (5, 1, 400), (6, 1, 350), (7, 1, 300),
    (8, 1, 500), (9, 1, 300), (10, 1, 100),
    (11, 1, 200), (12, 1, 250), (13, 1, 200),
    (14, 4, 1), (15, 1, 1000), (16, 1, 500), (17, 1, 500),
    (18, 3, 1), (19, 1, 400), (20, 1, 200),
    (21, 3, 0.5), (22, 1, 250), (23, 4, 12),
    (24, 1, 1000), (25, 1, 500), (26, 1, 500), (27, 1, 500),
    (28, 4, 3), (29, 4, 4), (30, 4, 250), (31, 4, 2),
    (32, 3, 1.5), (33, 3, 1), (34, 3, 0.6), (35, 3, 0.35), (36, 3, 0.75),
    (37, 1, 200), (38, 1, 100), (39, 3, 0.5), (40, 3, 0.5), (41, 3, 0.3), (42, 3, 0.2),
    (43, 3, 1), (44, 1, 400),
    (45, 1, 200), (46, 1, 200),
    (47, 4, 6), (48, 4, 4), (49, 4, 4),
    (50, 1, 300),
    (51, 1, 50), (52, 1, 50), (53, 1, 50);

-- =============================================================================
-- INVENTORY STOCK
-- =============================================================================
INSERT IGNORE INTO inventory_stock (supply_variant_id, storage_location_id, current_quantity) VALUES
    (1, 1, 50), (2, 1, 30), (3, 1, 25), (4, 1, 20), (5, 1, 15),
    (6, 1, 12), (7, 1, 10), (8, 1, 15), (9, 1, 8),
    (10, 1, 30), (11, 1, 25), (12, 1, 10), (13, 1, 20), (14, 1, 15),
    (15, 1, 15), (16, 1, 10), (17, 1, 40), (18, 1, 20), (19, 1, 15),
    (20, 1, 30), (21, 1, 15), (22, 1, 8), (23, 1, 12), (24, 1, 20),
    (25, 1, 36), (26, 1, 40), (27, 1, 20), (28, 1, 15), (29, 1, 15),
    (30, 1, 20), (31, 1, 15), (32, 1, 10), (33, 1, 10), (34, 1, 24),
    (35, 1, 20), (36, 1, 48), (37, 1, 24), (38, 1, 12), (39, 1, 15),
    (40, 1, 10), (41, 1, 12), (42, 1, 10), (43, 1, 15), (44, 1, 8),
    (45, 1, 20), (46, 1, 10), (47, 1, 15), (48, 1, 12), (49, 1, 30),
    (50, 1, 12), (51, 1, 20), (52, 1, 8), (53, 1, 10), (54, 1, 8), (55, 1, 8),
    (1, 2, 10), (3, 2, 8), (10, 2, 10), (11, 2, 8), (13, 2, 5),
    (17, 2, 15), (20, 2, 10), (21, 2, 5), (26, 2, 15), (39, 2, 3), (45, 2, 5);

-- =============================================================================
-- PRODUCTS
-- NOTE: Areas already exist from V6: 1=Cocina, 2=Bar
-- =============================================================================
INSERT IGNORE INTO products (name, base_price, has_options, active, category_id, area_id) VALUES
    ('Patas de Pollo Fritas', 18500.00, FALSE, TRUE, 1, 1),
    ('Dedos de Queso', 15000.00, FALSE, TRUE, 1, 1),
    ('Chorizo Argentino', 12000.00, FALSE, TRUE, 1, 1),
    ('Osobucoanco', 22000.00, FALSE, TRUE, 1, 1),
    ('Arroz con Pollo', 22000.00, FALSE, TRUE, 2, 1),
    ('Arroz con Camarones', 32000.00, FALSE, TRUE, 2, 1),
    ('Pasta Bolognesa', 24000.00, FALSE, TRUE, 2, 1),
    ('Mondongo', 20000.00, FALSE, TRUE, 2, 1),
    ('Ribeye 400g', 58000.00, TRUE, TRUE, 3, 1),
    ('NY Strip 350g', 52000.00, TRUE, TRUE, 3, 1),
    ('Tomahawk 600g', 72000.00, FALSE, TRUE, 3, 1),
    ('Lomo Alto 300g', 48000.00, TRUE, TRUE, 3, 1),
    ('Cerdo BBQ', 35000.00, FALSE, TRUE, 3, 1),
    ('Pechuga a la Plancha', 28000.00, TRUE, TRUE, 4, 1),
    ('Pollo Broaster', 25000.00, FALSE, TRUE, 4, 1),
    ('Alitas BBQ', 22000.00, TRUE, TRUE, 4, 1),
    ('Muslo Apanado', 20000.00, FALSE, TRUE, 4, 1),
    ('Salmón a la Parrilla', 42000.00, TRUE, TRUE, 5, 1),
    ('Tilapia Frita', 32000.00, FALSE, TRUE, 5, 1),
    ('Ceviche de Camarón', 38000.00, FALSE, TRUE, 5, 1),
    ('Pargo Rojo', 48000.00, FALSE, TRUE, 5, 1),
    ('Ensalada César', 18000.00, TRUE, TRUE, 6, 1),
    ('Ensalada de la Casa', 15000.00, FALSE, TRUE, 6, 1),
    ('Ensalada de Aguacate', 16000.00, FALSE, TRUE, 6, 1),
    ('Gaseosa 500ml', 4500.00, FALSE, TRUE, 7, 2),
    ('Jugo Natural 400ml', 8000.00, TRUE, TRUE, 7, 2),
    ('Limonada Natura', 6000.00, FALSE, TRUE, 7, 2),
    ('Agua con Gas 600ml', 4000.00, FALSE, TRUE, 7, 2),
    ('Mojito', 18000.00, FALSE, TRUE, 8, 2),
    ('Caipiriña', 18000.00, TRUE, TRUE, 8, 2),
    ('Piña Colada', 20000.00, FALSE, TRUE, 8, 2),
    ('Margarita', 18000.00, TRUE, TRUE, 8, 2),
    ('Cuba Libre', 16000.00, FALSE, TRUE, 8, 2),
    ('Moscow Mule', 20000.00, FALSE, TRUE, 8, 2),
    ('Brownie con Helado', 14000.00, FALSE, TRUE, 9, 1),
    ('Tres Leches', 12000.00, FALSE, TRUE, 9, 1),
    ('Tiramisú', 15000.00, FALSE, TRUE, 9, 1),
    ('Volcán de Chocolate', 16000.00, FALSE, TRUE, 9, 1),
    ('Suspiro Limeño', 10000.00, FALSE, TRUE, 9, 1);

-- =============================================================================
-- PRODUCT OPTIONS (customization options)
-- =============================================================================
INSERT IGNORE INTO product_options (name, option_category_id) VALUES
    ('Término Medio', 1), ('Tres Cuartos', 1), ('Bien Cocido', 1), ('Añejo', 1),
    ('Papa Frita', 2), ('Ensalada Verde', 2), ('Yuca Frita', 2), ('Arepa', 2), ('Cazuela de Frijoles', 2),
    ('Salsa BBQ', 7), ('Salsa Picante', 7), ('Salsa de Champiñones', 7),
    ('Pollo Desmechado', 8), ('Tocino', 8), ('Camaron', 8), ('Aguacate', 8),
    ('Pequeño', 3), ('Mediano', 3), ('Grande', 3),
    ('Suave', 4), ('Intenso', 4);

-- =============================================================================
-- PRODUCT RECIPES
-- =============================================================================
INSERT IGNORE INTO product_recipes (product_id, supply_variant_id, required_quantity) VALUES
    (9, 1, 0.4), (9, 41, 0.02), (9, 39, 0.005),
    (10, 6, 0.35), (10, 41, 0.02), (10, 39, 0.005),
    (5, 26, 0.15), (5, 3, 0.2), (5, 11, 0.05), (5, 12, 0.01), (5, 41, 0.02), (5, 53, 0.01),
    (13, 3, 0.3), (13, 41, 0.02), (13, 39, 0.005), (13, 40, 0.002),
    (30, 28, 1), (30, 45, 0.05),
    (21, 11, 0.08), (21, 21, 0.05), (22, 1, 0.01);

-- =============================================================================
-- OPTION RECIPES
-- =============================================================================
INSERT IGNORE INTO option_recipes (option_id, supply_variant_id, required_quantity) VALUES
    (13, 43, 0.05);

-- =============================================================================
-- SUPPLIERS
-- =============================================================================
INSERT IGNORE INTO suppliers (name, contact, active) VALUES
    ('Distribuidora Carnes del Valle', 'Carlos Martinez - 3001234567', TRUE),
    ('Frutas y Verduras Frescas', 'Maria Lopez - 3002345678', TRUE),
    ('Lácteos La Montana', 'Juan Perez - 3003456789', TRUE),
    ('Insumos de Mariscos del Litoral', 'Ana Gomez - 3004567890', TRUE),
    ('Bebidas y Licores XYZ', 'Roberto Diaz - 3005678901', TRUE),
    ('Embutidos Don Jorge', 'Jorge Rodriguez - 3006789012', TRUE);

-- =============================================================================
-- PURCHASE ORDERS
-- =============================================================================
INSERT IGNORE INTO purchase_orders (supplier_id, registered_by, purchased_at, total_amount, notes, created_at) VALUES
    (1, 3, '2026-04-10 10:30:00', 450000.00, 'Pedido semanal de carnes', '2026-04-10 10:30:00'),
    (2, 3, '2026-04-11 09:00:00', 180000.00, 'Verduras frescas', '2026-04-11 09:00:00'),
    (3, 3, '2026-04-11 14:00:00', 95000.00, 'Lacteos para la semana', '2026-04-11 14:00:00'),
    (1, 3, '2026-04-12 08:00:00', 320000.00, 'Reposición de inventario bajo', '2026-04-12 08:00:00');

-- =============================================================================
-- PURCHASE ORDER ITEMS
-- =============================================================================
INSERT IGNORE INTO purchase_order_items (purchase_order_id, supply_variant_id, quantity_ordered, quantity_received, unit_price) VALUES
    (1, 1, 20, 20, 45000), (1, 3, 15, 15, 35000), (1, 6, 10, 10, 48000),
    (2, 10, 30, 30, 3000), (2, 11, 25, 25, 2500), (2, 15, 20, 20, 5000), (2, 17, 15, 15, 4000),
    (3, 21, 30, 30, 15000), (3, 25, 36, 36, 12000), (3, 28, 10, 10, 8000),
    (4, 2, 10, 10, 85000), (4, 7, 8, 8, 62000);

-- =============================================================================
-- SAMPLE ORDERS
-- =============================================================================
INSERT IGNORE INTO orders (date, status, table_id) VALUES
    ('2026-04-12 12:30:00', 'PREPARING', 1),
    ('2026-04-12 12:45:00', 'QUEUE', 3),
    ('2026-04-12 13:00:00', 'READY', 5),
    ('2026-04-12 13:15:00', 'DELIVERED', 7),
    ('2026-04-12 13:30:00', 'QUEUE', 2);

-- =============================================================================
-- ORDER DETAILS
-- =============================================================================
INSERT IGNORE INTO order_details (order_id, product_id, unit_price, instructions) VALUES
    (1, 9, 58000.00, 'La carne tres cuartos, bien caliente'),
    (1, 24, 4500.00, NULL),
    (2, 5, 22000.00, 'Sin cilantro, por favor'),
    (2, 21, 18000.00, NULL),
    (2, 34, 4000.00, NULL),
    (3, 13, 28000.00, 'El pollo sin piel'),
    (3, 27, 24000.00, NULL),
    (3, 39, 16000.00, NULL),
    (4, 19, 42000.00, NULL),
    (4, 33, 12000.00, NULL),
    (5, 10, 52000.00, 'NY Strip termino medio'),
    (5, 30, 18000.00, 'Mojito suave');

-- =============================================================================
-- ORDER PREPARATION AREAS
-- =============================================================================
INSERT IGNORE INTO order_preparation_areas (order_id, area_id) VALUES
    (1, 1), (2, 1), (3, 1), (4, 1), (5, 1);

-- =============================================================================
-- DAY MENU (today's menu)
-- =============================================================================
INSERT IGNORE INTO day_menu (product_id, valid_from, created_by) VALUES
    (5, '2026-04-12 00:00:00', 'admin@dev.local'),
    (13, '2026-04-12 00:00:00', 'admin@dev.local'),
    (24, '2026-04-12 00:00:00', 'admin@dev.local');

-- =============================================================================
-- DAY MENU HISTORY
-- =============================================================================
INSERT IGNORE INTO day_menu_history (product_id, valid_from, valid_until, created_by) VALUES
    (5, '2026-04-11 00:00:00', '2026-04-12 00:00:00', 'admin@dev.local'),
    (15, '2026-04-11 00:00:00', '2026-04-12 00:00:00', 'admin@dev.local'),
    (23, '2026-04-11 00:00:00', '2026-04-12 00:00:00', 'admin@dev.local'),
    (5, '2026-04-10 00:00:00', '2026-04-11 00:00:00', 'admin@dev.local'),
    (9, '2026-04-10 00:00:00', '2026-04-11 00:00:00', 'admin@dev.local'),
    (30, '2026-04-10 00:00:00', '2026-04-11 00:00:00', 'admin@dev.local');
