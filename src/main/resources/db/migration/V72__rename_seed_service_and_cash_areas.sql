-- =============================================================================
-- V72: rename the seeded service and cash areas
-- =============================================================================
--
-- Areas are physical places identified by their name. The operations team uses
-- "Salón Principal" for the service floor and "Entrada" for the cash/check-in
-- area. The IDs do not change, so existing order/product assignments and
-- worker assignments stay valid.
-- =============================================================================

UPDATE areas
SET name = 'Salón Principal'
WHERE id = 2 OR name = 'Servicio';

UPDATE areas
SET name = 'Entrada'
WHERE id = 4 OR name = 'Caja';
