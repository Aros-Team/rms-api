-- =============================================================================
-- V71: area activities and per-worker activity assignments
-- =============================================================================
--
-- An area is a physical place (Cocina, Bar, Servicio...). What can be done
-- there is an activity: PREPARATION, SERVE or CASH. An area can have several
-- activities (a Bar can prepare drinks and serve customers).
--
-- Worker assignments are per area + activity, so the same worker can be
-- assigned to Bar/PREPARATION and Bar/SERVE, or to Bar/PREPARATION and
-- Servicio/SERVE.
--
-- MANAGE is intentionally absent: the ADMIN role defines administration.
-- =============================================================================

CREATE TABLE area_activities (
    area_id  BIGINT      NOT NULL,
    activity VARCHAR(20) NOT NULL,
    PRIMARY KEY (area_id, activity),
    CONSTRAINT fk_area_activities_area
        FOREIGN KEY (area_id) REFERENCES areas (id) ON DELETE CASCADE
);

INSERT IGNORE INTO area_activities (area_id, activity)
SELECT id, 'PREPARATION' FROM areas WHERE type IN ('KITCHEN', 'GRILL', 'BAR');

INSERT IGNORE INTO area_activities (area_id, activity)
SELECT id, 'SERVE' FROM areas WHERE type IN ('SERVICE', 'BAR');

INSERT IGNORE INTO area_activities (area_id, activity)
SELECT id, 'CASH' FROM areas WHERE type = 'CASH';

CREATE TABLE worker_area_activities (
    user_id  BIGINT      NOT NULL,
    area_id  BIGINT      NOT NULL,
    activity VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, area_id, activity),
    CONSTRAINT fk_worker_area_activities_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_worker_area_activities_area
        FOREIGN KEY (area_id) REFERENCES areas (id) ON DELETE CASCADE
);

-- Preserve existing access:
--   KITCHEN/GRILL -> PREPARATION
--   SERVICE       -> SERVE
--   BAR           -> SERVE (preparation is granted explicitly later)
--   CASH          -> CASH
INSERT IGNORE INTO worker_area_activities (user_id, area_id, activity)
SELECT uaa.user_id, uaa.area_id, 'PREPARATION'
FROM user_assigned_areas uaa
JOIN areas a ON a.id = uaa.area_id
WHERE a.type IN ('KITCHEN', 'GRILL');

INSERT IGNORE INTO worker_area_activities (user_id, area_id, activity)
SELECT uaa.user_id, uaa.area_id, 'SERVE'
FROM user_assigned_areas uaa
JOIN areas a ON a.id = uaa.area_id
WHERE a.type IN ('SERVICE', 'BAR');

INSERT IGNORE INTO worker_area_activities (user_id, area_id, activity)
SELECT uaa.user_id, uaa.area_id, 'CASH'
FROM user_assigned_areas uaa
JOIN areas a ON a.id = uaa.area_id
WHERE a.type = 'CASH';

ALTER TABLE areas DROP COLUMN type;
