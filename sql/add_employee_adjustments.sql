-- Migration: Adjustments type catalog + employee assignment table
-- Mirrors the deductions / employeedeductions pattern exactly.

-- Type catalog (mirrors `deductions` table)
CREATE TABLE IF NOT EXISTS `adjustments` (
    `adjustmentId`   INT(11)       NOT NULL AUTO_INCREMENT,
    `adjustmentName` VARCHAR(100)  DEFAULT NULL,
    `adjustmentType` VARCHAR(50)   DEFAULT NULL,  -- 'Earnings' or 'Deduction'
    PRIMARY KEY (`adjustmentId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Employee assignment table (mirrors `employeedeductions` table)
CREATE TABLE IF NOT EXISTS `employeeadjustments` (
    `employeeAdjustmentId` INT(11)       NOT NULL AUTO_INCREMENT,
    `employeeId`           INT(11)       DEFAULT NULL,
    `adjustmentId`         INT(11)       DEFAULT NULL,
    `amount`               DECIMAL(10,2) DEFAULT NULL,
    `isRecurring`          TINYINT(1)    DEFAULT NULL,
    `startDate`            DATE          DEFAULT NULL,
    `endDate`              DATE          DEFAULT NULL,
    `applyOnCutoff`        VARCHAR(10)   NOT NULL DEFAULT 'BOTH',
    PRIMARY KEY (`employeeAdjustmentId`),
    KEY `employeeId` (`employeeId`),
    KEY `adjustmentId` (`adjustmentId`),
    CONSTRAINT `employeeadjustments_ibfk_1` FOREIGN KEY (`employeeId`) REFERENCES `employees` (`employeeId`),
    CONSTRAINT `employeeadjustments_ibfk_2` FOREIGN KEY (`adjustmentId`) REFERENCES `adjustments` (`adjustmentId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
