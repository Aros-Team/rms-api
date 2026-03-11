-- V2__seed_data.sql
-- Seed data for testing

-- Insert areas
INSERT INTO areas (name, type, enabled) VALUES 
    ('Cocina Principal', 'KITCHEN', TRUE),
    ('Bar', 'BARTENDER', TRUE);

-- Insert test user (password: 123)
INSERT INTO users (document, name, email, password, address, phone, role)
VALUES (
    '1234567890',
    'Steven',
    'edwar5020@gmail.com',
    '$2a$10$UA3p51w/MeNyQBem5kXgFOWsEV14dS/oLZLx5d4RndiloCS.a/ory',
    'Test Address',
    '+1234567890',
    'ADMIN'
);

-- Assign areas to user
INSERT INTO user_assigned_areas (user_id, area_id) 
SELECT u.id, a.id FROM users u, areas a WHERE u.email = 'edwar5020@gmail.com';
