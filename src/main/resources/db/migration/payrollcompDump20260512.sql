-- MariaDB dump 10.19  Distrib 10.4.32-MariaDB, for Win64 (AMD64)
--
-- Host: 127.0.0.1    Database: payrollcomp
-- ------------------------------------------------------
-- Server version	10.4.32-MariaDB-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `adjustments`
--

DROP TABLE IF EXISTS `adjustments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `adjustments` (
  `adjustmentId` int(11) NOT NULL AUTO_INCREMENT,
  `adjustmentName` varchar(100) DEFAULT NULL,
  `adjustmentType` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`adjustmentId`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `adjustments`
--

LOCK TABLES `adjustments` WRITE;
/*!40000 ALTER TABLE `adjustments` DISABLE KEYS */;
INSERT INTO `adjustments` VALUES (1,'Mid-year Bonus','Earnings');
/*!40000 ALTER TABLE `adjustments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attendance`
--

DROP TABLE IF EXISTS `attendance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `attendance` (
  `attendanceId` int(11) NOT NULL AUTO_INCREMENT,
  `employeeId` int(11) NOT NULL,
  `attendance_date` date DEFAULT NULL,
  `time_in` time DEFAULT NULL,
  `time_out` time DEFAULT NULL,
  `work_hours` decimal(5,2) DEFAULT NULL,
  `late_minutes` int(11) DEFAULT 0,
  `undertime_minutes` int(11) DEFAULT 0,
  `overtime_hours` decimal(5,2) DEFAULT 0.00,
  `status` enum('Present','Absent','Late','Leave','Holiday') DEFAULT NULL,
  PRIMARY KEY (`attendanceId`),
  KEY `employeeId` (`employeeId`),
  CONSTRAINT `attendance_ibfk_1` FOREIGN KEY (`employeeId`) REFERENCES `employees` (`employeeId`)
) ENGINE=InnoDB AUTO_INCREMENT=281 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attendance`
--

LOCK TABLES `attendance` WRITE;
/*!40000 ALTER TABLE `attendance` DISABLE KEYS */;
INSERT INTO `attendance` VALUES (29,2,'2025-01-08','09:00:00','18:00:00',8.00,60,0,0.00,'Present'),(30,2,'2025-01-22','08:30:00','17:30:00',8.00,30,0,0.00,'Present'),(31,2,'2025-02-12','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(32,2,'2025-02-26','08:00:00','19:00:00',10.00,0,0,2.00,'Present'),(33,2,'2025-03-14','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(34,2,'2025-03-28','08:00:00','16:30:00',7.50,0,30,0.00,'Present'),(35,2,'2025-04-11','08:20:00','17:00:00',7.67,20,0,0.00,'Present'),(36,2,'2025-04-25','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(37,2,'2025-05-09','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(38,2,'2025-05-23','08:00:00','18:30:00',9.50,0,0,1.50,'Present'),(39,2,'2025-06-06','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(40,2,'2025-06-20','08:45:00','17:00:00',7.25,45,0,0.00,'Present'),(41,2,'2025-07-04','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(42,2,'2025-07-18','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(43,2,'2025-08-01','08:00:00','19:30:00',10.50,0,0,2.50,'Present'),(44,2,'2025-08-15','08:10:00','17:00:00',7.83,10,0,0.00,'Present'),(45,2,'2025-09-05','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(46,2,'2025-09-19','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(47,2,'2025-10-03','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(48,2,'2025-10-17','08:00:00','20:00:00',11.00,0,0,3.00,'Present'),(49,2,'2025-11-07','08:25:00','17:00:00',7.58,25,0,0.00,'Present'),(50,2,'2025-11-21','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(51,2,'2025-12-05','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(52,2,'2025-12-19','08:00:00','18:00:00',9.00,0,0,1.00,'Present'),(53,2,'2026-01-09','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(54,2,'2026-01-23','08:30:00','17:00:00',7.50,30,0,0.00,'Present'),(55,2,'2026-02-06','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(56,2,'2026-02-20','08:00:00','19:00:00',10.00,0,0,2.00,'Present'),(57,3,'2025-01-10','07:50:00','17:10:00',8.33,0,0,0.33,'Present'),(58,3,'2025-01-24','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(59,3,'2025-02-07','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(60,3,'2025-02-21','08:05:00','17:00:00',7.92,5,0,0.00,'Present'),(61,3,'2025-03-07','08:00:00','20:00:00',11.00,0,0,3.00,'Present'),(62,3,'2025-03-21','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(63,3,'2025-04-04','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(64,3,'2025-04-18','08:30:00','16:30:00',7.00,30,30,0.00,'Present'),(65,3,'2025-05-02','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(66,3,'2025-05-16','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(67,3,'2025-06-13','08:00:00','18:45:00',9.75,0,0,1.75,'Present'),(68,3,'2025-06-27','08:15:00','17:00:00',7.75,15,0,0.00,'Present'),(69,3,'2025-07-11','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(70,3,'2025-07-25','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(71,3,'2025-08-08','08:00:00','19:00:00',10.00,0,0,2.00,'Present'),(72,3,'2025-08-22','08:20:00','17:00:00',7.67,20,0,0.00,'Present'),(73,3,'2025-09-12','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(74,3,'2025-09-26','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(75,3,'2025-10-10','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(76,3,'2025-10-24','08:00:00','20:30:00',11.50,0,0,3.50,'Present'),(77,3,'2025-11-14','08:10:00','17:00:00',7.83,10,0,0.00,'Present'),(78,3,'2025-11-28','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(79,3,'2025-12-12','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(80,3,'2025-12-26','08:00:00','18:30:00',9.50,0,0,1.50,'Present'),(81,3,'2026-01-09','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(82,3,'2026-01-23','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(83,3,'2026-02-06','08:00:00','19:30:00',10.50,0,0,2.50,'Present'),(84,3,'2026-02-20','08:10:00','17:00:00',7.83,10,0,0.00,'Present'),(215,1,'2026-03-02','08:33:00','17:00:00',8.00,33,0,0.00,NULL),(216,1,'2026-03-03','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(217,1,'2026-03-04','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(218,1,'2026-03-05','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(219,1,'2026-03-06','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(220,1,'2026-03-09','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(221,1,'2026-03-10','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(222,1,'2026-03-11','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(223,1,'2026-03-12','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(224,1,'2026-03-13','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(225,1,'2026-03-16','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(226,1,'2026-03-17','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(227,1,'2026-03-18','08:00:00','17:00:00',8.00,33,0,0.00,NULL),(228,1,'2026-03-19','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(229,1,'2026-03-20','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(230,1,'2026-03-23','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(231,1,'2026-03-24','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(232,1,'2026-03-25','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(233,1,'2026-03-26','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(234,1,'2026-03-27','08:00:00','21:00:00',8.00,0,0,4.00,NULL),(235,120,'2026-03-16','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(236,120,'2026-03-17','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(237,120,'2026-03-18','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(238,120,'2026-03-19','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(239,120,'2026-03-20','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(240,120,'2026-03-23','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(241,120,'2026-03-24','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(242,120,'2026-03-25','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(243,120,'2026-03-26','08:00:00','17:00:00',8.00,0,0,0.00,NULL),(244,120,'2026-03-27','08:00:00','21:00:00',12.00,0,0,0.00,NULL),(260,119,'2026-03-02','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(261,119,'2026-03-03','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(262,119,'2026-03-04','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(263,119,'2026-03-05','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(264,119,'2026-03-06','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(265,119,'2026-03-09','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(266,119,'2026-03-10','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(267,119,'2026-03-11','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(268,119,'2026-03-12','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(269,119,'2026-03-13','08:00:00','12:00:00',4.00,0,0,0.00,'Present'),(270,119,'2026-03-16','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(271,119,'2026-03-17','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(272,119,'2026-03-18','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(273,119,'2026-03-19','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(274,119,'2026-03-20','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(275,119,'2026-03-23','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(276,119,'2026-03-24','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(277,119,'2026-03-25','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(278,119,'2026-03-26','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(279,119,'2026-03-27','08:00:00','17:00:00',8.00,0,0,0.00,'Present'),(280,119,'2026-03-30','08:00:00','12:00:00',4.00,0,0,0.00,'Present');
/*!40000 ALTER TABLE `attendance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auditlogs`
--

DROP TABLE IF EXISTS `auditlogs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auditlogs` (
  `logId` int(11) NOT NULL AUTO_INCREMENT,
  `action` varchar(50) DEFAULT NULL,
  `tableName` varchar(50) DEFAULT NULL,
  `recordId` int(11) DEFAULT NULL,
  `performedBy` varchar(50) DEFAULT NULL,
  `timestamp` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`logId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auditlogs`
--

LOCK TABLES `auditlogs` WRITE;
/*!40000 ALTER TABLE `auditlogs` DISABLE KEYS */;
/*!40000 ALTER TABLE `auditlogs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bonuses`
--

DROP TABLE IF EXISTS `bonuses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bonuses` (
  `bonusId` int(11) NOT NULL AUTO_INCREMENT,
  `employeeId` int(11) DEFAULT NULL,
  `bonusType` varchar(50) DEFAULT NULL,
  `amount` decimal(10,2) DEFAULT NULL,
  `taxable` tinyint(1) DEFAULT NULL,
  `bonusDate` date DEFAULT NULL,
  PRIMARY KEY (`bonusId`),
  KEY `employeeId` (`employeeId`),
  CONSTRAINT `bonuses_ibfk_1` FOREIGN KEY (`employeeId`) REFERENCES `employees` (`employeeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bonuses`
--

LOCK TABLES `bonuses` WRITE;
/*!40000 ALTER TABLE `bonuses` DISABLE KEYS */;
/*!40000 ALTER TABLE `bonuses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `deductions`
--

DROP TABLE IF EXISTS `deductions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `deductions` (
  `deductionId` int(11) NOT NULL AUTO_INCREMENT,
  `deductionName` varchar(50) DEFAULT NULL,
  `deductionType` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`deductionId`)
) ENGINE=InnoDB AUTO_INCREMENT=103 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `deductions`
--

LOCK TABLES `deductions` WRITE;
/*!40000 ALTER TABLE `deductions` DISABLE KEYS */;
INSERT INTO `deductions` VALUES (101,'LA','Union'),(102,'Cash Advance','Advance');
/*!40000 ALTER TABLE `deductions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `departments`
--

DROP TABLE IF EXISTS `departments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `departments` (
  `departmentId` int(11) NOT NULL AUTO_INCREMENT,
  `departmentName` varchar(100) NOT NULL,
  PRIMARY KEY (`departmentId`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `departments`
--

LOCK TABLES `departments` WRITE;
/*!40000 ALTER TABLE `departments` DISABLE KEYS */;
INSERT INTO `departments` VALUES (1,'Human Resources'),(2,'Information Technology'),(3,'Finance'),(4,'Sales'),(5,'Operations');
/*!40000 ALTER TABLE `departments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employee_schedule_assignment`
--

DROP TABLE IF EXISTS `employee_schedule_assignment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `employee_schedule_assignment` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `employeeId` int(11) NOT NULL,
  `template_id` int(11) NOT NULL,
  `schedule_year` int(11) NOT NULL,
  `schedule_month` tinyint(4) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_esa_emp_month` (`employeeId`,`schedule_year`,`schedule_month`),
  KEY `idx_esa_template` (`template_id`),
  CONSTRAINT `fk_esa_employee` FOREIGN KEY (`employeeId`) REFERENCES `employees` (`employeeId`),
  CONSTRAINT `fk_esa_template` FOREIGN KEY (`template_id`) REFERENCES `weekly_schedule_template` (`template_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employee_schedule_assignment`
--

LOCK TABLES `employee_schedule_assignment` WRITE;
/*!40000 ALTER TABLE `employee_schedule_assignment` DISABLE KEYS */;
INSERT INTO `employee_schedule_assignment` VALUES (7,34,3,2026,3),(8,39,3,2026,3),(9,120,3,2026,3),(10,26,3,2026,3);
/*!40000 ALTER TABLE `employee_schedule_assignment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employeeadjustments`
--

DROP TABLE IF EXISTS `employeeadjustments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `employeeadjustments` (
  `employeeAdjustmentId` int(11) NOT NULL AUTO_INCREMENT,
  `employeeId` int(11) DEFAULT NULL,
  `adjustmentId` int(11) DEFAULT NULL,
  `amount` decimal(10,2) DEFAULT NULL,
  `isRecurring` tinyint(1) DEFAULT NULL,
  `startDate` date DEFAULT NULL,
  `endDate` date DEFAULT NULL,
  `applyOnCutoff` varchar(10) NOT NULL DEFAULT 'BOTH',
  PRIMARY KEY (`employeeAdjustmentId`),
  KEY `employeeId` (`employeeId`),
  KEY `adjustmentId` (`adjustmentId`),
  CONSTRAINT `employeeadjustments_ibfk_1` FOREIGN KEY (`employeeId`) REFERENCES `employees` (`employeeId`),
  CONSTRAINT `employeeadjustments_ibfk_2` FOREIGN KEY (`adjustmentId`) REFERENCES `adjustments` (`adjustmentId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employeeadjustments`
--

LOCK TABLES `employeeadjustments` WRITE;
/*!40000 ALTER TABLE `employeeadjustments` DISABLE KEYS */;
/*!40000 ALTER TABLE `employeeadjustments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employeedeductions`
--

DROP TABLE IF EXISTS `employeedeductions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `employeedeductions` (
  `employeeDeductionId` int(11) NOT NULL AUTO_INCREMENT,
  `employeeId` int(11) DEFAULT NULL,
  `deductionId` int(11) DEFAULT NULL,
  `amount` decimal(10,2) DEFAULT NULL,
  `isRecurring` tinyint(1) DEFAULT NULL,
  `startDate` date DEFAULT NULL,
  `endDate` date DEFAULT NULL,
  `deductionCutoff` varchar(10) NOT NULL DEFAULT 'SEMI_2',
  PRIMARY KEY (`employeeDeductionId`),
  KEY `employeeId` (`employeeId`),
  KEY `deductionId` (`deductionId`),
  CONSTRAINT `employeedeductions_ibfk_1` FOREIGN KEY (`employeeId`) REFERENCES `employees` (`employeeId`),
  CONSTRAINT `employeedeductions_ibfk_2` FOREIGN KEY (`deductionId`) REFERENCES `deductions` (`deductionId`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employeedeductions`
--

LOCK TABLES `employeedeductions` WRITE;
/*!40000 ALTER TABLE `employeedeductions` DISABLE KEYS */;
INSERT INTO `employeedeductions` VALUES (5,1,102,1250.00,1,'2026-03-18','2026-03-31','SEMI_2');
/*!40000 ALTER TABLE `employeedeductions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employees`
--

DROP TABLE IF EXISTS `employees`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `employees` (
  `employeeId` int(11) NOT NULL AUTO_INCREMENT,
  `employeeNumber` varchar(20) NOT NULL,
  `firstName` varchar(50) DEFAULT NULL,
  `middleName` varchar(50) DEFAULT NULL,
  `lastName` varchar(50) DEFAULT NULL,
  `birthDate` date DEFAULT NULL,
  `address` text DEFAULT NULL,
  `contactNumber` varchar(20) DEFAULT NULL,
  `dateHired` date DEFAULT NULL,
  `employmentStatus` enum('Active','Resigned','Terminated') DEFAULT NULL,
  `employmentType` enum('Regular','Probationary','Contractual','Job Order') DEFAULT NULL,
  `payType` enum('Monthly','Daily','Hourly','Biweekly') DEFAULT NULL,
  `basicSalary` decimal(10,2) DEFAULT NULL,
  `factorRate` decimal(5,2) DEFAULT NULL,
  `bank_Account` varchar(50) DEFAULT NULL,
  `tin` varchar(20) DEFAULT NULL,
  `sssNumber` varchar(20) DEFAULT NULL,
  `philhealthNumber` varchar(20) DEFAULT NULL,
  `pagibigNumber` varchar(20) DEFAULT NULL,
  `departmentId` int(11) DEFAULT NULL,
  `positionId` int(11) DEFAULT NULL,
  `holidayPayEligible` tinyint(1) NOT NULL DEFAULT 0,
  `ot_multiplier` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`employeeId`),
  UNIQUE KEY `employeeNumber` (`employeeNumber`),
  KEY `departmentId` (`departmentId`),
  KEY `positionId` (`positionId`),
  CONSTRAINT `employees_ibfk_1` FOREIGN KEY (`departmentId`) REFERENCES `departments` (`departmentId`),
  CONSTRAINT `employees_ibfk_2` FOREIGN KEY (`positionId`) REFERENCES `positions` (`positionId`)
) ENGINE=InnoDB AUTO_INCREMENT=121 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employees`
--

LOCK TABLES `employees` WRITE;
/*!40000 ALTER TABLE `employees` DISABLE KEYS */;
INSERT INTO `employees` VALUES (1,'EMP-2024-001','Juan','Santos','Dela Cruz','1990-05-15','123 Rizal St, Manila','+639171234567','2020-01-15','Active','Job Order','Monthly',21000.00,20.00,'1234567890','123-456-789-000','34-1234567-8','12-345678901-2','1234-5678-9012',2,1,0,NULL),(2,'EMP-2024-002','Maria','Garcia','Reynolds','1995-08-20','456 Bonifacio Ave, Quezon City','+639181234568','2021-03-10','Active','Regular','Monthly',55000.00,NULL,'2345678901','234-567-890-111','34-2345678-9','12-456789012-3','2345-6789-0123',2,2,0,NULL),(3,'EMP-2024-003','Carlos','Mendoza','Santos','1992-11-08','789 Luna St, Makati','+639191234569','2021-06-20','Active','Regular','Monthly',58000.00,NULL,'3456789012','345-678-901-222','34-3456789-0','12-567890123-4','3456-7890-1234',2,2,0,NULL),(4,'EMP-2024-004','Sofia','Rivera','Castillo','1998-09-22','258 Quezon Ave, Quezon City','+639241234574','2024-01-08','Resigned','Probationary','Monthly',35000.00,NULL,'8901234567','890-123-456-777','34-8901234-5','12-012345678-9','8901-2345-6789',2,2,0,NULL),(5,'EMP-2024-005','Miguel','Torres','Cruz','1993-04-12','321 Taft Ave, Manila','+639201234570','2022-08-15','Active','Regular','Monthly',60000.00,NULL,'4567890123','456-789-012-333','34-4567890-1','12-678901234-5','4567-8901-2345',2,2,0,NULL),(6,'EMP-2024-006','Rosa','Mendoza','Torres','1991-11-30','987 Aguinaldo St, Taguig','+639221234572','2018-09-01','Resigned','Regular','Monthly',60000.00,NULL,'6789012345','678-901-234-555','34-6789012-3','12-890123456-7','6789-0123-4567',2,2,0,NULL),(7,'EMP-2024-007','Luis','Fernandez','Ramos','1994-07-18','654 Roxas Blvd, Pasay','+639231234573','2023-02-10','Active','Contractual','Monthly',45000.00,NULL,'7890123456','789-012-345-666','34-7890123-4','12-901234567-8','7890-1234-5678',2,2,0,NULL),(8,'EMP-2024-008','Ana','Lopez','Bautista','1996-03-25','147 Del Pilar St, Manila','+639241234575','2023-11-20','Resigned','Probationary','Monthly',38000.00,NULL,'8901234568','890-123-456-778','34-8901234-6','12-012345678-0','8901-2345-6790',2,2,0,NULL),(9,'EMP-2024-009','Diego','Aquino','Villanueva','1989-12-05','369 Mabini St, Makati','+639251234576','2019-05-15','Active','Regular','Monthly',62000.00,NULL,'9012345679','901-234-567-889','34-9012345-7','12-123456789-1','9012-3456-7891',2,2,0,NULL),(10,'EMP-2024-010','Elena','Cruz','Gonzales','1997-06-30','741 Legarda St, Manila','+639261234577','2022-03-01','Active','Regular','Monthly',56000.00,NULL,'0123456780','012-345-678-990','34-0123456-8','12-234567890-2','0123-4567-8902',2,2,0,NULL),(11,'EMP-2024-011','Pedro','Lopez','Santos','1988-12-05','789 Luna St, Makati','+639191234569','2019-06-01','Active','Regular','Monthly',45000.00,NULL,'3456789012','345-678-901-222','34-3456789-0','12-567890123-4','3456-7890-1234',1,4,0,NULL),(12,'EMP-2024-012','Isabella','Ramos','Diaz','1992-08-14','852 Espana Blvd, Manila','+639271234578','2020-07-10','Active','Regular','Monthly',48000.00,NULL,'1234567891','123-456-789-001','34-1234567-9','12-345678901-3','1234-5678-9013',1,4,0,NULL),(13,'EMP-2024-013','Gabriel','Santos','Morales','1995-02-20','963 Avenida Rizal, Manila','+639281234579','2021-09-15','Active','Regular','Monthly',42000.00,NULL,'2345678902','234-567-890-112','34-2345678-0','12-456789012-4','2345-6789-0124',1,4,0,NULL),(14,'EMP-2024-014','Camila','Reyes','Navarro','1993-11-11','159 P. Burgos St, Makati','+639291234580','2023-01-20','','Probationary','Monthly',38000.00,NULL,'3456789013','345-678-901-223','34-3456789-1','12-567890123-5','3456-7890-1235',1,4,0,NULL),(15,'EMP-2024-015','Mateo','Garcia','Ortega','1990-05-05','357 Katipunan Ave, Quezon City','+639301234581','2018-04-12','Active','Regular','Monthly',50000.00,NULL,'4567890124','456-789-012-334','34-4567890-2','12-678901234-6','4567-8901-2346',1,1,0,NULL),(16,'EMP-2024-016','Valentina','Torres','Jimenez','1994-09-28','468 Aurora Blvd, Quezon City','+639311234582','2022-06-18','Active','Regular','Monthly',43000.00,NULL,'5678901235','567-890-123-445','34-5678901-3','12-789012345-7','5678-9012-3457',1,4,0,NULL),(17,'EMP-2024-017','Santiago','Mendoza','Ruiz','1991-01-15','579 Marcos Highway, Pasig','+639321234583','2020-11-25','Active','Regular','Monthly',46000.00,NULL,'6789012346','678-901-234-556','34-6789012-4','12-890123456-8','6789-0123-4568',1,4,0,NULL),(18,'EMP-2024-018','Lucia','Fernandez','Herrera','1996-07-22','680 Shaw Blvd, Mandaluyong','+639331234584','2023-08-30','Terminated','Probationary','Monthly',37000.00,NULL,'7890123457','789-012-345-667','34-7890123-5','12-901234567-9','7890-1234-5679',1,4,0,NULL),(19,'EMP-2024-019','Andres','Cruz','Medina','1989-03-18','791 EDSA, Quezon City','+639341234585','2019-02-14','Active','Regular','Monthly',49000.00,NULL,'8901234569','890-123-456-779','34-8901234-7','12-012345678-1','8901-2345-6791',1,4,0,NULL),(20,'EMP-2024-020','Martina','Lopez','Castro','1997-12-08','802 C5 Road, Taguig','+639351234586','2022-10-05','Active','Regular','Monthly',44000.00,NULL,'9012345680','901-234-567-890','34-9012345-8','12-123456789-2','9012-3456-7892',1,4,0,NULL),(21,'EMP-2024-021','Ana','Cruz','Bautista','1992-03-25','321 Mabini St, Pasig','+639201234570','2022-01-20','Active','Probationary','Monthly',40000.00,NULL,'4567890123','456-789-012-333','34-4567890-1','12-678901234-5','4567-8901-2345',3,3,0,NULL),(22,'EMP-2024-022','Ricardo','Ramos','Silva','1988-06-17','913 Ortigas Ave, Pasig','+639361234587','2017-03-22','Active','Regular','Monthly',65000.00,NULL,'0123456781','012-345-678-991','34-0123456-9','12-234567890-3','0123-4567-8903',3,1,0,NULL),(23,'EMP-2024-023','Daniela','Santos','Vargas','1994-10-30','024 Meralco Ave, Pasig','+639371234588','2020-05-18','Active','Regular','Monthly',52000.00,NULL,'1234567892','123-456-789-002','34-1234567-0','12-345678901-4','1234-5678-9014',3,3,0,NULL),(24,'EMP-2024-024','Fernando','Garcia','Romero','1991-04-25','135 Julia Vargas Ave, Pasig','+639381234589','2019-08-12','Active','Regular','Monthly',54000.00,NULL,'2345678903','234-567-890-113','34-2345678-1','12-456789012-5','2345-6789-0125',3,3,0,NULL),(25,'EMP-2024-025','Patricia','Torres','Gutierrez','1995-11-19','246 ADB Ave, Ortigas','+639391234590','2021-12-01','Active','Regular','Monthly',48000.00,NULL,'3456789014','345-678-901-224','34-3456789-2','12-567890123-6','3456-7890-1236',3,3,0,NULL),(26,'EMP-2024-026','Alberto','Mendoza','Alvarez','1990-02-14','357 San Miguel Ave, Pasig','+639401234591','2018-07-20','Active','Regular','Monthly',58000.00,NULL,'4567890125','456-789-012-335','34-4567890-3','12-678901234-7','4567-8901-2347',3,3,0,NULL),(27,'EMP-2024-027','Carolina','Fernandez','Flores','1993-08-07','468 Emerald Ave, Ortigas','+639411234592','2022-02-28','Active','Regular','Monthly',50000.00,NULL,'5678901236','567-890-123-446','34-5678901-4','12-789012345-8','5678-9012-3458',3,3,0,NULL),(28,'EMP-2024-028','Javier','Cruz','Dominguez','1996-05-12','579 Sapphire St, Ortigas','+639421234593','2023-09-15','Terminated','Probationary','Monthly',42000.00,NULL,'6789012347','678-901-234-557','34-6789012-5','12-890123456-9','6789-0123-4569',3,3,0,NULL),(29,'EMP-2024-029','Monica','Lopez','Ramirez','1989-12-28','680 Ruby Road, Pasig','+639431234594','2017-11-10','Active','Regular','Monthly',62000.00,NULL,'7890123458','789-012-345-668','34-7890123-6','12-901234567-0','7890-1234-5680',3,3,0,NULL),(30,'EMP-2024-030','Rodrigo','Santos','Moreno','1992-07-03','791 Pearl Dr, Ortigas','+639441234595','2020-04-22','Active','Regular','Monthly',53000.00,NULL,'8901234570','890-123-456-780','34-8901234-8','12-012345678-2','8901-2345-6792',3,3,0,NULL),(31,'EMP-2024-031','Carlos','Ramos','Fernandez','1993-07-10','654 Del Pilar St, Pasay','+639211234571','2023-05-15','Active','Contractual','Daily',500.00,NULL,'5678901234','567-890-123-444','34-5678901-2','12-789012345-6','5678-9012-3456',4,5,0,NULL),(32,'EMP-2024-032','Beatriz','Garcia','Soto','1994-09-21','902 Buendia Ave, Makati','+639451234596','2022-06-08','Active','Regular','Monthly',38000.00,NULL,'9012345681','901-234-567-891','34-9012345-9','12-123456789-3','9012-3456-7893',4,5,0,NULL),(33,'EMP-2024-033','Emilio','Torres','Pena','1991-01-16','013 Ayala Ave, Makati','+639461234597','2019-03-14','Active','Regular','Monthly',42000.00,NULL,'0123456782','012-345-678-992','34-0123456-0','12-234567890-4','0123-4567-8904',4,5,0,NULL),(34,'EMP-2024-034','Adriana','Mendoza','Aguilar','1995-06-29','124 Paseo de Roxas, Makati','+639471234598','2021-08-20','Active','Regular','Monthly',40000.00,NULL,'1234567893','123-456-789-003','34-1234567-1','12-345678901-5','1234-5678-9015',4,5,0,NULL),(35,'EMP-2024-035','Francisco','Cruz','Vega','1990-11-05','235 Makati Ave, Makati','+639481234599','2018-12-12','Active','Regular','Monthly',45000.00,NULL,'2345678904','234-567-890-114','34-2345678-2','12-456789012-6','2345-6789-0126',4,1,0,NULL),(36,'EMP-2024-036','Gabriela','Fernandez','Rios','1996-04-18','346 Sen Gil Puyat Ave, Makati','+639491234600','2023-02-25','Terminated','Probationary','Monthly',35000.00,NULL,'3456789015','345-678-901-225','34-3456789-3','12-567890123-7','3456-7890-1237',4,5,0,NULL),(37,'EMP-2024-037','Hector','Lopez','Mendez','1992-08-23','457 Chino Roces Ave, Makati','+639501234601','2020-10-30','Active','Regular','Monthly',41000.00,NULL,'4567890126','456-789-012-336','34-4567890-4','12-678901234-8','4567-8901-2348',4,5,0,NULL),(38,'EMP-2024-038','Isabel','Santos','Ortiz','1994-12-11','568 Kalayaan Ave, Makati','+639511234602','2022-04-15','Active','Contractual','Daily',480.00,NULL,'5678901237','567-890-123-447','34-5678901-5','12-789012345-9','5678-9012-3459',4,5,0,NULL),(39,'EMP-2024-039','Jorge','Garcia','Nunez','1989-03-26','679 Jupiter St, Makati','+639521234603','2017-09-18','Active','Regular','Monthly',48000.00,NULL,'6789012348','678-901-234-558','34-6789012-6','12-890123456-0','6789-0123-4570',4,5,0,NULL),(40,'EMP-2024-040','Laura','Torres','Castillo','1997-07-14','780 Salcedo St, Makati','+639531234604','2023-11-22','Terminated','Probationary','Monthly',36000.00,NULL,'7890123459','789-012-345-669','34-7890123-7','12-901234567-1','7890-1234-5681',4,5,0,NULL),(41,'EMP-2024-041','Miguel','Aquino','Villanueva','1985-04-18','147 Roxas Blvd, Manila','+639231234573','2017-02-10','Active','Regular','Monthly',80000.00,NULL,'7890123456','789-012-345-666','34-7890123-4','12-901234567-8','7890-1234-5678',5,1,0,NULL),(42,'EMP-2024-042','Natalia','Mendoza','Guerrero','1991-10-09','891 United Nations Ave, Manila','+639541234605','2019-05-28','Active','Regular','Monthly',55000.00,NULL,'8901234571','890-123-456-781','34-8901234-9','12-012345678-3','8901-2345-6793',5,6,0,NULL),(43,'EMP-2024-043','Oscar','Cruz','Rojas','1993-02-15','902 Taft Ave, Manila','+639551234606','2020-08-14','Active','Regular','Monthly',52000.00,NULL,'9012345682','901-234-567-892','34-9012345-0','12-123456789-4','9012-3456-7894',5,6,0,NULL),(44,'EMP-2024-044','Paula','Fernandez','Molina','1995-06-22','013 Pedro Gil St, Manila','+639561234607','2022-01-10','Active','Regular','Monthly',50000.00,NULL,'0123456783','012-345-678-993','34-0123456-1','12-234567890-5','0123-4567-8905',5,6,0,NULL),(45,'EMP-2024-045','Raul','Lopez','Campos','1988-11-30','124 Quirino Ave, Manila','+639571234608','2016-07-05','Active','Regular','Monthly',60000.00,NULL,'1234567894','123-456-789-004','34-1234567-2','12-345678901-6','1234-5678-9016',5,6,0,NULL),(46,'EMP-2024-046','Sandra','Santos','Paredes','1992-04-08','235 San Marcelino St, Manila','+639581234609','2021-03-18','Active','Regular','Monthly',53000.00,NULL,'2345678905','234-567-890-115','34-2345678-3','12-456789012-7','2345-6789-0127',5,6,0,NULL),(47,'EMP-2024-047','Tomas','Garcia','Delgado','1994-09-17','346 Padre Faura St, Manila','+639591234610','2023-06-25','Terminated','Probationary','Monthly',45000.00,NULL,'3456789016','345-678-901-226','34-3456789-4','12-567890123-8','3456-7890-1238',5,6,0,NULL),(48,'EMP-2024-048','Veronica','Torres','Fuentes','1990-01-25','457 M.H. Del Pilar St, Manila','+639601234611','2019-11-08','Active','Regular','Monthly',56000.00,NULL,'4567890127','456-789-012-337','34-4567890-5','12-678901234-9','4567-8901-2349',5,6,0,NULL),(49,'EMP-2024-049','Xavier','Mendoza','Salazar','1996-07-12','568 Kalaw Ave, Manila','+639611234612','2022-09-20','Active','Contractual','Monthly',48000.00,NULL,'5678901238','567-890-123-448','34-5678901-6','12-789012345-0','5678-9012-3460',5,6,0,NULL),(50,'EMP-2024-050','Yolanda','Cruz','Cortez','1991-12-03','679 Harrison St, Manila','+639621234613','2020-02-14','Active','Regular','Monthly',54000.00,NULL,'6789012349','678-901-234-559','34-6789012-7','12-890123456-1','6789-0123-4571',5,6,0,NULL),(119,'EMP00051','Charlene','Cruz','Dilig','2003-07-09','013 Nicanor Reyes St, Manila','+639471234598','2026-03-05','Active','Job Order','Monthly',39000.00,20.00,'2345678901','456-789-012-335','34-2345678-9','12-345678901-3','8901-2345-6790',2,2,0,NULL),(120,'EMP00120','Charlene','Cortez','Acosta','2003-07-09','124 Paseo de Roxas, Makati','+639401234591','2026-03-05','Active','Job Order','Monthly',19500.00,20.00,'2345678901','123-456-789-001','34-2345678-9','12-345678901-3','4567-8901-2345',2,2,0,1.00);
/*!40000 ALTER TABLE `employees` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `holiday`
--

DROP TABLE IF EXISTS `holiday`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `holiday` (
  `holiday_id` int(11) NOT NULL AUTO_INCREMENT,
  `holiday_name` varchar(256) NOT NULL,
  `holiday_date` date NOT NULL,
  `holiday_type` varchar(32) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`holiday_id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `holiday`
--

LOCK TABLES `holiday` WRITE;
/*!40000 ALTER TABLE `holiday` DISABLE KEYS */;
INSERT INTO `holiday` VALUES (1,'New Year\'s Day','2026-01-01','REGULAR','2026-03-27 15:30:05','2026-03-27 16:36:10'),(2,'Lailatul Isra Wal Mi Raj','2026-01-16','SPECIAL_NON_WORKING','2026-03-27 15:30:05','2026-04-07 08:43:38'),(3,'Lunar New Year\'s Day','2026-02-17','REGULAR','2026-03-27 15:30:05','2026-03-27 15:30:05'),(4,'Ramadan Start','2026-02-19','SPECIAL_NON_WORKING','2026-03-27 15:30:05','2026-04-07 08:43:38'),(5,'People Power Anniversary','2026-02-25','SPECIAL_WORKING','2026-03-27 15:30:05','2026-03-27 15:37:06'),(6,'Eid al-Fitr Holiday','2026-03-20','REGULAR','2026-03-27 15:30:05','2026-04-07 08:43:38'),(7,'Eid al-Fitr','2026-03-21','REGULAR','2026-03-27 15:30:05','2026-04-07 08:43:38'),(8,'Maundy Thursday','2026-04-02','REGULAR','2026-03-27 15:30:05','2026-03-27 15:30:05'),(9,'Good Friday','2026-04-03','REGULAR','2026-03-27 15:30:05','2026-03-27 15:30:05'),(10,'Black Saturday','2026-04-04','SPECIAL_NON_WORKING','2026-03-27 15:30:05','2026-03-27 15:30:05'),(11,'Easter Sunday','2026-04-05','SPECIAL_NON_WORKING','2026-03-27 15:30:05','2026-04-07 08:43:38'),(12,'The Day of Valor','2026-04-09','REGULAR','2026-03-27 15:30:05','2026-04-07 08:43:38'),(13,'Labor Day','2026-05-01','REGULAR','2026-03-27 15:30:05','2026-03-27 15:30:05'),(14,'Eid al-Adha (tentative)','2026-05-27','REGULAR','2026-03-27 15:30:05','2026-04-07 08:43:38'),(15,'Eid al-Adha Day 2 (tentative)','2026-05-28','REGULAR','2026-03-27 15:30:05','2026-04-07 08:43:38'),(16,'Independence Day','2026-06-12','REGULAR','2026-03-27 15:30:05','2026-03-27 15:30:05'),(17,'Amun Jadid (tentative)','2026-06-17','SPECIAL_NON_WORKING','2026-03-27 15:30:05','2026-04-07 08:43:38'),(18,'Ninoy Aquino Day','2026-08-21','SPECIAL_NON_WORKING','2026-03-27 15:30:05','2026-03-27 15:30:05'),(19,'Maulid un-Nabi (tentative)','2026-08-26','SPECIAL_NON_WORKING','2026-03-27 15:30:05','2026-04-07 08:43:38'),(20,'National Heroes Day','2026-08-31','REGULAR','2026-03-27 15:30:05','2026-03-27 15:30:05'),(21,'All Saints\' Day','2026-11-01','SPECIAL_NON_WORKING','2026-03-27 15:30:05','2026-03-27 15:30:05'),(22,'All Souls\' Day','2026-11-02','SPECIAL_NON_WORKING','2026-03-27 15:30:05','2026-03-27 15:30:05'),(23,'Bonifacio Day','2026-11-30','REGULAR','2026-03-27 15:30:05','2026-03-27 15:30:05'),(24,'Feast of the Immaculate Conception','2026-12-08','SPECIAL_NON_WORKING','2026-03-27 15:30:05','2026-03-27 15:30:05'),(25,'Christmas Eve','2026-12-24','SPECIAL_NON_WORKING','2026-03-27 15:30:05','2026-03-27 15:30:05'),(26,'Christmas Day','2026-12-25','REGULAR','2026-03-27 15:30:05','2026-03-27 15:30:05'),(27,'Rizal Day','2026-12-30','REGULAR','2026-03-27 15:30:05','2026-03-27 15:30:05'),(28,'New Year\'s Eve','2026-12-31','REGULAR','2026-03-27 15:30:05','2026-03-27 15:30:05');
/*!40000 ALTER TABLE `holiday` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `leavebalance`
--

DROP TABLE IF EXISTS `leavebalance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `leavebalance` (
  `employeeId` int(11) NOT NULL,
  `leaveTypeId` int(11) NOT NULL,
  `balance` decimal(5,2) DEFAULT NULL,
  `remainingDays` decimal(5,2) NOT NULL DEFAULT 0.00,
  `totalDays` decimal(5,2) NOT NULL DEFAULT 15.00,
  PRIMARY KEY (`employeeId`,`leaveTypeId`),
  KEY `leaveTypeId` (`leaveTypeId`),
  CONSTRAINT `leavebalance_ibfk_1` FOREIGN KEY (`employeeId`) REFERENCES `employees` (`employeeId`),
  CONSTRAINT `leavebalance_ibfk_2` FOREIGN KEY (`leaveTypeId`) REFERENCES `leavetype` (`leaveTypeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `leavebalance`
--

LOCK TABLES `leavebalance` WRITE;
/*!40000 ALTER TABLE `leavebalance` DISABLE KEYS */;
INSERT INTO `leavebalance` VALUES (1,1,10.00,0.00,15.00),(1,2,15.00,0.00,15.00),(2,1,15.00,0.00,15.00),(2,2,15.00,0.00,15.00),(3,1,15.00,0.00,15.00),(3,2,15.00,0.00,15.00),(4,1,15.00,0.00,15.00),(4,2,15.00,0.00,15.00),(5,1,15.00,0.00,15.00),(5,2,15.00,0.00,15.00),(6,1,15.00,0.00,15.00),(6,2,15.00,0.00,15.00),(7,1,15.00,0.00,15.00),(7,2,15.00,0.00,15.00),(8,1,15.00,0.00,15.00),(8,2,15.00,0.00,15.00),(9,1,15.00,0.00,15.00),(9,2,15.00,0.00,15.00),(10,1,15.00,0.00,15.00),(10,2,15.00,0.00,15.00),(11,1,15.00,0.00,15.00),(11,2,15.00,0.00,15.00),(12,1,15.00,0.00,15.00),(12,2,15.00,0.00,15.00),(13,1,15.00,0.00,15.00),(13,2,15.00,0.00,15.00),(14,1,15.00,0.00,15.00),(14,2,15.00,0.00,15.00),(15,1,15.00,0.00,15.00),(15,2,15.00,0.00,15.00),(16,1,15.00,0.00,15.00),(16,2,15.00,0.00,15.00),(17,1,15.00,0.00,15.00),(17,2,15.00,0.00,15.00),(18,1,15.00,0.00,15.00),(18,2,15.00,0.00,15.00),(19,1,15.00,0.00,15.00),(19,2,15.00,0.00,15.00),(20,1,15.00,0.00,15.00),(20,2,15.00,0.00,15.00),(21,1,15.00,0.00,15.00),(21,2,15.00,0.00,15.00),(22,1,15.00,0.00,15.00),(22,2,15.00,0.00,15.00),(23,1,15.00,0.00,15.00),(23,2,15.00,0.00,15.00),(24,1,15.00,0.00,15.00),(24,2,15.00,0.00,15.00),(25,1,15.00,0.00,15.00),(25,2,15.00,0.00,15.00),(26,1,15.00,0.00,15.00),(26,2,15.00,0.00,15.00),(27,1,15.00,0.00,15.00),(27,2,15.00,0.00,15.00),(28,1,15.00,0.00,15.00),(28,2,15.00,0.00,15.00),(29,1,15.00,0.00,15.00),(29,2,15.00,0.00,15.00),(30,1,15.00,0.00,15.00),(30,2,15.00,0.00,15.00),(31,1,15.00,0.00,15.00),(31,2,15.00,0.00,15.00),(32,1,15.00,0.00,15.00),(32,2,15.00,0.00,15.00),(33,1,15.00,0.00,15.00),(33,2,15.00,0.00,15.00),(34,1,15.00,0.00,15.00),(34,2,15.00,0.00,15.00),(35,1,15.00,0.00,15.00),(35,2,15.00,0.00,15.00),(36,1,15.00,0.00,15.00),(36,2,15.00,0.00,15.00),(37,1,15.00,0.00,15.00),(37,2,15.00,0.00,15.00),(38,1,15.00,0.00,15.00),(38,2,15.00,0.00,15.00),(39,1,15.00,0.00,15.00),(39,2,15.00,0.00,15.00),(40,1,15.00,0.00,15.00),(40,2,15.00,0.00,15.00),(41,1,15.00,0.00,15.00),(41,2,15.00,0.00,15.00),(42,1,15.00,0.00,15.00),(42,2,15.00,0.00,15.00),(43,1,15.00,0.00,15.00),(43,2,15.00,0.00,15.00),(44,1,15.00,0.00,15.00),(44,2,15.00,0.00,15.00),(45,1,15.00,0.00,15.00),(45,2,15.00,0.00,15.00),(46,1,15.00,0.00,15.00),(46,2,15.00,0.00,15.00),(47,1,15.00,0.00,15.00),(47,2,15.00,0.00,15.00),(48,1,15.00,0.00,15.00),(48,2,15.00,0.00,15.00),(49,1,15.00,0.00,15.00),(49,2,15.00,0.00,15.00),(50,1,15.00,0.00,15.00),(50,2,15.00,0.00,15.00),(119,1,7.00,0.00,15.00),(119,2,15.00,0.00,15.00),(120,1,15.00,0.00,15.00),(120,2,15.00,0.00,15.00);
/*!40000 ALTER TABLE `leavebalance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `leaverequests`
--

DROP TABLE IF EXISTS `leaverequests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `leaverequests` (
  `leaveRequestId` int(11) NOT NULL AUTO_INCREMENT,
  `employeeId` int(11) DEFAULT NULL,
  `leaveTypeId` int(11) DEFAULT NULL,
  `startDate` date DEFAULT NULL,
  `endDate` date DEFAULT NULL,
  `status` enum('Pending','Approved','Rejected') DEFAULT NULL,
  `approved_by` int(11) DEFAULT NULL,
  `reason` varchar(500) NOT NULL DEFAULT 'No reason provided',
  `requestedDate` date NOT NULL DEFAULT curdate(),
  `respondedAt` datetime DEFAULT NULL,
  PRIMARY KEY (`leaveRequestId`),
  KEY `employeeId` (`employeeId`),
  KEY `leaveTypeId` (`leaveTypeId`),
  CONSTRAINT `leaverequests_ibfk_1` FOREIGN KEY (`employeeId`) REFERENCES `employees` (`employeeId`),
  CONSTRAINT `leaverequests_ibfk_2` FOREIGN KEY (`leaveTypeId`) REFERENCES `leavetype` (`leaveTypeId`)
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `leaverequests`
--

LOCK TABLES `leaverequests` WRITE;
/*!40000 ALTER TABLE `leaverequests` DISABLE KEYS */;
INSERT INTO `leaverequests` VALUES (1,2,1,'2026-02-26','2026-02-28','Approved',1,'Quarantine','2026-02-24',NULL),(2,2,1,'2026-03-04','2026-03-07','Rejected',1,'Appointment','2026-02-24',NULL),(3,119,1,'2026-03-27','2026-04-03','Approved',1,'Quarantine','2026-03-05','2026-03-05 13:28:04'),(4,119,2,'2026-03-27','2026-04-10','Approved',1,'Im sick ugh','2026-03-05','2026-03-05 13:29:06'),(5,119,2,'2026-03-21','2026-04-04','Approved',1,'sdfsdfsddsf','2026-03-05','2026-03-05 13:30:20'),(6,119,1,'2026-03-30','2026-04-03','Approved',1,'sfdfsdfsdf','2026-03-05','2026-03-05 13:35:16'),(7,119,1,'2026-03-22','2026-04-04','Rejected',1,'PLEASEPLEASEPLEASEPLEASEPLEASE','2026-03-05','2026-03-05 13:35:17'),(8,119,1,'2026-03-24','2026-04-09','Approved',1,'lksdfjslkdjfskdjfdsf','2026-03-05','2026-03-05 13:35:56'),(9,119,1,'2026-03-24','2026-04-03','Approved',1,'fdsfsdfsfsdf','2026-03-05','2026-03-05 13:37:58'),(10,119,2,'2026-03-22','2026-04-04','Approved',1,'ldsfjaldjfa;lsjdfjdasf','2026-03-05','2026-03-05 13:40:28'),(11,119,1,'2026-03-26','2026-04-10','Rejected',1,'DFSDFdfSFsfsdfs','2026-03-05','2026-03-05 13:41:26'),(12,119,1,'2026-03-29','2026-04-02','Rejected',1,'gfsdfsdfsfsdfsf','2026-03-05','2026-03-05 13:43:43'),(13,119,1,'2026-03-27','2026-04-11','Rejected',1,'dsfsdfsfdfsdf','2026-03-05','2026-03-05 13:46:56'),(14,119,1,'2026-04-04','2026-05-08','Approved',1,'ghfgfgdgdfgdfg','2026-03-05','2026-03-05 13:51:18'),(15,119,1,'2026-03-29','2026-04-03','Approved',1,'dfsfdfsfdfsdfsdf','2026-03-05','2026-03-05 13:56:24'),(16,119,1,'2026-03-28','2026-04-10','Approved',1,'sdfdsfsdfsdfsdfdsf','2026-03-05','2026-03-05 14:02:58'),(17,119,2,'2026-04-03','2026-05-09','Approved',1,'xsadasdasdaddasd','2026-03-05','2026-03-05 14:08:39'),(18,119,1,'2026-03-22','2026-03-28','Approved',1,'zxczxczczxczcx','2026-03-05','2026-03-05 14:13:51'),(19,119,2,'2026-03-29','2026-04-04','Rejected',1,'uiiopuiopuopuipuip','2026-03-05','2026-03-05 14:20:05'),(20,119,1,'2026-03-09','2026-03-28','Approved',1,'asdasdasdsadasdada','2026-03-05','2026-03-05 14:42:24'),(21,119,2,'2026-03-09','2026-03-21','Approved',1,'sdfsdfdfsdfsfsdf','2026-03-05','2026-03-05 15:07:14'),(22,119,2,'2026-04-10','2026-05-08','Approved',1,'ghfghfghgfhfghfhfhgfgh','2026-03-05','2026-03-05 15:32:31'),(23,119,2,'2026-03-22','2026-03-28','Approved',1,'ergergergegerg','2026-03-05','2026-03-05 15:32:32'),(24,119,1,'2026-03-05','2026-04-04','Approved',1,'xcxvdvdvdehrjm87om8l9;m','2026-03-05','2026-03-05 15:36:53'),(25,119,1,'2026-03-05','2026-04-03','Rejected',1,'dghghdghdhdfghdh','2026-03-05','2026-03-05 15:45:00'),(26,119,1,'2026-03-26','2026-04-04','Approved',1,'PLEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAASE','2026-03-05','2026-03-05 15:47:08'),(27,119,1,'2026-03-11','2026-03-14','Approved',1,'dfgsdgsgsgsdgsdgsdg','2026-03-05','2026-03-05 16:00:56'),(28,119,1,'2026-03-18','2026-03-21','Approved',1,'ipaidfpifapsidfid','2026-03-05','2026-03-05 16:03:15'),(29,1,1,'2026-03-26','2026-03-27','Rejected',1,'fjkfjkghkjgjkjhkgkgkgjhkghjkgkgjkghkjh','2026-03-05','2026-03-05 16:20:36'),(30,1,1,'2026-03-31','2026-04-02','Rejected',118,'gjdghjghjfghjgjgjfj','2026-03-05','2026-03-05 16:54:17'),(31,1,1,'2026-03-28','2026-04-11','Rejected',118,'sdgarhmid;0/i[IHO{o','2026-03-05','2026-03-05 16:54:18'),(32,1,1,'2026-04-07','2026-05-01','Rejected',118,'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','2026-03-05','2026-03-05 16:54:19'),(33,1,2,'2026-03-23','2026-03-27','Rejected',118,'poaipvaiepot iapwi api','2026-03-05','2026-03-05 16:54:19'),(34,1,1,'2026-03-31','2026-04-11','Rejected',118,'wofiowfiwofwofwoofwf','2026-03-05','2026-03-05 16:54:20'),(35,120,1,'2026-03-25','2026-04-10','Rejected',1,'adgagagadgadgadg','2026-03-05','2026-03-05 16:55:39'),(36,120,1,'2026-04-05','2026-04-23','Rejected',1,'agdsgdgagagsdg','2026-03-05','2026-03-05 16:56:19'),(37,120,1,'2026-03-26','2026-04-11','Rejected',1,'yarmymsrtmsrmsrmrsm','2026-03-05','2026-03-05 16:58:51'),(38,120,1,'2026-03-25','2026-04-04','Rejected',1,'47m4ms5serysneryns5s4i','2026-03-05','2026-03-05 17:04:09'),(39,120,1,'2026-04-08','2026-05-02','Rejected',1,'iuo43uqb9qu34b09u34b','2026-03-05','2026-03-05 17:05:30'),(40,120,2,'2026-04-05','2026-05-02','Rejected',1,'HIHIHIHIHIHIHIHI','2026-03-05','2026-03-05 17:06:29'),(41,1,1,'2026-04-06','2026-04-25','Rejected',118,'HOHOHOHOHOHOHO','2026-03-05','2026-03-05 17:08:34'),(42,1,1,'2026-03-22','2026-03-26','Rejected',118,'GHEHGEHGHEHGHG','2026-03-05','2026-03-05 17:09:15'),(43,1,1,'2026-03-21','2026-04-11','Rejected',118,'POPEOWPEOWPEOPEWO','2026-03-05','2026-03-05 17:16:57'),(44,1,2,'2026-03-30','2026-04-11','Rejected',118,'ayan4 a4345353453','2026-03-05','2026-03-05 17:17:45'),(45,1,1,'2026-04-08','2026-04-30','Rejected',118,'laPSPDPSDPDPP','2026-03-05','2026-03-05 17:32:06'),(46,1,1,'2026-03-07','2026-03-28','Rejected',118,'yearning szn','2026-03-07','2026-03-07 16:28:48'),(47,1,2,'2026-03-08','2026-03-14','Rejected',118,'im soooo tired ','2026-03-07','2026-03-07 16:28:58'),(48,120,1,'2026-03-08','2026-03-14','Rejected',118,'huhuhuhuhuhuhuhuhu','2026-03-07','2026-03-07 16:37:12'),(49,120,1,'2026-03-28','2026-04-10','Rejected',118,'HHAHAHAAAHAHAHAHAH','2026-03-07','2026-03-07 16:39:39');
/*!40000 ALTER TABLE `leaverequests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `leavetype`
--

DROP TABLE IF EXISTS `leavetype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `leavetype` (
  `leaveTypeId` int(11) NOT NULL AUTO_INCREMENT,
  `leaveName` varchar(50) DEFAULT NULL,
  `withPay` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`leaveTypeId`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `leavetype`
--

LOCK TABLES `leavetype` WRITE;
/*!40000 ALTER TABLE `leavetype` DISABLE KEYS */;
INSERT INTO `leavetype` VALUES (1,'Sick Leave',1),(2,'Vacation Leave',1);
/*!40000 ALTER TABLE `leavetype` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `overtime_request`
--

DROP TABLE IF EXISTS `overtime_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `overtime_request` (
  `overtime_request_id` int(11) NOT NULL AUTO_INCREMENT,
  `employeeId` int(11) NOT NULL,
  `work_date` date NOT NULL,
  `overtime_in` time NOT NULL,
  `overtime_out` time NOT NULL,
  `total_hours` decimal(10,2) NOT NULL,
  `reason` varchar(1000) NOT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'Pending',
  `requested_at` datetime(6) NOT NULL,
  `responded_at` datetime(6) DEFAULT NULL,
  `approved_by_user_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`overtime_request_id`),
  KEY `idx_ot_req_emp` (`employeeId`),
  KEY `idx_ot_req_status` (`status`),
  KEY `fk_ot_req_user` (`approved_by_user_id`),
  CONSTRAINT `fk_ot_req_employee` FOREIGN KEY (`employeeId`) REFERENCES `employees` (`employeeId`),
  CONSTRAINT `fk_ot_req_user` FOREIGN KEY (`approved_by_user_id`) REFERENCES `users` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `overtime_request`
--

LOCK TABLES `overtime_request` WRITE;
/*!40000 ALTER TABLE `overtime_request` DISABLE KEYS */;
/*!40000 ALTER TABLE `overtime_request` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pagibigtable`
--

DROP TABLE IF EXISTS `pagibigtable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pagibigtable` (
  `pagibigId` int(11) NOT NULL AUTO_INCREMENT,
  `rangeFrom` decimal(10,2) DEFAULT NULL,
  `rangeTo` decimal(10,2) DEFAULT NULL,
  `employeeShare` decimal(10,2) DEFAULT NULL,
  `employerShare` decimal(10,2) DEFAULT NULL,
  `effectiveYear` year(4) DEFAULT NULL,
  PRIMARY KEY (`pagibigId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pagibigtable`
--

LOCK TABLES `pagibigtable` WRITE;
/*!40000 ALTER TABLE `pagibigtable` DISABLE KEYS */;
/*!40000 ALTER TABLE `pagibigtable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `passwordresettokens`
--

DROP TABLE IF EXISTS `passwordresettokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `passwordresettokens` (
  `tokenId` int(11) NOT NULL AUTO_INCREMENT,
  `token` varchar(255) NOT NULL,
  `userId` int(11) NOT NULL,
  `expiryDate` datetime NOT NULL,
  PRIMARY KEY (`tokenId`),
  UNIQUE KEY `token` (`token`),
  KEY `fk_password_reset_user` (`userId`),
  CONSTRAINT `fk_password_reset_user` FOREIGN KEY (`userId`) REFERENCES `users` (`userId`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `passwordresettokens`
--

LOCK TABLES `passwordresettokens` WRITE;
/*!40000 ALTER TABLE `passwordresettokens` DISABLE KEYS */;
/*!40000 ALTER TABLE `passwordresettokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payroll`
--

DROP TABLE IF EXISTS `payroll`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `payroll` (
  `payrollId` int(11) NOT NULL AUTO_INCREMENT,
  `payPeriodStart` date DEFAULT NULL,
  `payPeriodEnd` date DEFAULT NULL,
  `payrollType` enum('Regular','13th Month','Final Pay') DEFAULT NULL,
  `status` enum('Draft','Processed','Released') DEFAULT NULL,
  `dateProcessed` date DEFAULT NULL,
  `dateReleased` date DEFAULT NULL,
  PRIMARY KEY (`payrollId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payroll`
--

LOCK TABLES `payroll` WRITE;
/*!40000 ALTER TABLE `payroll` DISABLE KEYS */;
/*!40000 ALTER TABLE `payroll` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payrollitems`
--

DROP TABLE IF EXISTS `payrollitems`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `payrollitems` (
  `payrollItemId` int(11) NOT NULL AUTO_INCREMENT,
  `payrollId` int(11) DEFAULT NULL,
  `employeeId` int(11) DEFAULT NULL,
  `basicPay` decimal(10,2) DEFAULT NULL,
  `overtimePay` decimal(10,2) DEFAULT NULL,
  `holidayPay` decimal(10,2) DEFAULT NULL,
  `allowances` decimal(10,2) DEFAULT NULL,
  `grossPay` decimal(10,2) DEFAULT NULL,
  `sss` decimal(10,2) DEFAULT NULL,
  `philhealth` decimal(10,2) DEFAULT NULL,
  `pagibig` decimal(10,2) DEFAULT NULL,
  `tax` decimal(10,2) DEFAULT NULL,
  `lateUndertimeDeduction` decimal(10,2) DEFAULT 0.00,
  `otherDeductions` decimal(10,2) DEFAULT NULL,
  `totalDeductions` decimal(10,2) DEFAULT NULL,
  `netPay` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`payrollItemId`),
  KEY `payrollId` (`payrollId`),
  KEY `employeeId` (`employeeId`),
  CONSTRAINT `payrollitems_ibfk_1` FOREIGN KEY (`payrollId`) REFERENCES `payroll` (`payrollId`),
  CONSTRAINT `payrollitems_ibfk_2` FOREIGN KEY (`employeeId`) REFERENCES `employees` (`employeeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payrollitems`
--

LOCK TABLES `payrollitems` WRITE;
/*!40000 ALTER TABLE `payrollitems` DISABLE KEYS */;
/*!40000 ALTER TABLE `payrollitems` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `philhealthtable`
--

DROP TABLE IF EXISTS `philhealthtable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `philhealthtable` (
  `philhealthId` int(11) NOT NULL AUTO_INCREMENT,
  `rangeFrom` decimal(10,2) DEFAULT NULL,
  `rangeTo` decimal(10,2) DEFAULT NULL,
  `employeeShare` decimal(10,2) DEFAULT NULL,
  `employerShare` decimal(10,2) DEFAULT NULL,
  `effectiveYear` year(4) DEFAULT NULL,
  PRIMARY KEY (`philhealthId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `philhealthtable`
--

LOCK TABLES `philhealthtable` WRITE;
/*!40000 ALTER TABLE `philhealthtable` DISABLE KEYS */;
/*!40000 ALTER TABLE `philhealthtable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `positions`
--

DROP TABLE IF EXISTS `positions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `positions` (
  `positionId` int(11) NOT NULL AUTO_INCREMENT,
  `positionName` varchar(100) NOT NULL,
  PRIMARY KEY (`positionId`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `positions`
--

LOCK TABLES `positions` WRITE;
/*!40000 ALTER TABLE `positions` DISABLE KEYS */;
INSERT INTO `positions` VALUES (1,'Manager'),(2,'Software Engineer'),(3,'Accountant'),(4,'HR Specialist'),(5,'Sales Representative'),(6,'Operations Staff');
/*!40000 ALTER TABLE `positions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `roles` (
  `roleId` int(11) NOT NULL AUTO_INCREMENT,
  `roleName` varchar(50) NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`roleId`),
  UNIQUE KEY `roleName` (`roleName`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'ADMIN','System administrator with full access'),(2,'EMPLOYEE','Regular employee with limited acess'),(3,'HR','Manage employee records'),(4,'PAYROLL','Process payroll'),(5,'MANAGER','Approve employee records'),(6,'SUPERVISOR','Approve employee leave');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shifts`
--

DROP TABLE IF EXISTS `shifts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shifts` (
  `shiftId` int(11) NOT NULL AUTO_INCREMENT,
  `shiftName` varchar(256) NOT NULL,
  `time_in` time DEFAULT NULL,
  `time_out` time DEFAULT NULL,
  `remarks` varchar(256) DEFAULT NULL,
  `monday` tinyint(1) NOT NULL DEFAULT 0,
  `tuesday` tinyint(1) NOT NULL DEFAULT 0,
  `wednesday` tinyint(1) NOT NULL DEFAULT 0,
  `thursday` tinyint(1) NOT NULL DEFAULT 0,
  `friday` tinyint(1) NOT NULL DEFAULT 0,
  `saturday` tinyint(1) NOT NULL DEFAULT 0,
  `sunday` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`shiftId`),
  UNIQUE KEY `shiftName` (`shiftName`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shifts`
--

LOCK TABLES `shifts` WRITE;
/*!40000 ALTER TABLE `shifts` DISABLE KEYS */;
INSERT INTO `shifts` VALUES (2,'normal','08:00:00','18:00:00',NULL,1,1,1,1,1,0,0);
/*!40000 ALTER TABLE `shifts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ssstable`
--

DROP TABLE IF EXISTS `ssstable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ssstable` (
  `sssId` int(11) NOT NULL AUTO_INCREMENT,
  `rangeFrom` decimal(10,2) DEFAULT NULL,
  `rangeTo` decimal(10,2) DEFAULT NULL,
  `employeeShare` decimal(10,2) DEFAULT NULL,
  `employerShare` decimal(10,2) DEFAULT NULL,
  `effectiveYear` year(4) DEFAULT NULL,
  PRIMARY KEY (`sssId`)
) ENGINE=InnoDB AUTO_INCREMENT=176 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ssstable`
--

LOCK TABLES `ssstable` WRITE;
/*!40000 ALTER TABLE `ssstable` DISABLE KEYS */;
INSERT INTO `ssstable` VALUES (1,0.00,4249.99,180.00,380.00,2024),(2,4250.00,4749.99,202.50,427.50,2024),(3,4750.00,5249.99,225.00,475.00,2024),(4,5250.00,5749.99,247.50,522.50,2024),(5,5750.00,6249.99,270.00,570.00,2024),(6,6250.00,6749.99,292.50,617.50,2024),(7,6750.00,7249.99,315.00,665.00,2024),(8,7250.00,7749.99,337.50,712.50,2024),(9,7750.00,8249.99,360.00,760.00,2024),(10,8250.00,8749.99,382.50,807.50,2024),(11,8750.00,9249.99,405.00,855.00,2024),(12,9250.00,9749.99,427.50,902.50,2024),(13,9750.00,10249.99,450.00,950.00,2024),(14,10250.00,10749.99,472.50,997.50,2024),(15,10750.00,11249.99,495.00,1045.00,2024),(16,11250.00,11749.99,517.50,1092.50,2024),(17,11750.00,12249.99,540.00,1140.00,2024),(18,12250.00,12749.99,562.50,1187.50,2024),(19,12750.00,13249.99,585.00,1235.00,2024),(20,13250.00,13749.99,607.50,1282.50,2024),(21,13750.00,14249.99,630.00,1330.00,2024),(22,14250.00,14749.99,652.50,1377.50,2024),(23,14750.00,15249.99,675.00,1425.00,2024),(24,15250.00,15749.99,697.50,1472.50,2024),(25,15750.00,16249.99,720.00,1520.00,2024),(26,16250.00,16749.99,742.50,1567.50,2024),(27,16750.00,17249.99,765.00,1615.00,2024),(28,17250.00,17749.99,787.50,1662.50,2024),(29,17750.00,18249.99,810.00,1710.00,2024),(30,18250.00,18749.99,832.50,1757.50,2024),(31,18750.00,19249.99,855.00,1805.00,2024),(32,19250.00,19749.99,877.50,1852.50,2024),(33,19750.00,20249.99,900.00,1900.00,2024),(34,20250.00,20749.99,922.50,1947.50,2024),(35,20750.00,21249.99,945.00,1995.00,2024),(36,21250.00,21749.99,967.50,2042.50,2024),(37,21750.00,22249.99,990.00,2090.00,2024),(38,22250.00,22749.99,1012.50,2137.50,2024),(39,22750.00,23249.99,1035.00,2185.00,2024),(40,23250.00,23749.99,1057.50,2232.50,2024),(41,23750.00,24249.99,1080.00,2280.00,2024),(42,24250.00,24749.99,1102.50,2327.50,2024),(43,24750.00,25249.99,1125.00,2375.00,2024),(44,25250.00,25749.99,1147.50,2422.50,2024),(45,25750.00,26249.99,1170.00,2470.00,2024),(46,26250.00,26749.99,1192.50,2517.50,2024),(47,26750.00,27249.99,1215.00,2565.00,2024),(48,27250.00,27749.99,1237.50,2612.50,2024),(49,27750.00,28249.99,1260.00,2660.00,2024),(50,28250.00,28749.99,1282.50,2707.50,2024),(51,28750.00,29249.99,1305.00,2755.00,2024),(52,29250.00,29749.99,1327.50,2802.50,2024),(53,29750.00,999999.99,1350.00,2850.00,2024),(54,0.00,5249.99,250.00,500.00,2025),(55,5250.00,5749.99,275.00,550.00,2025),(56,5750.00,6249.99,300.00,600.00,2025),(57,6250.00,6749.99,325.00,650.00,2025),(58,6750.00,7249.99,350.00,700.00,2025),(59,7250.00,7749.99,375.00,750.00,2025),(60,7750.00,8249.99,400.00,800.00,2025),(61,8250.00,8749.99,425.00,850.00,2025),(62,8750.00,9249.99,450.00,900.00,2025),(63,9250.00,9749.99,475.00,950.00,2025),(64,9750.00,10249.99,500.00,1000.00,2025),(65,10250.00,10749.99,525.00,1050.00,2025),(66,10750.00,11249.99,550.00,1100.00,2025),(67,11250.00,11749.99,575.00,1150.00,2025),(68,11750.00,12249.99,600.00,1200.00,2025),(69,12250.00,12749.99,625.00,1250.00,2025),(70,12750.00,13249.99,650.00,1300.00,2025),(71,13250.00,13749.99,675.00,1350.00,2025),(72,13750.00,14249.99,700.00,1400.00,2025),(73,14250.00,14749.99,725.00,1450.00,2025),(74,14750.00,15249.99,750.00,1500.00,2025),(75,15250.00,15749.99,775.00,1550.00,2025),(76,15750.00,16249.99,800.00,1600.00,2025),(77,16250.00,16749.99,825.00,1650.00,2025),(78,16750.00,17249.99,850.00,1700.00,2025),(79,17250.00,17749.99,875.00,1750.00,2025),(80,17750.00,18249.99,900.00,1800.00,2025),(81,18250.00,18749.99,925.00,1850.00,2025),(82,18750.00,19249.99,950.00,1900.00,2025),(83,19250.00,19749.99,975.00,1950.00,2025),(84,19750.00,20249.99,1000.00,2000.00,2025),(85,20250.00,20749.99,1025.00,2050.00,2025),(86,20750.00,21249.99,1050.00,2100.00,2025),(87,21250.00,21749.99,1075.00,2150.00,2025),(88,21750.00,22249.99,1100.00,2200.00,2025),(89,22250.00,22749.99,1125.00,2250.00,2025),(90,22750.00,23249.99,1150.00,2300.00,2025),(91,23250.00,23749.99,1175.00,2350.00,2025),(92,23750.00,24249.99,1200.00,2400.00,2025),(93,24250.00,24749.99,1225.00,2450.00,2025),(94,24750.00,25249.99,1250.00,2500.00,2025),(95,25250.00,25749.99,1275.00,2550.00,2025),(96,25750.00,26249.99,1300.00,2600.00,2025),(97,26250.00,26749.99,1325.00,2650.00,2025),(98,26750.00,27249.99,1350.00,2700.00,2025),(99,27250.00,27749.99,1375.00,2750.00,2025),(100,27750.00,28249.99,1400.00,2800.00,2025),(101,28250.00,28749.99,1425.00,2850.00,2025),(102,28750.00,29249.99,1450.00,2900.00,2025),(103,29250.00,29749.99,1475.00,2950.00,2025),(104,29750.00,30249.99,1500.00,3000.00,2025),(105,30250.00,30749.99,1525.00,3050.00,2025),(106,30750.00,31249.99,1550.00,3100.00,2025),(107,31250.00,31749.99,1575.00,3150.00,2025),(108,31750.00,32249.99,1600.00,3200.00,2025),(109,32250.00,32749.99,1625.00,3250.00,2025),(110,32750.00,33249.99,1650.00,3300.00,2025),(111,33250.00,33749.99,1675.00,3350.00,2025),(112,33750.00,34249.99,1700.00,3400.00,2025),(113,34250.00,34749.99,1725.00,3450.00,2025),(114,34750.00,999999.99,1750.00,3500.00,2025),(115,0.00,5249.99,250.00,500.00,2026),(116,5250.00,5749.99,275.00,550.00,2026),(117,5750.00,6249.99,300.00,600.00,2026),(118,6250.00,6749.99,325.00,650.00,2026),(119,6750.00,7249.99,350.00,700.00,2026),(120,7250.00,7749.99,375.00,750.00,2026),(121,7750.00,8249.99,400.00,800.00,2026),(122,8250.00,8749.99,425.00,850.00,2026),(123,8750.00,9249.99,450.00,900.00,2026),(124,9250.00,9749.99,475.00,950.00,2026),(125,9750.00,10249.99,500.00,1000.00,2026),(126,10250.00,10749.99,525.00,1050.00,2026),(127,10750.00,11249.99,550.00,1100.00,2026),(128,11250.00,11749.99,575.00,1150.00,2026),(129,11750.00,12249.99,600.00,1200.00,2026),(130,12250.00,12749.99,625.00,1250.00,2026),(131,12750.00,13249.99,650.00,1300.00,2026),(132,13250.00,13749.99,675.00,1350.00,2026),(133,13750.00,14249.99,700.00,1400.00,2026),(134,14250.00,14749.99,725.00,1450.00,2026),(135,14750.00,15249.99,750.00,1500.00,2026),(136,15250.00,15749.99,775.00,1550.00,2026),(137,15750.00,16249.99,800.00,1600.00,2026),(138,16250.00,16749.99,825.00,1650.00,2026),(139,16750.00,17249.99,850.00,1700.00,2026),(140,17250.00,17749.99,875.00,1750.00,2026),(141,17750.00,18249.99,900.00,1800.00,2026),(142,18250.00,18749.99,925.00,1850.00,2026),(143,18750.00,19249.99,950.00,1900.00,2026),(144,19250.00,19749.99,975.00,1950.00,2026),(145,19750.00,20249.99,1000.00,2000.00,2026),(146,20250.00,20749.99,1025.00,2050.00,2026),(147,20750.00,21249.99,1050.00,2100.00,2026),(148,21250.00,21749.99,1075.00,2150.00,2026),(149,21750.00,22249.99,1100.00,2200.00,2026),(150,22250.00,22749.99,1125.00,2250.00,2026),(151,22750.00,23249.99,1150.00,2300.00,2026),(152,23250.00,23749.99,1175.00,2350.00,2026),(153,23750.00,24249.99,1200.00,2400.00,2026),(154,24250.00,24749.99,1225.00,2450.00,2026),(155,24750.00,25249.99,1250.00,2500.00,2026),(156,25250.00,25749.99,1275.00,2550.00,2026),(157,25750.00,26249.99,1300.00,2600.00,2026),(158,26250.00,26749.99,1325.00,2650.00,2026),(159,26750.00,27249.99,1350.00,2700.00,2026),(160,27250.00,27749.99,1375.00,2750.00,2026),(161,27750.00,28249.99,1400.00,2800.00,2026),(162,28250.00,28749.99,1425.00,2850.00,2026),(163,28750.00,29249.99,1450.00,2900.00,2026),(164,29250.00,29749.99,1475.00,2950.00,2026),(165,29750.00,30249.99,1500.00,3000.00,2026),(166,30250.00,30749.99,1525.00,3050.00,2026),(167,30750.00,31249.99,1550.00,3100.00,2026),(168,31250.00,31749.99,1575.00,3150.00,2026),(169,31750.00,32249.99,1600.00,3200.00,2026),(170,32250.00,32749.99,1625.00,3250.00,2026),(171,32750.00,33249.99,1650.00,3300.00,2026),(172,33250.00,33749.99,1675.00,3350.00,2026),(173,33750.00,34249.99,1700.00,3400.00,2026),(174,34250.00,34749.99,1725.00,3450.00,2026),(175,34750.00,999999.99,1750.00,3500.00,2026);
/*!40000 ALTER TABLE `ssstable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `taxtable`
--

DROP TABLE IF EXISTS `taxtable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `taxtable` (
  `taxId` int(11) NOT NULL AUTO_INCREMENT,
  `compensationFrom` decimal(10,2) DEFAULT NULL,
  `compensationTo` decimal(10,2) DEFAULT NULL,
  `taxRate` decimal(5,2) DEFAULT NULL,
  `additionalTax` decimal(10,2) DEFAULT NULL,
  `effectiveYear` year(4) DEFAULT NULL,
  `pay_frequency` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`taxId`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `taxtable`
--

LOCK TABLES `taxtable` WRITE;
/*!40000 ALTER TABLE `taxtable` DISABLE KEYS */;
INSERT INTO `taxtable` VALUES (1,1.00,684.99,0.00,0.00,2026,'DAILY'),(2,685.00,1095.99,0.15,0.00,2026,'DAILY'),(3,1096.00,2191.99,0.20,61.65,2026,'DAILY'),(4,2192.00,5478.99,0.25,280.85,2026,'DAILY'),(5,5479.00,21917.99,0.30,1102.60,2026,'DAILY'),(6,21918.00,9999999.99,0.35,6034.00,2026,'DAILY'),(7,1.00,4807.99,0.00,0.00,2026,'WEEKLY'),(8,4808.00,7691.99,0.15,0.00,2026,'WEEKLY'),(9,7692.00,15384.99,0.20,432.60,2026,'WEEKLY'),(10,15385.00,38461.99,0.25,1971.20,2026,'WEEKLY'),(11,38462.00,153845.99,0.30,7740.45,2026,'WEEKLY'),(12,153846.00,9999999.99,0.35,42355.65,2026,'WEEKLY'),(13,1.00,10416.99,0.00,0.00,2026,'SEMI_MONTHLY'),(14,10417.00,16666.99,0.15,0.00,2026,'SEMI_MONTHLY'),(15,16667.00,33332.99,0.20,937.50,2026,'SEMI_MONTHLY'),(16,33333.00,83332.99,0.25,4270.70,2026,'SEMI_MONTHLY'),(17,83333.00,333332.99,0.30,16770.70,2026,'SEMI_MONTHLY'),(18,333333.00,9999999.99,0.35,91770.70,2026,'SEMI_MONTHLY'),(19,1.00,20832.99,0.00,0.00,2026,'MONTHLY'),(20,20833.00,33332.99,0.15,0.00,2026,'MONTHLY'),(21,33333.00,66666.99,0.20,1875.00,2026,'MONTHLY'),(22,66667.00,166666.99,0.25,8541.80,2026,'MONTHLY'),(23,166667.00,666666.99,0.30,33541.80,2026,'MONTHLY'),(24,666667.00,9999999.99,0.35,183541.80,2026,'MONTHLY'),(25,1.00,249999.99,0.00,0.00,2026,'ANNUALLY'),(26,250000.00,399999.99,0.15,0.00,2026,'ANNUALLY'),(27,400000.00,799999.99,0.20,22500.00,2026,'ANNUALLY'),(28,800000.00,1999999.99,0.25,102500.00,2026,'ANNUALLY'),(29,2000000.00,7999999.99,0.30,402500.00,2026,'ANNUALLY'),(30,8000000.00,9999999.99,0.35,2202500.00,2026,'ANNUALLY');
/*!40000 ALTER TABLE `taxtable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `userId` int(11) NOT NULL AUTO_INCREMENT,
  `employeeId` int(11) DEFAULT NULL,
  `passwordHash` varchar(255) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `roleId` int(11) NOT NULL,
  `isActive` tinyint(1) DEFAULT 1,
  `lastLogin` datetime DEFAULT NULL,
  `createdAt` datetime DEFAULT current_timestamp(),
  `updatedAt` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `lastLeaveViewedAt` datetime DEFAULT NULL,
  PRIMARY KEY (`userId`),
  UNIQUE KEY `email` (`email`),
  KEY `employeeId` (`employeeId`),
  KEY `roleId` (`roleId`),
  CONSTRAINT `users_ibfk_1` FOREIGN KEY (`employeeId`) REFERENCES `employees` (`employeeId`),
  CONSTRAINT `users_ibfk_2` FOREIGN KEY (`roleId`) REFERENCES `roles` (`roleId`)
) ENGINE=InnoDB AUTO_INCREMENT=120 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,1,'$2a$10$dWw1VsZoBcTjtzAPRXxLeeYq.2Ho7JvxdEumJW4r3HvlkEWJV3Lg6','juan.delacruz@company.com',1,1,'2026-03-13 14:00:10','2026-02-12 15:07:29','2026-03-13 14:00:10','2026-03-07 16:29:00'),(2,2,'$2a$10$5JaKgaqvomp6JOVDJcmg4uQsNEXAkq7eEV8jYlaglBIG1hHz5F2aS','maria.reyes@company.com',2,1,'2026-02-26 13:52:10','2026-02-12 15:07:29','2026-02-26 13:52:10',NULL),(5,3,'$2a$10$.gnIr6Gcqye.5UlIPVQWxe5sx/SSzYO6bCdlrUpwe9GaGF7VBE.3.','carlos.santos@company.com',2,1,NULL,'2026-02-28 00:49:47','2026-02-28 00:49:47',NULL),(6,4,'$2a$10$lunCK7ORDFMu9tG41FYZMef0tyWX831UcAG0ezhjZZfjC6gj1.lh.','sofia.castillo@company.com',2,1,NULL,'2026-02-28 00:49:47','2026-02-28 00:49:47',NULL),(7,5,'$2a$10$mUd.viFA7QXTaxlKfJRiZOyGU/sVTOqI/3vvhgE8WjLLZPd/RmIei','miguel.cruz@company.com',2,1,'2026-03-03 08:43:37','2026-02-28 00:49:47','2026-03-03 08:43:37',NULL),(8,6,'$2a$10$2QwF7foDuAVPQvdKaZUmQeeWMizs1qckNpWVqL5wQgziQvNyzNpRe','rosa.torres@company.com',2,1,NULL,'2026-02-28 00:49:47','2026-02-28 00:49:47',NULL),(9,7,'$2a$10$ArqjdKnzUOTFGecYT9OL9Od5OugjVgSHyp3uVhizCBdcIlR6FYM7m','luis.ramos@company.com',2,1,NULL,'2026-02-28 00:49:48','2026-02-28 00:49:48',NULL),(10,8,'$2a$10$8/496/Jwi.Br9x..UAICC./qICePQdjJMwWj8Ev3bh/j10HddFCg6','ana.bautista@company.com',2,1,NULL,'2026-02-28 00:49:48','2026-02-28 00:49:48',NULL),(11,9,'$2a$10$.ojvijuMYhFNRsePFpc9nOYzxTyPIzcKEKvaWWQg3J1COVY48LX3y','diego.villanueva@company.com',2,1,NULL,'2026-02-28 00:49:48','2026-02-28 00:49:48',NULL),(12,10,'$2a$10$eez.U/S6pjgc9w9Wzo9P1.Bix4VoshNDPJOyzgH8uIHY6.HiXLlxS','elena.gonzales@company.com',2,1,NULL,'2026-02-28 00:49:48','2026-02-28 00:49:48',NULL),(13,11,'$2a$10$SocVaJgWUQTYvTrpTQz5PuO5AH6PreDUrtY9VEgfOBGyvqKRIbx2i','pedro.santos@company.com',2,1,NULL,'2026-02-28 00:49:48','2026-02-28 00:49:48',NULL),(14,12,'$2a$10$n6T8YKPaJEfEHvU6ixmowe/7TDlxxcHL.9M7qOieSQ4yKpXBfjRQO','isabella.diaz@company.com',2,1,NULL,'2026-02-28 00:49:48','2026-02-28 00:49:48',NULL),(15,13,'$2a$10$Xj82VQeyXGbsxW/wnTXuXeHtiYn.Z4IW.j0JWJVeD3Lh3HMGV8P8K','gabriel.morales@company.com',2,1,NULL,'2026-02-28 00:49:48','2026-02-28 00:49:48',NULL),(16,14,'$2a$10$fi234t/LoOeDfg8.MKasG.uQIkPGd21ezVtMndV6Tiw7HxnbK7WXa','camila.navarro@company.com',2,1,NULL,'2026-02-28 00:49:48','2026-02-28 00:49:48',NULL),(17,15,'$2a$10$cWDTEvnSpfvdMUWGui2Ja.FBrecW2ZgwLM8RnlPIosaejQE3T5bAO','mateo.ortega@company.com',2,1,NULL,'2026-02-28 00:49:48','2026-02-28 00:49:48',NULL),(18,16,'$2a$10$hQXTUUyufOOKN4Mzn85s/e.YruF8aRnFDymGdH8R.hmRsPVPhqvuK','valentina.jimenez@company.com',2,1,NULL,'2026-02-28 00:49:48','2026-02-28 00:49:48',NULL),(19,17,'$2a$10$WtyepPIyCrOJ9K1iElGaZuCZRc5rGyvj7O0glAGfaUICAiq1iVSwe','santiago.ruiz@company.com',2,1,NULL,'2026-02-28 00:49:48','2026-02-28 00:49:48',NULL),(20,18,'$2a$10$.5vJ1HhKNHOvbCUi2rwgpOSR9WITrfbrD1350TkYoYPMPGj.mQ.Ha','lucia.herrera@company.com',2,1,NULL,'2026-02-28 00:49:48','2026-02-28 00:49:48',NULL),(21,19,'$2a$10$AeQHKaTwLtjcyG98VQuyF.IUEOSPJcNnn9buCuJBy3r5grbmQ2y/W','andres.medina@company.com',2,1,NULL,'2026-02-28 00:49:48','2026-02-28 00:49:48',NULL),(22,20,'$2a$10$5RWbmcsi/uZCnqmo/wAiQ.Kp7nAUmQ3GNyBwD34HJusa4SK56c80e','martina.castro@company.com',2,1,NULL,'2026-02-28 00:49:48','2026-02-28 00:49:48',NULL),(23,21,'$2a$10$3Rbmw5DSSoZEK5/8O4YJJOlylS7nuGWY3nBxgRk/OgtiBVaKW2rpO','ana.bautista1@company.com',2,1,NULL,'2026-02-28 00:49:48','2026-02-28 00:49:48',NULL),(24,22,'$2a$10$//5TyxYJZ5oc34hnuRJs4e26agsnbWLMldHEpTYeOT/MvH1xGi88.','ricardo.silva@company.com',2,1,NULL,'2026-02-28 00:49:49','2026-02-28 00:49:49',NULL),(25,23,'$2a$10$0gtuP.pHr7.rB3xMxgDPPusnl9qUHgFm7nBpBgM7vvoLAsuyJgq96','daniela.vargas@company.com',2,1,NULL,'2026-02-28 00:49:49','2026-02-28 00:49:49',NULL),(26,24,'$2a$10$/yCmY4nuA.mkaognEfvLheu.8bCscXQmjsmXWnjHrReth0DjbMH9C','fernando.romero@company.com',2,1,NULL,'2026-02-28 00:49:49','2026-02-28 00:49:49',NULL),(27,25,'$2a$10$JKdER1dmF7ZUOZDS9brXgO7nnvqqOoghkvFFvHU7PnXU/PgDIqrS2','patricia.gutierrez@company.com',2,1,NULL,'2026-02-28 00:49:49','2026-02-28 00:49:49',NULL),(28,26,'$2a$10$jRHNO3xywXgXqvrGWLtKb.yVowttPlu1DzyKdzkySDULUE1m9NJzm','alberto.alvarez@outlook.com',2,1,NULL,'2026-02-28 00:49:49','2026-03-03 15:12:28',NULL),(29,27,'$2a$10$zEquDH0UJ42LsXHo5IO8IeZHvph3SV1N96wHknd5hSXcf/86iVedq','carolina.flores@company.com',2,1,NULL,'2026-02-28 00:49:49','2026-02-28 00:49:49',NULL),(30,28,'$2a$10$eL5NBlnDHEm9PiOZcAraBe6tV2uldfOWw3wGI5wJQd6E9iRVasUN.','javier.dominguez@company.com',2,1,NULL,'2026-02-28 00:49:49','2026-02-28 00:49:49',NULL),(31,29,'$2a$10$wJNQ6YSitz1muVVINAm6v.xmupQP8l9r83OFgCEA/ewBFjmB9GdqS','monica.ramirez@company.com',2,1,NULL,'2026-02-28 00:49:49','2026-02-28 00:49:49',NULL),(32,30,'$2a$10$dvtrxYk6/7B8O52vF2hv8uO0ZSLbkgrI4/hFoD7kIrRepntccQw2.','rodrigo.moreno@company.com',2,1,NULL,'2026-02-28 00:49:49','2026-02-28 00:49:49',NULL),(33,31,'$2a$10$9T8kMiNNONrl7OcwN9Yoc.umhi2o1ePK8xzry3GpFDLBDLHRrlrKi','carlos.fernandez@company.com',2,1,NULL,'2026-02-28 00:49:49','2026-02-28 00:49:49',NULL),(34,32,'$2a$10$ntYy.F0zwNMzXvB17uxyj.v4ca6RX5tKA/y4XmsUzM.nMqEK/00F6','beatriz.soto@company.com',2,1,NULL,'2026-02-28 00:49:49','2026-02-28 00:49:49',NULL),(35,33,'$2a$10$0PLgLtJrJXmqBxZDvoDqietTrgAjt4Ul.pISH91d/N2p0zJFhihZW','emilio.pena@company.com',2,1,NULL,'2026-02-28 00:49:49','2026-02-28 00:49:49',NULL),(36,34,'$2a$10$pLyLftEcZP81vGX3L28gCek82dj5rXqV9Re/Z2q73ljs6hSfSZZkq','adriana.aguilar@company.com',2,1,NULL,'2026-02-28 00:49:49','2026-02-28 00:49:49',NULL),(37,35,'$2a$10$btr72paMo1VT4POi6mwLEuegjldiEkM7hYwNaXC0vDcrUzpQywR3i','francisco.vega@company.com',2,1,NULL,'2026-02-28 00:49:49','2026-02-28 00:49:49',NULL),(38,36,'$2a$10$9oS3yvZd9rI3zL6qqRm.JeyUSvhuT12HC.eCLrwZorNUN1.ZQV5Km','gabriela.rios@company.com',2,1,NULL,'2026-02-28 00:49:49','2026-02-28 00:49:49',NULL),(39,37,'$2a$10$.VQt8Ck7EyuJu2gycrvyKeko7hq3j8nA.sYd0WcVoNOFTZ/j8TW6a','hector.mendez@company.com',2,1,NULL,'2026-02-28 00:49:50','2026-02-28 00:49:50',NULL),(40,38,'$2a$10$4v/S6d0i5lEY/pKxE3tPCuj5CYdbgYURoTPdNFhQGGUXJHeLata4e','isabel.ortiz@company.com',2,1,NULL,'2026-02-28 00:49:50','2026-02-28 00:49:50',NULL),(41,39,'$2a$10$xGBMkDqzO9xx5E.8zz7nt.IiTWjf20AyuFqgEjWZYkFK1IvsR8PKO','jorge.nunez@company.com',2,1,NULL,'2026-02-28 00:49:50','2026-02-28 00:49:50',NULL),(42,40,'$2a$10$ePlFDM/A7HdxNxbK7gOSKeYDkWrFzGug3b9TbRdVFOV.b/x4xZBeG','laura.castillo@company.com',2,1,NULL,'2026-02-28 00:49:50','2026-02-28 00:49:50',NULL),(43,41,'$2a$10$2YR8DdlT6bC2jsyX7SjaXedZnP3NzKQJh2S0smxR.WtzHi4Nnfo5.','miguel.villanueva@company.com',2,1,NULL,'2026-02-28 00:49:50','2026-02-28 00:49:50',NULL),(44,42,'$2a$10$BAd0z5LcbZ0GZzfbpX0pReAYidh.QY3STo5VxV8OXHF9nsaKEsru.','natalia.guerrero@company.com',2,1,NULL,'2026-02-28 00:49:50','2026-02-28 00:49:50',NULL),(45,43,'$2a$10$tmeZU2p2ssYL7mll22R25.FWSePHnpLCjCCFQWnJsATvteZl8MGOi','oscar.rojas@company.com',2,1,NULL,'2026-02-28 00:49:50','2026-02-28 00:49:50',NULL),(46,44,'$2a$10$vLIUsh4cnnhIwJV8gPb3X.2lRfUrXhJp9Kgx5ZXewP7huQTLBVhHa','paula.molina@company.com',2,1,NULL,'2026-02-28 00:49:50','2026-02-28 00:49:50',NULL),(47,45,'$2a$10$sLNWrtYbPlJlal/XmicQmeFvzUomW5jzKIwDnTJ/ZlL0fOM8tpJ1e','raul.campos@company.com',2,1,NULL,'2026-02-28 00:49:50','2026-02-28 00:49:50',NULL),(48,46,'$2a$10$IFBfJ5I1WjC.cP/fhKUkk.YyXt45DnuLw57Dy8TCK3ERlkDVyx9vC','sandra.paredes@company.com',2,1,NULL,'2026-02-28 00:49:50','2026-02-28 00:49:50',NULL),(49,47,'$2a$10$h7SQ9oBP8L97alVMC9kIxOA8Rfobo2DO8MgtHxdHbCMn98bvLbide','tomas.delgado@company.com',2,1,NULL,'2026-02-28 00:49:50','2026-02-28 00:49:50',NULL),(50,48,'$2a$10$P.2cIortx94l.VrlW4P1Mu/X5ZDwjnT01BXyDbrFwFkFPlFxcdBIG','veronica.fuentes@company.com',2,1,NULL,'2026-02-28 00:49:50','2026-02-28 00:49:50',NULL),(51,49,'$2a$10$ZG42SnFH8JBVqMxO1/5rduMxfOK8bI/.a8iGMuNOsXKgbhRjTHA8y','xavier.salazar@company.com',2,1,NULL,'2026-02-28 00:49:50','2026-02-28 00:49:50',NULL),(52,50,'$2a$10$HgptzTcD6QMisQD59ByHhuGPqAQ0bMQTLE2Lihkvfh3E6sW.g4rHK','yolanda.cortez@company.com',2,1,NULL,'2026-02-28 00:49:50','2026-02-28 00:49:50',NULL),(118,119,'$2a$10$6nE7MMXXWUWBnZLgJ6gRquCTeNOOcK7TQ7hes3obEO8vJfyjqafg6','diligcharlene@gmail.com',1,1,'2026-05-12 11:52:52','2026-03-05 09:44:36','2026-05-12 11:52:52','2026-03-31 14:10:41'),(119,120,'$2a$10$C45WVE9Hvcs5kMpnF7fw7OGNbqpaw4hgiNeRIOlfXahO6CV11k/SG','charlenedilig@gmail.com',2,1,'2026-03-07 16:35:40','2026-03-05 09:59:12','2026-03-07 16:39:58','2026-03-07 16:39:58');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `weekly_schedule_template`
--

DROP TABLE IF EXISTS `weekly_schedule_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `weekly_schedule_template` (
  `template_id` int(11) NOT NULL AUTO_INCREMENT,
  `template_name` varchar(256) NOT NULL,
  `schedule_year` int(11) NOT NULL,
  `schedule_month` tinyint(4) NOT NULL,
  PRIMARY KEY (`template_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `weekly_schedule_template`
--

LOCK TABLES `weekly_schedule_template` WRITE;
/*!40000 ALTER TABLE `weekly_schedule_template` DISABLE KEYS */;
INSERT INTO `weekly_schedule_template` VALUES (3,'it march',2026,3);
/*!40000 ALTER TABLE `weekly_schedule_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `weekly_schedule_template_day`
--

DROP TABLE IF EXISTS `weekly_schedule_template_day`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `weekly_schedule_template_day` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `template_id` int(11) NOT NULL,
  `day_of_week` tinyint(4) NOT NULL,
  `is_rest_day` tinyint(1) NOT NULL DEFAULT 0,
  `time_in` time DEFAULT NULL,
  `time_out` time DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ws_tpl_dow` (`template_id`,`day_of_week`),
  CONSTRAINT `fk_ws_tpl_day_template` FOREIGN KEY (`template_id`) REFERENCES `weekly_schedule_template` (`template_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `weekly_schedule_template_day`
--

LOCK TABLES `weekly_schedule_template_day` WRITE;
/*!40000 ALTER TABLE `weekly_schedule_template_day` DISABLE KEYS */;
/*!40000 ALTER TABLE `weekly_schedule_template_day` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-12 14:11:06
