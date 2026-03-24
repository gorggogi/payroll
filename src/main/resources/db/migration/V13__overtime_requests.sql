CREATE TABLE overtime_request (
    overtime_request_id INT NOT NULL AUTO_INCREMENT,
    employeeId INT NOT NULL,
    work_date DATE NOT NULL,
    overtime_in TIME NOT NULL,
    overtime_out TIME NOT NULL,
    total_hours DECIMAL(10, 2) NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'Pending',
    requested_at DATETIME(6) NOT NULL,
    responded_at DATETIME(6) NULL,
    approved_by_user_id INT NULL,
    PRIMARY KEY (overtime_request_id),
    KEY idx_ot_req_emp (employeeId),
    KEY idx_ot_req_status (status),
    CONSTRAINT fk_ot_req_employee FOREIGN KEY (employeeId) REFERENCES employees (employeeId),
    CONSTRAINT fk_ot_req_user FOREIGN KEY (approved_by_user_id) REFERENCES users (userId)
);
