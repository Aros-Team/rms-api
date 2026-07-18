-- ShedLock table for distributed scheduling locks (used by RollupDailyJob and future cron jobs)
CREATE TABLE IF NOT EXISTS shedlock (
    name       VARCHAR(64)  NOT NULL,
    lock_until TIMESTAMP    NOT NULL,
    locked_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    locked_by  VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);

-- DOWN (commented, not auto-run; for ops reference)
-- DROP TABLE IF EXISTS shedlock;
