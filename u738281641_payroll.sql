-- phpMyAdmin SQL Dump
-- version 5.2.2
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: May 28, 2026 at 03:37 AM
-- Server version: 11.8.6-MariaDB-log
-- PHP Version: 7.2.34

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `u738281641_payroll`
--
CREATE DATABASE IF NOT EXISTS `u738281641_payroll` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `u738281641_payroll`;

-- --------------------------------------------------------

--
-- Table structure for table `adjustments`
--

DROP TABLE IF EXISTS `adjustments`;
CREATE TABLE `adjustments` (
  `adjustmentId` int(11) NOT NULL,
  `adjustmentName` varchar(255) DEFAULT NULL,
  `adjustmentType` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `adjustments`
--

INSERT INTO `adjustments` (`adjustmentId`, `adjustmentName`, `adjustmentType`) VALUES
(1, 'Mid-year Bonus', 'Earnings');

-- --------------------------------------------------------

--
-- Table structure for table `attendance`
--

DROP TABLE IF EXISTS `attendance`;
CREATE TABLE `attendance` (
  `attendanceId` int(11) NOT NULL,
  `employeeId` int(11) NOT NULL,
  `attendance_date` date DEFAULT NULL,
  `time_in` time DEFAULT NULL,
  `time_out` time DEFAULT NULL,
  `work_hours` decimal(38,2) NOT NULL,
  `late_minutes` int(11) DEFAULT 0,
  `undertime_minutes` int(11) DEFAULT 0,
  `overtime_hours` decimal(38,2) NOT NULL,
  `status` enum('Present','Absent','Late','Leave','Holiday') DEFAULT NULL,
  `shift_override` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `attendance`
--

INSERT INTO `attendance` (`attendanceId`, `employeeId`, `attendance_date`, `time_in`, `time_out`, `work_hours`, `late_minutes`, `undertime_minutes`, `overtime_hours`, `status`, `shift_override`) VALUES
(323, 4, '2026-05-04', '08:03:00', '17:26:00', 8.00, 0, 0, 0.27, 'Present', '08:10 - 17:10'),
(324, 4, '2026-05-05', '07:40:00', '17:13:00', 8.00, 0, 0, 0.05, 'Present', '08:10 - 17:10'),
(325, 5, '2026-05-04', '10:16:00', '18:00:00', 6.73, 66, 0, 0.00, 'Present', NULL),
(326, 5, '2026-05-05', '08:48:00', '18:12:00', 8.00, 0, 0, 0.03, 'Present', NULL),
(327, 15, '2026-05-01', '07:54:00', '22:22:00', 7.83, 0, 0, 5.37, 'Present', NULL),
(328, 15, '2026-05-04', '17:47:00', '17:47:00', 0.00, 0, 0, 0.00, 'Present', NULL),
(329, 15, '2026-05-05', '07:50:00', '17:47:00', 7.83, 0, 0, 0.78, 'Present', NULL),
(330, 48, '2026-05-01', '08:38:00', '19:22:00', 7.83, 0, 0, 1.37, 'Present', NULL),
(331, 48, '2026-05-04', '08:51:00', '19:18:00', 7.83, 0, 0, 1.30, 'Present', NULL),
(332, 48, '2026-05-05', '08:52:00', '18:31:00', 7.83, 0, 0, 0.52, 'Present', NULL),
(333, 32, '2026-05-03', '06:48:00', '21:34:00', 11.83, 0, 0, 0.07, 'Present', NULL),
(334, 32, '2026-05-04', '07:19:00', '21:34:00', 11.83, 0, 0, 0.07, 'Present', NULL),
(335, 32, '2026-05-05', '13:23:00', '21:35:00', 6.83, 0, 0, 0.08, 'Present', NULL),
(336, 31, '2026-05-02', '07:51:00', '21:33:00', 13.70, 0, 0, 0.00, 'Present', NULL),
(337, 31, '2026-05-03', '07:48:00', '21:33:00', 11.83, 0, 0, 0.05, 'Present', NULL),
(338, 44, '2026-05-01', '05:19:00', '20:51:00', 7.83, 0, 0, 5.85, 'Present', NULL),
(339, 44, '2026-05-04', '05:25:00', '20:46:00', 7.83, 0, 0, 5.77, 'Present', NULL),
(340, 44, '2026-05-05', '05:29:00', '01:12:00', 7.83, 0, 0, 10.20, 'Present', NULL),
(341, 53, '2026-05-01', '08:26:00', '21:37:00', 11.83, 0, 0, 0.12, 'Present', NULL),
(342, 53, '2026-05-04', '08:07:00', '21:35:00', 13.47, 0, 0, 0.00, 'Present', NULL),
(343, 53, '2026-05-05', '08:08:00', '21:38:00', 5.83, 0, 0, 6.13, 'Present', NULL),
(344, 51, '2026-05-03', '07:04:00', '21:34:00', 11.83, 0, 0, 0.07, 'Present', NULL),
(345, 51, '2026-05-04', '07:14:00', '21:33:00', 11.83, 0, 0, 0.05, 'Present', NULL),
(346, 51, '2026-05-05', '13:21:00', '21:34:00', 6.83, 0, 0, 0.07, 'Present', NULL),
(347, 8, '2026-05-03', '07:40:00', '21:49:00', 12.00, 0, 0, 0.00, 'Present', NULL),
(348, 8, '2026-05-04', '07:37:00', '21:48:00', 12.00, 0, 0, 0.00, 'Present', NULL),
(349, 8, '2026-05-05', '14:30:00', '00:28:00', 6.83, 0, 0, 0.00, 'Present', NULL),
(350, 18, '2026-05-01', '08:28:00', '21:35:00', 11.83, 0, 0, 0.08, 'Present', NULL),
(351, 18, '2026-05-02', '21:34:00', '21:34:00', 0.00, 0, 0, 0.00, 'Present', NULL),
(352, 18, '2026-05-05', '08:33:00', '15:30:00', 5.83, 0, 0, 0.00, 'Present', NULL),
(353, 25, '2026-05-01', '07:37:00', '21:40:00', 11.83, 0, 0, 0.17, 'Present', NULL),
(354, 25, '2026-05-02', '07:43:00', '21:55:00', 11.83, 0, 0, 0.42, 'Present', NULL),
(355, 25, '2026-05-05', '06:39:00', '15:30:00', 5.83, 0, 0, 0.00, 'Present', NULL),
(356, 17, '2026-05-01', '08:33:00', '21:35:00', 11.83, 0, 0, 0.08, 'Present', NULL),
(357, 17, '2026-05-02', '07:59:00', '21:39:00', 11.83, 0, 0, 0.15, 'Present', NULL),
(358, 17, '2026-05-05', '07:44:00', '15:30:00', 0.83, 0, 360, 0.00, 'Present', NULL),
(359, 37, '2026-05-01', '08:02:00', '21:31:00', 11.83, 0, 0, 0.02, 'Present', NULL),
(360, 37, '2026-05-02', '08:02:00', '21:31:00', 11.83, 0, 0, 0.02, 'Present', NULL),
(361, 37, '2026-05-05', '08:15:00', '15:30:00', 5.83, 0, 0, 0.00, 'Present', NULL),
(362, 27, '2026-05-03', '08:06:00', '21:33:00', 11.83, 0, 0, 0.05, 'Present', NULL),
(363, 27, '2026-05-04', '08:00:00', '21:33:00', 11.83, 0, 0, 0.05, 'Present', NULL),
(364, 27, '2026-05-05', '14:02:00', '21:33:00', 6.83, 0, 0, 0.05, 'Present', NULL),
(365, 40, '2026-05-01', '09:30:00', '19:01:00', 7.83, 0, 0, 0.02, 'Present', NULL),
(366, 40, '2026-05-05', '10:48:00', '19:12:00', 7.20, 38, 0, 0.20, 'Present', NULL),
(367, 12, '2026-05-03', '07:41:00', '21:30:00', 11.83, 0, 0, 0.00, 'Present', NULL),
(368, 12, '2026-05-04', '07:59:00', '21:30:00', 11.83, 0, 0, 0.00, 'Present', NULL),
(369, 12, '2026-05-05', '08:04:00', '15:30:00', 0.83, 0, 360, 0.00, 'Present', NULL),
(370, 39, '2026-05-01', '08:28:00', '21:32:00', 11.83, 0, 0, 0.03, 'Present', NULL),
(371, 4, '2026-05-06', '07:03:00', '17:12:00', 8.00, 0, 0, 0.20, 'Present', '08:10 - 17:10'),
(372, 4, '2026-05-07', '07:09:00', '17:34:00', 8.00, 0, 0, 0.57, 'Present', '08:10 - 17:10'),
(373, 4, '2026-05-11', '07:23:00', '17:16:00', 8.00, 0, 0, 0.27, 'Present', '08:10 - 17:10'),
(374, 4, '2026-05-12', '07:05:00', '18:19:00', 8.00, 0, 0, 1.32, 'Present', '08:10 - 17:10'),
(375, 4, '2026-05-13', '06:57:00', '17:31:00', 8.00, 0, 0, 0.52, 'Present', '08:10 - 17:10'),
(376, 4, '2026-05-14', '06:58:00', '17:34:00', 8.00, 0, 0, 0.57, 'Present', '08:10 - 17:10'),
(377, 5, '2026-05-06', '08:44:00', '18:18:00', 8.00, 0, 0, 0.13, 'Present', NULL),
(378, 5, '2026-05-07', '08:54:00', '18:16:00', 8.00, 0, 0, 0.10, 'Present', NULL),
(379, 5, '2026-05-08', '08:56:00', '18:16:00', 8.00, 0, 0, 0.10, 'Present', NULL),
(380, 5, '2026-05-11', '08:52:00', '18:10:00', 8.00, 0, 0, 0.00, 'Present', NULL),
(381, 5, '2026-05-12', '08:51:00', '18:21:00', 8.00, 0, 0, 0.18, 'Present', NULL),
(382, 5, '2026-05-13', '08:52:00', '18:12:00', 8.00, 0, 0, 0.03, 'Present', NULL),
(383, 5, '2026-05-15', '08:48:00', '18:10:00', 8.00, 0, 0, 0.00, 'Present', NULL),
(384, 8, '2026-05-06', '14:05:00', '21:39:00', 6.83, 0, 0, 0.15, 'Present', NULL),
(385, 8, '2026-05-07', '13:36:00', '21:47:00', 6.83, 0, 0, 0.28, 'Present', NULL),
(386, 8, '2026-05-10', '08:14:00', '21:47:00', 12.00, 0, 0, 0.12, 'Present', NULL),
(387, 8, '2026-05-11', '07:35:00', '21:41:00', 12.00, 0, 0, 0.02, 'Present', NULL),
(388, 8, '2026-05-12', '14:19:00', '21:40:00', 6.83, 0, 0, 0.00, 'Present', NULL),
(389, 8, '2026-05-13', '13:48:00', '21:49:00', 6.83, 0, 0, 0.00, 'Present', NULL),
(390, 8, '2026-05-14', '14:21:00', '21:49:00', 6.83, 0, 0, 0.00, 'Present', NULL),
(391, 29, '2026-05-01', '08:34:00', '17:13:00', 7.55, 0, 17, 0.00, 'Present', NULL),
(392, 29, '2026-05-04', '08:27:00', '17:53:00', 7.83, 0, 0, 0.38, 'Present', NULL),
(393, 29, '2026-05-05', '08:35:00', '17:25:00', 7.75, 0, 5, 0.00, 'Present', NULL),
(394, 29, '2026-05-06', '08:35:00', '17:29:00', 7.82, 0, 11, 0.00, 'Present', NULL),
(395, 29, '2026-05-07', '08:33:00', '17:26:00', 7.77, 0, 14, 0.00, 'Present', NULL),
(396, 29, '2026-05-08', '08:29:00', '21:35:00', 8.00, 0, 0, 3.92, 'Present', NULL),
(397, 29, '2026-05-11', '08:30:00', '17:45:00', 8.00, 0, 0, 0.25, 'Present', NULL),
(398, 29, '2026-05-12', '08:39:00', '18:05:00', 7.83, 0, 0, 0.58, 'Present', NULL),
(399, 29, '2026-05-13', '12:40:00', '17:31:00', 4.50, 200, 0, 0.02, 'Present', NULL),
(400, 29, '2026-05-14', '08:32:00', '17:13:00', 7.55, 0, 17, 0.00, 'Present', NULL),
(401, 29, '2026-05-15', '12:51:00', '17:14:00', 4.23, 200, 16, 0.00, 'Present', NULL),
(402, 9, '2026-05-05', '07:31:00', '17:45:00', 8.00, 0, 0, 0.58, 'Present', NULL),
(403, 9, '2026-05-06', '07:25:00', '17:28:00', 8.00, 0, 0, 0.30, 'Present', NULL),
(404, 9, '2026-05-07', '07:56:00', '17:11:00', 8.00, 0, 0, 0.02, 'Present', NULL),
(405, 9, '2026-05-12', '07:37:00', '16:58:00', 7.80, 0, 12, 1.02, 'Present', NULL),
(406, 9, '2026-05-13', '07:43:00', '17:54:00', 8.00, 0, 0, 0.73, 'Present', NULL),
(407, 9, '2026-05-14', '07:15:00', '17:20:00', 8.00, 0, 0, 0.17, 'Present', NULL),
(408, 9, '2026-05-01', '17:08:00', '17:15:00', 0.03, 478, 0, 0.08, 'Present', NULL),
(409, 9, '2026-05-08', '07:37:00', '17:14:00', 8.00, 0, 0, 0.07, 'Present', NULL),
(410, 9, '2026-05-15', '07:46:00', '17:13:00', 8.00, 0, 0, 0.05, 'Present', NULL),
(411, 15, '2026-05-06', '07:44:00', '17:46:00', 8.00, 0, 0, 0.60, 'Present', NULL),
(412, 15, '2026-05-07', '07:56:00', '18:01:00', 8.00, 0, 0, 0.85, 'Present', NULL),
(413, 48, '2026-05-06', '09:13:00', '20:01:00', 7.95, 3, 0, 1.85, 'Present', NULL),
(414, 48, '2026-05-07', '08:54:00', '18:39:00', 8.00, 0, 0, 0.48, 'Present', NULL),
(415, 48, '2026-05-08', '08:58:00', '19:05:00', 8.00, 0, 0, 0.92, 'Present', NULL),
(416, 32, '2026-05-06', '13:14:00', '21:34:00', 6.83, 0, 0, 0.07, 'Present', NULL),
(417, 32, '2026-05-07', '13:40:00', '21:33:00', 6.83, 0, 0, 0.05, 'Present', NULL),
(418, 32, '2026-05-10', '06:58:00', '21:34:00', 11.90, 0, 6, 0.00, 'Present', NULL),
(419, 31, '2026-05-06', '07:55:00', '21:35:00', 6.83, 0, 0, 0.08, 'Present', NULL),
(420, 31, '2026-05-07', '13:44:00', '21:34:00', 6.83, 0, 0, 0.07, 'Present', NULL),
(421, 31, '2026-05-09', '07:51:00', '21:33:00', 13.70, 0, 0, 0.00, 'Present', NULL),
(422, 31, '2026-05-10', '07:49:00', '21:33:00', 11.88, 0, 7, 0.00, 'Present', NULL),
(423, 44, '2026-05-06', '05:46:00', '20:00:00', 8.00, 0, 0, 4.83, 'Present', NULL),
(424, 44, '2026-05-07', '05:40:00', '20:17:00', 8.00, 0, 0, 5.12, 'Present', NULL),
(425, 44, '2026-05-08', '05:35:00', '05:35:00', 0.00, 0, 0, 0.00, 'Present', NULL),
(426, 53, '2026-05-07', '08:08:00', '15:41:00', 6.00, 0, 0, 0.02, 'Present', NULL),
(427, 53, '2026-05-08', '08:08:00', '21:46:00', 12.00, 0, 0, 0.10, 'Present', NULL),
(428, 51, '2026-05-06', '13:02:00', '21:43:00', 6.83, 0, 0, 0.22, 'Present', NULL),
(429, 51, '2026-05-07', '12:54:00', '21:41:00', 6.83, 0, 0, 0.18, 'Present', NULL),
(430, 51, '2026-05-10', '07:18:00', '21:43:00', 12.00, 0, 0, 0.05, 'Present', NULL),
(431, 18, '2026-05-06', '08:25:00', '15:40:00', 6.00, 0, 0, 0.00, 'Present', NULL),
(432, 18, '2026-05-07', '08:31:00', '15:40:00', 6.00, 0, 0, 0.00, 'Present', NULL),
(433, 18, '2026-05-08', '08:30:00', '21:43:00', 12.00, 0, 0, 0.05, 'Present', NULL),
(434, 18, '2026-05-09', '08:34:00', '21:43:00', 12.00, 0, 0, 0.05, 'Present', NULL),
(435, 25, '2026-05-06', '06:40:00', '15:30:00', 5.83, 0, 10, 0.00, 'Present', NULL),
(436, 25, '2026-05-07', '06:59:00', '15:30:00', 5.83, 0, 10, 0.00, 'Present', NULL),
(437, 25, '2026-05-08', '07:04:00', '21:38:00', 11.97, 0, 2, 0.00, 'Present', NULL),
(438, 25, '2026-05-09', '06:55:00', '21:41:00', 12.00, 0, 0, 0.02, 'Present', NULL),
(439, 17, '2026-05-06', '08:15:00', '15:30:00', 0.83, 0, 370, 0.00, 'Present', NULL),
(440, 17, '2026-05-07', '08:15:00', '15:38:00', 0.97, 0, 362, 0.00, 'Present', NULL),
(441, 17, '2026-05-08', '08:14:00', '21:34:00', 11.90, 0, 6, 0.00, 'Present', NULL),
(442, 17, '2026-05-09', '08:07:00', '21:35:00', 11.92, 0, 5, 0.00, 'Present', NULL),
(443, 37, '2026-05-06', '08:21:00', '15:40:00', 6.00, 0, 0, 0.00, 'Present', NULL),
(444, 37, '2026-05-07', '08:09:00', '15:40:00', 6.00, 0, 0, 0.00, 'Present', NULL),
(445, 37, '2026-05-08', '08:04:00', '21:41:00', 12.00, 0, 0, 0.02, 'Present', NULL),
(446, 37, '2026-05-09', '08:14:00', '21:41:00', 12.00, 0, 0, 0.02, 'Present', NULL),
(447, 27, '2026-05-06', '13:48:00', '21:43:00', 6.83, 0, 0, 0.22, 'Present', NULL),
(448, 27, '2026-05-07', '13:54:00', '21:42:00', 6.83, 0, 0, 0.20, 'Present', NULL),
(449, 27, '2026-05-10', '07:57:00', '21:42:00', 12.00, 0, 0, 0.03, 'Present', NULL),
(450, 40, '2026-05-06', '11:16:00', '19:16:00', 6.90, 66, 0, 0.10, 'Present', NULL),
(451, 40, '2026-05-08', '19:02:00', '19:16:00', 0.13, 472, 0, 0.10, 'Present', NULL),
(452, 12, '2026-05-06', '12:43:00', '21:40:00', 6.83, 0, 0, 0.17, 'Present', NULL),
(453, 12, '2026-05-07', '12:49:00', '21:40:00', 6.83, 0, 0, 0.17, 'Present', NULL),
(454, 12, '2026-05-10', '07:56:00', '21:43:00', 12.00, 0, 0, 0.05, 'Present', NULL),
(455, 39, '2026-05-06', '08:25:00', '16:18:00', 6.00, 0, 0, 0.63, 'Present', NULL),
(456, 39, '2026-05-07', '08:31:00', '16:32:00', 6.00, 0, 0, 0.87, 'Present', NULL),
(457, 39, '2026-05-08', '08:29:00', '21:41:00', 12.00, 0, 0, 0.02, 'Present', NULL),
(458, 39, '2026-05-09', '08:34:00', '21:41:00', 12.00, 0, 0, 0.02, 'Present', NULL),
(459, 24, '2026-05-06', '08:02:00', '17:14:00', 8.00, 0, 0, 0.07, 'Present', NULL),
(460, 24, '2026-05-07', '08:02:00', '17:20:00', 8.00, 0, 0, 0.17, 'Present', NULL),
(461, 24, '2026-05-08', '08:01:00', '17:20:00', 8.00, 0, 0, 0.17, 'Present', NULL),
(462, 34, '2026-05-06', '14:27:00', '21:39:00', 6.83, 0, 0, 0.15, 'Present', NULL),
(463, 34, '2026-05-07', '14:28:00', '21:37:00', 6.83, 0, 0, 0.12, 'Present', NULL),
(464, 34, '2026-05-10', '08:30:00', '21:37:00', 11.95, 0, 3, 0.00, 'Present', NULL),
(465, 36, '2026-05-06', '13:05:00', '21:34:00', 8.48, 5, 6, 0.00, 'Present', NULL),
(466, 36, '2026-05-07', '13:10:00', '21:46:00', 8.50, 10, 0, 0.10, 'Present', NULL),
(467, 36, '2026-05-10', '13:04:00', '21:46:00', 8.60, 4, 0, 0.10, 'Present', NULL),
(468, 33, '2026-05-06', '07:55:00', '15:41:00', 6.00, 0, 0, 0.02, 'Present', NULL),
(469, 33, '2026-05-07', '07:47:00', '15:40:00', 6.00, 0, 0, 0.00, 'Present', NULL),
(470, 33, '2026-05-08', '08:08:00', '21:43:00', 12.00, 0, 0, 0.05, 'Present', NULL),
(471, 33, '2026-05-09', '08:12:00', '21:43:00', 12.00, 0, 0, 0.05, 'Present', NULL),
(472, 50, '2026-05-06', '10:18:00', '21:46:00', 8.67, 0, 0, 0.10, 'Present', NULL),
(473, 50, '2026-05-07', '10:31:00', '22:08:00', 8.67, 0, 0, 0.47, 'Present', NULL),
(474, 50, '2026-05-08', '10:22:00', '22:07:00', 8.67, 0, 0, 0.45, 'Present', NULL),
(475, 47, '2026-05-06', '08:46:00', '18:17:00', 8.00, 0, 0, 0.12, 'Present', NULL),
(476, 47, '2026-05-07', '08:39:00', '18:22:00', 8.00, 0, 0, 0.20, 'Present', NULL),
(477, 47, '2026-05-08', '09:01:00', '18:22:00', 8.00, 0, 0, 0.20, 'Present', NULL),
(478, 20, '2026-05-06', '14:44:00', '19:20:00', 4.43, 214, 0, 0.17, 'Present', NULL),
(479, 20, '2026-05-07', '10:31:00', '20:17:00', 7.65, 31, 0, 1.12, 'Present', NULL),
(480, 20, '2026-05-08', '09:54:00', '19:20:00', 8.00, 0, 0, 0.17, 'Present', NULL),
(481, 6, '2026-05-06', '12:50:00', '21:47:00', 8.67, 0, 0, 0.12, 'Present', NULL),
(482, 6, '2026-05-08', '12:41:00', '21:44:00', 8.67, 0, 0, 0.07, 'Present', NULL),
(483, 6, '2026-05-09', '21:35:00', '21:46:00', 0.08, 515, 0, 0.10, 'Present', NULL),
(484, 11, '2026-05-06', '08:52:00', '18:13:00', 8.00, 0, 0, 0.05, 'Present', NULL),
(485, 11, '2026-05-07', '08:26:00', '18:11:00', 8.00, 0, 0, 0.02, 'Present', NULL),
(486, 11, '2026-05-08', '08:41:00', '18:11:00', 8.00, 0, 0, 0.02, 'Present', NULL),
(487, 28, '2026-05-06', '06:43:00', '18:11:00', 8.00, 0, 0, 0.02, 'Present', NULL),
(488, 28, '2026-05-07', '07:18:00', '18:11:00', 8.00, 0, 0, 0.02, 'Present', NULL),
(489, 28, '2026-05-08', '07:18:00', '18:11:00', 8.00, 0, 0, 0.02, 'Present', NULL),
(490, 13, '2026-05-06', '08:08:00', '18:15:00', 8.00, 0, 0, 0.08, 'Present', NULL),
(491, 13, '2026-05-07', '08:07:00', '18:10:00', 8.00, 0, 0, 0.00, 'Present', NULL),
(492, 13, '2026-05-08', '07:40:00', '18:11:00', 8.00, 0, 0, 0.02, 'Present', NULL),
(493, 13, '2026-05-09', '07:55:00', '17:11:00', 9.27, 0, 0, 0.00, 'Present', NULL),
(494, 49, '2026-05-06', '14:06:00', '21:39:00', 6.83, 0, 0, 0.15, 'Present', NULL),
(495, 49, '2026-05-07', '12:50:00', '21:37:00', 6.83, 0, 0, 0.12, 'Present', NULL),
(496, 49, '2026-05-10', '08:14:00', '21:37:00', 11.95, 0, 3, 0.00, 'Present', NULL),
(497, 52, '2026-05-06', '08:45:00', '18:16:00', 8.00, 0, 0, 0.10, 'Present', NULL),
(498, 52, '2026-05-07', '08:40:00', '18:11:00', 8.00, 0, 0, 0.02, 'Present', NULL),
(499, 52, '2026-05-08', '08:41:00', '18:11:00', 8.00, 0, 0, 0.02, 'Present', NULL),
(500, 16, '2026-05-06', '08:27:00', '15:42:00', 6.00, 0, 0, 0.03, 'Present', NULL),
(501, 16, '2026-05-07', '08:25:00', '15:41:00', 6.00, 0, 0, 0.02, 'Present', NULL),
(502, 16, '2026-05-08', '08:24:00', '21:49:00', 12.00, 0, 0, 0.15, 'Present', NULL),
(503, 16, '2026-05-09', '08:31:00', '21:47:00', 12.00, 0, 0, 0.12, 'Present', NULL),
(504, 14, '2026-05-06', '09:43:00', '19:03:00', 7.45, 33, 0, 0.88, 'Present', NULL),
(505, 14, '2026-05-07', '09:44:00', '21:13:00', 7.43, 34, 0, 3.05, 'Present', NULL),
(506, 14, '2026-05-08', '09:26:00', '22:08:00', 7.73, 16, 0, 3.97, 'Present', NULL),
(507, 38, '2026-05-06', '08:57:00', '17:11:00', 7.22, 47, 0, 0.02, 'Present', NULL),
(508, 38, '2026-05-07', '08:09:00', '17:16:00', 8.00, 0, 0, 0.10, 'Present', NULL),
(509, 38, '2026-05-08', '08:20:00', '17:16:00', 7.83, 10, 0, 0.10, 'Present', NULL),
(510, 7, '2026-05-06', '07:39:00', '18:11:00', 8.00, 0, 0, 1.02, 'Present', NULL),
(511, 7, '2026-05-07', '07:53:00', '17:56:00', 8.00, 0, 0, 0.77, 'Present', NULL),
(512, 7, '2026-05-08', '07:42:00', '17:56:00', 8.00, 0, 0, 0.77, 'Present', NULL),
(513, 41, '2026-05-06', '08:12:00', '18:15:00', 8.00, 0, 0, 0.08, 'Present', NULL),
(514, 41, '2026-05-07', '08:06:00', '18:18:00', 8.00, 0, 0, 0.13, 'Present', NULL),
(515, 41, '2026-05-08', '08:14:00', '18:16:00', 8.00, 0, 0, 0.10, 'Present', NULL),
(516, 45, '2026-05-06', '10:03:00', '18:59:00', 7.82, 0, 11, 0.00, 'Present', NULL),
(517, 45, '2026-05-07', '10:11:00', '20:55:00', 7.98, 11, 0, 1.75, 'Present', NULL),
(518, 45, '2026-05-08', '10:13:00', '19:15:00', 7.95, 3, 0, 0.08, 'Present', NULL),
(519, 15, '2026-05-11', '07:51:00', '18:11:00', 8.00, 0, 0, 1.02, 'Present', NULL),
(520, 48, '2026-05-11', '08:45:00', '19:25:00', 8.00, 0, 0, 1.25, 'Present', NULL),
(521, 32, '2026-05-11', '06:49:00', '21:43:00', 12.00, 0, 0, 0.05, 'Present', NULL),
(522, 53, '2026-05-11', '08:24:00', '21:49:00', 13.42, 0, 0, 0.00, 'Present', NULL),
(523, 51, '2026-05-11', '06:48:00', '21:44:00', 12.00, 0, 0, 0.07, 'Present', NULL),
(524, 27, '2026-05-11', '08:21:00', '21:43:00', 12.00, 0, 0, 0.05, 'Present', NULL),
(525, 40, '2026-05-11', '10:13:00', '19:22:00', 7.95, 3, 0, 0.20, 'Present', NULL),
(526, 12, '2026-05-11', '07:51:00', '21:40:00', 12.00, 0, 0, 0.00, 'Present', NULL),
(527, 34, '2026-05-11', '08:30:00', '21:41:00', 12.00, 0, 0, 0.02, 'Present', NULL),
(528, 36, '2026-05-11', '12:51:00', '21:41:00', 8.67, 0, 0, 0.02, 'Present', NULL),
(529, 50, '2026-05-11', '08:50:00', '23:14:00', 8.67, 0, 0, 1.57, 'Present', NULL),
(530, 47, '2026-05-11', '09:01:00', '23:12:00', 8.00, 0, 0, 5.03, 'Present', NULL),
(531, 20, '2026-05-11', '09:49:00', '19:10:00', 8.00, 0, 0, 0.00, 'Present', NULL),
(532, 11, '2026-05-11', '08:27:00', '18:11:00', 8.00, 0, 0, 0.02, 'Present', NULL),
(533, 28, '2026-05-11', '08:10:00', '06:46:00', 8.00, 0, 0, 12.60, 'Present', NULL),
(534, 13, '2026-05-11', '07:58:00', '18:51:00', 8.00, 0, 0, 0.68, 'Present', NULL),
(535, 49, '2026-05-11', '08:24:00', '21:42:00', 12.00, 0, 0, 0.03, 'Present', NULL),
(536, 52, '2026-05-11', '08:40:00', '18:10:00', 8.00, 0, 0, 0.00, 'Present', NULL),
(537, 14, '2026-05-11', '09:31:00', '22:01:00', 7.65, 21, 0, 3.85, 'Present', NULL),
(538, 38, '2026-05-11', '08:06:00', '17:10:00', 8.00, 0, 0, 0.00, 'Present', NULL),
(539, 7, '2026-05-11', '07:53:00', '18:12:00', 8.00, 0, 0, 1.03, 'Present', NULL),
(540, 41, '2026-05-11', '08:13:00', '18:14:00', 8.00, 0, 0, 0.07, 'Present', NULL),
(541, 45, '2026-05-11', '10:08:00', '19:10:00', 8.00, 0, 0, 0.00, 'Present', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `auditlogs`
--

DROP TABLE IF EXISTS `auditlogs`;
CREATE TABLE `auditlogs` (
  `logId` int(11) NOT NULL,
  `action` varchar(255) NOT NULL,
  `tableName` varchar(255) NOT NULL,
  `recordId` int(11) DEFAULT NULL,
  `performedBy` varchar(255) NOT NULL,
  `timestamp` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `bonuses`
--

DROP TABLE IF EXISTS `bonuses`;
CREATE TABLE `bonuses` (
  `bonusId` int(11) NOT NULL,
  `employeeId` int(11) DEFAULT NULL,
  `bonusType` varchar(255) NOT NULL,
  `amount` decimal(38,2) NOT NULL,
  `taxable` tinyint(1) DEFAULT NULL,
  `bonusDate` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `deductions`
--

DROP TABLE IF EXISTS `deductions`;
CREATE TABLE `deductions` (
  `deductionId` int(11) NOT NULL,
  `deductionName` varchar(255) NOT NULL,
  `deductionType` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `deductions`
--

INSERT INTO `deductions` (`deductionId`, `deductionName`, `deductionType`) VALUES
(101, 'LA', 'Union'),
(102, 'Cash Advance', 'Advance'),
(103, 'Loan', 'Loan');

-- --------------------------------------------------------

--
-- Table structure for table `departments`
--

DROP TABLE IF EXISTS `departments`;
CREATE TABLE `departments` (
  `departmentId` int(11) NOT NULL,
  `departmentName` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `departments`
--

INSERT INTO `departments` (`departmentId`, `departmentName`) VALUES
(1, 'Human Resources'),
(2, 'Information Technology'),
(3, 'Finance'),
(4, 'Sales'),
(5, 'Operations'),
(6, 'Admin Department');

-- --------------------------------------------------------

--
-- Table structure for table `employeeadjustments`
--

DROP TABLE IF EXISTS `employeeadjustments`;
CREATE TABLE `employeeadjustments` (
  `employeeAdjustmentId` int(11) NOT NULL,
  `employeeId` int(11) DEFAULT NULL,
  `adjustmentId` int(11) DEFAULT NULL,
  `amount` decimal(38,2) DEFAULT NULL,
  `isRecurring` tinyint(1) DEFAULT NULL,
  `startDate` date DEFAULT NULL,
  `endDate` date DEFAULT NULL,
  `applyOnCutoff` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `employeedeductions`
--

DROP TABLE IF EXISTS `employeedeductions`;
CREATE TABLE `employeedeductions` (
  `employeeDeductionId` int(11) NOT NULL,
  `employeeId` int(11) DEFAULT NULL,
  `deductionId` int(11) DEFAULT NULL,
  `amount` decimal(38,2) NOT NULL,
  `isRecurring` tinyint(1) DEFAULT NULL,
  `startDate` date DEFAULT NULL,
  `endDate` date DEFAULT NULL,
  `total_obligation` decimal(19,2) DEFAULT NULL,
  `deductionCutoff` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `employees`
--

DROP TABLE IF EXISTS `employees`;
CREATE TABLE `employees` (
  `employeeId` int(11) NOT NULL,
  `employeeNumber` varchar(255) NOT NULL,
  `firstName` varchar(255) NOT NULL,
  `middleName` varchar(255) NOT NULL,
  `lastName` varchar(255) NOT NULL,
  `birthDate` date DEFAULT NULL,
  `address` varchar(255) NOT NULL,
  `contactNumber` varchar(255) NOT NULL,
  `dateHired` date DEFAULT NULL,
  `employmentStatus` varchar(255) NOT NULL,
  `employmentType` varchar(255) NOT NULL,
  `payType` varchar(255) NOT NULL,
  `basicSalary` decimal(38,2) NOT NULL,
  `factorRate` decimal(5,2) DEFAULT NULL,
  `bank_Account` varchar(255) NOT NULL,
  `tin` varchar(255) NOT NULL,
  `sssNumber` varchar(255) NOT NULL,
  `philhealthNumber` varchar(255) NOT NULL,
  `pagibigNumber` varchar(255) NOT NULL,
  `departmentId` int(11) DEFAULT NULL,
  `positionId` int(11) DEFAULT NULL,
  `holidayPayEligible` tinyint(1) NOT NULL DEFAULT 0,
  `biometric_id` varchar(255) DEFAULT NULL,
  `ot_multiplier` decimal(5,2) DEFAULT NULL,
  `allowance` decimal(38,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `employees`
--

INSERT INTO `employees` (`employeeId`, `employeeNumber`, `firstName`, `middleName`, `lastName`, `birthDate`, `address`, `contactNumber`, `dateHired`, `employmentStatus`, `employmentType`, `payType`, `basicSalary`, `factorRate`, `bank_Account`, `tin`, `sssNumber`, `philhealthNumber`, `pagibigNumber`, `departmentId`, `positionId`, `holidayPayEligible`, `biometric_id`, `ot_multiplier`, `allowance`) VALUES
(1, 'EMP-2024-001', 'ADMIN', '-', '-', '1990-05-14', 'Manila', '+639171234567', '2020-01-14', 'Active', 'Job Order', 'monthly', 21000.00, 20.00, '1234567890', '123-456-789-000', '34-1234567-8', '12-345678901-2', '1234-5678-9012', 2, 1, 0, NULL, 1.00, 0.00),
(4, 'EMP00004', 'TRISTEN REIGH', 'PONCE', 'REMIENDO', '2004-03-23', 'Dasmarinas Cavite', '09568842320', '2026-04-04', 'Active', 'Probationary', 'semi-monthly', 10000.00, NULL, '942100050800', '000000000', '0444607629', '022509635130', '121311323441', 6, 26, 0, '100', 1.00, 0.00),
(5, 'EMP00005', 'JENNIFER', 'GUEVARA', 'DELA CRUZ', '2002-08-23', 'Manila City', '09563149062', '2026-05-04', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '0000000000', '510826827', '3506780698', '022500791423', '3506780698', 6, 26, 0, '101', 1.00, 0.00),
(6, 'EMP00006', 'REGINA FE', 'VILLANUEVA', 'ABANE', '1996-07-20', '1600 NORTH DRIVE, SAN CLEMENTE VILLAGE 2, BRGY PAG-ASA, BINANGONAN RIZAL', '09763578467', '2025-11-29', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934108086261', '319520420', '3448479876', '03025689483', '121131582798', 5, 27, 0, '76', 1.00, 1000.00),
(7, 'EMP00007', 'SACHI', 'SANTOS', 'KOIKE', '2004-12-19', 'CALOOCAN CITY', '09707397177', '2026-04-20', 'Active', 'Probationary', 'semi-monthly', 17000.00, NULL, '942100007596', '801716615', '3522869797', '0000000000', '0000000000', 6, 28, 0, '96', 1.00, 0.00),
(8, 'EMP00008', 'CIDMAR', 'ESTAÑERO', 'ANDRADE', '1976-10-09', '65 ILANG-ILANG ST, BATASAN HILLS QUEZON', '09393970040', '2025-01-01', 'Active', 'Job Order', 'semi-monthly', 37000.00, NULL, '934104513164', '230511068', '3384690476', '010517372310', '121097587615', 5, 29, 0, '22', 1.00, 0.00),
(9, 'EMP00009', 'JUDEA LYN', 'HEMBRA', 'BERNARDO', '2000-04-13', '135-A LAWA BRGY LAWA, OBANDO, BULACAN', '09673145805', '2026-01-09', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934108466842', '395756663', '3505386543', '020274243121', '121294525986', 5, 30, 0, '79', 1.00, 3000.00),
(10, 'EMP00010', 'ANDRES', 'DE ARAG', 'BONIFACIO', '1978-07-20', '1 WACK-WACK GREENHILLS, MANDALUYONG', '09171630473', '2025-01-01', 'Active', 'Fixed', 'semi-monthly', 30000.00, NULL, '934104498269', '188643030', '3328659224', '030501484497', '121047627675', 5, 31, 0, '4', 1.00, 0.00),
(11, 'EMP00011', 'FERNANDO', 'ASI', 'CAPULE', '1999-02-16', '#3 INDIGO STREET, STELLA MARIS SUBDIVISION, BRGY MAYBUNGA, PASIG CITY', '09665541146', '2025-11-06', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934107814134', '662940602', '353494190', '01-027002546', '121352937265', 5, 32, 0, '78', 1.00, 10000.00),
(12, 'EMP00012', 'JONEL', 'MIRAMON', 'CARPIO', '1984-07-21', 'GUMAMELA ST. BRGY. SAN CARLOS BINANGONAN RIZAL', '09430890413', '2025-01-01', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934104458966', '228938794', '3372983593', '190905253682', '121005723528', 5, 33, 0, '37', 1.00, 9000.00),
(13, 'EMP00013', 'JEFFREY', 'CARILLA', 'CHA', '2001-07-18', '1200 MARIAN TOWNHOMES BARANGAY 752, MANILA CITY', '09663654702', '2026-03-10', 'Active', 'Probationary', 'semi-monthly', 20000.00, NULL, '934109338232', '653272163', '0518042385', '100255553423', '121345614127', 5, 33, 0, '86', 1.00, 3000.00),
(14, 'EMP00014', 'TYRONE', 'SEBALLOS', 'DAA', '1980-09-18', '#2099 HOLIDAY HOMES SUBDIVISION BRGY SAN ANTONIO, SAN PEDRO CITY LAGUNA', '09276239980', '2026-03-02', 'Active', 'Probationary', 'semi-monthly', 20000.00, NULL, '934109161694', '211654067', '0452925173', '010257173068', '121381465353', 5, 34, 0, '92', 1.00, 2000.00),
(15, 'EMP00015', 'JASPER KIM', 'LIGUTAN', 'DAYAO', '1994-10-18', 'BLK 4 LOT 10 PHASE 3 SOLAR URBAN HOMES NORTH BRGY BAGUMBONG, CALOOCAN CITY', '09369841024', '2025-01-01', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934104428921', '334484330', '3464995745', '022506396135', '121190837920', 5, 33, 0, '12', 1.00, 9000.00),
(16, 'EMP00016', 'NORWIN', 'EXCONDE', 'DE LA FUENTE', '1979-05-15', '092 JOSE FULE STREET BRGY IV ALAMINOS LAGUNA', '09196369083', '2026-02-24', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934109056340', '233163255', '0415463371', '020263302215', '121128454654', 5, 35, 0, '90', 1.00, 2000.00),
(17, 'EMP00017', 'JASON', 'ANTONIO', 'DELA CRUZ', '1985-11-20', 'NO. 57 HULO ST. BAGONG BARRIO PANDI BULACAN', '09310279491', '2025-01-01', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934104483489', '496245682', '3465387381', '212002018443', '121193918349', 5, 33, 0, '26', 1.00, 9000.00),
(18, 'EMP00018', 'RONALD ALLAN', 'TABUCOL', 'DOCTOR', '1979-12-15', '97 ROSAL ST. SGT. DE LEON EXT. SANTOLAN, PASIG CITY', '09625101966', '2025-01-01', 'Active', 'Job Order', 'semi-monthly', 39000.00, NULL, '934104482078', '204204406', '3347971611', '030500471944', '108001085885', 5, 29, 0, '24', 1.00, 0.00),
(19, 'EMP00019', 'JOSE MARIO', 'ACOT', 'FUDERANAN', '1962-06-19', '31 PARIS, CIUDAD GRANDE EXCUTIVE VILLAGE, BRGY ROSARIO PASIG CITY', '09151473566', '2025-11-27', 'Active', 'Fixed', 'semi-monthly', 50000.00, NULL, '934108134396', '112950058', '0385885982', '010252762901', '121376145169', 5, 36, 0, '75', 1.00, 0.00),
(20, 'EMP00020', 'ARMANDO', 'SALAZAR', 'GANTE', '1969-01-18', '422 3RD ALLEY KALAYAAN B ST., BATASAN HILLS, QUEZON CITY', '09173868814', '2025-11-26', 'Active', 'Job Order', 'semi-monthly', 32000.00, NULL, '934108121005', '165356883', '0331976752', '190901125248', '107000960116', 5, 37, 0, '74', 1.00, 0.00),
(21, 'EMP00021', 'IMEE MARIE', 'TIANZON', 'GATCHALIAN', '2001-10-16', '822 FT REYES ST, BRGY SANTO ROSARIO MALOLOS BULACAN', '09453690775', '2026-01-20', 'Active', 'Probationary', 'semi-monthly', 20000.00, NULL, '934108686078', '394602671', '3532132786', '210258814112', '121343634754', 5, 38, 0, '80', 1.00, 0.00),
(22, 'EMP00022', 'ARIEL RAMESES', 'LIM', 'GATINGA', '2000-10-25', '12G SPACE TAFT, TAFT AVENUE BRGY 724, MANILA CITY', '09773312919', '2026-02-17', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934109076471', '601842571', '3541264920', '092540466175', '121376030769', 5, 34, 0, NULL, 1.00, 3000.00),
(23, 'EMP00023', 'PRINCE ANDREI', 'PUOD', 'GENETA', '2005-02-25', '119 IBAYO II BAGBAG NOVALICHES, BRGY BAGBAG, QUEZON CITY', '09942161198', '2025-04-09', 'Active', 'Job Order', 'semi-monthly', 19500.00, NULL, '934105297093', '696948804', '3534690048', '032539337911', '121352226177', 6, 39, 0, NULL, 1.00, 0.00),
(24, 'EMP00024', 'SHAMARIE', 'QUINERY', 'GONZALES', '1998-10-03', 'SANTOLAN ROAD BRGY PALLOCAN WEST, BATANGAS CITY', '09664411563', '2025-03-03', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934104836531', '379956868', '3502475211', '090257487872', '121280176058', 6, 26, 0, '42', 1.00, 0.00),
(25, 'EMP00025', 'EVAN LEE', 'ALMENDRA', 'GONZALES', '1985-06-07', '73 E GOMEZ ST. BRGY KAINGEN BACOOR, BACOOR CITY', '09610747610', '2025-01-01', 'Active', 'Job Order', 'semi-monthly', 35000.00, NULL, '934104482011', '305648222', '3390997260', '080504677293', '103000208590', 5, 34, 0, '25', 1.00, 0.00),
(26, 'EMP00026', 'NEIL GABRIELLE', 'DALAS', 'GUTIERREZ', '2002-10-15', 'BLOCK 9 LOT 37 PHASE 3B PELOTA DR, BRGY SAN FRANCISCO (HALANG), BINAN CITY LAGUNA', '09600684737', '2026-02-24', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934109034302', '698174593', '0452925814', '080272561021', '121380154860', 5, 35, 0, NULL, 1.00, 2000.00),
(27, 'EMP00027', 'DENNIS', 'GOMEZ', 'HORTILANO', '1990-09-27', 'BLK 105 LOT 3 C UNIT 3 DALANGHITA ST. BRGY PAGRAI HILLS MAYAMOT, ANTIPOLO CITY RIZAL', '09708191052', '2025-01-01', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934104496970', '391381058', '0819362340', '150503104883', '121115967150', 5, 33, 0, '29', 1.00, 9000.00),
(28, 'EMP00028', 'ALLEN JOHN', 'CONSTANTINO', 'IBAÑEZ', '1997-03-20', 'BLK 6, LOT 5 SCARLET ST., SPRING COUNTRY, FILINVEST 2, BRGY BAGONG SILANGAN QUEZON CITY', '09568719137', '2026-02-02', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934108720618', '746012991', '3482331420', '030264083938', '919269462624', 5, 40, 0, '81', 1.00, 3000.00),
(29, 'EMP00029', 'DAVID ERNESTO', 'MICALLER', 'JALANDONI', '2000-11-15', '1410 CRISANTA TOWER BRGY ORANBO, PASIG CITY', '09954579261', '2026-04-15', 'Active', 'Probationary', 'semi-monthly', 20000.00, NULL, '934109485501', '643219484', '35-2955909', '012512727466', '121356474573', 5, 32, 0, '95', 1.00, 1000.00),
(30, 'EMP00030', 'ACE', 'GENOVE', 'JUANCHO', '1982-10-11', '19 MALAYA, BRGY TANDANG SORA, QUEZON CITY', '09777376598', '2026-03-02', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934109153208', '208323291', '3379252890', '030505383237', '121063578815', 5, 33, 0, '91', 1.00, 4000.00),
(31, 'EMP00031', 'ROLAND', 'PACARO', 'LAPINIG', '1977-01-26', 'B89 L65 2ND ST. MMH TOWNHOMES, BRGY  SAN JOSE, RODRIGUEZ MONTALBAN RIZAL', '09565571161', '2025-01-01', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934104509237', '401476753', '3320125099', '030257818434', '121140169267', 5, 33, 0, '17', 1.00, 9000.00),
(32, 'EMP00032', 'RUEL', 'PACARO', 'LAPINIG', '1975-10-16', 'B27 L18 CARISSA 2-A PALMERA HOMES, BRGY KAYPIAN C.S.J.O.M, SAN JOSE DEL MONTE BULACAN', '09163510936', '2025-01-01', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934104506013', '293016359', '3320125028', '220000243345', '121319868575', 5, 33, 0, '15', 1.00, 9000.00),
(33, 'EMP00033', 'MARYJESS', 'DELACRUZ', 'LEDESMA', '1989-05-28', '2311 SULU ST BRGY 349, MANILA CITY BULACAN', '09814641602', '2026-02-17', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934108989001', '320606739', '3409172439', '220001349945', '12113353766', 5, 41, 0, '47', 1.00, 2000.00),
(34, 'EMP00034', 'PATRICK JOHN', 'ELAURIA', 'LIM', '2003-06-16', '312 MARCELLINE, MULBERRY PLACE, ACACIA ESTATES BRGY BAMBANG TAGUIG CITY', '09174608805', '2025-03-03', 'Active', 'Regular', 'semi-monthly', 20000.00, NULL, '934105045397', '000000000', '3542467302', '0000000000', '121382217225', 5, 34, 0, '43', 1.00, 5000.00),
(35, 'EMP00035', 'JOANA MARIE', 'BOROGEL', 'LUNA', '1998-10-13', '5 RAMON MAGSAYSAY STREET, BRGY PARANG MARIKINA CITY', '09054819243', '2026-02-02', 'Active', 'Job Order', 'semi-monthly', 30000.00, NULL, '934108712166', '508 797 5', '34-8735475', '03-026673386', '1212-8814-77', 5, 31, 0, '82', 1.00, 0.00),
(36, 'EMP00036', 'CHARIE MAE', 'FRANCO', 'MAG-APAN', '2002-02-07', 'BLOCK 2 LOT 87 MOLAVE STREET, SIKAT ARAW, NAGPAYONG , BRGY PINAGBUHATAN PASIG CITY', '09544893109', '2025-03-10', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934104898263', '684278017', '3493247833', '012511397292', '121269111731', 5, 32, 0, '44', 1.00, 1000.00),
(37, 'EMP00037', 'JOEI', 'MANAYTAY', 'MANILA', '1971-10-05', 'CATTHEYA BLDG. KAAYUSAN ST. KARANGALAN VILL.', '09062454889', '2025-01-01', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934104486031', '295697240', '3305600898', '030506258495', '121146446493', 5, 33, 0, '27', 1.00, 9000.00),
(38, 'EMP00038', 'ROSEMARIE', 'ROLLO', 'MANOZO', '2002-12-15', 'FLOVI HOME PH 8 PARADISE VILLAGE,  BRGY TONSUYA MALABON CITY', '09516010524', '2026-02-10', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934109192964', '000000000', '3539827971', '0000000000', '121370517674', 5, 34, 0, '93', 1.00, 2000.00),
(39, 'EMP00039', 'RICHARD', 'LAGRADILLA', 'NEFIEL', '1992-10-18', 'ZONE 4A MASAGANA ST. MAYAMOT ANTIPOLO CITY RIZAL', '09466277325', '2025-01-01', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934104483372', '446631615', '3442288005', '010255566283', '121105717571', 5, 33, 0, '39', 1.00, 9000.00),
(40, 'EMP00040', 'CHRISTIAN', 'MENESES', 'OFALLA', '1994-12-20', '001 MANGO ST. BRGY COMMONWEALTH QC ', '09933604104', '2025-01-01', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934104482115', '336143336', '3537546212', '032508647475', '121361334147', 5, 34, 0, '35', 1.00, 9000.00),
(41, 'EMP00041', 'JEOMARIC', 'LAURETA', ' PENAFLOR', '2003-06-18', '77-E A.T. REYES STREET POBLACION MANDALUYONG CITY', '09215834251', '2026-04-27', 'Active', 'Probationary', 'semi-monthly', 10000.00, NULL, '934109768059', '000000000', '0000000000', '0000000000', '0000000000', 5, 42, 0, '97', 1.00, 0.00),
(42, 'EMP00042', 'CHRISTIAN', 'VICENTE', 'PUOD', '1991-01-12', '119 IBAYO 2 BAGBAG QUEZON CITY', '09633235827', '2025-01-01', 'Active', 'Fixed', 'semi-monthly', 10000.00, NULL, '934104496716', '0', '3443787372', '192012853595', '121151276530', 5, 43, 0, NULL, 1.00, 0.00),
(43, 'EMP00043', 'RANDY', 'DIOQUINO', 'RECATO', '1980-08-09', '45 CM BORJA SANTA ANA, PATEROS', '09322980721', '2026-02-25', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934109063973', '215433887', '3350308815', '070261459270', '121053359197', 5, 35, 0, NULL, 1.00, 2000.00),
(44, 'EMP00044', 'GENELYN', 'LINCHOCO', 'RICOHERMOSO', '1989-06-10', '34 STO NINO ST ORANBO PASIG CITY', '09193121217', '2026-02-09', 'Active', 'Job Order', 'semi-monthly', 36000.00, NULL, '934108364369', '420046589', '3426510472', '010512945568', '121018515320', 5, 29, 0, '18', 1.00, 0.00),
(45, 'EMP00045', 'JASON MARVELOUS', 'CAMPOS', 'SAGUIN', '2003-10-12', '2172 AZUCENA STREET SANTA ANA MANILA CITY', '09098240765', '2026-05-04', 'Active', 'Probationary', 'semi-monthly', 10000.00, NULL, '000000000000000', '000000000', '00000000', '0000000000', '0000000000', 5, 34, 0, '99', 1.00, 0.00),
(46, 'EMP00046', 'CARL BRIAN', 'QUIZON', 'SALVADOR', '1978-11-08', '0', '0', '2025-01-01', 'Active', 'Fixed', 'semi-monthly', 50000.00, NULL, '934104512173', '000000000', '00000000', '0000000000', '0000000000', 5, 44, 0, NULL, 1.00, 0.00),
(47, 'EMP00047', 'AL FIRDAUS GHAZI', 'SEMING', 'SARIP', '2002-01-12', '211 9A ACE TOWER 1 CONDOMINIUM BANAWE DOÑA JOSEFA QUEZON CITY', '09976503940', '2025-06-02', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934105912627', '661131693', '3534874220', '010269996701', '121352749452', 5, 30, 0, '64', 1.00, 2000.00),
(48, 'EMP00048', 'LORENCRIS', 'GIMENA', 'SIAREZ', '1973-12-24', 'BLOCK 15 LOT 10 DREAM HOMES SUBDIVISION SILANGAN SAN MATEO RIZAL', '09196462851', '2025-02-13', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934104757271', '166190866', '3379075022', '030250885136', '121009426777', 5, 45, 0, '14', 1.00, 4000.00),
(49, 'EMP00049', 'BENIGNO', 'SAMONTE', 'SOMBILLA', '1971-10-24', '2532 JUAN LUNA ST. GAGALANGIN TONDO BRGY 162 MANILA CITY', '09772500420', '2026-02-27', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934109034369', '162690842', '3311249272', '190523820935', '000928967802', 5, 35, 0, '87', 1.00, 5000.00),
(50, 'EMP00050', 'RAMIL', 'DE JESUS', 'STO DOMINGO', '1979-12-21', '151 RIO CHICO GENERAL TINIO NUEVA ECIJA', '09301411750', '2025-04-16', 'Active', 'Job Order', 'semi-monthly', 55000.00, NULL, '916106664782', '279770956', '3379444187', '070505326111', '121023122642', 5, 46, 0, '60', 1.00, 0.00),
(51, 'EMP00051', 'RICHEL', 'PALUBON', 'SUMINGUIT', '1975-05-25', 'lot 10 blk 46 BISTEK VILLE 2 QUEZON CITY', 'n/a', '2025-01-01', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934104492947', '463689242', '3318024212', '030251604440', '911236014876', 5, 41, 0, '21', 1.00, 5000.00),
(52, 'EMP00052', 'ELMER', 'MATA', 'SUNGA', '1971-03-15', '356 GEN. FRANCISCO ST. SAN AGUSTIN QUEZON CITY', '09156528086', '2026-02-19', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934109059291', '251111884', '3314681222', '030506258304', '121204347530', 5, 33, 0, '88', 1.00, 5000.00),
(53, 'EMP00053', 'MANUEL', 'PEÑA', 'VEGA', '1972-03-07', 'B1  OMEGA SUBD. SAN GABRIEL TERESA RIZAL', '09668142900', '2025-01-01', 'Active', 'Job Order', 'semi-monthly', 20000.00, NULL, '934104508008', '250256916', '3312729636', '010252677696', '002862334905', 5, 33, 0, '20', 1.00, 9000.00);

-- --------------------------------------------------------

--
-- Table structure for table `employee_schedule_assignment`
--

DROP TABLE IF EXISTS `employee_schedule_assignment`;
CREATE TABLE `employee_schedule_assignment` (
  `id` int(11) NOT NULL,
  `employeeId` int(11) NOT NULL,
  `template_id` int(11) NOT NULL,
  `schedule_year` int(11) NOT NULL,
  `schedule_month` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `employee_schedule_assignment`
--

INSERT INTO `employee_schedule_assignment` (`id`, `employeeId`, `template_id`, `schedule_year`, `schedule_month`) VALUES
(29, 4, 20, 2026, 5),
(30, 5, 21, 2026, 5),
(31, 6, 32, 2026, 5),
(32, 7, 20, 2026, 5),
(33, 8, 29, 2026, 5),
(34, 9, 20, 2026, 5),
(35, 11, 21, 2026, 5),
(36, 12, 29, 2026, 5),
(37, 13, 21, 2026, 5),
(38, 14, 21, 2026, 5),
(39, 15, 20, 2026, 5),
(40, 16, 30, 2026, 5),
(41, 17, 31, 2026, 5),
(42, 18, 30, 2026, 5),
(43, 20, 22, 2026, 5),
(44, 22, 21, 2026, 5),
(45, 23, 27, 2026, 5),
(46, 24, 20, 2026, 5),
(47, 25, 30, 2026, 5),
(48, 26, 30, 2026, 5),
(49, 27, 29, 2026, 5),
(50, 28, 21, 2026, 5),
(51, 29, 23, 2026, 5),
(52, 31, 29, 2026, 5),
(53, 32, 29, 2026, 5),
(54, 33, 30, 2026, 5),
(55, 34, 29, 2026, 5),
(56, 35, 24, 2026, 5),
(57, 36, 33, 2026, 5),
(58, 37, 30, 2026, 5),
(59, 38, 20, 2026, 5),
(60, 39, 30, 2026, 5),
(61, 40, 22, 2026, 5),
(62, 41, 21, 2026, 5),
(63, 43, 29, 2026, 5),
(64, 44, 25, 2026, 5),
(65, 45, 22, 2026, 5),
(66, 47, 21, 2026, 5),
(67, 48, 21, 2026, 5),
(68, 49, 29, 2026, 5),
(69, 50, 26, 2026, 5),
(70, 51, 29, 2026, 5),
(72, 52, 21, 2026, 5),
(73, 53, 30, 2026, 5);

-- --------------------------------------------------------

--
-- Table structure for table `holiday`
--

DROP TABLE IF EXISTS `holiday`;
CREATE TABLE `holiday` (
  `holiday_id` int(11) NOT NULL,
  `holiday_name` varchar(255) NOT NULL,
  `holiday_date` date NOT NULL,
  `holiday_type` varchar(32) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `holiday`
--

INSERT INTO `holiday` (`holiday_id`, `holiday_name`, `holiday_date`, `holiday_type`, `created_at`, `updated_at`) VALUES
(1, 'New Year\'s Day', '2026-01-01', 'REGULAR', '2026-03-27 15:30:05', '2026-03-27 16:36:10'),
(2, 'Lailatul Isra Wal Mi Raj', '2026-01-16', NULL, '2026-03-27 15:30:05', '2026-03-31 10:29:41'),
(3, 'Lunar New Year\'s Day', '2026-02-17', 'REGULAR', '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(4, 'Ramadan Start', '2026-02-19', NULL, '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(5, 'People Power Anniversary', '2026-02-25', 'SPECIAL_WORKING', '2026-03-27 15:30:05', '2026-03-27 15:37:06'),
(6, 'Eid al-Fitr Holiday', '2026-03-20', NULL, '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(7, 'Eid al-Fitr', '2026-03-21', NULL, '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(8, 'Maundy Thursday', '2026-04-02', 'REGULAR', '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(9, 'Good Friday', '2026-04-03', 'REGULAR', '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(10, 'Black Saturday', '2026-04-04', 'SPECIAL_NON_WORKING', '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(11, 'Easter Sunday', '2026-04-05', NULL, '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(12, 'The Day of Valor', '2026-04-09', NULL, '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(13, 'Labor Day', '2026-05-01', 'REGULAR', '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(14, 'Eid al-Adha (tentative)', '2026-05-27', NULL, '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(15, 'Eid al-Adha Day 2 (tentative)', '2026-05-28', NULL, '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(16, 'Independence Day', '2026-06-12', 'REGULAR', '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(17, 'Amun Jadid (tentative)', '2026-06-17', NULL, '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(18, 'Ninoy Aquino Day', '2026-08-21', 'SPECIAL_NON_WORKING', '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(19, 'Maulid un-Nabi (tentative)', '2026-08-26', NULL, '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(20, 'National Heroes Day', '2026-08-31', 'REGULAR', '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(21, 'All Saints\' Day', '2026-11-01', 'SPECIAL_NON_WORKING', '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(22, 'All Souls\' Day', '2026-11-02', 'SPECIAL_NON_WORKING', '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(23, 'Bonifacio Day', '2026-11-30', 'REGULAR', '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(24, 'Feast of the Immaculate Conception', '2026-12-08', 'SPECIAL_NON_WORKING', '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(25, 'Christmas Eve', '2026-12-24', 'SPECIAL_NON_WORKING', '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(26, 'Christmas Day', '2026-12-25', 'REGULAR', '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(27, 'Rizal Day', '2026-12-30', 'REGULAR', '2026-03-27 15:30:05', '2026-03-27 15:30:05'),
(28, 'New Year\'s Eve', '2026-12-31', 'REGULAR', '2026-03-27 15:30:05', '2026-03-27 15:30:05');

-- --------------------------------------------------------

--
-- Table structure for table `leavebalance`
--

DROP TABLE IF EXISTS `leavebalance`;
CREATE TABLE `leavebalance` (
  `employeeId` int(11) NOT NULL,
  `leaveTypeId` int(11) NOT NULL,
  `balance` decimal(38,2) NOT NULL,
  `remainingDays` decimal(5,2) NOT NULL DEFAULT 0.00,
  `totalDays` decimal(5,2) NOT NULL DEFAULT 15.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `leavebalance`
--

INSERT INTO `leavebalance` (`employeeId`, `leaveTypeId`, `balance`, `remainingDays`, `totalDays`) VALUES
(4, 1, 15.00, 0.00, 15.00),
(4, 2, 15.00, 0.00, 15.00),
(4, 3, 15.00, 0.00, 15.00),
(4, 4, 15.00, 0.00, 15.00),
(5, 1, 15.00, 0.00, 15.00),
(5, 2, 15.00, 0.00, 15.00),
(5, 3, 15.00, 0.00, 15.00),
(5, 4, 15.00, 0.00, 15.00),
(6, 1, 15.00, 0.00, 15.00),
(6, 2, 15.00, 0.00, 15.00),
(6, 3, 15.00, 0.00, 15.00),
(6, 4, 15.00, 0.00, 15.00),
(7, 1, 15.00, 0.00, 15.00),
(7, 2, 15.00, 0.00, 15.00),
(7, 3, 15.00, 0.00, 15.00),
(7, 4, 15.00, 0.00, 15.00),
(8, 1, 15.00, 0.00, 15.00),
(8, 2, 15.00, 0.00, 15.00),
(8, 3, 15.00, 0.00, 15.00),
(8, 4, 15.00, 0.00, 15.00),
(9, 1, 15.00, 0.00, 15.00),
(9, 2, 15.00, 0.00, 15.00),
(9, 3, 15.00, 0.00, 15.00),
(9, 4, 15.00, 0.00, 15.00),
(10, 1, 15.00, 0.00, 15.00),
(10, 2, 15.00, 0.00, 15.00),
(10, 3, 15.00, 0.00, 15.00),
(10, 4, 15.00, 0.00, 15.00),
(11, 1, 15.00, 0.00, 15.00),
(11, 2, 15.00, 0.00, 15.00),
(11, 3, 15.00, 0.00, 15.00),
(11, 4, 15.00, 0.00, 15.00),
(12, 1, 15.00, 0.00, 15.00),
(12, 2, 15.00, 0.00, 15.00),
(12, 3, 15.00, 0.00, 15.00),
(12, 4, 15.00, 0.00, 15.00),
(13, 1, 15.00, 0.00, 15.00),
(13, 2, 15.00, 0.00, 15.00),
(13, 3, 15.00, 0.00, 15.00),
(13, 4, 15.00, 0.00, 15.00),
(14, 1, 15.00, 0.00, 15.00),
(14, 2, 15.00, 0.00, 15.00),
(14, 3, 15.00, 0.00, 15.00),
(14, 4, 15.00, 0.00, 15.00),
(15, 1, 15.00, 0.00, 15.00),
(15, 2, 15.00, 0.00, 15.00),
(15, 3, 15.00, 0.00, 15.00),
(15, 4, 15.00, 0.00, 15.00),
(16, 1, 15.00, 0.00, 15.00),
(16, 2, 15.00, 0.00, 15.00),
(16, 3, 15.00, 0.00, 15.00),
(16, 4, 15.00, 0.00, 15.00),
(17, 1, 15.00, 0.00, 15.00),
(17, 2, 15.00, 0.00, 15.00),
(17, 3, 15.00, 0.00, 15.00),
(17, 4, 15.00, 0.00, 15.00),
(18, 1, 15.00, 0.00, 15.00),
(18, 2, 15.00, 0.00, 15.00),
(18, 3, 15.00, 0.00, 15.00),
(18, 4, 15.00, 0.00, 15.00),
(19, 1, 15.00, 0.00, 15.00),
(19, 2, 15.00, 0.00, 15.00),
(19, 3, 15.00, 0.00, 15.00),
(19, 4, 15.00, 0.00, 15.00),
(20, 1, 15.00, 0.00, 15.00),
(20, 2, 15.00, 0.00, 15.00),
(20, 3, 15.00, 0.00, 15.00),
(20, 4, 15.00, 0.00, 15.00),
(21, 1, 15.00, 0.00, 15.00),
(21, 2, 15.00, 0.00, 15.00),
(21, 3, 15.00, 0.00, 15.00),
(21, 4, 15.00, 0.00, 15.00),
(22, 1, 15.00, 0.00, 15.00),
(22, 2, 15.00, 0.00, 15.00),
(22, 3, 15.00, 0.00, 15.00),
(22, 4, 15.00, 0.00, 15.00),
(23, 1, 15.00, 0.00, 15.00),
(23, 2, 15.00, 0.00, 15.00),
(23, 3, 15.00, 0.00, 15.00),
(23, 4, 15.00, 0.00, 15.00),
(24, 1, 15.00, 0.00, 15.00),
(24, 2, 15.00, 0.00, 15.00),
(24, 3, 15.00, 0.00, 15.00),
(24, 4, 15.00, 0.00, 15.00),
(25, 1, 15.00, 0.00, 15.00),
(25, 2, 15.00, 0.00, 15.00),
(25, 3, 15.00, 0.00, 15.00),
(25, 4, 15.00, 0.00, 15.00),
(26, 1, 15.00, 0.00, 15.00),
(26, 2, 15.00, 0.00, 15.00),
(26, 3, 15.00, 0.00, 15.00),
(26, 4, 15.00, 0.00, 15.00),
(27, 1, 15.00, 0.00, 15.00),
(27, 2, 15.00, 0.00, 15.00),
(27, 3, 15.00, 0.00, 15.00),
(27, 4, 15.00, 0.00, 15.00),
(28, 1, 15.00, 0.00, 15.00),
(28, 2, 15.00, 0.00, 15.00),
(28, 3, 15.00, 0.00, 15.00),
(28, 4, 15.00, 0.00, 15.00),
(29, 1, 15.00, 0.00, 15.00),
(29, 2, 15.00, 0.00, 15.00),
(29, 3, 15.00, 0.00, 15.00),
(29, 4, 15.00, 0.00, 15.00),
(30, 1, 15.00, 0.00, 15.00),
(30, 2, 15.00, 0.00, 15.00),
(30, 3, 15.00, 0.00, 15.00),
(30, 4, 15.00, 0.00, 15.00),
(31, 1, 15.00, 0.00, 15.00),
(31, 2, 15.00, 0.00, 15.00),
(31, 3, 15.00, 0.00, 15.00),
(31, 4, 15.00, 0.00, 15.00),
(32, 1, 15.00, 0.00, 15.00),
(32, 2, 15.00, 0.00, 15.00),
(32, 3, 15.00, 0.00, 15.00),
(32, 4, 15.00, 0.00, 15.00),
(33, 1, 15.00, 0.00, 15.00),
(33, 2, 15.00, 0.00, 15.00),
(33, 3, 15.00, 0.00, 15.00),
(33, 4, 15.00, 0.00, 15.00),
(34, 1, 15.00, 0.00, 15.00),
(34, 2, 15.00, 0.00, 15.00),
(34, 3, 15.00, 0.00, 15.00),
(34, 4, 15.00, 0.00, 15.00),
(35, 1, 15.00, 0.00, 15.00),
(35, 2, 15.00, 0.00, 15.00),
(35, 3, 15.00, 0.00, 15.00),
(35, 4, 15.00, 0.00, 15.00),
(36, 1, 15.00, 0.00, 15.00),
(36, 2, 15.00, 0.00, 15.00),
(36, 3, 15.00, 0.00, 15.00),
(36, 4, 15.00, 0.00, 15.00),
(37, 1, 15.00, 0.00, 15.00),
(37, 2, 15.00, 0.00, 15.00),
(37, 3, 15.00, 0.00, 15.00),
(37, 4, 15.00, 0.00, 15.00),
(38, 1, 15.00, 0.00, 15.00),
(38, 2, 15.00, 0.00, 15.00),
(38, 3, 15.00, 0.00, 15.00),
(38, 4, 15.00, 0.00, 15.00),
(39, 1, 15.00, 0.00, 15.00),
(39, 2, 15.00, 0.00, 15.00),
(39, 3, 15.00, 0.00, 15.00),
(39, 4, 15.00, 0.00, 15.00),
(40, 1, 15.00, 0.00, 15.00),
(40, 2, 15.00, 0.00, 15.00),
(40, 3, 15.00, 0.00, 15.00),
(40, 4, 15.00, 0.00, 15.00),
(41, 1, 15.00, 0.00, 15.00),
(41, 2, 15.00, 0.00, 15.00),
(41, 3, 15.00, 0.00, 15.00),
(41, 4, 15.00, 0.00, 15.00),
(42, 1, 15.00, 0.00, 15.00),
(42, 2, 15.00, 0.00, 15.00),
(42, 3, 15.00, 0.00, 15.00),
(42, 4, 15.00, 0.00, 15.00),
(43, 1, 15.00, 0.00, 15.00),
(43, 2, 15.00, 0.00, 15.00),
(43, 3, 15.00, 0.00, 15.00),
(43, 4, 15.00, 0.00, 15.00),
(44, 1, 15.00, 0.00, 15.00),
(44, 2, 15.00, 0.00, 15.00),
(44, 3, 15.00, 0.00, 15.00),
(44, 4, 15.00, 0.00, 15.00),
(45, 1, 15.00, 0.00, 15.00),
(45, 2, 15.00, 0.00, 15.00),
(45, 3, 15.00, 0.00, 15.00),
(45, 4, 15.00, 0.00, 15.00),
(46, 1, 15.00, 0.00, 15.00),
(46, 2, 15.00, 0.00, 15.00),
(46, 3, 15.00, 0.00, 15.00),
(46, 4, 15.00, 0.00, 15.00),
(47, 1, 15.00, 0.00, 15.00),
(47, 2, 15.00, 0.00, 15.00),
(47, 3, 15.00, 0.00, 15.00),
(47, 4, 15.00, 0.00, 15.00),
(48, 1, 15.00, 0.00, 15.00),
(48, 2, 15.00, 0.00, 15.00),
(48, 3, 15.00, 0.00, 15.00),
(48, 4, 15.00, 0.00, 15.00),
(49, 1, 15.00, 0.00, 15.00),
(49, 2, 15.00, 0.00, 15.00),
(49, 3, 15.00, 0.00, 15.00),
(49, 4, 15.00, 0.00, 15.00),
(50, 1, 15.00, 0.00, 15.00),
(50, 2, 15.00, 0.00, 15.00),
(50, 3, 15.00, 0.00, 15.00),
(50, 4, 15.00, 0.00, 15.00),
(51, 1, 15.00, 0.00, 15.00),
(51, 2, 15.00, 0.00, 15.00),
(51, 3, 15.00, 0.00, 15.00),
(51, 4, 15.00, 0.00, 15.00),
(52, 1, 15.00, 0.00, 15.00),
(52, 2, 15.00, 0.00, 15.00),
(52, 3, 15.00, 0.00, 15.00),
(52, 4, 15.00, 0.00, 15.00),
(53, 1, 15.00, 0.00, 15.00),
(53, 2, 15.00, 0.00, 15.00),
(53, 3, 15.00, 0.00, 15.00),
(53, 4, 15.00, 0.00, 15.00);

-- --------------------------------------------------------

--
-- Table structure for table `leaverequests`
--

DROP TABLE IF EXISTS `leaverequests`;
CREATE TABLE `leaverequests` (
  `leaveRequestId` int(11) NOT NULL,
  `employeeId` int(11) DEFAULT NULL,
  `leaveTypeId` int(11) DEFAULT NULL,
  `startDate` date DEFAULT NULL,
  `endDate` date DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  `approved_by` int(11) DEFAULT NULL,
  `reason` varchar(500) NOT NULL DEFAULT 'No reason provided',
  `requestedDate` date NOT NULL,
  `respondedAt` datetime DEFAULT NULL,
  `attachment_path` varchar(500) DEFAULT NULL,
  `reliever` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `leavetype`
--

DROP TABLE IF EXISTS `leavetype`;
CREATE TABLE `leavetype` (
  `leaveTypeId` int(11) NOT NULL,
  `leaveName` varchar(255) NOT NULL,
  `withPay` tinyint(1) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `leavetype`
--

INSERT INTO `leavetype` (`leaveTypeId`, `leaveName`, `withPay`) VALUES
(1, 'Sick Leave', 0),
(2, 'Emergency Leave', 0),
(3, 'Service Incentive Leave', 1),
(4, 'Official Business', 0);

-- --------------------------------------------------------

--
-- Table structure for table `overtime_request`
--

DROP TABLE IF EXISTS `overtime_request`;
CREATE TABLE `overtime_request` (
  `overtime_request_id` int(11) NOT NULL,
  `employeeId` int(11) NOT NULL,
  `work_date` date NOT NULL,
  `overtime_in` time NOT NULL,
  `overtime_out` time NOT NULL,
  `total_hours` decimal(38,2) NOT NULL,
  `reason` varchar(1000) NOT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'Pending',
  `requested_at` datetime(6) NOT NULL,
  `responded_at` datetime(6) DEFAULT NULL,
  `approved_by_user_id` int(11) DEFAULT NULL,
  `attachment_path` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `pagibigtable`
--

DROP TABLE IF EXISTS `pagibigtable`;
CREATE TABLE `pagibigtable` (
  `pagibigId` int(11) NOT NULL,
  `rangeFrom` decimal(38,2) NOT NULL,
  `rangeTo` decimal(38,2) NOT NULL,
  `employeeShare` decimal(38,2) NOT NULL,
  `employerShare` decimal(38,2) NOT NULL,
  `effectiveYear` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `passwordresettokens`
--

DROP TABLE IF EXISTS `passwordresettokens`;
CREATE TABLE `passwordresettokens` (
  `tokenId` int(11) NOT NULL,
  `token` varchar(255) NOT NULL,
  `userId` int(11) NOT NULL,
  `expiryDate` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `passwordresettokens`
--

INSERT INTO `passwordresettokens` (`tokenId`, `token`, `userId`, `expiryDate`) VALUES
(25, '7d83e6f2-e065-4fea-9bee-e59220ae9143', 129, '2026-05-26 07:55:04'),
(27, '3651236e-7ac0-4ff9-9aea-6aad6b4f91a0', 131, '2026-05-26 08:53:08'),
(28, '3a91f99a-4e99-459b-adfd-f265c885bb05', 132, '2026-05-26 08:58:15'),
(29, '8bab1de7-7e89-411d-ac1c-fba93f80912b', 133, '2026-05-26 09:05:03'),
(30, '928a37c2-6fef-4b5f-bb9f-d0cf798f5f23', 134, '2026-05-26 09:09:51'),
(31, '5b913e37-8bce-4de1-9cb7-39e7d5bcd271', 135, '2026-05-26 09:14:37'),
(32, 'a3ec013c-d929-4ec4-8494-a59e0f4bf2f4', 136, '2026-05-26 09:20:12'),
(33, 'b5ce6041-4040-4b2b-897e-9fadf1e634cc', 137, '2026-05-26 09:24:25'),
(34, '22aa62bb-b61c-4008-9ea7-ce41fe88c28d', 138, '2026-05-26 09:29:32'),
(35, '04f02b4b-bda2-4f57-853a-6c18edd995d8', 140, '2026-05-26 10:26:35'),
(36, 'c68dedb8-eef7-42fd-94bf-2612fa206388', 141, '2026-05-26 10:29:41'),
(37, '5f7f2090-c0a2-4439-a0d6-4224c91dff66', 142, '2026-05-26 10:33:12'),
(38, '43814eee-3fb8-4316-821f-ed348a8712bf', 143, '2026-05-26 10:46:31'),
(39, 'ddf6b727-2aaf-4c3b-a8e2-335f6f7e600e', 144, '2026-05-26 10:54:44'),
(40, '06adf486-85ed-4a03-be00-36dadb1b6f9a', 145, '2026-05-26 11:03:41'),
(41, '050c8324-b5f4-46fe-8dcb-ccf80e8a0b78', 146, '2026-05-26 11:07:01'),
(42, 'efb66271-a6de-425b-a527-7a821777ef75', 147, '2026-05-26 11:17:09'),
(43, '0b8afc4f-629b-4d9c-8533-7988f6bcf849', 148, '2026-05-26 11:23:14'),
(44, 'fc6e4f1c-ed96-4745-8d9d-684727e157b5', 149, '2026-05-26 11:26:28'),
(45, 'f89f2e1a-182a-42eb-9e99-a579bb95b9a2', 150, '2026-05-26 11:31:56'),
(46, '8392b929-007b-4a17-b01a-7b03dc63e530', 151, '2026-05-26 11:36:59'),
(47, 'de5bc6e7-58a0-430b-b1e1-e1898d28d360', 153, '2026-05-26 11:47:13'),
(48, 'a5b7271a-0217-4201-b80f-8ca1b1bcb6d9', 154, '2026-05-26 11:50:56'),
(49, '436c12ed-a049-4517-a285-8e9bf9c9aec6', 155, '2026-05-26 11:54:05'),
(50, 'd6905b1e-d190-4a60-87d2-a811f3e9cd3d', 156, '2026-05-26 12:07:07'),
(51, 'a995ee82-3929-4513-9c34-5902f688bfc9', 157, '2026-05-26 12:11:19'),
(52, '028a936b-d37e-4098-90cb-af2b297377a8', 158, '2026-05-26 12:14:37'),
(53, '5c38f6b3-7a6f-4550-b487-19b113530db9', 159, '2026-05-26 12:20:39'),
(54, '4eb25603-20c2-4f71-82b5-a903a9823759', 160, '2026-05-26 15:09:36'),
(55, '66334a71-43a6-4a01-b313-cb022b735131', 161, '2026-05-26 15:16:49'),
(56, 'b78e58bf-f09a-4819-a4ac-f75bab9d17fc', 162, '2026-05-26 15:45:29'),
(57, '05013c01-f86f-4339-8472-54330e10549a', 163, '2026-05-26 16:03:20'),
(58, '78ebf88d-5474-442e-a09e-2f5e0c8d7572', 164, '2026-05-26 16:14:11'),
(59, '34371b88-8af0-453e-9958-f011803a026b', 165, '2026-05-26 16:31:02'),
(60, '65934858-84b6-4633-8b1e-262c96e41485', 166, '2026-05-26 16:45:13'),
(61, 'e9cdff0a-636c-47ea-99ef-b3df488a2446', 168, '2026-05-26 16:55:32'),
(62, '8371cee9-5a05-4d79-bac5-679cc9084f22', 169, '2026-05-27 09:24:01'),
(63, 'b178ae5e-c077-4eaa-85ce-2227da3bc89f', 170, '2026-05-27 09:28:04'),
(64, 'beccc5aa-6dc2-4096-b330-fa47e8d462f1', 172, '2026-05-27 09:34:51'),
(65, '5c819a35-b9a6-403d-a434-e7cc69c3cb36', 174, '2026-05-27 09:48:33'),
(66, '587ba208-b60c-4d99-9d94-3cc36044b51c', 175, '2026-05-27 09:59:55'),
(67, '38960444-1cbe-4e8f-918c-b12e00627e0b', 176, '2026-05-27 10:21:37');

-- --------------------------------------------------------

--
-- Table structure for table `payroll`
--

DROP TABLE IF EXISTS `payroll`;
CREATE TABLE `payroll` (
  `payrollId` int(11) NOT NULL,
  `payPeriodStart` date DEFAULT NULL,
  `payPeriodEnd` date DEFAULT NULL,
  `payrollType` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  `dateProcessed` date DEFAULT NULL,
  `dateReleased` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `payrollItems`
--

DROP TABLE IF EXISTS `payrollItems`;
CREATE TABLE `payrollItems` (
  `payrollItemId` int(11) NOT NULL,
  `payrollId` int(11) NOT NULL,
  `employeeId` int(11) NOT NULL,
  `basicPay` decimal(38,2) NOT NULL,
  `overtimePay` decimal(38,2) NOT NULL,
  `holidayPay` decimal(38,2) NOT NULL,
  `allowances` decimal(38,2) NOT NULL,
  `grossPay` decimal(38,2) NOT NULL,
  `sss` decimal(38,2) NOT NULL,
  `philhealth` decimal(38,2) NOT NULL,
  `pagibig` decimal(38,2) NOT NULL,
  `tax` decimal(38,2) NOT NULL,
  `otherDeductions` decimal(38,2) NOT NULL,
  `totalDeductions` decimal(38,2) NOT NULL,
  `netPay` decimal(38,2) NOT NULL,
  `dailyRate` decimal(38,2) DEFAULT NULL,
  `hourlyRate` decimal(38,2) DEFAULT NULL,
  `perMinuteRate` decimal(38,2) DEFAULT NULL,
  `totalWorkedHours` decimal(38,2) DEFAULT NULL,
  `totalOtHours` decimal(38,2) DEFAULT NULL,
  `lateUndertimeMinutes` int(11) DEFAULT NULL,
  `lateUndertimeDeduction` decimal(38,2) DEFAULT NULL,
  `cashAdvance` decimal(38,2) DEFAULT NULL,
  `adjustmentEarnings` decimal(38,2) DEFAULT NULL,
  `adjustmentDeductions` decimal(38,2) DEFAULT NULL,
  `totalEarnings` decimal(38,2) DEFAULT NULL,
  `serviceFee` decimal(38,2) DEFAULT NULL,
  `semiMonthlyContributions` decimal(38,2) DEFAULT NULL,
  `employmentType` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `payrollitems`
--

DROP TABLE IF EXISTS `payrollitems`;
CREATE TABLE `payrollitems` (
  `payrollItemId` int(11) NOT NULL,
  `payrollId` int(11) DEFAULT NULL,
  `employeeId` int(11) DEFAULT NULL,
  `basicPay` decimal(38,2) NOT NULL,
  `overtimePay` decimal(38,2) NOT NULL,
  `holidayPay` decimal(38,2) NOT NULL,
  `allowances` decimal(38,2) NOT NULL,
  `grossPay` decimal(38,2) NOT NULL,
  `sss` decimal(38,2) NOT NULL,
  `philhealth` decimal(38,2) NOT NULL,
  `pagibig` decimal(38,2) NOT NULL,
  `tax` decimal(38,2) NOT NULL,
  `lateUndertimeDeduction` decimal(38,2) DEFAULT NULL,
  `otherDeductions` decimal(38,2) NOT NULL,
  `totalDeductions` decimal(38,2) NOT NULL,
  `netPay` decimal(38,2) NOT NULL,
  `adjustmentDeductions` decimal(38,2) DEFAULT NULL,
  `adjustmentEarnings` decimal(38,2) DEFAULT NULL,
  `cashAdvance` decimal(38,2) DEFAULT NULL,
  `dailyRate` decimal(38,2) DEFAULT NULL,
  `employmentType` varchar(255) DEFAULT NULL,
  `hourlyRate` decimal(38,2) DEFAULT NULL,
  `lateUndertimeMinutes` int(11) DEFAULT NULL,
  `perMinuteRate` decimal(38,2) DEFAULT NULL,
  `semiMonthlyContributions` decimal(38,2) DEFAULT NULL,
  `serviceFee` decimal(38,2) DEFAULT NULL,
  `totalEarnings` decimal(38,2) DEFAULT NULL,
  `totalOtHours` decimal(38,2) DEFAULT NULL,
  `totalWorkedHours` decimal(38,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `philhealthtable`
--

DROP TABLE IF EXISTS `philhealthtable`;
CREATE TABLE `philhealthtable` (
  `philhealthId` int(11) NOT NULL,
  `rangeFrom` decimal(38,2) NOT NULL,
  `rangeTo` decimal(38,2) NOT NULL,
  `employeeShare` decimal(38,2) NOT NULL,
  `employerShare` decimal(38,2) NOT NULL,
  `effectiveYear` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `positions`
--

DROP TABLE IF EXISTS `positions`;
CREATE TABLE `positions` (
  `positionId` int(11) NOT NULL,
  `positionName` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `positions`
--

INSERT INTO `positions` (`positionId`, `positionName`) VALUES
(1, 'Manager'),
(2, 'Software Engineer'),
(3, 'Accountant'),
(4, 'HR Specialist'),
(5, 'Sales Representative'),
(6, 'Operations Staff'),
(26, 'HR and Admin Assistant'),
(27, 'Chargen Operator'),
(28, 'Accounting and Admin Assistant'),
(29, 'Technical Director'),
(30, 'JUNIOR ON-CAM REPORTER'),
(31, 'Producer'),
(32, 'Graphic Artist and Operator'),
(33, 'Camera Man'),
(34, 'Video Editor'),
(35, 'Audio Man'),
(36, 'News Desk Head'),
(37, 'Production Coordinator'),
(38, 'Social Media Assistant'),
(39, 'General Services and Maintenance Staff'),
(40, 'Multimedia Producer'),
(41, 'Light Man'),
(42, 'Broadcast IT Technician'),
(43, 'Production Assistant'),
(44, 'Program Director'),
(45, 'Researcher and Writer'),
(46, 'Systems Engineer');

-- --------------------------------------------------------

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
  `roleId` int(11) NOT NULL,
  `roleName` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `roles`
--

INSERT INTO `roles` (`roleId`, `roleName`, `description`) VALUES
(1, 'ADMIN', 'System administrator with full access'),
(2, 'EMPLOYEE', 'Regular employee with limited acess'),
(3, 'HR', 'Manage employee records'),
(4, 'PAYROLL', 'Process payroll'),
(5, 'MANAGER', 'Approve employee records'),
(6, 'SUPERVISOR', 'Approve employee leave');

-- --------------------------------------------------------

--
-- Table structure for table `shifts`
--

DROP TABLE IF EXISTS `shifts`;
CREATE TABLE `shifts` (
  `shiftId` int(11) NOT NULL,
  `shiftName` varchar(255) NOT NULL,
  `time_in` time DEFAULT NULL,
  `time_out` time DEFAULT NULL,
  `remarks` varchar(255) DEFAULT NULL,
  `monday` tinyint(1) NOT NULL DEFAULT 0,
  `tuesday` tinyint(1) NOT NULL DEFAULT 0,
  `wednesday` tinyint(1) NOT NULL DEFAULT 0,
  `thursday` tinyint(1) NOT NULL DEFAULT 0,
  `friday` tinyint(1) NOT NULL DEFAULT 0,
  `saturday` tinyint(1) NOT NULL DEFAULT 0,
  `sunday` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ssstable`
--

DROP TABLE IF EXISTS `ssstable`;
CREATE TABLE `ssstable` (
  `sssId` int(11) NOT NULL,
  `rangeFrom` decimal(38,2) NOT NULL,
  `rangeTo` decimal(38,2) NOT NULL,
  `employeeShare` decimal(38,2) NOT NULL,
  `employerShare` decimal(38,2) NOT NULL,
  `effectiveYear` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `ssstable`
--

INSERT INTO `ssstable` (`sssId`, `rangeFrom`, `rangeTo`, `employeeShare`, `employerShare`, `effectiveYear`) VALUES
(1, 0.00, 4249.99, 180.00, 380.00, 2024),
(2, 4250.00, 4749.99, 202.50, 427.50, 2024),
(3, 4750.00, 5249.99, 225.00, 475.00, 2024),
(4, 5250.00, 5749.99, 247.50, 522.50, 2024),
(5, 5750.00, 6249.99, 270.00, 570.00, 2024),
(6, 6250.00, 6749.99, 292.50, 617.50, 2024),
(7, 6750.00, 7249.99, 315.00, 665.00, 2024),
(8, 7250.00, 7749.99, 337.50, 712.50, 2024),
(9, 7750.00, 8249.99, 360.00, 760.00, 2024),
(10, 8250.00, 8749.99, 382.50, 807.50, 2024),
(11, 8750.00, 9249.99, 405.00, 855.00, 2024),
(12, 9250.00, 9749.99, 427.50, 902.50, 2024),
(13, 9750.00, 10249.99, 450.00, 950.00, 2024),
(14, 10250.00, 10749.99, 472.50, 997.50, 2024),
(15, 10750.00, 11249.99, 495.00, 1045.00, 2024),
(16, 11250.00, 11749.99, 517.50, 1092.50, 2024),
(17, 11750.00, 12249.99, 540.00, 1140.00, 2024),
(18, 12250.00, 12749.99, 562.50, 1187.50, 2024),
(19, 12750.00, 13249.99, 585.00, 1235.00, 2024),
(20, 13250.00, 13749.99, 607.50, 1282.50, 2024),
(21, 13750.00, 14249.99, 630.00, 1330.00, 2024),
(22, 14250.00, 14749.99, 652.50, 1377.50, 2024),
(23, 14750.00, 15249.99, 675.00, 1425.00, 2024),
(24, 15250.00, 15749.99, 697.50, 1472.50, 2024),
(25, 15750.00, 16249.99, 720.00, 1520.00, 2024),
(26, 16250.00, 16749.99, 742.50, 1567.50, 2024),
(27, 16750.00, 17249.99, 765.00, 1615.00, 2024),
(28, 17250.00, 17749.99, 787.50, 1662.50, 2024),
(29, 17750.00, 18249.99, 810.00, 1710.00, 2024),
(30, 18250.00, 18749.99, 832.50, 1757.50, 2024),
(31, 18750.00, 19249.99, 855.00, 1805.00, 2024),
(32, 19250.00, 19749.99, 877.50, 1852.50, 2024),
(33, 19750.00, 20249.99, 900.00, 1900.00, 2024),
(34, 20250.00, 20749.99, 922.50, 1947.50, 2024),
(35, 20750.00, 21249.99, 945.00, 1995.00, 2024),
(36, 21250.00, 21749.99, 967.50, 2042.50, 2024),
(37, 21750.00, 22249.99, 990.00, 2090.00, 2024),
(38, 22250.00, 22749.99, 1012.50, 2137.50, 2024),
(39, 22750.00, 23249.99, 1035.00, 2185.00, 2024),
(40, 23250.00, 23749.99, 1057.50, 2232.50, 2024),
(41, 23750.00, 24249.99, 1080.00, 2280.00, 2024),
(42, 24250.00, 24749.99, 1102.50, 2327.50, 2024),
(43, 24750.00, 25249.99, 1125.00, 2375.00, 2024),
(44, 25250.00, 25749.99, 1147.50, 2422.50, 2024),
(45, 25750.00, 26249.99, 1170.00, 2470.00, 2024),
(46, 26250.00, 26749.99, 1192.50, 2517.50, 2024),
(47, 26750.00, 27249.99, 1215.00, 2565.00, 2024),
(48, 27250.00, 27749.99, 1237.50, 2612.50, 2024),
(49, 27750.00, 28249.99, 1260.00, 2660.00, 2024),
(50, 28250.00, 28749.99, 1282.50, 2707.50, 2024),
(51, 28750.00, 29249.99, 1305.00, 2755.00, 2024),
(52, 29250.00, 29749.99, 1327.50, 2802.50, 2024),
(53, 29750.00, 999999.99, 1350.00, 2850.00, 2024),
(54, 0.00, 5249.99, 250.00, 500.00, 2025),
(55, 5250.00, 5749.99, 275.00, 550.00, 2025),
(56, 5750.00, 6249.99, 300.00, 600.00, 2025),
(57, 6250.00, 6749.99, 325.00, 650.00, 2025),
(58, 6750.00, 7249.99, 350.00, 700.00, 2025),
(59, 7250.00, 7749.99, 375.00, 750.00, 2025),
(60, 7750.00, 8249.99, 400.00, 800.00, 2025),
(61, 8250.00, 8749.99, 425.00, 850.00, 2025),
(62, 8750.00, 9249.99, 450.00, 900.00, 2025),
(63, 9250.00, 9749.99, 475.00, 950.00, 2025),
(64, 9750.00, 10249.99, 500.00, 1000.00, 2025),
(65, 10250.00, 10749.99, 525.00, 1050.00, 2025),
(66, 10750.00, 11249.99, 550.00, 1100.00, 2025),
(67, 11250.00, 11749.99, 575.00, 1150.00, 2025),
(68, 11750.00, 12249.99, 600.00, 1200.00, 2025),
(69, 12250.00, 12749.99, 625.00, 1250.00, 2025),
(70, 12750.00, 13249.99, 650.00, 1300.00, 2025),
(71, 13250.00, 13749.99, 675.00, 1350.00, 2025),
(72, 13750.00, 14249.99, 700.00, 1400.00, 2025),
(73, 14250.00, 14749.99, 725.00, 1450.00, 2025),
(74, 14750.00, 15249.99, 750.00, 1500.00, 2025),
(75, 15250.00, 15749.99, 775.00, 1550.00, 2025),
(76, 15750.00, 16249.99, 800.00, 1600.00, 2025),
(77, 16250.00, 16749.99, 825.00, 1650.00, 2025),
(78, 16750.00, 17249.99, 850.00, 1700.00, 2025),
(79, 17250.00, 17749.99, 875.00, 1750.00, 2025),
(80, 17750.00, 18249.99, 900.00, 1800.00, 2025),
(81, 18250.00, 18749.99, 925.00, 1850.00, 2025),
(82, 18750.00, 19249.99, 950.00, 1900.00, 2025),
(83, 19250.00, 19749.99, 975.00, 1950.00, 2025),
(84, 19750.00, 20249.99, 1000.00, 2000.00, 2025),
(85, 20250.00, 20749.99, 1025.00, 2050.00, 2025),
(86, 20750.00, 21249.99, 1050.00, 2100.00, 2025),
(87, 21250.00, 21749.99, 1075.00, 2150.00, 2025),
(88, 21750.00, 22249.99, 1100.00, 2200.00, 2025),
(89, 22250.00, 22749.99, 1125.00, 2250.00, 2025),
(90, 22750.00, 23249.99, 1150.00, 2300.00, 2025),
(91, 23250.00, 23749.99, 1175.00, 2350.00, 2025),
(92, 23750.00, 24249.99, 1200.00, 2400.00, 2025),
(93, 24250.00, 24749.99, 1225.00, 2450.00, 2025),
(94, 24750.00, 25249.99, 1250.00, 2500.00, 2025),
(95, 25250.00, 25749.99, 1275.00, 2550.00, 2025),
(96, 25750.00, 26249.99, 1300.00, 2600.00, 2025),
(97, 26250.00, 26749.99, 1325.00, 2650.00, 2025),
(98, 26750.00, 27249.99, 1350.00, 2700.00, 2025),
(99, 27250.00, 27749.99, 1375.00, 2750.00, 2025),
(100, 27750.00, 28249.99, 1400.00, 2800.00, 2025),
(101, 28250.00, 28749.99, 1425.00, 2850.00, 2025),
(102, 28750.00, 29249.99, 1450.00, 2900.00, 2025),
(103, 29250.00, 29749.99, 1475.00, 2950.00, 2025),
(104, 29750.00, 30249.99, 1500.00, 3000.00, 2025),
(105, 30250.00, 30749.99, 1525.00, 3050.00, 2025),
(106, 30750.00, 31249.99, 1550.00, 3100.00, 2025),
(107, 31250.00, 31749.99, 1575.00, 3150.00, 2025),
(108, 31750.00, 32249.99, 1600.00, 3200.00, 2025),
(109, 32250.00, 32749.99, 1625.00, 3250.00, 2025),
(110, 32750.00, 33249.99, 1650.00, 3300.00, 2025),
(111, 33250.00, 33749.99, 1675.00, 3350.00, 2025),
(112, 33750.00, 34249.99, 1700.00, 3400.00, 2025),
(113, 34250.00, 34749.99, 1725.00, 3450.00, 2025),
(114, 34750.00, 999999.99, 1750.00, 3500.00, 2025),
(177, 20000.00, 29000.00, 1000.00, 1000.00, 2026),
(178, 11000.00, 19500.00, 975.00, 975.00, 2026),
(179, 30000.00, 34500.00, 1600.00, 1600.00, 2026),
(180, 35000.00, 55000.00, 1750.00, 1750.00, 2026);

-- --------------------------------------------------------

--
-- Table structure for table `taxtable`
--

DROP TABLE IF EXISTS `taxtable`;
CREATE TABLE `taxtable` (
  `taxId` int(11) NOT NULL,
  `compensationFrom` decimal(38,2) NOT NULL,
  `compensationTo` decimal(38,2) NOT NULL,
  `taxRate` decimal(38,2) NOT NULL,
  `additionalTax` decimal(38,2) NOT NULL,
  `effectiveYear` int(11) NOT NULL,
  `pay_frequency` varchar(32) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `taxtable`
--

INSERT INTO `taxtable` (`taxId`, `compensationFrom`, `compensationTo`, `taxRate`, `additionalTax`, `effectiveYear`, `pay_frequency`) VALUES
(1, 1.00, 684.99, 0.00, 0.00, 2026, 'DAILY'),
(2, 685.00, 1095.99, 0.15, 0.00, 2026, 'DAILY'),
(3, 1096.00, 2191.99, 0.20, 61.65, 2026, 'DAILY'),
(4, 2192.00, 5478.99, 0.25, 280.85, 2026, 'DAILY'),
(5, 5479.00, 21917.99, 0.30, 1102.60, 2026, 'DAILY'),
(6, 21918.00, 9999999.99, 0.35, 6034.00, 2026, 'DAILY'),
(7, 1.00, 4807.99, 0.00, 0.00, 2026, 'WEEKLY'),
(8, 4808.00, 7691.99, 0.15, 0.00, 2026, 'WEEKLY'),
(9, 7692.00, 15384.99, 0.20, 432.60, 2026, 'WEEKLY'),
(10, 15385.00, 38461.99, 0.25, 1971.20, 2026, 'WEEKLY'),
(11, 38462.00, 153845.99, 0.30, 7740.45, 2026, 'WEEKLY'),
(12, 153846.00, 9999999.99, 0.35, 42355.65, 2026, 'WEEKLY'),
(13, 1.00, 10416.99, 0.00, 0.00, 2026, 'SEMI_MONTHLY'),
(14, 10417.00, 16666.99, 0.15, 0.00, 2026, 'SEMI_MONTHLY'),
(15, 16667.00, 33332.99, 0.20, 937.50, 2026, 'SEMI_MONTHLY'),
(16, 33333.00, 83332.99, 0.25, 4270.70, 2026, 'SEMI_MONTHLY'),
(17, 83333.00, 333332.99, 0.30, 16770.70, 2026, 'SEMI_MONTHLY'),
(18, 333333.00, 9999999.99, 0.35, 91770.70, 2026, 'SEMI_MONTHLY'),
(19, 1.00, 20832.99, 0.00, 0.00, 2026, 'MONTHLY'),
(20, 20833.00, 33332.99, 0.15, 0.00, 2026, 'MONTHLY'),
(21, 33333.00, 66666.99, 0.20, 1875.00, 2026, 'MONTHLY'),
(22, 66667.00, 166666.99, 0.25, 8541.80, 2026, 'MONTHLY'),
(23, 166667.00, 666666.99, 0.30, 33541.80, 2026, 'MONTHLY'),
(24, 666667.00, 9999999.99, 0.35, 183541.80, 2026, 'MONTHLY'),
(25, 1.00, 249999.99, 0.00, 0.00, 2026, 'ANNUALLY'),
(26, 250000.00, 399999.99, 0.15, 0.00, 2026, 'ANNUALLY'),
(27, 400000.00, 799999.99, 0.20, 22500.00, 2026, 'ANNUALLY'),
(28, 800000.00, 1999999.99, 0.25, 102500.00, 2026, 'ANNUALLY'),
(29, 2000000.00, 7999999.99, 0.30, 402500.00, 2026, 'ANNUALLY'),
(30, 8000000.00, 9999999.99, 0.35, 2202500.00, 2026, 'ANNUALLY');

-- --------------------------------------------------------

--
-- Table structure for table `undertime_requests`
--

DROP TABLE IF EXISTS `undertime_requests`;
CREATE TABLE `undertime_requests` (
  `undertime_request_id` int(11) NOT NULL,
  `employeeId` int(11) NOT NULL,
  `request_date` date NOT NULL,
  `total_hours` decimal(4,2) NOT NULL,
  `reason` varchar(500) NOT NULL,
  `reliever` varchar(255) DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'Pending',
  `requested_at` datetime NOT NULL,
  `responded_at` datetime DEFAULT NULL,
  `approved_by_user_id` int(11) DEFAULT NULL,
  `denial_reason` varchar(500) DEFAULT NULL,
  `attachment_path` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `undertime_requests`
--

INSERT INTO `undertime_requests` (`undertime_request_id`, `employeeId`, `request_date`, `total_hours`, `reason`, `reliever`, `status`, `requested_at`, `responded_at`, `approved_by_user_id`, `denial_reason`, `attachment_path`) VALUES
(1, 4, '2026-05-25', 8.00, 'try only', 'JENNIFER DELA CRUZ', 'Rejected', '2026-05-24 19:49:26', '2026-05-24 19:50:37', 1, 'no attachment provided', 'uploads/undertime_attachments/1779623366576_25a6adec-dac3-49d6-914d-00dc31cfc5d4.jfif');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `userId` int(11) NOT NULL,
  `employeeId` int(11) DEFAULT NULL,
  `passwordHash` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `roleId` int(11) NOT NULL,
  `isActive` tinyint(1) DEFAULT 1,
  `lastLogin` datetime DEFAULT NULL,
  `createdAt` datetime DEFAULT current_timestamp(),
  `updatedAt` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `lastLeaveViewedAt` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`userId`, `employeeId`, `passwordHash`, `email`, `roleId`, `isActive`, `lastLogin`, `createdAt`, `updatedAt`, `lastLeaveViewedAt`) VALUES
(1, 1, '$2a$10$Z1bUfjixBtmdwNEzeHqqKeUuviKdC35HAguumaNXTeHVN0Nq7Ks2u', 'admin@d8tvnews.com', 1, 1, '2026-05-27 19:09:06', '2026-02-12 15:07:29', '2026-05-27 19:09:06', '2026-05-23 05:00:33'),
(127, 4, '$2a$10$mcJPUYz1ek7w5q6oCSVc9.KmRHG8zgQBEBsUG4nb2HWIKMUNjo.qO', 'remiendotristen@gmail.com', 1, 1, '2026-05-28 11:24:02', '2026-05-23 20:40:39', '2026-05-28 11:24:02', '2026-05-24 19:51:00'),
(128, 5, '$2a$10$0ks6sr2FkTG4DV0GB9g.7OGcWzpXVuQuWpvaFRl3mLJcZFmEzHsg6', 'jnnferrer23@gmail.com', 1, 1, '2026-05-24 17:08:45', '2026-05-24 16:59:00', '2026-05-24 17:08:45', NULL),
(129, 6, '$2a$10$mmq4oM/36UBcYI8/6Y5ib.xTo09iGKkQB.mvHRtvYouNIm8IZqsPa', 'rfabane20@gmail.com', 2, 1, NULL, '2026-05-25 07:55:03', '2026-05-25 07:55:03', NULL),
(130, 7, '$2a$10$ygXWEgeAqgR80B7QDVXZIu1d5nLz8NeGT/19LalN7zGtKcFAL3k5O', 'sachikoike2119@gmail.com', 1, 1, '2026-05-25 10:05:49', '2026-05-25 08:20:10', '2026-05-25 10:05:49', NULL),
(131, 8, '$2a$10$g3S.r.hNjXg4.Ecj5i2sQ.Le0rnwlLonIZ6OjFINVGBSlA7MgUXPa', 'cidmar100976@gmail.com', 2, 1, NULL, '2026-05-25 08:53:07', '2026-05-25 08:53:07', NULL),
(132, 9, '$2a$10$40LfoogD.pvatlTAbkYJwuqCeFQy397MBs/djZ7KWRcnFTdMCckm.', 'judealynhbernardo@gmail.com', 2, 1, NULL, '2026-05-25 08:58:14', '2026-05-25 08:58:14', NULL),
(133, 10, '$2a$10$HN09r7Pm3IlkYW3QJFaOLeg1I1T4LAXpHK9UEIXFKsGsWYGOOan8e', 'ptv4andresbonifaciojr@gmail.com', 2, 1, NULL, '2026-05-25 09:05:02', '2026-05-25 09:05:02', NULL),
(134, 11, '$2a$10$Nx1dmOObRkDEjeyJabC6zO0yR3aB4pHv/5h9gF3/Q0/HNMMTcrNEG', 'jon.capule@gmail.com', 2, 1, NULL, '2026-05-25 09:09:51', '2026-05-25 09:09:51', NULL),
(135, 12, '$2a$10$wTg/94eqb0E0b8WkkY8juOLJuRpxzjdZ/.rXpE41SfP3epHD8JjSS', 'jonelcarpio99@gmail.com', 2, 1, NULL, '2026-05-25 09:14:37', '2026-05-25 09:14:37', NULL),
(136, 13, '$2a$10$HU.gysgtwfkLbVYiVL3Uv.7Z9YiT42ydx967bk6sL9Dmn6MbsLn9G', 'jeffreycarilla.cha@gmail.com', 2, 1, NULL, '2026-05-25 09:20:11', '2026-05-25 09:20:11', NULL),
(137, 14, '$2a$10$EOkS6xGhwt/VkAdy1FFVAu9VAq6OZirrY957DqIWWsk.SoWjgw/OS', 'greyface89@gmail.com', 2, 1, NULL, '2026-05-25 09:24:25', '2026-05-25 09:24:25', NULL),
(138, 15, '$2a$10$jTMKvjDKL2yn1bau/lJde.a6n2kz17t1JM68Z7QuX9GwjRl0givcG', 'japdayao@gmail.com', 2, 1, NULL, '2026-05-25 09:29:31', '2026-05-25 09:29:31', NULL),
(139, 16, '$2a$10$lOnXiQIvw2XX7XJAR/Utb.wtK6pafnqSZQByY4h2cYoKQssrwmlou', 'djowen15@yahoo.com', 2, 1, NULL, '2026-05-25 10:04:54', '2026-05-25 10:04:54', NULL),
(140, 17, '$2a$10$ji1TgHGkOY4M2eVsDCucweUVvNmzSILO9xposkGDrnLqYzcyaklXi', 'jasondelacruz1120@gmail.com', 2, 1, NULL, '2026-05-25 10:26:34', '2026-05-25 10:26:34', NULL),
(141, 18, '$2a$10$CNYAbZFF6E2OXFRnBDFOPuuIcwevB09nT4OJ6RoifGyTD1QHK95iK', 'allandoc15@gmail.com', 2, 1, NULL, '2026-05-25 10:29:40', '2026-05-25 10:29:40', NULL),
(142, 19, '$2a$10$KJGzaKm6IeCKXbrAjk3QS.jyswcXGI.ZDoML/Or1Jw224GIQ55Y2e', 'epoyfuderanan@gmail.com', 2, 1, NULL, '2026-05-25 10:33:11', '2026-05-25 10:33:11', NULL),
(143, 20, '$2a$10$LkFC4ZkstpRV/.6GAOkrMepxMh.DJ8deKq9xoS59mf430j1Yxs28q', 'armandgante@gmail.com', 2, 1, NULL, '2026-05-25 10:46:31', '2026-05-25 10:46:31', NULL),
(144, 21, '$2a$10$MCd37Ug6pu1e5rstJchwmOaxYsD./3KXBFJ9Cv9kzb3pGchpBigfe', 'gtchlnimeemarie@gmail.com', 2, 1, NULL, '2026-05-25 10:54:43', '2026-05-25 10:54:43', NULL),
(145, 22, '$2a$10$Ddg/uDuEukZNtFEcDEKaLuTvC7cusJvcH1p1y3xWPgti2SB62Nde2', 'Ramesesgatinga@gmail.com', 2, 1, NULL, '2026-05-25 11:03:40', '2026-05-25 11:03:40', NULL),
(146, 23, '$2a$10$ABgHZKEzL47hBelUBRDECupJrOe22NnYMM/SRuzb9xhNAc82JJyS2', 'princeandreigeneta@gmail.com', 2, 1, NULL, '2026-05-25 11:07:00', '2026-05-25 11:07:00', NULL),
(147, 24, '$2a$10$dCKShzscLbDp/p6PoTw2AemQckBuuNE37KGdNZ8./PZuoTsl9k7Ri', 'shamskie03@gmail.com', 2, 1, NULL, '2026-05-25 11:17:09', '2026-05-25 11:17:09', NULL),
(148, 25, '$2a$10$Yc1fAEqB0Si4Z1g0NTG9tuAHEqa.omgmCRKf5DyGhVS/5Qd0lyUQu', 'gonzalesevanlee@gmail.com', 2, 1, NULL, '2026-05-25 11:23:14', '2026-05-25 11:23:14', NULL),
(149, 26, '$2a$10$19bz8piecWArX3BJ2u1YdeMx7PH3Z1HPMniU.G6c8hTabKdSfsGAi', 'gabriellehope6@gmail.com', 2, 1, NULL, '2026-05-25 11:26:27', '2026-05-25 11:26:27', NULL),
(150, 27, '$2a$10$QCQf891XAzh.tTOwZZv3KuTp3shMVriDhIUVHtw5zA.6T.N4dRATW', 'dennisgomezhortilano27@gmail.com', 2, 1, NULL, '2026-05-25 11:31:55', '2026-05-25 11:31:55', NULL),
(151, 28, '$2a$10$P962XGvPfbrSCp6aeBaFwuaNp4lKStMvwcE1Fjw00XIrnY2uHGBKq', 'ajcibanez@gmail.com', 2, 1, NULL, '2026-05-25 11:36:58', '2026-05-25 11:36:58', NULL),
(152, 29, '$2a$10$sWI98gGYrAzc.tjJw955xeEnwjg80bgWbFbXB.PxlTE/9YzTH91eS', 'david_jalandoni@yahoo.com', 2, 1, NULL, '2026-05-25 11:40:27', '2026-05-25 11:40:27', NULL),
(153, 30, '$2a$10$RYfIdHyun1EwNjtEQWkKLe/PRusCtLV/2oI7qBWD6eaVmek26MR.O', 'juanzoace.11.aj@gmail.com', 2, 1, NULL, '2026-05-25 11:47:13', '2026-05-25 11:47:13', NULL),
(154, 31, '$2a$10$vpZ4Slm213hze6/.5eVz0O/BKTACc0.8pBcSMH1afl4yfL3Wj29k.', 'rolandlapinig45@gmail.com', 2, 1, NULL, '2026-05-25 11:50:55', '2026-05-25 11:50:55', NULL),
(155, 32, '$2a$10$xxCK88iqdyqapNIRXrZRPechYw2ZjyjlyKzPOwoJhglG7GEX3Ygk6', 'lapinigruel16@gmail.com', 2, 1, NULL, '2026-05-25 11:54:04', '2026-05-25 11:54:04', NULL),
(156, 33, '$2a$10$f7JEn7ROmy87QtmQKcWBoumKNa7aqQiYJVoWWqSGh5.xcdRUQQ6wy', 'ledesmajess28@gmail.com', 2, 1, NULL, '2026-05-25 12:07:06', '2026-05-25 12:07:06', NULL),
(157, 34, '$2a$10$VSiUKssqUYAHLcAyyxXOkuiwItRYPNZVb0Vx48M7o9recJmxTscCG', 'mr.patrickjohnlim@gmail.com', 2, 1, NULL, '2026-05-25 12:11:18', '2026-05-25 12:11:18', NULL),
(158, 35, '$2a$10$Ylcr2KoRqUmmjqJTYc01KOVI/8.aBycC2C7LrDLTZilsb6Lb9B8VK', 'jowanama.luna@gmail.com', 2, 1, NULL, '2026-05-25 12:14:36', '2026-05-25 12:14:36', NULL),
(159, 36, '$2a$10$/n.Mep3N03syVAkHszos8ee7OgnVp/AEgwLz2pj6PzfU.xC/gkIGy', 'chariemmagapan@gmail.com', 2, 1, NULL, '2026-05-25 12:20:38', '2026-05-25 12:20:38', NULL),
(160, 37, '$2a$10$S1ViIo9Hj5eytcNogRyY4e9BnPcS1gwGVitOhrZxNH9zBin8lj.QG', 'manilajoei08@gmail.com', 2, 1, NULL, '2026-05-25 15:09:36', '2026-05-25 15:09:36', NULL),
(161, 38, '$2a$10$6GiRdhV4PyFMorX0qv.IMu/ZHqurZRUYe.BU8X3BBqFJ/ipRArkT6', 'rosemariemanozo79@gmail.com', 2, 1, NULL, '2026-05-25 15:16:47', '2026-05-25 15:16:47', NULL),
(162, 39, '$2a$10$n5oYppANrWH2JNQU.gKEe.fWyb6feNqWv7sEIfLZs.XI7xotpWEUe', 'chadnefiel64@gmail.com', 2, 1, NULL, '2026-05-25 15:45:28', '2026-05-25 15:45:28', NULL),
(163, 40, '$2a$10$TmDXcnuPp1gMJb1yH9bFmeYYGmc.DGQ.RC/EV3C1F9aGlk7v/RB/2', 'chanmenesesph1994@gmail.com', 2, 1, NULL, '2026-05-25 16:03:19', '2026-05-25 16:03:19', NULL),
(164, 41, '$2a$10$VNFHb9/UA86LQagiPFn41.wIqGjXcf/68aDvx/cW2tEzYC2ZdvNPa', 'JEOMARICP@GMAIL.COM', 2, 1, NULL, '2026-05-25 16:14:10', '2026-05-25 16:14:10', NULL),
(165, 42, '$2a$10$92XjubaO2bEYcn4FkuRIPuSRnxqRgfqSseFEHeeXuSZ3T6L4jO2ja', 'christianvicente50@gmail.com', 2, 1, NULL, '2026-05-25 16:31:01', '2026-05-25 16:31:01', NULL),
(166, 43, '$2a$10$MQA8J0qJTf.6iFTOsqYKnuTWojUoWyGRMcsy8zv3MLUXDtlxkqzzG', 'randy.rapha.recato@gmail.com', 2, 1, NULL, '2026-05-25 16:45:12', '2026-05-25 16:45:12', NULL),
(167, 44, '$2a$10$c3qlicOCmbU7s1y8k08na.rBomsbOWYW2kv2QoOB7W50evcILKff6', 'ricohermoso_genelyn@yahoo.com', 2, 1, NULL, '2026-05-25 16:49:44', '2026-05-25 16:49:44', NULL),
(168, 45, '$2a$10$n3AjdirU5ayrQmiBVVmunecvfRbJlVSWvKYxhJB5/huvpaaiZG1Qu', 'jason63192003@gmail.com', 2, 1, NULL, '2026-05-25 16:55:31', '2026-05-25 16:55:31', NULL),
(169, 46, '$2a$10$F4TRB8KVSL/FUMIlKNjxpuP.w72balzx7wGfVOkzgSPSIe3TMmSNi', 'direkcarby@gmail.com', 2, 1, NULL, '2026-05-26 09:24:00', '2026-05-26 09:24:00', NULL),
(170, 47, '$2a$10$ZwTJI.l9oiW3o4pybgznnOwgkeO3iX2flM4PtXfQvVcnIipNNN0Hu', 'alfirdausghazisarip@gmail.com', 2, 1, NULL, '2026-05-26 09:28:03', '2026-05-26 09:28:03', NULL),
(171, 48, '$2a$10$jxuATNA1cd9Bzq9aO/aVjO25jTgKI/0EHR.je6kHKcWSYCQEpoWJK', 'allanortega_8@yahoo.com', 2, 1, NULL, '2026-05-26 09:31:39', '2026-05-26 09:31:39', NULL),
(172, 49, '$2a$10$9XiwzN.n4pXouq2H6MjMHuIr0YYJI.QfBKZoGmy13cAnOM2xQeFVa', 'ogielair08@gmail.com', 2, 1, NULL, '2026-05-26 09:34:50', '2026-05-26 09:34:50', NULL),
(173, 50, '$2a$10$ay4UO5GXkR85N5op4iRCVe.WXyn0y.DG4A5Uf18rRYhwJrN76uieW', 'ramil_stodomingo@yahoo.com', 2, 1, NULL, '2026-05-26 09:39:06', '2026-05-26 09:39:06', NULL),
(174, 51, '$2a$10$ZSZyPpfp8C5eJJ6guUFPpevEyUmbV2AOLaQd9yOuKppoek5cWCJEK', 'hr.d8manila@gmail.com', 2, 1, NULL, '2026-05-26 09:48:32', '2026-05-26 09:48:32', NULL),
(175, 52, '$2a$10$W84uDVQs0PvvWqX2VAfnsemfM6RDMhtCsKBnn2V6uxzGVTgynyOnO', 'ninongelmer3150@gmail.com', 2, 1, NULL, '2026-05-26 09:59:55', '2026-05-26 09:59:55', NULL),
(176, 53, '$2a$10$nD1XV.pqx5lwuQPHJIQw3.OqDx9nK.y2kHphY7ycdw3vcEANn0Sfa', 'mv0001270@gmail.com', 2, 1, NULL, '2026-05-26 10:21:36', '2026-05-26 10:21:36', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `weekly_schedule_template`
--

DROP TABLE IF EXISTS `weekly_schedule_template`;
CREATE TABLE `weekly_schedule_template` (
  `template_id` int(11) NOT NULL,
  `template_name` varchar(255) NOT NULL,
  `schedule_year` int(11) NOT NULL,
  `schedule_month` int(11) NOT NULL,
  `indefinite` bit(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `weekly_schedule_template`
--

INSERT INTO `weekly_schedule_template` (`template_id`, `template_name`, `schedule_year`, `schedule_month`, `indefinite`) VALUES
(18, 'Try', 2026, 4, b'1'),
(20, 'Mezzanine 8:00-5:00', 2026, 5, b'1'),
(21, 'Mezzanine 9:00-6:00', 2026, 5, b'1'),
(22, 'Mezzanine 10:00-7:00', 2026, 5, b'1'),
(23, 'Mezzanine 8:30-5:30', 2026, 5, b'1'),
(24, 'Mezzanine 7:00-4:00', 2026, 5, b'1'),
(25, 'Mezzanine 6:00-3:00', 2026, 5, b'1'),
(26, 'STO. DOMINGO - 12:30-9:30', 2026, 5, b'1'),
(27, 'GENETA - 8:00-5:00 MON-SAT', 2026, 5, b'1'),
(29, 'TEAM CID', 2026, 5, b'0'),
(30, 'TEAM DOC', 2026, 5, b'0'),
(31, 'DELA CRUZ, JASON - TEAM DOC', 2026, 5, b'0'),
(32, 'ABANE,  REGINA FE', 2026, 5, b'0'),
(33, 'MAG-APAN, CHARIE MAE', 2026, 5, b'0');

-- --------------------------------------------------------

--
-- Table structure for table `weekly_schedule_template_day`
--

DROP TABLE IF EXISTS `weekly_schedule_template_day`;
CREATE TABLE `weekly_schedule_template_day` (
  `id` int(11) NOT NULL,
  `template_id` int(11) NOT NULL,
  `day_of_week` int(11) NOT NULL,
  `is_rest_day` tinyint(1) NOT NULL DEFAULT 0,
  `time_in` time DEFAULT NULL,
  `time_out` time DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `weekly_schedule_template_day`
--

INSERT INTO `weekly_schedule_template_day` (`id`, `template_id`, `day_of_week`, `is_rest_day`, `time_in`, `time_out`) VALUES
(63, 18, 1, 0, '08:00:00', '17:00:00'),
(64, 18, 2, 0, '08:00:00', '17:00:00'),
(65, 18, 3, 0, '08:00:00', '17:00:00'),
(66, 18, 4, 0, '08:00:00', '17:00:00'),
(67, 18, 5, 0, '08:00:00', '17:00:00'),
(68, 18, 6, 1, NULL, NULL),
(69, 18, 7, 1, NULL, NULL),
(77, 20, 1, 0, '08:10:00', '17:10:00'),
(78, 20, 2, 0, '08:10:00', '17:10:00'),
(79, 20, 3, 0, '08:10:00', '17:10:00'),
(80, 20, 4, 0, '08:10:00', '17:10:00'),
(81, 20, 5, 0, '08:10:00', '17:10:00'),
(82, 20, 6, 1, NULL, NULL),
(83, 20, 7, 1, NULL, NULL),
(84, 21, 1, 0, '09:10:00', '18:10:00'),
(85, 21, 2, 0, '09:10:00', '18:10:00'),
(86, 21, 3, 0, '09:10:00', '18:10:00'),
(87, 21, 4, 0, '09:10:00', '18:10:00'),
(88, 21, 5, 0, '09:10:00', '18:10:00'),
(89, 21, 6, 1, NULL, NULL),
(90, 21, 7, 1, NULL, NULL),
(91, 22, 1, 0, '10:10:00', '19:10:00'),
(92, 22, 2, 0, '10:10:00', '19:10:00'),
(93, 22, 3, 0, '10:10:00', '19:10:00'),
(94, 22, 4, 0, '10:00:00', '19:10:00'),
(95, 22, 5, 0, '10:10:00', '19:10:00'),
(96, 22, 6, 1, NULL, NULL),
(97, 22, 7, 1, NULL, NULL),
(98, 23, 1, 0, '08:40:00', '17:40:00'),
(99, 23, 2, 0, '08:40:00', '17:40:00'),
(100, 23, 6, 1, NULL, NULL),
(101, 23, 7, 1, NULL, NULL),
(102, 23, 3, 0, '08:40:00', '17:40:00'),
(103, 23, 4, 0, '08:40:00', '17:40:00'),
(104, 23, 5, 0, '08:40:00', '17:40:00'),
(105, 24, 1, 0, '07:10:00', '16:10:00'),
(106, 24, 2, 0, '07:10:00', '16:10:00'),
(107, 24, 3, 0, '07:10:00', '16:10:00'),
(108, 24, 4, 0, '07:10:00', '16:10:00'),
(109, 24, 5, 0, '07:10:00', '16:10:00'),
(110, 24, 6, 1, NULL, NULL),
(111, 24, 7, 1, NULL, NULL),
(112, 25, 1, 0, '06:10:00', '15:10:00'),
(113, 25, 2, 0, '06:10:00', '15:10:00'),
(114, 25, 3, 0, '06:10:00', '15:10:00'),
(115, 25, 4, 0, '06:10:00', '15:10:00'),
(116, 25, 5, 0, '06:10:00', '15:10:00'),
(117, 25, 6, 1, NULL, NULL),
(118, 25, 7, 1, NULL, NULL),
(119, 26, 1, 0, '12:40:00', '21:40:00'),
(120, 26, 2, 0, '12:40:00', '21:40:00'),
(121, 26, 3, 0, '12:40:00', '21:40:00'),
(122, 26, 4, 0, '12:40:00', '21:40:00'),
(123, 26, 5, 0, '12:40:00', '21:40:00'),
(124, 26, 6, 1, NULL, NULL),
(125, 26, 7, 1, NULL, NULL),
(133, 29, 1, 0, '08:40:00', '21:40:00'),
(134, 29, 2, 0, '14:40:00', '21:30:00'),
(135, 29, 3, 0, '14:40:00', '21:30:00'),
(136, 29, 4, 0, '14:40:00', '21:30:00'),
(137, 29, 5, 1, NULL, NULL),
(138, 29, 6, 1, NULL, NULL),
(139, 29, 7, 0, '08:40:00', '21:40:00'),
(140, 30, 1, 1, NULL, NULL),
(141, 30, 2, 0, '08:40:00', '15:40:00'),
(142, 30, 3, 0, '08:40:00', '15:40:00'),
(143, 30, 4, 0, '08:40:00', '15:40:00'),
(144, 30, 5, 0, '08:40:00', '21:40:00'),
(145, 30, 6, 0, '08:40:00', '21:40:00'),
(146, 30, 7, 1, NULL, NULL),
(147, 31, 1, 1, NULL, NULL),
(148, 31, 2, 0, '14:40:00', '21:40:00'),
(149, 31, 3, 0, '14:40:00', '21:40:00'),
(150, 31, 4, 0, '14:40:00', '21:40:00'),
(151, 31, 5, 0, '08:40:00', '21:40:00'),
(152, 31, 6, 0, '08:40:00', '21:40:00'),
(153, 31, 7, 1, NULL, NULL),
(154, 32, 1, 1, NULL, NULL),
(155, 32, 2, 0, '12:40:00', '21:40:00'),
(156, 32, 3, 0, '12:40:00', '21:40:00'),
(157, 32, 4, 0, '12:40:00', '21:40:00'),
(158, 32, 5, 0, '12:40:00', '21:40:00'),
(159, 32, 6, 0, '12:40:00', '21:40:00'),
(160, 32, 7, 1, NULL, NULL),
(161, 33, 1, 0, '12:40:00', '21:40:00'),
(162, 33, 2, 0, '12:40:00', '21:40:00'),
(163, 33, 3, 0, '12:40:00', '21:40:00'),
(164, 33, 4, 0, '12:40:00', '21:40:00'),
(165, 33, 5, 1, NULL, NULL),
(166, 33, 6, 1, NULL, NULL),
(167, 33, 7, 0, '12:40:00', '21:40:00'),
(168, 27, 1, 0, '08:10:00', '17:10:00'),
(169, 27, 2, 0, '08:10:00', '17:10:00'),
(170, 27, 3, 0, '08:10:00', '17:10:00'),
(171, 27, 4, 0, '08:10:00', '17:10:00'),
(172, 27, 5, 0, '08:10:00', '17:10:00'),
(173, 27, 6, 0, '08:10:00', '17:10:00'),
(174, 27, 7, 1, NULL, NULL);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `adjustments`
--
ALTER TABLE `adjustments`
  ADD PRIMARY KEY (`adjustmentId`);

--
-- Indexes for table `attendance`
--
ALTER TABLE `attendance`
  ADD PRIMARY KEY (`attendanceId`),
  ADD KEY `employeeId` (`employeeId`);

--
-- Indexes for table `auditlogs`
--
ALTER TABLE `auditlogs`
  ADD PRIMARY KEY (`logId`);

--
-- Indexes for table `bonuses`
--
ALTER TABLE `bonuses`
  ADD PRIMARY KEY (`bonusId`),
  ADD KEY `employeeId` (`employeeId`);

--
-- Indexes for table `deductions`
--
ALTER TABLE `deductions`
  ADD PRIMARY KEY (`deductionId`);

--
-- Indexes for table `departments`
--
ALTER TABLE `departments`
  ADD PRIMARY KEY (`departmentId`);

--
-- Indexes for table `employeeadjustments`
--
ALTER TABLE `employeeadjustments`
  ADD PRIMARY KEY (`employeeAdjustmentId`),
  ADD KEY `employeeId` (`employeeId`),
  ADD KEY `adjustmentId` (`adjustmentId`);

--
-- Indexes for table `employeedeductions`
--
ALTER TABLE `employeedeductions`
  ADD PRIMARY KEY (`employeeDeductionId`),
  ADD KEY `employeeId` (`employeeId`),
  ADD KEY `deductionId` (`deductionId`);

--
-- Indexes for table `employees`
--
ALTER TABLE `employees`
  ADD PRIMARY KEY (`employeeId`),
  ADD UNIQUE KEY `employeeNumber` (`employeeNumber`),
  ADD UNIQUE KEY `uk_employees_biometric_id` (`biometric_id`),
  ADD KEY `departmentId` (`departmentId`),
  ADD KEY `positionId` (`positionId`);

--
-- Indexes for table `employee_schedule_assignment`
--
ALTER TABLE `employee_schedule_assignment`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_esa_emp_month` (`employeeId`,`schedule_year`,`schedule_month`),
  ADD KEY `idx_esa_template` (`template_id`);

--
-- Indexes for table `holiday`
--
ALTER TABLE `holiday`
  ADD PRIMARY KEY (`holiday_id`);

--
-- Indexes for table `leavebalance`
--
ALTER TABLE `leavebalance`
  ADD PRIMARY KEY (`employeeId`,`leaveTypeId`),
  ADD KEY `leaveTypeId` (`leaveTypeId`);

--
-- Indexes for table `leaverequests`
--
ALTER TABLE `leaverequests`
  ADD PRIMARY KEY (`leaveRequestId`),
  ADD KEY `employeeId` (`employeeId`),
  ADD KEY `leaveTypeId` (`leaveTypeId`);

--
-- Indexes for table `leavetype`
--
ALTER TABLE `leavetype`
  ADD PRIMARY KEY (`leaveTypeId`);

--
-- Indexes for table `overtime_request`
--
ALTER TABLE `overtime_request`
  ADD PRIMARY KEY (`overtime_request_id`),
  ADD KEY `idx_ot_req_emp` (`employeeId`),
  ADD KEY `idx_ot_req_status` (`status`),
  ADD KEY `fk_ot_req_user` (`approved_by_user_id`);

--
-- Indexes for table `pagibigtable`
--
ALTER TABLE `pagibigtable`
  ADD PRIMARY KEY (`pagibigId`);

--
-- Indexes for table `passwordresettokens`
--
ALTER TABLE `passwordresettokens`
  ADD PRIMARY KEY (`tokenId`),
  ADD UNIQUE KEY `token` (`token`),
  ADD KEY `fk_password_reset_user` (`userId`);

--
-- Indexes for table `payroll`
--
ALTER TABLE `payroll`
  ADD PRIMARY KEY (`payrollId`),
  ADD UNIQUE KEY `uk_payroll_period_type` (`payPeriodStart`,`payPeriodEnd`,`payrollType`);

--
-- Indexes for table `payrollItems`
--
ALTER TABLE `payrollItems`
  ADD PRIMARY KEY (`payrollItemId`);

--
-- Indexes for table `payrollitems`
--
ALTER TABLE `payrollitems`
  ADD PRIMARY KEY (`payrollItemId`),
  ADD KEY `payrollId` (`payrollId`),
  ADD KEY `employeeId` (`employeeId`);

--
-- Indexes for table `philhealthtable`
--
ALTER TABLE `philhealthtable`
  ADD PRIMARY KEY (`philhealthId`);

--
-- Indexes for table `positions`
--
ALTER TABLE `positions`
  ADD PRIMARY KEY (`positionId`);

--
-- Indexes for table `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`roleId`),
  ADD UNIQUE KEY `roleName` (`roleName`);

--
-- Indexes for table `shifts`
--
ALTER TABLE `shifts`
  ADD PRIMARY KEY (`shiftId`),
  ADD UNIQUE KEY `shiftName` (`shiftName`);

--
-- Indexes for table `ssstable`
--
ALTER TABLE `ssstable`
  ADD PRIMARY KEY (`sssId`);

--
-- Indexes for table `taxtable`
--
ALTER TABLE `taxtable`
  ADD PRIMARY KEY (`taxId`);

--
-- Indexes for table `undertime_requests`
--
ALTER TABLE `undertime_requests`
  ADD PRIMARY KEY (`undertime_request_id`),
  ADD KEY `fk_undertime_employee` (`employeeId`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`userId`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `employeeId` (`employeeId`),
  ADD KEY `roleId` (`roleId`);

--
-- Indexes for table `weekly_schedule_template`
--
ALTER TABLE `weekly_schedule_template`
  ADD PRIMARY KEY (`template_id`);

--
-- Indexes for table `weekly_schedule_template_day`
--
ALTER TABLE `weekly_schedule_template_day`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_ws_tpl_dow` (`template_id`,`day_of_week`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `adjustments`
--
ALTER TABLE `adjustments`
  MODIFY `adjustmentId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `attendance`
--
ALTER TABLE `attendance`
  MODIFY `attendanceId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=542;

--
-- AUTO_INCREMENT for table `auditlogs`
--
ALTER TABLE `auditlogs`
  MODIFY `logId` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `bonuses`
--
ALTER TABLE `bonuses`
  MODIFY `bonusId` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `deductions`
--
ALTER TABLE `deductions`
  MODIFY `deductionId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=104;

--
-- AUTO_INCREMENT for table `departments`
--
ALTER TABLE `departments`
  MODIFY `departmentId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `employeeadjustments`
--
ALTER TABLE `employeeadjustments`
  MODIFY `employeeAdjustmentId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `employeedeductions`
--
ALTER TABLE `employeedeductions`
  MODIFY `employeeDeductionId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT for table `employees`
--
ALTER TABLE `employees`
  MODIFY `employeeId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=54;

--
-- AUTO_INCREMENT for table `employee_schedule_assignment`
--
ALTER TABLE `employee_schedule_assignment`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=74;

--
-- AUTO_INCREMENT for table `holiday`
--
ALTER TABLE `holiday`
  MODIFY `holiday_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- AUTO_INCREMENT for table `leaverequests`
--
ALTER TABLE `leaverequests`
  MODIFY `leaveRequestId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=52;

--
-- AUTO_INCREMENT for table `leavetype`
--
ALTER TABLE `leavetype`
  MODIFY `leaveTypeId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `overtime_request`
--
ALTER TABLE `overtime_request`
  MODIFY `overtime_request_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `pagibigtable`
--
ALTER TABLE `pagibigtable`
  MODIFY `pagibigId` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `passwordresettokens`
--
ALTER TABLE `passwordresettokens`
  MODIFY `tokenId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=68;

--
-- AUTO_INCREMENT for table `payroll`
--
ALTER TABLE `payroll`
  MODIFY `payrollId` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `payrollItems`
--
ALTER TABLE `payrollItems`
  MODIFY `payrollItemId` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `payrollitems`
--
ALTER TABLE `payrollitems`
  MODIFY `payrollItemId` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `philhealthtable`
--
ALTER TABLE `philhealthtable`
  MODIFY `philhealthId` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `positions`
--
ALTER TABLE `positions`
  MODIFY `positionId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=47;

--
-- AUTO_INCREMENT for table `roles`
--
ALTER TABLE `roles`
  MODIFY `roleId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `shifts`
--
ALTER TABLE `shifts`
  MODIFY `shiftId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `ssstable`
--
ALTER TABLE `ssstable`
  MODIFY `sssId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=181;

--
-- AUTO_INCREMENT for table `taxtable`
--
ALTER TABLE `taxtable`
  MODIFY `taxId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=31;

--
-- AUTO_INCREMENT for table `undertime_requests`
--
ALTER TABLE `undertime_requests`
  MODIFY `undertime_request_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `userId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=177;

--
-- AUTO_INCREMENT for table `weekly_schedule_template`
--
ALTER TABLE `weekly_schedule_template`
  MODIFY `template_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=35;

--
-- AUTO_INCREMENT for table `weekly_schedule_template_day`
--
ALTER TABLE `weekly_schedule_template_day`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=175;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `attendance`
--
ALTER TABLE `attendance`
  ADD CONSTRAINT `attendance_ibfk_1` FOREIGN KEY (`employeeId`) REFERENCES `employees` (`employeeId`);

--
-- Constraints for table `bonuses`
--
ALTER TABLE `bonuses`
  ADD CONSTRAINT `bonuses_ibfk_1` FOREIGN KEY (`employeeId`) REFERENCES `employees` (`employeeId`);

--
-- Constraints for table `employeeadjustments`
--
ALTER TABLE `employeeadjustments`
  ADD CONSTRAINT `employeeadjustments_ibfk_1` FOREIGN KEY (`employeeId`) REFERENCES `employees` (`employeeId`),
  ADD CONSTRAINT `employeeadjustments_ibfk_2` FOREIGN KEY (`adjustmentId`) REFERENCES `adjustments` (`adjustmentId`);

--
-- Constraints for table `employeedeductions`
--
ALTER TABLE `employeedeductions`
  ADD CONSTRAINT `employeedeductions_ibfk_1` FOREIGN KEY (`employeeId`) REFERENCES `employees` (`employeeId`),
  ADD CONSTRAINT `employeedeductions_ibfk_2` FOREIGN KEY (`deductionId`) REFERENCES `deductions` (`deductionId`);

--
-- Constraints for table `employees`
--
ALTER TABLE `employees`
  ADD CONSTRAINT `employees_ibfk_1` FOREIGN KEY (`departmentId`) REFERENCES `departments` (`departmentId`),
  ADD CONSTRAINT `employees_ibfk_2` FOREIGN KEY (`positionId`) REFERENCES `positions` (`positionId`);

--
-- Constraints for table `employee_schedule_assignment`
--
ALTER TABLE `employee_schedule_assignment`
  ADD CONSTRAINT `fk_esa_employee` FOREIGN KEY (`employeeId`) REFERENCES `employees` (`employeeId`),
  ADD CONSTRAINT `fk_esa_template` FOREIGN KEY (`template_id`) REFERENCES `weekly_schedule_template` (`template_id`) ON DELETE CASCADE;

--
-- Constraints for table `leavebalance`
--
ALTER TABLE `leavebalance`
  ADD CONSTRAINT `leavebalance_ibfk_1` FOREIGN KEY (`employeeId`) REFERENCES `employees` (`employeeId`),
  ADD CONSTRAINT `leavebalance_ibfk_2` FOREIGN KEY (`leaveTypeId`) REFERENCES `leavetype` (`leaveTypeId`);

--
-- Constraints for table `leaverequests`
--
ALTER TABLE `leaverequests`
  ADD CONSTRAINT `leaverequests_ibfk_1` FOREIGN KEY (`employeeId`) REFERENCES `employees` (`employeeId`),
  ADD CONSTRAINT `leaverequests_ibfk_2` FOREIGN KEY (`leaveTypeId`) REFERENCES `leavetype` (`leaveTypeId`);

--
-- Constraints for table `overtime_request`
--
ALTER TABLE `overtime_request`
  ADD CONSTRAINT `fk_ot_req_employee` FOREIGN KEY (`employeeId`) REFERENCES `employees` (`employeeId`),
  ADD CONSTRAINT `fk_ot_req_user` FOREIGN KEY (`approved_by_user_id`) REFERENCES `users` (`userId`);

--
-- Constraints for table `passwordresettokens`
--
ALTER TABLE `passwordresettokens`
  ADD CONSTRAINT `fk_password_reset_user` FOREIGN KEY (`userId`) REFERENCES `users` (`userId`) ON DELETE CASCADE;

--
-- Constraints for table `payrollitems`
--
ALTER TABLE `payrollitems`
  ADD CONSTRAINT `payrollItems_ibfk_1` FOREIGN KEY (`payrollId`) REFERENCES `payroll` (`payrollId`),
  ADD CONSTRAINT `payrollItems_ibfk_2` FOREIGN KEY (`employeeId`) REFERENCES `employees` (`employeeId`);

--
-- Constraints for table `undertime_requests`
--
ALTER TABLE `undertime_requests`
  ADD CONSTRAINT `fk_undertime_employee` FOREIGN KEY (`employeeId`) REFERENCES `employees` (`employeeId`);

--
-- Constraints for table `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `users_ibfk_1` FOREIGN KEY (`employeeId`) REFERENCES `employees` (`employeeId`),
  ADD CONSTRAINT `users_ibfk_2` FOREIGN KEY (`roleId`) REFERENCES `roles` (`roleId`);

--
-- Constraints for table `weekly_schedule_template_day`
--
ALTER TABLE `weekly_schedule_template_day`
  ADD CONSTRAINT `fk_ws_tpl_day_template` FOREIGN KEY (`template_id`) REFERENCES `weekly_schedule_template` (`template_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
