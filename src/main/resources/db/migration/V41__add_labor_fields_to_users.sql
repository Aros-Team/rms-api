ALTER TABLE users
    ADD COLUMN expected_hours_per_month INT NOT NULL DEFAULT 160;

ALTER TABLE users
    ADD COLUMN hourly_rate DECIMAL(10,2)
    GENERATED ALWAYS AS (
        CASE WHEN expected_hours_per_month > 0
             THEN ROUND(salary / expected_hours_per_month, 2)
             ELSE 0 END
    ) STORED;
