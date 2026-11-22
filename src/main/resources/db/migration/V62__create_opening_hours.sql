-- Service-hours lane. Owns V62.
-- Per-weekday opening hours. The dashboard limits the hourly chart to the current day's
-- window and the schedule coverage matrix raises staffing gaps inside it. Holidays use the
-- Sunday window until a holiday calendar exists.
CREATE TABLE opening_hours (
    day_of_week VARCHAR(10) NOT NULL PRIMARY KEY,
    open_time   TIME,
    close_time  TIME,
    is_closed   BOOLEAN     NOT NULL DEFAULT FALSE
);

INSERT INTO opening_hours (day_of_week, open_time, close_time, is_closed) VALUES
    ('MONDAY',    NULL,       NULL,       TRUE),
    ('TUESDAY',   '08:00:00', '22:00:00', FALSE),
    ('WEDNESDAY', '08:00:00', '22:00:00', FALSE),
    ('THURSDAY',  '08:00:00', '22:00:00', FALSE),
    ('FRIDAY',    '08:00:00', '22:00:00', FALSE),
    ('SATURDAY',  '08:00:00', '17:00:00', FALSE),
    ('SUNDAY',    '08:00:00', '17:00:00', FALSE);
