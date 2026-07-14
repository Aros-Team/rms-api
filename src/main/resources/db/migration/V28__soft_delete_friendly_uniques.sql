/* V28: Replace email/document UNIQUE with soft-delete-friendly composite indexes */
/* Allows reusing email/document from soft-deleted users while keeping active users unique */

ALTER TABLE users
  ADD COLUMN deleted_at_sentinel DATETIME
  GENERATED ALWAYS AS (COALESCE(deleted_at, '1000-01-01 00:00:00')) STORED;

DROP INDEX email ON users;
DROP INDEX document ON users;

CREATE UNIQUE INDEX idx_users_email ON users(email, deleted_at_sentinel);
CREATE UNIQUE INDEX idx_users_document ON users(document, deleted_at_sentinel);
