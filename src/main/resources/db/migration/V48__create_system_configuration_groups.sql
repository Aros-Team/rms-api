CREATE TABLE system_configuration_group (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(80) NOT NULL UNIQUE,
    description VARCHAR(200),
    sort_order  INT NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO system_configuration_group (name, description, sort_order) VALUES
    ('General', 'Moneda, zona horaria, país', 1),
    ('Nómina', 'Configuración de nómina y liquidaciones', 2),
    ('Inventario', 'Configuración de inventario y costos', 3);
