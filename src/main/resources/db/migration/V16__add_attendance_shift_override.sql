-- Add shift_override column to attendance table for manual shift editing
ALTER TABLE attendance ADD COLUMN shift_override VARCHAR(100) NULL;
