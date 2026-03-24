-- Run once on MySQL if column missing (spring.jpa.hibernate.ddl-auto=none)
ALTER TABLE taxtable ADD COLUMN pay_frequency VARCHAR(32) NULL;

-- Withholding tax brackets (2026) from `files/Withholding Tax - Sheet1.csv`
-- NOTE: `compensationTo` isn't used by current PayrollService logic; it still must be NON-NULL in schema.
-- We set it to the next bracket start minus 0.01 (inclusive upper bound). Last bracket uses a large number.

-- DAILY
INSERT INTO taxtable (compensationFrom, compensationTo, taxRate, additionalTax, effectiveYear, pay_frequency) VALUES
(1,       684.99,     0.00, 0.00,    2026, 'DAILY'),
(685,     1095.99,    0.15, 0.00,    2026, 'DAILY'),
(1096,    2191.99,    0.20, 61.65,   2026, 'DAILY'),
(2192,    5478.99,    0.25, 280.85,  2026, 'DAILY'),
(5479,    21917.99,   0.30, 1102.60, 2026, 'DAILY'),
(21918,   9999999.99,  0.35, 6034.00, 2026, 'DAILY');

-- WEEKLY
INSERT INTO taxtable (compensationFrom, compensationTo, taxRate, additionalTax, effectiveYear, pay_frequency) VALUES
(1,       4807.99,    0.00, 0.00,     2026, 'WEEKLY'),
(4808,    7691.99,    0.15, 0.00,     2026, 'WEEKLY'),
(7692,    15384.99,   0.20, 432.60,   2026, 'WEEKLY'),
(15385,   38461.99,   0.25, 1971.20,  2026, 'WEEKLY'),
(38462,   153845.99,  0.30, 7740.45,  2026, 'WEEKLY'),
(153846,  9999999.99,  0.35, 42355.65, 2026, 'WEEKLY');

-- SEMI-MONTHLY
INSERT INTO taxtable (compensationFrom, compensationTo, taxRate, additionalTax, effectiveYear, pay_frequency) VALUES
(1,       10416.99,   0.00, 0.00,     2026, 'SEMI_MONTHLY'),
(10417,   16666.99,   0.15, 0.00,     2026, 'SEMI_MONTHLY'),
(16667,   33332.99,   0.20, 937.50,   2026, 'SEMI_MONTHLY'),
(33333,   83332.99,   0.25, 4270.70,  2026, 'SEMI_MONTHLY'),
(83333,   333332.99,  0.30, 16770.70, 2026, 'SEMI_MONTHLY'),
(333333,  9999999.99,  0.35, 91770.70, 2026, 'SEMI_MONTHLY');

-- MONTHLY
INSERT INTO taxtable (compensationFrom, compensationTo, taxRate, additionalTax, effectiveYear, pay_frequency) VALUES
(1,       20832.99,   0.00, 0.00,      2026, 'MONTHLY'),
(20833,   33332.99,   0.15, 0.00,      2026, 'MONTHLY'),
(33333,   66666.99,   0.20, 1875.00,   2026, 'MONTHLY'),
(66667,   166666.99,  0.25, 8541.80,   2026, 'MONTHLY'),
(166667,  666666.99,  0.30, 33541.80,  2026, 'MONTHLY'),
(666667,  9999999.99,  0.35, 183541.80, 2026, 'MONTHLY');

-- ANNUALLY
INSERT INTO taxtable (compensationFrom, compensationTo, taxRate, additionalTax, effectiveYear, pay_frequency) VALUES
(1,        249999.99,  0.00, 0.00,       2026, 'ANNUALLY'),
(250000,   399999.99,  0.15, 0.00,       2026, 'ANNUALLY'),
(400000,   799999.99,  0.20, 22500.00,   2026, 'ANNUALLY'),
(800000,   1999999.99, 0.25, 102500.00,  2026, 'ANNUALLY'),
(2000000,  7999999.99, 0.30, 402500.00,  2026, 'ANNUALLY'),
(8000000,  9999999.99, 0.35, 2202500.00, 2026, 'ANNUALLY');
