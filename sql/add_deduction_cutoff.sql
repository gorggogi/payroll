-- Migration: Add deductionCutoff column to employeedeductions table
-- This controls which semi-monthly cutoff a recurring deduction applies to.
-- Values: 'SEMI_1' (1st cutoff, 1-15), 'SEMI_2' (2nd cutoff, 16-end), 'BOTH' (every cutoff)
-- Default: 'SEMI_2' — most Philippine companies deduct on the 2nd cutoff.

ALTER TABLE employeedeductions
ADD COLUMN IF NOT EXISTS deductionCutoff VARCHAR(10) NOT NULL DEFAULT 'SEMI_2';
