-- Insert test Attendance and Deductions matching the 21k Salary target
USE payroll;

-- Clean existing data for employee 1 to prevent conflicts
DELETE FROM attendance WHERE employeeId = 1;
DELETE FROM employee_deductions WHERE employeeId = 1;

-- 1. Insert 160 hours total work (20 days), 4 hrs overtime, 33 mins late for MAR 2026
INSERT INTO attendance (employeeId, attendance_date, time_in, time_out, late_minutes, undertime_minutes, work_hours, overtime_hours) VALUES
(1, '2026-03-02', '08:33:00', '17:00:00', 33, 0, 8, 0),
(1, '2026-03-03', '08:00:00', '17:00:00', 0, 0, 8, 0),
(1, '2026-03-04', '08:00:00', '17:00:00', 0, 0, 8, 0),
(1, '2026-03-05', '08:00:00', '17:00:00', 0, 0, 8, 0),
(1, '2026-03-06', '08:00:00', '17:00:00', 0, 0, 8, 0),
(1, '2026-03-09', '08:00:00', '17:00:00', 0, 0, 8, 0),
(1, '2026-03-10', '08:00:00', '17:00:00', 0, 0, 8, 0),
(1, '2026-03-11', '08:00:00', '17:00:00', 0, 0, 8, 0),
(1, '2026-03-12', '08:00:00', '17:00:00', 0, 0, 8, 0),
(1, '2026-03-13', '08:00:00', '17:00:00', 0, 0, 8, 0),
(1, '2026-03-16', '08:00:00', '17:00:00', 0, 0, 8, 0),
(1, '2026-03-17', '08:00:00', '17:00:00', 0, 0, 8, 0),
(1, '2026-03-18', '08:00:00', '17:00:00', 0, 0, 8, 0),
(1, '2026-03-19', '08:00:00', '17:00:00', 0, 0, 8, 0),
(1, '2026-03-20', '08:00:00', '17:00:00', 0, 0, 8, 0),
(1, '2026-03-23', '08:00:00', '17:00:00', 0, 0, 8, 0),
(1, '2026-03-24', '08:00:00', '17:00:00', 0, 0, 8, 0),
(1, '2026-03-25', '08:00:00', '17:00:00', 0, 0, 8, 0),
(1, '2026-03-26', '08:00:00', '17:00:00', 0, 0, 8, 0),
(1, '2026-03-27', '08:00:00', '21:00:00', 0, 0, 8, 4);

-- 2. Update existing Cash Advance (ID 101) to set the missing deductionType
UPDATE deductions SET deductionType = 'Advance' WHERE deductionId = 101;

-- 3. Assign 1250 Cash Advance to Employee 1 for March 2026
INSERT INTO employee_deductions (employeeId, deductionId, amount, startDate, endDate) VALUES
(1, 101, 1250.00, '2026-03-01', '2026-03-31');

-- Also, let's just make sure Employee 1 is definitely set as a "Regular" employee with "Monthly" pay type to match the 1.0x OT multiplier from the sample
UPDATE employees SET employmentType='Regular', payType='Monthly', basicSalary=21000 WHERE employeeId=1;
