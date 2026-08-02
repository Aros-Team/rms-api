ALTER TABLE areas
    ADD COLUMN default_expected_hours INT NOT NULL DEFAULT 160;

-- Set typical values for existing areas
UPDATE areas SET default_expected_hours = 160 WHERE name LIKE '%Cocina%' OR name LIKE '%Parrilla%' OR name LIKE '%Kitchen%';
UPDATE areas SET default_expected_hours = 120 WHERE name LIKE '%Servicio%' OR name LIKE '%Caja%' OR name LIKE '%Bar%';

-- Sync existing workers' expected_hours_per_month with their area's default
UPDATE users u
JOIN user_assigned_areas uaa ON uaa.user_id = u.id
JOIN areas a ON a.id = uaa.area_id
SET u.expected_hours_per_month = a.default_expected_hours
WHERE u.role = 'WORKER'
  AND u.deleted_at IS NULL
  AND u.expected_hours_per_month = 160
  AND a.default_expected_hours != 160;
