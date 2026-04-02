-- V6__seed_worker_user.sql
-- Test worker user for development (password: worker123)

INSERT INTO users (document, name, email, password, role)
VALUES ('00000001', 'Worker Test', 'jeronimoguerreromaya@gmail.com', '$2a$10$E2.x8iVj82PqLJGYBW.pt.RZcDLK8kb7ebH.k9ycvUfeecfe1gA1a', 'WORKER');
