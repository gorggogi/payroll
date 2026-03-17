# Payroll System Computation Redesign & Implementation Log
**Date:** March 2026
**Target:** To bring the Spring Boot `PayrollService` math into perfect alignment with the official company payroll spreadsheet and modern Philippine DOLE / TRAIN Law requirements.

---

## 1. Core Rate Constants & Rules Adapted

To ensure perfect centavo matching with the spreadsheet, the following fundamental formulas were hardcoded into `PayrollService.java`:
- **Working Days Per Month:** Fixed 20.
- **Hours Per Day:** Fixed 8.
- **Daily Rate Formula:** `Monthly Rate ÷ 20`
- **Hourly Rate Formula:** `Daily Rate ÷ 8`
- **Per-Minute Rate Formula:** `Hourly Rate ÷ 60` 
  - *Fix Applied:* The per-minute rate is forced to exactly 2 decimal places unconditionally before multiplying against late/undertime minutes, as standard DOLE payroll spreadsheets typically do.

### Employment Type Switching
The application now supports dynamic rate calculation via `employmentType` string switching (`Regular` vs `Job Order`):
- **Job Order:** 
  - Overtime Multiplier = 1.0x
  - SSS, PhilHealth, Pag-IBIG Contributions = ₱0.00
  - Tax Calculation = 5% flat Withholding / EWT on Total Earnings
- **Regular:**
  - Overtime Multiplier = 1.25x
  - SSS = Computed via `ssstable` SQL brackets lookup
  - PhilHealth & Pag-IBIG = Standard % flat formulas
  - Tax Calculation = Base TRAIN table / Simplified formula (`Base Rate × 10% - 2,395.90`)

---

## 2. Statutory Contributions (PhilHealth, Pag-IBIG, SSS)

Instead of relying on outdated DB lookup structures, we overhauled how these are computed:

- **Salary Splitting Cap:** Both PhilHealth and Pag-IBIG formulas are applied against a capped "Premium Base". If an employee's Monthly Salary is below ₱30,000, their Premium Base is strictly capped at ₱20,000 for these exact percentage calculations.
- **PhilHealth:** Shifted to a mathematical formula `(Premium Base × 5%) ÷ 2` to represent the 50/50 Employee/Employer split. It no longer relies on heavy SQL table fetching.
- **Pag-IBIG:** Shifted to a flat `Premium Base × 2%` formula. Removed the hardcoded legacy ₱100.00 maximum cap logic since real Pag-IBIG contributions scale upwards.
- **SSS Bracket Generation:** Created a robust NodeJS script (`generate_sss.js`) that outputs real-world SSS Circular No. 2024-006 brackets (14%-15% bounds up to ₱35,000 MSC ceilings) directly into `populate_statutory_tables.sql` for instant DB population.

---

## 3. Deductions & Adjustments Rework

- **Unified Deduction Architecture:**
  - All employee deductions (Cash Advances, Loans, Union Dues, Penalties, etc.) are now treated **identically** — no special-casing for any deduction type.
  - Administrators create a deduction on the Deductions page, choose a type from the dropdown, assign it to an employee, and it automatically appears as its own named row on the payslip.
  - The backend uses a single `computeEmployeeDeductions()` method and a single `getDeductionsBreakdown()` method. There is no type-based filtering or branching.
  - The old `computeCashAdvance()` method and hardcoded "Cash Advance" UI row have been completely removed.
- **Deduction Enums/Types:** Fixed SQL truncation (`Data truncated for column 'deductionType'`) to guarantee that test data respects the system's strict `deductionType` Enumerated values ('Advance', 'Loan', 'Union', 'Other').
- **Non-Recurring Timeframes:** Adjusted test SQL mocks to ensure non-recurring deductions strictly cover their active `startDate` / `endDate`, ensuring `PayrollService` successfully harvests them.

---

## 4. UI & Payslip Transparency (`payroll.html` & `payroll.js`)

To visually mirror the detailed spreadsheet, we expanded the static HTML tables and dynamically-generated Javascript payslips to display **3x times as much information:**
- Splitting generic Gross Pay into transparent: `Daily Rate`, `Hourly Rate`, `Overtime Rate`, and `Allowances`.
- Breaking down Statutory Taxes directly line-by-line (`SSS`, `PhilHealth`, `Pag-IBIG`, etc.).
- Exposing the `Total Late/Undertime Minutes` variable directly on the UI alongside the resulting peso value deduction.
- Introduced the concept of the `Service Fee` (Total Earnings minus Non-Statutory generic Deductions) before determining `Net Pay`.

---

## 5. SQL DB Schema Modifications

Because the backend Java object grew from a simple `netPay / grossPay` layout into a high-granularity snapshot, several `ALTER TABLE payrollitems` changes were drafted in `update_payroll_schema.sql` to persist the following nullable columns:
- `daily_rate`, `hourly_rate`, `per_minute_rate`
- `total_worked_hours`, `total_ot_hours`
- `late_undertime_minutes`, `late_undertime_deduction`
- `cash_advance`
- `adjustment_earnings`, `adjustment_deductions`
- `service_fee`
- `semi_monthly_contributions`, `employment_type`

---

## Summary
The system is now capable of correctly ingesting standard DOLE-style workweeks with partial minute truncations, calculating precise OT and EWT percentages selectively by Employment Type, reading real-world PHP SSS database rules alongside mathematically perfect PhilHealth/Pag-IBIG % calculations, and seamlessly storing—and displaying—these granular results dynamically. 
