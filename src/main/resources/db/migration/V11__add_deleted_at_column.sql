/* V11: Add deleted_at column for soft-delete support */
ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP NULL DEFAULT NULL AFTER status;
