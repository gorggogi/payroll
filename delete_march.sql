USE payroll;

-- Delete Employee 1's March attendance records
DELETE FROM attendance 
WHERE employeeId = 1 
  AND attendance_date >= '2026-03-01' 
  AND attendance_date <= '2026-03-31';

-- Delete Employee 1's March deductions
DELETE FROM employee_deductions 
WHERE employeeId = 1 
  AND startDate >= '2026-03-01' 
  AND startDate <= '2026-03-31';

-- Delete the generated payslips for Employee 1
DELETE FROM payrollitems 
WHERE employeeId = 1;
