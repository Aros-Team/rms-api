-- Settings lane (P4-SET-02, P4-SET-13). Owns V56 per docs/PLAN.md.
-- Business identity singleton. It complements the legacy business_timezone config entry,
-- which is kept in sync by the settings service for backwards compatibility.
CREATE TABLE business_profile (
    id              BIGINT       NOT NULL PRIMARY KEY,
    restaurant_name VARCHAR(120) NOT NULL,
    legal_name      VARCHAR(160),
    tax_id          VARCHAR(40),
    address         VARCHAR(200),
    phone           VARCHAR(30),
    email           VARCHAR(120),
    timezone        VARCHAR(60)  NOT NULL,
    locale          VARCHAR(20)  NOT NULL,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO business_profile
    (id, restaurant_name, legal_name, tax_id, address, phone, email, timezone, locale)
VALUES
    (1, 'RMS', '', '', '', '', '', 'America/Bogota', 'es-CO');
