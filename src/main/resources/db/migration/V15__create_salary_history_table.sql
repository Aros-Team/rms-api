CREATE TABLE salary_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    old_salary DECIMAL(10,2),
    new_salary DECIMAL(10,2) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason VARCHAR(255) NOT NULL,
    observations TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE users ADD COLUMN salary DECIMAL(10,2);

CREATE INDEX idx_salary_history_user_id ON salary_history(user_id);
CREATE INDEX idx_salary_history_changed_at ON salary_history(changed_at DESC);
