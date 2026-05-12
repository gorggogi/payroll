ALTER TABLE employees
    ADD COLUMN biometric_id VARCHAR(64) NULL;

ALTER TABLE employees
    ADD UNIQUE KEY uk_employees_biometric_id (biometric_id);
