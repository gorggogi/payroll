-- V14__add_employee_ot_multiplier.sql
-- Adds per-employee OT multiplier column to support configurable overtime pay rates.
-- Resolves: PAYROLL_SYSTEM_AUDIT.md section 2.5

ALTER TABLE employees ADD COLUMN ot_multiplier DECIMAL(5,2);
UPDATE employees SET ot_multiplier = 1.0 WHERE ot_multiplier IS NULL;
