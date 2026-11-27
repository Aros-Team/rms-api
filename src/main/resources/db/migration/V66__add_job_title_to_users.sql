-- =============================================================================
-- V66: users.job_title
-- =============================================================================
--
-- PURPOSE:
--   A worker's area (kitchen, cashier, service) says where they work, not what
--   they do. The "rol" gives the concrete position inside that area, for
--   example "jefe de cocina", "cocinero" or "mesero".
--
--   This is a free-text labour attribute, independent from users.role, which
--   keeps the access-control value (ADMIN / WORKER).
--
-- MIGRATION SAFETY (forward-only additive):
--   * New nullable column; existing rows keep working with no job title.
--   * No DROP / RENAME / MODIFY of existing columns.
-- =============================================================================

ALTER TABLE users
    ADD COLUMN job_title VARCHAR(100) NULL AFTER phone;
