# Semi-monthly Payroll Logic (System Documentation)

This document describes **how semi-monthly payslips are computed** in this system, including:
- **Where inputs come from** (UI, APIs, database tables/entities)
- **Exact cutoff rules**
- **Computation formulas**
- **Rounding behavior**
- **Withholding tax bracket logic** and how it maps to the HR Excel formulas
- **Known limitations / important assumptions**

Applies to the current implementation in:
- `src/main/java/digital8/payroll/services/PayrollService.java`

---

### Inputs 

| Input | Source | Notes |
|------|--------|------|
| `period` (`semi_1` / `semi_2`) | UI dropdown → query param | `semi_1` = 1–15, `semi_2` = 16–end |
| `month`, `year` | UI dropdown → query param | Drives cutoff date range |
| `monthlyRate` | `Employees.basicSalary` | Monthly salary |
| `factorRate` | `Employees.factorRate` (default 20) | Used for daily/hourly rate derivation |
| Attendance rows | `attendance` table | Filtered to cutoff range |
| Non-statutory deductions | `EmployeeDeductions` table | Filtered to cutoff range |
| SSS bracket | `ssstable` table | Looked up by `premiumBase` and `year` |
| WHT brackets | `taxtable` table | `effectiveYear` and `pay_frequency='SEMI_MONTHLY'` |

### Cutoff date range (semi-monthly)

| Period value | Start | End |
|-------------|-------|-----|
| `semi_1` | 1st of month | 15th of month |
| `semi_2` | 16th of month | last day of month |

### Formulas (semi-monthly)

All currency amounts are rounded to **2 decimals** (HALF_UP) at the points indicated.

#### A) Rates (from employee salary)

- **Daily rate** = `monthlyRate / factorRate`
- **Hourly rate** = `dailyRate / 8`
- **Per-minute rate** = `hourlyRate / 60`

#### B) Attendance totals (from `attendance` within cutoff)

- **Total worked hours** = `SUM(work_hours)`
- **Total OT hours** = `SUM(overtime_hours)`
- **Total late/undertime minutes** = `SUM(late_minutes) + SUM(undertime_minutes)`

#### C) Earnings

- **Basic pay** = `hourlyRate * totalWorkedHours`
- **OT pay** = `hourlyRate * totalOtHours * otMultiplier`  
  - `otMultiplier = 1.0` (Job Order) or `1.25` (Regular)
- **Total earnings / gross pay** = `basicPay + overtimePay + adjustmentEarnings`  
  - `adjustmentEarnings` is currently **0**

#### D) Non-statutory deductions (cutoff-scoped)

- **Late/undertime deduction** = `ROUND(perMinuteRate, 2) * totalLateUndertimeMinutes`
- **Employee deductions** = sum of `EmployeeDeductions.amount` active in the cutoff
- **Total non-statutory deductions** = employeeDeductions + lateUndertimeDeduction

#### E) Service fee

- **Service fee** = `totalEarnings - totalNonStatutoryDeductions`

#### F) Government contributions (monthly amounts, but deducted semi-monthly)

1) **Premium base (HR rule)**  
   - If `monthlyRate < 30000`: `premiumBase = MIN(monthlyRate, 20000)`  
     - Meaning: use the employee’s salary **if it’s below 20k** (e.g. 19,500 → 19,500), otherwise cap at **20k** (e.g. 21,000 → 20,000).
   - Else: `premiumBase = monthlyRate` (no cap when salary is 30k+)

2) Compute monthly contribution components:
- **SSS** = bracket lookup from `ssstable` using `premiumBase` and `year`
- **PhilHealth** = `(premiumBase * 0.05) / 2`  (2.5% employee share)
- **Pag-IBIG** = `premiumBase * 0.02`

#### G) Withholding tax (WHT) — SEMI_MONTHLY table for semi cutoffs

For semi-monthly payslips, the system computes WHT using the **SEMI_MONTHLY** bracket table:

- `J31 = monthlyRate / 2`
- `taxablePay = J31`
- WHT is computed using `taxtable` rows where `pay_frequency='SEMI_MONTHLY'`

WHT is computed from bracket rows like this:

1) Select bracket rows for the chosen `effectiveYear` and `pay_frequency='SEMI_MONTHLY'` (falling back to `year - 1` if none are found).
2) Pick the bracket whose `compensationFrom` is the highest value such that `compensationFrom <= taxablePay`.
3) Compute `excess = taxablePay - compensationFrom`.
4) Compute `WHT = additionalTax + taxRate * excess`.
5) Round WHT to 2 decimals (HALF_UP) and floor at 0 (never negative).

On the payslip:
- SSS / PhilHealth / Pag-IBIG are deducted at half (because they are monthly amounts)
- WHT is deducted as the computed `WHT_semi` value (not divided again)

#### H) What’s actually deducted on the slip (semi-monthly)

- **Statutory total (monthly bundle)** = `SSS + PhilHealth + Pag-IBIG + WHT`
- **Total Semi-monthly Contributions (deducted)** = `(SSS + PhilHealth + Pag-IBIG) / 2 + WHT_semi`

#### I) Net pay

- **Net pay** = `serviceFee - totalSemiMonthlyContributions`

---

### Formulas (monthly)

Monthly uses the **same pipeline** as semi-monthly, but with these differences:

- **Date range**: full month (1st → last day)
- **WHT base**: `taxablePay = monthlyRate` (MONTHLY bracket table)
- **Statutory deducted**: `statutoryDeductedThisSlip = statutoryTotal` (no `/ 2`)

Specifically:

1) Period bounds:
- `period = monthly` → start = 1st of month, end = last day of month

2) WHT:
- `taxablePay = monthlyRate`
- Use `taxtable` rows where `pay_frequency = 'MONTHLY'`
- `WHT = additionalTax + taxRate * (taxablePay - compensationFrom)`

3) Net pay:
- `netPay = serviceFee - statutoryTotal`

## 1) Key concepts and terminology

- **Semi-monthly payslip**: a payslip computed for one of two fixed cutoffs within a calendar month.
  - **Cutoff 1**: days **1–15**
  - **Cutoff 2**: days **16–end of month**

- **Period selector**: the UI sends a `period` string to indicate which cutoff to compute.

- **Service fee** (system term): effectively **gross earnings for the cutoff minus non-statutory deductions**.

- **Statutory / government contributions**: **SSS, PhilHealth, Pag-IBIG (HDMF)**.

- **Withholding tax (WHT)**: computed using **bracket tables** loaded in the database from HR’s table.

---

## 2) User flows and URLs

### 2.1 Server-rendered payroll page

- **Route**: `GET /payroll/{empId}`
- **Query parameters**:
  - `month`: string month name, e.g. `MARCH` (must match `java.time.Month` enum names)
  - `year`: integer year, e.g. `2026`
  - `period`:
    - `semi_1` → Semi-monthly (1–15)
    - `semi_2` → Semi-monthly (16–end)
    - `monthly` → Monthly (full month)
    - `biweekly` → present in UI; currently uses **full month** placeholder bounds in backend (not a true 14-day cycle yet)

**Controller**: `src/main/java/digital8/payroll/controllers/payrollViewController.java`

### 2.2 Payroll API (used by Compute button)

- **Route**: `GET /api/payroll/{empId}`
- **Query parameters**:
  - `period`
  - `month`
  - `year`

**Controller**: `src/main/java/digital8/payroll/controllers/PayrollApiController.java`  
**Client**: `src/main/resources/static/js/payroll.js`

---

## 3) Data sources (database → entities)

### 3.1 Employee master record

Entity: `digital8.payroll.entities.Employees`

Fields used:
- **`basicSalary`** → monthly base salary used as the starting point for rate derivation and contributions base
- **`factorRate`** → divisor used to compute daily rate from monthly salary (defaults to 20 if missing/invalid)
- **`employmentType`** → currently impacts **overtime multiplier** only (not contributions and not withholding tax)

### 3.2 Attendance records

Entity: `digital8.payroll.entities.Attendance` (`attendance` table)

Fields used (within the cutoff date range):
- **`attendance_date`** (LocalDate)
- **`work_hours`** (BigDecimal)
- **`overtime_hours`** (BigDecimal)
- **`late_minutes`** (Integer)
- **`undertime_minutes`** (Integer)

The system aggregates:
- `totalWorkedHours = SUM(work_hours)`
- `totalOtHours = SUM(overtime_hours)`
- `totalLateUndertimeMinutes = SUM(late_minutes) + SUM(undertime_minutes)`

### 3.3 Employee deductions (non-statutory)

Entity: `digital8.payroll.entities.EmployeeDeductions` (`EmployeeDeductions` table)

The system includes deductions that are **active within the cutoff window**:
- **Recurring** deductions: included if their active date range overlaps the cutoff.
- **One-time** deductions: included only if their `startDate` falls inside the cutoff.

Important: the system currently treats these amounts as **full amounts per pay period when active**. If HR expects certain deductions to be split across cutoffs, that is a separate policy rule.

### 3.4 SSS table

Entity: `digital8.payroll.entities.SssTable` (`ssstable` table)

Used as a bracket lookup based on the employee’s contributions base and year.

### 3.5 Withholding tax bracket table

Entity: `digital8.payroll.entities.TaxTable` (`taxtable` table)

Columns used:
- `compensationFrom` (lower bound / “excess over”)
- `compensationTo` (upper bound; **not used by current lookup**, but must be non-null in schema)
- `taxRate` (rate as decimal; e.g. 0.20 for 20%)
- `additionalTax` (base tax / fixed amount for the bracket)
- `effectiveYear`
- `pay_frequency` (new column; e.g. `SEMI_MONTHLY`, `MONTHLY`, etc.)

Seed script:
- `docs/..` helper: `files/taxtable_pay_frequency.sql` (includes DAILY/WEEKLY/SEMI_MONTHLY/MONTHLY/ANNUALLY)

---

## 4) Pay period resolution (semi-monthly cutoffs)

Implementation: `PayrollService.resolvePayPeriod(period, YearMonth ym)`

For semi-monthly:
- `period = "semi_1"` → `[start=YYYY-MM-01, end=YYYY-MM-15]`
- `period = "semi_2"` → `[start=YYYY-MM-16, end=YYYY-MM-lastDay]`

All attendance and employee deductions are filtered to `start <= date <= end`.

The controller uses the same logic (through a service helper) for the deduction breakdown display:
- `PayrollService.getPayrollPeriodBounds(year, month, period)`

---

## 5) Rate derivation (from monthly salary)

Inputs:
- `monthlyRate` = employee `basicSalary` (BigDecimal; defaults to 0 if null)
- `factorRate` = employee `factorRate` (defaults to **20** if null/<=0)

Constants:
- `HOURS_PER_DAY = 8`
- `MINUTES_PER_HOUR = 60`

Formulas:
- **Daily rate**:
  - `dailyRate = monthlyRate / factorRate`
- **Hourly rate**:
  - `hourlyRate = dailyRate / 8`
- **Per-minute rate**:
  - `perMinuteRate = hourlyRate / 60`

Rounding:
- intermediate divisions use more precision
- amounts stored in payslip outputs are generally rounded to **2 decimals** using **HALF_UP**

---

## 6) Semi-monthly earnings computation

### 6.1 Attendance aggregation

The system loads all employee attendance then filters by cutoff:

- include record if `periodStart <= attendance_date <= periodEnd`

Totals:
- `totalWorkedHours`
- `totalOtHours`
- `totalLateUndertimeMinutes`

### 6.2 Basic pay

- `basicPay = hourlyRate * totalWorkedHours`
- rounded to 2 decimals (HALF_UP)

### 6.3 Overtime pay

OT multiplier depends on employment type:
- Job Order: `1.0`
- Regular: `1.25`

Formula:
- `overtimePay = hourlyRate * totalOtHours * otMultiplier`

### 6.4 Adjustment earnings

Currently:
- `adjustmentEarnings = 0` (placeholder)

### 6.5 Total earnings / Gross pay

- `totalEarnings = basicPay + overtimePay + adjustmentEarnings`
- This is also used as `grossPay` in the payslip object.

---

## 7) Non-statutory deductions (semi-monthly)

### 7.1 Late/undertime deduction

Important rounding behavior:
- The system rounds **per-minute rate** to 2 decimals before multiplying minutes, matching spreadsheet-style calculations.

Formula:
- `roundedPerMinuteRate = perMinuteRate (rounded to 2 decimals)`
- `lateUndertimeDeduction = roundedPerMinuteRate * totalLateUndertimeMinutes`

### 7.2 Employee deductions (custom/other)

Computed using `computeEmployeeDeductions(empId, periodStart, periodEnd)`:
- sums the `amount` for deductions active in the cutoff

### 7.3 Total non-statutory deductions

- `totalNonStatutoryDeductions = lateUndertimeDeduction + adjustmentDeductions`

### 7.4 Service fee

- `serviceFee = totalEarnings - totalNonStatutoryDeductions`

---

## 8) Government contributions base (“premium base”)

Contributions use a **rule-based premium base** based on HR guidance:

- **If salary is below ₱30,000**: treat as **₱20,000 basic + allowance (non-taxable)**, so premiums are computed on basic only (capped at ₱20,000).
- **If salary is ₱30,000 and above**: premiums are computed on the **full salary**.

In code, this is:

- `premiumBase = (monthlyRate < 30000) ? min(monthlyRate, 20000) : monthlyRate`

### Why this rule exists

HR’s spreadsheet scenarios show two behaviors:
- A ₱19,500 salary computes PhilHealth as `19,500 * 0.05 / 2 = 487.50` (no forced up-to-20k).
- Higher salaries may compute premiums on the full salary per HR instruction.

### Notes

- This `premiumBase` is used for **SSS, PhilHealth, and Pag-IBIG** calculations only.
- This is separate from the withholding-tax bracket logic.

### Example (below 30k)
- monthlyRate = ₱19,500 → premiumBase = ₱19,500
  - PhilHealth employee share = `19,500 * 0.05 / 2 = 487.50`
  - Pag-IBIG = `19,500 * 0.02 = 390`

---

## 9) Statutory (government) contributions computation

HR policy implemented: **all employees** are subject to contributions (no exclusions by employment type).

### 9.1 SSS

Lookup:
- query `ssstable` rows for `effectiveYear`
- select the row where `rangeFrom <= premiumBase <= rangeTo`
- take `employeeShare`

### 9.2 PhilHealth (employee share)

The system computes:
- `philhealth = (premiumBase * 0.05) / 2`

This equals **2.5%** of `premiumBase`.

### 9.3 Pag-IBIG (HDMF)

The system computes:
- `pagibig = premiumBase * 0.02`

---

## 10) Semi-monthly withholding tax (WHT)

### 10.1 What is “taxable pay” (Excel J31 equivalent)?

For semi-monthly, the system uses:

- `semiMonthlyBase = monthlyRate / 2` (this is HR’s **J31**)
- `taxablePay = semiMonthlyBase`

Notes:
- This `taxablePay` is **independent of attendance hours**; it comes from salary, not `totalEarnings`.
- This is not shown as a column in the HR sample CSV, but it is the implied value that explains why some employees have WHT = 0 (their `monthlyRate / 2` falls below the first taxable threshold for the `SEMI_MONTHLY` table).

### 10.2 Bracket lookup table

The system queries:
- `taxtable` rows for `effectiveYear` and `pay_frequency = 'SEMI_MONTHLY'`

If rows for `effectiveYear` are missing, it tries `year - 1`.

### 10.3 Bracket formula (mapping to HR Excel)

For the selected bracket row:
- `compensationFrom` = “excess over” / threshold
- `taxRate` = rate
- `additionalTax` = base tax

Formula:

`tax = additionalTax + taxRate * (taxablePay - compensationFrom)`

This is the same as the HR Excel example:

`(937.5 + (0.2 * (J31 - 16667)))`

Where, for Semi-monthly Bracket 3:
- `additionalTax = 937.5`
- `taxRate = 0.2`
- `compensationFrom = 16667`
- `J31 = taxablePay`

The implementation selects the **highest threshold** `compensationFrom` that is `<= taxablePay`.

### 10.4 Rounding and floors

- Result is rounded to 2 decimals (HALF_UP)
- Result is floored at 0 (`max(BigDecimal.ZERO)`)

---

## 11) Semi-monthly statutory deduction shown on the payslip

The payslip shows individual monthly contribution components:
- SSS
- PhilHealth
- Pag-IBIG
- Withholding Tax (computed as described above)

Then the system computes the amount actually deducted on the semi-monthly payslip:

- `monthlyGovShares = SSS + PhilHealth + Pag-IBIG`
- `WHT` is computed for the semi cutoff using the SEMI_MONTHLY bracket table
- `statutoryDeductedThisSlip = monthlyGovShares / 2 + WHT`  (semi-monthly only)

This matches the HR sample sheet column **“Total Semi-monthly Contributions”** which is effectively half of the monthly bundle (plus the WHT logic used for that slip).

---

## 12) Net pay (semi-monthly)

Final net pay is:

- `netPay = serviceFee - statutoryDeductedThisSlip`

Where:
- `serviceFee = totalEarnings - totalNonStatutoryDeductions`

---

## 13) Output fields on the payslip

The system returns a single `PayrollItems` record containing:
- Rates: daily/hourly/per-minute
- Hours totals: worked hours, overtime hours
- Earnings: basic pay, overtime pay, total earnings/gross
- Non-statutory deductions: late/undertime deduction, other deductions
- Service fee
- Statutory: SSS/PhilHealth/Pag-IBIG/WHT + “Total Semi-monthly Contributions” (the deducted amount for the slip)
- Net pay

Rendering:
- Server page: `src/main/resources/templates/html/payroll.html`
- API-driven UI: `src/main/resources/static/js/payroll.js`

---

## 14) Important assumptions and known limitations

- **Holiday pay** is still a placeholder (0). If HR requires 2x pay on regular holidays when clocked in, that requires holiday-date data and additional earnings logic.
- **Biweekly** is not a true biweekly window yet (needs a calendar/anchor date).
- **Adjustment earnings** are not implemented (always 0).
- **Recurring deductions** are applied at full amount per active period; if HR expects some deductions to be split per cutoff, implement a “frequency” rule for deductions.
- **Overtime multiplier** currently depends on employment type; HR sample may assume different OT rules (some rows appear to use 1.0x).
- **Tax table `compensationTo`** is not used in the bracket selection logic; selection is done by highest `compensationFrom <= taxablePay`.

---

## 15) Reference: files involved

- Payroll computation:
  - `src/main/java/digital8/payroll/services/PayrollService.java`
- Payroll page:
  - `src/main/resources/templates/html/payroll.html`
- Payroll API:
  - `src/main/java/digital8/payroll/controllers/PayrollApiController.java`
  - `src/main/resources/static/js/payroll.js`
- Tax table:
  - `src/main/java/digital8/payroll/entities/TaxTable.java`
  - `src/main/java/digital8/payroll/repositories/TaxTableRepository.java`
  - seed: `files/taxtable_pay_frequency.sql`
- Sample HR computations:
  - `files/Sample computation - Sheet1.csv`
  - `files/Withholding Tax - Sheet1.csv`

