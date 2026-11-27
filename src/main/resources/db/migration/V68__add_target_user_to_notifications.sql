-- =============================================================================
-- V68: targeted notifications
-- =============================================================================
--
-- PURPOSE:
--   user_notifications was a single global admin feed. Worker-specific events
--   (for example a newly assigned area/module) need a recipient without
--   leaking into the admin feed.
--
--   user_id NULL  -> global notification (admin feed, existing behaviour)
--   user_id set   -> targeted notification visible only to that worker
--
-- MIGRATION SAFETY (forward-only additive):
--   * New nullable column; existing rows remain global.
--   * ON DELETE CASCADE removes a worker's notifications with the account.
-- =============================================================================

ALTER TABLE user_notifications
    ADD COLUMN user_id BIGINT NULL AFTER id;

ALTER TABLE user_notifications
    ADD CONSTRAINT fk_user_notifications_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

CREATE INDEX idx_user_notifications_user_read
    ON user_notifications (user_id, read_at);
