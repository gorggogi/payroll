-- Reusable schedule "class" + Sun–Sat rows + many employees linked per month
CREATE TABLE weekly_schedule_template (
    template_id INT NOT NULL AUTO_INCREMENT,
    template_name VARCHAR(256) NOT NULL,
    schedule_year INT NOT NULL,
    schedule_month TINYINT NOT NULL,
    PRIMARY KEY (template_id)
);

CREATE TABLE weekly_schedule_template_day (
    id INT NOT NULL AUTO_INCREMENT,
    template_id INT NOT NULL,
    day_of_week TINYINT NOT NULL,
    is_rest_day TINYINT(1) NOT NULL DEFAULT 0,
    time_in TIME NULL,
    time_out TIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ws_tpl_dow (template_id, day_of_week),
    CONSTRAINT fk_ws_tpl_day_template FOREIGN KEY (template_id) REFERENCES weekly_schedule_template (template_id) ON DELETE CASCADE
);

CREATE TABLE employee_schedule_assignment (
    id INT NOT NULL AUTO_INCREMENT,
    employeeId INT NOT NULL,
    template_id INT NOT NULL,
    schedule_year INT NOT NULL,
    schedule_month TINYINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_esa_emp_month (employeeId, schedule_year, schedule_month),
    KEY idx_esa_template (template_id),
    CONSTRAINT fk_esa_employee FOREIGN KEY (employeeId) REFERENCES employees (employeeId),
    CONSTRAINT fk_esa_template FOREIGN KEY (template_id) REFERENCES weekly_schedule_template (template_id) ON DELETE CASCADE
);
