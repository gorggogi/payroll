-- Scope weekly rows to a calendar month (same Sun–Sat pattern for that month).
ALTER TABLE employee_weekly_schedule
    ADD COLUMN schedule_year INT NOT NULL DEFAULT 2026,
    ADD COLUMN schedule_month TINYINT NOT NULL DEFAULT 1;

ALTER TABLE employee_weekly_schedule
    DROP INDEX uk_employee_weekly_schedule_emp_dow;

ALTER TABLE employee_weekly_schedule
    ADD UNIQUE KEY uk_employee_weekly_schedule_emp_dow_ym (employeeId, day_of_week, schedule_year, schedule_month);
