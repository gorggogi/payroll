-- Weekly recurring schedule: one row per employee per day-of-week (Java DayOfWeek: 1=Mon .. 7=Sun).
CREATE TABLE employee_weekly_schedule (
    id INT NOT NULL AUTO_INCREMENT,
    employeeId INT NOT NULL,
    day_of_week TINYINT NOT NULL COMMENT '1=Monday .. 7=Sunday',
    is_rest_day TINYINT(1) NOT NULL DEFAULT 0,
    time_in TIME NULL,
    time_out TIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_employee_weekly_schedule_emp_dow (employeeId, day_of_week),
    CONSTRAINT fk_employee_weekly_schedule_employee
        FOREIGN KEY (employeeId) REFERENCES employees (employeeId)
);
