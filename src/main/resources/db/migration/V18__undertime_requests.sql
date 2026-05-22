CREATE TABLE IF NOT EXISTS undertime_requests (
    undertime_request_id INT NOT NULL AUTO_INCREMENT,
    employeeId INT NOT NULL,
    request_date DATE NOT NULL,
    total_hours DECIMAL(4,2) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    reliever VARCHAR(255) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'Pending',
    requested_at DATETIME NOT NULL,
    responded_at DATETIME NULL,
    approved_by_user_id INT NULL,
    denial_reason VARCHAR(500) NULL,
    attachment_path VARCHAR(255) NULL,
    PRIMARY KEY (undertime_request_id),
    CONSTRAINT fk_undertime_employee FOREIGN KEY (employeeId) REFERENCES employees(employeeId)
);
