-- V25__create_special_selection_tables.sql
-- Creates tables for the special selection (combo) system

-- 1. Selection groups (categories within a combo, e.g., "Soup", "Protein")
CREATE TABLE special_selection_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    required BOOLEAN NOT NULL DEFAULT FALSE,
    min_selections INT NOT NULL DEFAULT 1,
    max_selections INT NOT NULL DEFAULT 1,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- 2. Extend product_product_options with selection group reference, extra price, ordering
ALTER TABLE product_product_options
  ADD COLUMN selection_group_id BIGINT NULL,
  ADD COLUMN extra_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  ADD COLUMN display_order INT NOT NULL DEFAULT 0,
  ADD CONSTRAINT fk_ppo_group FOREIGN KEY (selection_group_id) REFERENCES special_selection_groups(id) ON DELETE SET NULL;

-- 3. Additions (extras that cost more, e.g., "Extra cheese")
-- Must reference an existing product_option for inventory deduction
CREATE TABLE special_selection_additions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    extra_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    display_order INT NOT NULL DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (option_id) REFERENCES product_options(id) ON DELETE RESTRICT
);

-- 4. Clarification questions (e.g., "Any allergies?")
CREATE TABLE special_selection_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    required BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- 5. Per-day/per-time availability
CREATE TABLE special_selection_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    day_of_week VARCHAR(10) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    CONSTRAINT chk_schedule_time_range CHECK (start_time < end_time),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX idx_sss_product_day ON special_selection_schedules(product_id, day_of_week);

-- 6. History with JSON snapshots
CREATE TABLE special_selection_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    version INT NOT NULL,
    change_type VARCHAR(20) NOT NULL,
    snapshot_json LONGTEXT NOT NULL,
    changed_by VARCHAR(255) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_ssh_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX idx_ssh_product_version ON special_selection_history(product_id, version DESC);
CREATE INDEX idx_ssh_changed_at ON special_selection_history(product_id, changed_at DESC);

-- 7. Order detail additions (which extras were chosen)
CREATE TABLE order_detail_additions (
    order_detail_id BIGINT NOT NULL,
    addition_id BIGINT NOT NULL,
    extra_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (order_detail_id, addition_id),
    FOREIGN KEY (order_detail_id) REFERENCES order_details(id) ON DELETE CASCADE,
    FOREIGN KEY (addition_id) REFERENCES special_selection_additions(id) ON DELETE RESTRICT
);

-- 8. Order detail clarifications (answers to combo questions)
CREATE TABLE order_detail_clarifications (
    order_detail_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer TEXT NOT NULL,
    PRIMARY KEY (order_detail_id, question_id),
    FOREIGN KEY (order_detail_id) REFERENCES order_details(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES special_selection_questions(id) ON DELETE RESTRICT
);
