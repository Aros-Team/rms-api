-- =============================================================================
-- V67: areas.exclusive
-- =============================================================================
--
-- PURPOSE:
--   An exclusive area cannot share a worker with any other area: the worker
--   belongs to that area alone (for example Cocina). The flag is configurable
--   from the area management screen.
--
-- MIGRATION SAFETY (forward-only additive):
--   * New NOT NULL column with a default; existing rows keep working.
--   * Kitchen starts exclusive to match the current roster.
-- =============================================================================

ALTER TABLE areas
    ADD COLUMN exclusive BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE areas
SET exclusive = TRUE
WHERE type = 'KITCHEN';
