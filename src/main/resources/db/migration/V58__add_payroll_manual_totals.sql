ALTER TABLE payroll
    ADD COLUMN manual_bonuses DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER bonuses,
    ADD COLUMN manual_deductions DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER deductions;

-- Preserve the totals that existed before event aggregation was wired in.
-- From now on bonuses = manual_bonuses + SUM(earning events) and
-- deductions = manual_deductions + SUM(deduction events).
UPDATE payroll SET manual_bonuses = bonuses, manual_deductions = deductions;
