USE payroll;

-- Delete Employee 1's January attendance records
DELETE FROM attendance 
WHERE employeeId = 1 
  AND attendance_date >= '2026-01-01' 
  AND attendance_date <= '2026-01-31';

-- Delete Employee 1's January deductions (e.g., Cash Advance)
DELETE FROM employee_deductions 
WHERE employeeId = 1 
  AND startDate >= '2026-01-01' 
  AND startDate <= '2026-01-31';

-- Delete the generated payslip for Employee 1 for January
-- Note: You might need to adjust the column names if your payrollId/month/year columns are named differently in payrollitems
DELETE FROM payrollitems 
WHERE employeeId = 1;
-- If you ONLY want to delete January payslips and keep others, you would add a condition here if payrollitems tracks the period month.
