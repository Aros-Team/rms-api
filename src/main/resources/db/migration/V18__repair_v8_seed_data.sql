-- V18__repair_v8_seed_data.sql
-- Repara datos incorrectos sembrados por V8 en bases existentes.
-- Idempotente: seguro de correr sobre bases ya corregidas.

-- =============================================================================
-- 1. Corregir área del producto Limonada / Jugo Natural
-- V8 asignaba area_id=2 (Servicio) por error; el área correcta es Bar (id=3).
-- Cocina=1, Servicio=2, Bar=3, Caja=4 (Caja lo agrega V14).
-- =============================================================================
UPDATE products
SET area_id = 3
WHERE id = 6
  AND area_id <> 3;

-- =============================================================================
-- 2. Re-sembrar product_product_options faltantes para Hamburguesa (product_id=1)
-- V8 pudo haber fallado parcialmente en algunas ejecuciones y omitido las
-- 13 asociaciones del producto Hamburguesa. Inserta solo las faltantes para
-- no violar el PRIMARY KEY (product_id, option_id).
-- =============================================================================
INSERT IGNORE INTO product_product_options (product_id, option_id)
SELECT 1, id
FROM product_options
WHERE id BETWEEN 1 AND 13
  AND NOT EXISTS (
    SELECT 1 FROM product_product_options ppo
    WHERE ppo.product_id = 1 AND ppo.option_id = product_options.id
  );