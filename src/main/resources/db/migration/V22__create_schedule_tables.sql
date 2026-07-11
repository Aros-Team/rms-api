CREATE TABLE schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE schedule_shifts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT NOT NULL,
    day_of_week VARCHAR(10) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    CONSTRAINT fk_shift_schedule FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE,
    CONSTRAINT chk_time_range CHECK (start_time < end_time)
);

CREATE TABLE worker_schedule_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    worker_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assignment_worker FOREIGN KEY (worker_id) REFERENCES users(id),
    CONSTRAINT fk_assignment_schedule FOREIGN KEY (schedule_id) REFERENCES schedules(id),
    UNIQUE KEY uk_worker_schedule (worker_id, schedule_id)
);

CREATE TABLE time_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    worker_id BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    type VARCHAR(10) NOT NULL DEFAULT 'IN',
    within_shift BOOLEAN NOT NULL DEFAULT FALSE,
    related_shift_id BIGINT,
    CONSTRAINT fk_timelog_worker FOREIGN KEY (worker_id) REFERENCES users(id),
    CONSTRAINT fk_timelog_shift FOREIGN KEY (related_shift_id) REFERENCES schedule_shifts(id)
);

CREATE INDEX idx_assignments_worker ON worker_schedule_assignments(worker_id);
CREATE INDEX idx_time_logs_worker ON time_logs(worker_id);
CREATE INDEX idx_time_logs_ts ON time_logs(timestamp);
