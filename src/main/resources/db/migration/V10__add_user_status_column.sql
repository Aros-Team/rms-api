/* V10: Add status column to users table */
ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING' AFTER role;

UPDATE users SET status = 'ACTIVE' WHERE role = 'ADMIN';
UPDATE users SET status = 'PENDING' WHERE role = 'WORKER';
