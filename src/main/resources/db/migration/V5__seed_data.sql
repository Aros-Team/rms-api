-- V5__seed_data.sql
-- Seed data for testing all RMS functionalities

-- =============================================================================
-- ÁREAS DE PREPARACIÓN
-- =============================================================================
INSERT INTO areas (id, name, type, enabled) VALUES
(1, 'Bar', 'BAR', TRUE),
(2, 'Cocina Principal', 'KITCHEN', TRUE),
(3, 'Parrilla', 'GRILL', TRUE),
(4, 'Bebidas', 'BAR', TRUE);

-- =============================================================================
-- CATEGORÍAS DE PRODUCTOS
-- =============================================================================
INSERT INTO categories (id, name, description, enabled) VALUES
(1, 'Bebidas', 'Bebidas frías y calientes', TRUE),
(2, 'Entradas', 'Aperitivos y entradas', TRUE),
(3, 'Platos Fuertes', 'Platos principales', TRUE),
(4, 'Postres', 'Dulces y postres', TRUE),
(5, 'Acompañamientos', 'Guarniciones', TRUE);

-- =============================================================================
-- OPTION CATEGORIES (categorías de opciones para productos)
-- =============================================================================
INSERT INTO option_categories (id, name, description) VALUES
(1, 'Tamaño', 'Tamaño de la bebida o porción'),
(2, 'Tipo de Carne', 'Tipo de carne para platos'),
(3, 'Extras', 'Ingredientes adicionales'),
(4, 'Nivel de Cocción', 'Cómo quieres tu carne'),
(5, 'Tipo de Pan', 'Tipo de pan para hamburgesas');

-- =============================================================================
-- MESAS
-- =============================================================================
INSERT INTO tables (id, table_number, capacity, status) VALUES
(1, 1, 2, 'AVAILABLE'),
(2, 2, 2, 'AVAILABLE'),
(3, 3, 4, 'AVAILABLE'),
(4, 4, 4, 'AVAILABLE'),
(5, 5, 6, 'AVAILABLE'),
(6, 6, 6, 'AVAILABLE'),
(7, 7, 8, 'AVAILABLE'),
(8, 8, 8, 'AVAILABLE'),
(9, 9, 10, 'AVAILABLE'),
(10, 10, 12, 'AVAILABLE');

-- =============================================================================
-- PRODUCTOS
-- =============================================================================
INSERT INTO products (id, name, base_price, has_options, active, category_id, area_id) VALUES
-- =============================================================================
-- BEBIDAS SIN ALCOHOL (Bar - area_id = 1)
-- =============================================================================
(1, 'Coca Cola', 25.00, FALSE, TRUE, 1, 1),
(2, 'Sprite', 25.00, FALSE, TRUE, 1, 1),
(3, 'Fanta', 25.00, FALSE, TRUE, 1, 1),
(4, 'Agua Mineral', 20.00, FALSE, TRUE, 1, 1),
(5, 'Agua de Horchata', 30.00, FALSE, TRUE, 1, 1),
(6, 'Agua de Tamarindo', 30.00, FALSE, TRUE, 1, 1),
(7, 'Jugo de Naranja', 35.00, FALSE, TRUE, 1, 1),
(8, 'Jugo de Manzana', 35.00, FALSE, TRUE, 1, 1),
(9, 'Jugo de Uva', 35.00, FALSE, TRUE, 1, 1),
(10, 'Limonada Natural', 30.00, FALSE, TRUE, 1, 1),
(11, 'Limonada con Chile', 35.00, FALSE, TRUE, 1, 1),
(12, 'Batido de Fresa', 45.00, FALSE, TRUE, 1, 1),
(13, 'Batido de Chocolate', 45.00, FALSE, TRUE, 1, 1),
(14, 'Batido de Vainilla', 45.00, FALSE, TRUE, 1, 1),

-- =============================================================================
-- BEBIDAS CALIENTES (area_id = 1)
-- =============================================================================
(15, 'Café Americano', 30.00, FALSE, TRUE, 1, 1),
(16, 'Café Espresso', 25.00, FALSE, TRUE, 1, 1),
(17, 'Café Latte', 40.00, FALSE, TRUE, 1, 1),
(18, 'Cappuccino', 40.00, FALSE, TRUE, 1, 1),
(19, 'Moka', 45.00, FALSE, TRUE, 1, 1),
(20, 'Té Negro', 25.00, FALSE, TRUE, 1, 1),
(21, 'Té de Hierba Luisa', 25.00, FALSE, TRUE, 1, 1),

-- =============================================================================
-- BEBIDAS ALCOHÓLICAS (area_id = 1)
-- =============================================================================
(22, 'Cerveza Nacional', 35.00, FALSE, TRUE, 1, 1),
(23, 'Cervezalight', 35.00, FALSE, TRUE, 1, 1),
(24, 'Cerveza Importada', 55.00, FALSE, TRUE, 1, 1),
(25, 'Cerveza artesanal', 65.00, FALSE, TRUE, 1, 1),
(26, 'Tequila Blanco', 45.00, FALSE, TRUE, 1, 1),
(27, 'Tequila Reposado', 55.00, FALSE, TRUE, 1, 1),
(28, 'Mezcal', 60.00, FALSE, TRUE, 1, 1),
(29, 'Whiskey', 65.00, FALSE, TRUE, 1, 1),
(30, 'Vodka', 55.00, FALSE, TRUE, 1, 1),
(31, 'Ron', 50.00, FALSE, TRUE, 1, 1),
(32, 'Vino Tinto Copa', 65.00, FALSE, TRUE, 1, 1),
(33, 'Vino Blanco Copa', 65.00, FALSE, TRUE, 1, 1),
(34, 'Champagne', 85.00, FALSE, TRUE, 1, 1),

-- =============================================================================
-- COCTELES (area_id = 1)
-- =============================================================================
(35, 'Margarita', 85.00, TRUE, TRUE, 1, 1),
(36, 'Margarita de Mango', 95.00, TRUE, TRUE, 1, 1),
(37, 'Margarita de Fresa', 95.00, TRUE, TRUE, 1, 1),
(38, 'Margarita de Tamarindo', 95.00, TRUE, TRUE, 1, 1),
(39, 'Piña Colada', 85.00, FALSE, TRUE, 1, 1),
(40, 'Daiquiri de Fresa', 85.00, FALSE, TRUE, 1, 1),
(41, 'Mojito', 75.00, FALSE, TRUE, 1, 1),
(42, 'Cuba Libre', 70.00, FALSE, TRUE, 1, 1),
(43, 'Michelada', 65.00, FALSE, TRUE, 1, 1),
(44, 'Paloma', 70.00, FALSE, TRUE, 1, 1),
(45, 'Mexican Mule', 75.00, FALSE, TRUE, 1, 1),
(46, 'Old Fashioned', 85.00, FALSE, TRUE, 1, 1),
(47, 'Martini', 80.00, FALSE, TRUE, 1, 1),
(48, 'Negroni', 85.00, FALSE, TRUE, 1, 1),

-- =============================================================================
-- ENTRADAS Y APERITIVOS (Cocina Principal - area_id = 2)
-- =============================================================================
(49, 'Nachos con Queso', 75.00, FALSE, TRUE, 2, 2),
(50, 'Nachos con Carne', 95.00, FALSE, TRUE, 2, 2),
(51, 'Alitas BBQ (10 pzs)', 95.00, FALSE, TRUE, 2, 2),
(52, 'Alitas Buffalo (10 pzs)', 95.00, FALSE, TRUE, 2, 2),
(53, 'Alitas Habanero (10 pzs)', 100.00, FALSE, TRUE, 2, 2),
(54, 'Boneless (8 pzs)', 110.00, FALSE, TRUE, 2, 2),
(55, 'Quesadillas de Queso', 65.00, FALSE, TRUE, 2, 2),
(56, 'Quesadillas Mixtas', 85.00, FALSE, TRUE, 2, 2),
(57, 'Quesadillas de Pollo', 85.00, FALSE, TRUE, 2, 2),
(58, 'Sopa del Día', 45.00, FALSE, TRUE, 2, 2),
(59, 'Sopa de Mariscos', 95.00, FALSE, TRUE, 2, 2),
(60, 'Ensalada César', 65.00, FALSE, TRUE, 2, 2),
(61, 'Ensalada de Pollo', 85.00, FALSE, TRUE, 2, 2),
(62, 'Ensalada Mixta', 55.00, FALSE, TRUE, 2, 2),
(63, 'Guacamole con Totopos', 75.00, FALSE, TRUE, 2, 2),
(64, 'Dip de Queso', 55.00, FALSE, TRUE, 2, 2),
(65, 'Pan de Ajo', 35.00, FALSE, TRUE, 2, 2),
(66, 'Pan Quesadilla', 40.00, FALSE, TRUE, 2, 2),
(67, 'Costillas BBQ (porción)', 120.00, FALSE, TRUE, 2, 2),
(68, 'Calamares Fritos', 110.00, FALSE, TRUE, 2, 2),
(69, 'Papas a la Francesas', 50.00, FALSE, TRUE, 2, 2),

-- =============================================================================
-- PLATOS FUERTES - HAMBURGUESAS (area_id = 2)
-- =============================================================================
(70, 'Hamburguesa Clásica', 120.00, TRUE, TRUE, 3, 2),
(71, 'Hamburguesa con Queso', 135.00, TRUE, TRUE, 3, 2),
(72, 'Hamburguesa Doble', 175.00, TRUE, TRUE, 3, 2),
(73, 'Hamburguesa BBQ', 145.00, TRUE, TRUE, 3, 2),
(74, 'Hamburguesa Especial', 165.00, TRUE, TRUE, 3, 2),
(75, 'Hamburguesa de Pollo', 110.00, TRUE, TRUE, 3, 2),
(76, 'Veggie Burger', 120.00, FALSE, TRUE, 3, 2),

-- =============================================================================
-- PLATOS FUERTES - ANTOJITOS MEXICANOS (area_id = 2 o 4)
-- =============================================================================
(77, 'Tacos de Carne Asada (3 pzs)', 95.00, FALSE, TRUE, 3, 4),
(78, 'Tacos de Pollo (3 pzs)', 85.00, FALSE, TRUE, 3, 2),
(79, 'Tacos de Carnitas (3 pzs)', 95.00, FALSE, TRUE, 3, 4),
(80, 'Tacos de Bistel (3 pzs)', 90.00, FALSE, TRUE, 3, 4),
(81, 'Tacos de Pastor (3 pzs)', 85.00, FALSE, TRUE, 3, 4),
(82, 'Tortas de Jamón', 75.00, FALSE, TRUE, 3, 2),
(83, 'Tortas de Milanesa', 85.00, FALSE, TRUE, 3, 2),
(84, 'Tortas Ahogadas', 90.00, FALSE, TRUE, 3, 2),
(85, 'Quesadillas de Harina', 55.00, FALSE, TRUE, 3, 2),
(86, 'Sopes (3 pzs)', 70.00, FALSE, TRUE, 3, 2),
(87, 'Huaraches', 75.00, FALSE, TRUE, 3, 2),
(88, 'Enchiladas Rojas', 110.00, FALSE, TRUE, 3, 2),
(89, 'Enchiladas Verdes', 110.00, FALSE, TRUE, 3, 2),
(90, 'Enchiladas de Mole', 125.00, FALSE, TRUE, 3, 2),
(91, 'Chiles Rellenos', 125.00, FALSE, TRUE, 3, 2),
(92, 'Mole Poblano', 145.00, FALSE, TRUE, 3, 2),
(93, 'Milanesa de Res', 145.00, FALSE, TRUE, 3, 2),
(94, 'Milanesa de Pollo', 125.00, FALSE, TRUE, 3, 2),
(95, 'Caldo de Res', 95.00, FALSE, TRUE, 3, 2),
(96, 'Caldo de Pollo', 85.00, FALSE, TRUE, 3, 2),
(97, 'Menudo', 110.00, FALSE, TRUE, 3, 2),
(98, 'Birria de Res', 135.00, FALSE, TRUE, 3, 4),
(99, 'Birria de Chivo', 155.00, FALSE, TRUE, 3, 4),

-- =============================================================================
-- PLATOS FUERTES - CARNES Y MARISCOS (area_id = 2 o 4)
-- =============================================================================
(100, 'Filete de Res', 250.00, TRUE, TRUE, 3, 4),
(101, 'Rib Eye', 350.00, TRUE, TRUE, 3, 4),
(102, 'New York Steak', 320.00, TRUE, TRUE, 3, 4),
(103, 'T-Bone', 380.00, TRUE, TRUE, 3, 4),
(104, 'Pechuga de Pollo', 150.00, TRUE, TRUE, 3, 2),
(105, 'Muslo de Pollo', 120.00, TRUE, TRUE, 3, 2),
(106, 'Pollo Rostizado', 140.00, FALSE, TRUE, 3, 2),
(107, 'Pollo en Crema', 145.00, FALSE, TRUE, 3, 2),
(108, 'Albondigas en Chipotle', 125.00, FALSE, TRUE, 3, 2),
(109, 'Costillas de Cerdo', 180.00, FALSE, TRUE, 3, 4),
(110, 'Chuleta de Cerdo', 150.00, TRUE, TRUE, 3, 4),
(111, 'Lomo de Cerdo', 165.00, FALSE, TRUE, 3, 4),
(112, 'Salmón', 280.00, FALSE, TRUE, 3, 2),
(113, 'Camarones al Ajillo', 220.00, FALSE, TRUE, 3, 2),
(114, 'Camarones a la Diabla', 235.00, FALSE, TRUE, 3, 2),
(115, 'Camarones Emperador', 280.00, FALSE, TRUE, 3, 2),
(116, 'Pescado Zarandeado', 250.00, FALSE, TRUE, 3, 4),
(117, 'Huachinango al Mojo de Ajo', 260.00, FALSE, TRUE, 3, 2),
(118, 'Ceviche de Camarón', 145.00, FALSE, TRUE, 3, 2),
(119, 'Ceviche Mixto', 165.00, FALSE, TRUE, 3, 2),
(120, 'Coctél de Camarón', 145.00, FALSE, TRUE, 3, 2),
(121, 'Parrillada para 2', 450.00, FALSE, TRUE, 3, 4),
(122, 'Parrillada para 4', 850.00, FALSE, TRUE, 3, 4),
(123, 'Mariscos a la Mexicana', 195.00, FALSE, TRUE, 3, 2),
(124, 'Arroz con Mariscos', 175.00, FALSE, TRUE, 3, 2),

-- =============================================================================
-- POSTRES (area_id = 2)
-- =============================================================================
(125, 'Flan Napolitano', 45.00, FALSE, TRUE, 4, 2),
(126, 'Flan de Chocolate', 50.00, FALSE, TRUE, 4, 2),
(127, 'Brownie con Helado', 65.00, FALSE, TRUE, 4, 2),
(128, 'Cheesecake', 60.00, FALSE, TRUE, 4, 2),
(129, 'Cheesecake de Fresa', 70.00, FALSE, TRUE, 4, 2),
(130, 'Helado (3 bolas)', 50.00, FALSE, TRUE, 4, 2),
(131, 'Pay de Queso', 55.00, FALSE, TRUE, 4, 2),
(132, 'Pay de Manzana', 55.00, FALSE, TRUE, 4, 2),
(133, 'Pay de Limón', 50.00, FALSE, TRUE, 4, 2),
(134, 'Pastel de Chocolate', 60.00, FALSE, TRUE, 4, 2),
(135, 'Volcán de Chocolate', 75.00, FALSE, TRUE, 4, 2),
(136, 'Churros', 55.00, FALSE, TRUE, 4, 2),
(137, 'Crepas con Chocolate', 65.00, FALSE, TRUE, 4, 2),
(138, 'Gelatina', 30.00, FALSE, TRUE, 4, 2),
(139, 'Arroz con Leche', 40.00, FALSE, TRUE, 4, 2),
(140, 'Dulce de Leche', 35.00, FALSE, TRUE, 4, 2),

-- =============================================================================
-- ACOMPAÑAMIENTOS (area_id = 2)
-- =============================================================================
(141, 'Papas a la Francesas', 40.00, FALSE, TRUE, 5, 2),
(142, 'Papas Gajo', 45.00, FALSE, TRUE, 5, 2),
(143, 'Aros de Cebolla', 45.00, FALSE, TRUE, 5, 2),
(144, 'Elote Mexicano', 30.00, FALSE, TRUE, 5, 2),
(145, 'Esquites', 30.00, FALSE, TRUE, 5, 2),
(146, 'Arroz Blanco', 25.00, FALSE, TRUE, 5, 2),
(147, 'Arroz Rojo', 30.00, FALSE, TRUE, 5, 2),
(148, 'Frijoles Charros', 35.00, FALSE, TRUE, 5, 2),
(149, 'Frijoles Refritos', 30.00, FALSE, TRUE, 5, 2),
(150, 'Verduras al Vapor', 40.00, FALSE, TRUE, 5, 2),
(151, 'Ensalada de Ejotes', 35.00, FALSE, TRUE, 5, 2),
(152, 'Calabaza con Elote', 40.00, FALSE, TRUE, 5, 2),
(153, 'Aguacate', 35.00, FALSE, TRUE, 5, 2),
(154, 'Guacamole', 55.00, FALSE, TRUE, 5, 2),
(155, 'Salsa Verde', 0.00, FALSE, TRUE, 5, 2),
(156, 'Salsa Roja', 0.00, FALSE, TRUE, 5, 2),
(157, 'Crema', 0.00, FALSE, TRUE, 5, 2),
(158, 'Queso Fresco', 25.00, FALSE, TRUE, 5, 2),
(159, 'Queso Manchego', 35.00, FALSE, TRUE, 5, 2),
(160, 'Tortillas de Maíz', 0.00, FALSE, TRUE, 5, 2),
(161, 'Tortillas de Harina', 0.00, FALSE, TRUE, 5, 2);

-- =============================================================================
-- OPCIONES DE PRODUCTOS (para productos con has_options = TRUE)
-- =============================================================================
-- Opciones para Hamburguesa Clásica (product_id = 70)
INSERT INTO product_options (id, name, option_category_id, product_id) VALUES
(1, 'Chica', 1, 70),
(2, 'Mediana', 1, 70),
(3, 'Grande', 1, 70),
(4, 'Con Queso', 3, 70),
(5, 'Con Tocino', 3, 70),
(6, 'Con Aguacate', 3, 70);

-- Opciones para Hamburguesa con Queso (product_id = 71)
INSERT INTO product_options (id, name, option_category_id, product_id) VALUES
(7, 'Chica', 1, 71),
(8, 'Mediana', 1, 71),
(9, 'Grande', 1, 71),
(10, 'Con Tocino', 3, 71),
(11, 'Con Aguacate', 3, 71);

-- Opciones para Hamburguesa Doble (product_id = 72)
INSERT INTO product_options (id, name, option_category_id, product_id) VALUES
(12, 'Mediana', 1, 72),
(13, 'Grande', 1, 72),
(14, 'Con Queso Extra', 3, 72),
(15, 'Con Tocino', 3, 72);

-- Opciones para Hamburguesa BBQ (product_id = 73)
INSERT INTO product_options (id, name, option_category_id, product_id) VALUES
(16, 'Chica', 1, 73),
(17, 'Mediana', 1, 73),
(18, 'Grande', 1, 73),
(19, 'Con Queso Extra', 3, 73),
(20, 'Cebolla Caramelizada', 3, 73);

-- Opciones para Hamburguesa Especial (product_id = 74)
INSERT INTO product_options (id, name, option_category_id, product_id) VALUES
(21, 'Chica', 1, 74),
(22, 'Mediana', 1, 74),
(23, 'Grande', 1, 74),
(24, 'Con Queso', 3, 74),
(25, 'Con Tocino', 3, 74),
(26, 'Con Aguacate', 3, 74),
(27, 'Con Huevo', 3, 74);

-- Opciones para Hamburguesa de Pollo (product_id = 75)
INSERT INTO product_options (id, name, option_category_id, product_id) VALUES
(28, 'Con Queso', 3, 75),
(29, 'Con Tocino', 3, 75),
(30, 'Con Aguacate', 3, 75);

-- Opciones para Filete de Res (product_id = 100)
INSERT INTO product_options (id, name, option_category_id, product_id) VALUES
(31, 'Rib Eye', 2, 100),
(32, 'New York', 2, 100),
(33, 'Sirloin', 2, 100),
(34, 'Rare', 4, 100),
(35, 'Medium Rare', 4, 100),
(36, 'Medium', 4, 100),
(37, 'Medium Well', 4, 100),
(38, 'Well Done', 4, 100);

-- Opciones para Rib Eye (product_id = 101)
INSERT INTO product_options (id, name, option_category_id, product_id) VALUES
(39, 'Rare', 4, 101),
(40, 'Medium Rare', 4, 101),
(41, 'Medium', 4, 101),
(42, 'Medium Well', 4, 101),
(43, 'Well Done', 4, 101);

-- Opciones para New York Steak (product_id = 102)
INSERT INTO product_options (id, name, option_category_id, product_id) VALUES
(44, 'Rare', 4, 102),
(45, 'Medium Rare', 4, 102),
(46, 'Medium', 4, 102),
(47, 'Medium Well', 4, 102),
(48, 'Well Done', 4, 102);

-- Opciones para T-Bone (product_id = 103)
INSERT INTO product_options (id, name, option_category_id, product_id) VALUES
(49, 'Rare', 4, 103),
(50, 'Medium Rare', 4, 103),
(51, 'Medium', 4, 103),
(52, 'Medium Well', 4, 103),
(53, 'Well Done', 4, 103);

-- Opciones para Pechuga de Pollo (product_id = 104)
INSERT INTO product_options (id, name, option_category_id, product_id) VALUES
(54, 'A la Plancha', 3, 104),
(55, 'Empanizada', 3, 104),
(56, 'Enchipotlada', 3, 104),
(57, 'Gratinada', 3, 104),
(58, 'Al Horno', 3, 104);

-- Opciones para Muslo de Pollo (product_id = 105)
INSERT INTO product_options (id, name, option_category_id, product_id) VALUES
(59, 'A la Plancha', 3, 105),
(60, 'Empanizada', 3, 105),
(61, 'Frita', 3, 105);

-- Opciones para Chuleta de Cerdo (product_id = 110)
INSERT INTO product_options (id, name, option_category_id, product_id) VALUES
(62, 'A la Plancha', 3, 110),
(63, 'Empanizada', 3, 110),
(64, 'Frita', 3, 110);

-- Opciones para Margarita (product_id = 35)
INSERT INTO product_options (id, name, option_category_id, product_id) VALUES
(65, 'Mango', 1, 35),
(66, 'Fresa', 1, 35),
(67, 'Limón', 1, 35),
(68, 'Maracuyá', 1, 35),
(69, 'Sin Alcohol', 1, 35);

-- Opciones para Margarita de Mango (product_id = 36)
INSERT INTO product_options (id, name, option_category_id, product_id) VALUES
(70, 'Natural', 1, 36),
(71, 'Con Chile', 1, 36);

-- Opciones para Margarita de Fresa (product_id = 37)
INSERT INTO product_options (id, name, option_category_id, product_id) VALUES
(72, 'Natural', 1, 37),
(73, 'Con Chile', 1, 37);

-- Opciones para Margarita de Tamarindo (product_id = 38)
INSERT INTO product_options (id, name, option_category_id, product_id) VALUES
(74, 'Natural', 1, 38),
(75, 'Con Chile', 1, 38);
