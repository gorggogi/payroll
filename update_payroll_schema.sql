-- Run these statements in your MySQL client to update the payrollItems table
-- to support the new computation fields.

ALTER TABLE payrollItems
ADD COLUMN dailyRate DECIMAL(10,2) NULL,
ADD COLUMN hourlyRate DECIMAL(10,2) NULL,
ADD COLUMN perMinuteRate DECIMAL(10,4) NULL,
ADD COLUMN totalWorkedHours DECIMAL(10,2) NULL,
ADD COLUMN totalOtHours DECIMAL(10,2) NULL,
ADD COLUMN lateUndertimeMinutes INT NULL,
ADD COLUMN lateUndertimeDeduction DECIMAL(10,2) NULL,
ADD COLUMN cashAdvance DECIMAL(10,2) NULL,
ADD COLUMN adjustmentEarnings DECIMAL(10,2) NULL,
ADD COLUMN adjustmentDeductions DECIMAL(10,2) NULL,
ADD COLUMN totalEarnings DECIMAL(10,2) NULL,
ADD COLUMN serviceFee DECIMAL(10,2) NULL,
ADD COLUMN semiMonthlyContributions DECIMAL(10,2) NULL,
ADD COLUMN employmentType VARCHAR(50) NULL;
