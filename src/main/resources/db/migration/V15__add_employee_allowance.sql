-- Adds per-employee allowance used in payroll proration.

ALTER TABLE employees ADD COLUMN allowance DECIMAL(10,2) NOT NULL DEFAULT 0.00;
