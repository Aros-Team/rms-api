CREATE TABLE system_configuration (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    `key`     VARCHAR(120) NOT NULL UNIQUE,
    value     VARCHAR(500) NOT NULL,
    updated_by BIGINT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_sc_user FOREIGN KEY (updated_by) REFERENCES users(id)
);

INSERT INTO system_configuration (`key`, value) VALUES
    ('labor_cost_mode', 'AUTO'),
    ('default_currency', 'COP'),
    ('business_timezone', 'America/Bogota');
