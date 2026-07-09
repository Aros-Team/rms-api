-- V19__repair_inventory_storage_locations.sql
-- Repara la ubicación de almacenamiento de insumos refrigerados.
-- V8 sembraba algunos refrigerados (carnes, huevos, lácteos, limón) en Bodega
-- por error; el lugar correcto es Cuarto Frío (storage_location_id=3).
-- Idempotente: solo afecta registros que aún están en Bodega.

UPDATE inventory_stock
SET storage_location_id = 3
WHERE storage_location_id = 1
  AND supply_variant_id IN (
    1,  -- Carne de Res Molida 150g
    2,  -- Carne de Res Molida 100g
    3,  -- Pollo Apanado 180g
    4,  -- Lenteja Preparada 150g
    5,  -- Pechuga de Pollo 200g
    6,  -- Pechuga de Pollo 100g
    7,  -- Carne de Cerdo 200g
    12, -- Huevo 1u
    13, -- Huevo 2u
    31, -- Queso Parmesano 15g
    51  -- Limón Tahití 2u
  );