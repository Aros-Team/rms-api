-- Add new columns to system_configuration
ALTER TABLE system_configuration
    ADD COLUMN group_id BIGINT AFTER `value`,
    ADD COLUMN description VARCHAR(200) AFTER `key`,
    ADD COLUMN sort_order INT NOT NULL DEFAULT 0 AFTER description;

-- Add FK constraint
ALTER TABLE system_configuration
    ADD CONSTRAINT fk_sc_group FOREIGN KEY (group_id)
        REFERENCES system_configuration_group(id);

-- Migrate existing seed data to group "General" (id=1)
UPDATE system_configuration SET group_id = 1, sort_order = 1 WHERE `key` = 'labor_cost_mode';
UPDATE system_configuration SET group_id = 1, sort_order = 2 WHERE `key` = 'default_currency';
UPDATE system_configuration SET group_id = 1, sort_order = 3 WHERE `key` = 'business_timezone';

-- Add index for faster group lookups
CREATE INDEX idx_sc_group ON system_configuration(group_id);
