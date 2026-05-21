# Payroll Computation Guide

**Status:** Current Implementation
**Last Updated:** May 2026
**Applies to:** `PayrollService.java`

This document describes how payroll is computed in this system for semi-monthly and monthly pay periods. It is the single source of truth for the current implementation.

---

## 1. Overview

### 1.1 Pay Periods

| Period Parameter | Description |
|---|---|
| `semi_1` | Days 1-15 of the month |
| `semi_2` | Days 16 through the last day of the month |
| `monthly` | Full calendar month (days 1 through last day) |

### 1.2 Key Entities

| Entity | Table | Role |
|---|---|---|
| `Employees` | `employees` | Employee master record; provides `basicSalary`, `factorRate`, `employmentType`, `holidayPayEligible` |
| `Attendance` | `attendance` | Daily attendance: work hours, OT, late/undertime minutes |
| `EmployeeDeductions` | `employeedeductions` | Per-employee non-statutory deductions (loans, cash advances, union dues, etc.) |
| `EmployeeAdjustments` | `employeeadjustments` | Per-employee adjustments (bonuses, other earnings/deductions) |
| `Deductions` | `deductions` | Deduction type catalog |
| `Adjustments` | `adjustments` | Adjustment type catalog |
| `Holiday` | `holiday` | Holiday calendar |
| `SssTable` | `ssstable` | SSS contribution bracket table |
| `PhilhealthTable` | `philhealth_table` | PhilHealth rate table |
| `PagibigTable` | `pagibig_table` | Pag-IBIG rate table |
| `TaxTable` | `taxtable` | Withholding tax bracket table |

---

## 2. Rate Derivation

All rates are derived from the employee's monthly salary (`basicSalary`) using the **actual number of scheduled working days** in the payroll month, taken from the employee's shift template assignment (Shifting page).

```
workingDaysInMonth = count of non-rest-day calendar dates in the month
                     per the employee's WeeklyScheduleTemplateDay assignment

dailyRate     = monthlyRate / workingDaysInMonth   (6 decimal places, HALF_UP)
hourlyRate    = dailyRate / 8                      (6 decimal places, HALF_UP)
perMinuteRate = hourlyRate / 60                    (6 decimal places, HALF_UP)
```

**Why per-month?** The employee's monthly salary is fixed. But the number of scheduled working days differs between months (e.g. January Mon–Fri = 23 days vs. February Mon–Fri = 20 days). Dividing by the actual working days gives a fair daily rate each month — an employee who works all scheduled days will always receive exactly their monthly salary.

**Working-day count rules:**
- Only non-rest-day dates are counted (using `is_rest_day` from `WeeklyScheduleTemplateDay`).
- Public holidays are **not** subtracted — the count is based purely on the schedule pattern. Holiday pay is a separate additive premium (see Section 5.3).
- For semi-monthly periods (`semi_1` / `semi_2`), the divisor is still the **full month's** working day count, since `basicSalary` is a monthly figure.

**No schedule assigned → payroll is blocked.** If an employee has no shift template assigned for the payroll month (and no prior assignment to fall back on), an error is returned:
```
HTTP 422: "Employee ID <n> has no schedule assignment for <Month> <Year>.
           Please assign a shift schedule before computing payroll."
```

The `factorRate` field on the employee record (20 or 21.75) is retained in the employee form for reference but is **no longer used** in rate computation.

All monetary outputs are ultimately rounded to **2 decimal places (HALF_UP)**.

---

## 3. Pay Period Resolution

```
semi_1  → start = YYYY-MM-01,   end = YYYY-MM-15,   semiMonthly = true
semi_2  → start = YYYY-MM-16,   end = YYYY-MM-lastDay, semiMonthly = true
monthly → start = YYYY-MM-01,   end = YYYY-MM-lastDay, semiMonthly = false
```

Attendance records and employee deductions are filtered to `start <= attendance_date <= end`.

---

## 4. Attendance Aggregation

For all attendance records where `periodStart <= attendance_date <= periodEnd`:

```
totalWorkedHours       = SUM(work_hours)
totalOtHours           = SUM(overtime_hours)
totalLateUndertimeMinutes = SUM(late_minutes) + SUM(undertime_minutes)
```

---

## 5. Earnings Computation

### 5.1 Basic Pay

```
basicPay = hourlyRate * totalWorkedHours
           (rounded to 2 decimals, HALF_UP)
```

### 5.2 Overtime Pay

```
otMultiplier = 1.0   (both Regular and Job Order -- see Known Issues)

overtimePay = hourlyRate * totalOtHours * otMultiplier
              (rounded to 2 decimals, HALF_UP)
```

### 5.3 Regular Holiday Pay

Only **REGULAR** holidays trigger holiday pay, and only for employees where `employee.holidayPayEligible = true`.

**Worked regular holidays:**
- For every hour worked (including OT hours) on a REGULAR holiday that the employee clocked in: add **+1x the hourly rate** as a premium (making the effective rate 2x for those hours).
- `holidayPay += hourlyRate * (work_hours + overtime_hours)` for each worked regular-holiday day

**Unworked regular holidays:**
- For every REGULAR holiday in the period where the employee did **not** clock in: add **+1x the daily rate**.
- `holidayPay += dailyRate` for each unworked regular-holiday day

Special Non-Working holidays are **not** compensated.

```
holidayPay = (hourlyRate * workedRegularHolidayHours)
           + (dailyRate * unworkedRegularHolidayDays)
           (rounded to 2 decimals, HALF_UP)
```

### 5.4 Adjustment Earnings

Earnings-type adjustments are pulled from `EmployeeAdjustments` filtered to the current period and cutoff. See [Section 6.3](#63-adjustment-deductionsearnings).

```
adjustmentEarnings = sum of amounts from EmployeeAdjustments
                     where adjustmentType = "Earnings"
```

### 5.5 Total Earnings (Gross Pay)

```
totalEarnings = basicPay + overtimePay + holidayPay + adjustmentEarnings
                (rounded to 2 decimals, HALF_UP)
```

---

## 6. Non-Statutory Deductions

### 6.1 Late/Undertime Deduction

The per-minute rate is **rounded to 2 decimals first** (matching spreadsheet behavior), then multiplied by total late/undertime minutes.

```
roundedPerMinuteRate = perMinuteRate (rounded to 2 decimals)
lateUndertimeDeduction = roundedPerMinuteRate * totalLateUndertimeMinutes
                          (rounded to 2 decimals, HALF_UP)
```

### 6.2 Employee Deductions

Deductions are summed from `EmployeeDeductions` active in the period, filtered by the `deductionCutoff` field.

**Recurring deductions:**
- Included if the deduction's active window overlaps the pay period: `startDate <= periodEnd AND endDate >= periodStart`
- Further filtered by `deductionCutoff`:
  - `SEMI_1` -- applied only on `semi_1`
  - `SEMI_2` -- applied only on `semi_2` (default)
  - `BOTH` -- applied on both cutoffs

**One-time deductions:**
- Included only if `startDate` falls within the pay period (cutoff filter does not apply)

```
employeeDeductions = SUM(amount) of all qualifying deduction records
                     (rounded to 2 decimals, HALF_UP)
```

### 6.3 Adjustment Deductions/Earnings

Adjustments come from `EmployeeAdjustments` filtered to the current period, resolved against the `Adjustments` type catalog:

- If `adjustmentType = "Deduction"`: added to `adjustmentDeductions`
- If `adjustmentType = "Earnings"`: added to `adjustmentEarnings`

The same date-range and cutoff filtering rules apply as for employee deductions.

```
combinedDeductions = employeeDeductions + adjustmentDeductions
totalNonStatutoryDeductions = combinedDeductions + lateUndertimeDeduction
                               (rounded to 2 decimals, HALF_UP)
```

### 6.4 Service Fee

```
serviceFee = totalEarnings - totalNonStatutoryDeductions
             (rounded to 2 decimals, HALF_UP)
```

---

## 7. Premium Base (Government Contributions Base)

The **premium base** is the salary amount used to compute government contributions. It implements HR's salary-splitting rule:

```
if monthlyRate < 30,000:
    premiumBase = MIN(monthlyRate, 20,000)   // capped at 20,000
else:
    premiumBase = monthlyRate                // no cap
```

Employees below 30,000 PHP are treated as having their salary split into a 20,000 PHP basic (subject to contributions) plus a non-taxable allowance portion.

The premium base is used for **SSS, PhilHealth, Pag-IBIG, and withholding tax** computations.

---

## 8. Statutory (Government) Contributions

All employees are subject to government contributions regardless of employment type.

### 8.1 SSS

SSS is computed via a bracket lookup from the `ssstable` table:

1. Query `ssstable` rows for the current year, ordered by `rangeFrom ASC`
2. Find the row where `rangeFrom <= premiumBase <= rangeTo`
3. Return `employeeShare`

If no matching bracket is found, return 0.

### 8.2 PhilHealth

PhilHealth uses the employee's share rate from `philhealth_table` (effective for the given year). If the table has no configured rate, fall back to **2.5%** of the premium base.

```
philhealth = premiumBase * (employeeShareRate / 100)
             (rounded to 2 decimals, HALF_UP)
```

This represents the **2.5% employee share** (total rate is 5%, split 50/50 between employer and employee).

### 8.3 Pag-IBIG (HDMF)

Pag-IBIG uses the employee's share rate from `pagibig_table` (effective for the given year). If the table has no configured rate, fall back to **2.0%** of the premium base.

```
pagibig = premiumBase * (employeeShareRate / 100)
          (rounded to 2 decimals, HALF_UP)
```

---

## 9. Withholding Tax (WHT)

WHT is computed from the `taxtable` bracket table using the `pay_frequency` column.

### 9.1 Tax Base

| Pay Period | Tax Base | Tax Table |
|---|---|---|
| Semi-monthly | `premiumBase / 2` | `pay_frequency = 'SEMI_MONTHLY'` |
| Monthly | `premiumBase` | `pay_frequency = 'MONTHLY'` |

> **Important:** The tax base uses `premiumBase`, not `monthlyRate`. Employees earning below 30,000 PHP have `premiumBase = 20,000`, so their semi-monthly tax base is `10,000`. This keeps them in the 0% bracket, yielding WHT = 0, matching HR's expected computation. Using `monthlyRate / 2` would incorrectly push them into a taxable bracket.

### 9.2 Bracket Lookup

1. Query `taxtable` rows for `effectiveYear` and `pay_frequency` (e.g. `SEMI_MONTHLY`)
2. Select the bracket row with the **highest** `compensationFrom` value where `compensationFrom <= taxablePay`
3. Compute: `WHT = additionalTax + taxRate * (taxablePay - compensationFrom)`
4. Round to 2 decimals (HALF_UP), floor at 0

### 9.3 Fallback Chain

If the tax table has no rows for the given year and frequency, the system tries these fallbacks in order:

1. Same `pay_frequency`, year `effectiveYear - 1`
2. `MONTHLY` table for current year (converts: `impliedMonthly = taxablePay * 2`, then divides result by 2)
3. Same `pay_frequency`, year `effectiveYear - 1` with `MONTHLY` conversion
4. Simplified formula: `WHT = (taxablePay * 0.10) - 2395.90`, floored at 0

---

## 10. Total Statutory Deductions Per Payslip

### 10.1 Semi-monthly

```
statutoryTotal = SSS + PhilHealth + Pag-IBIG + SEMI_WHT
statutoryDeductedThisSlip = statutoryTotal / 2
```

All four values are summed first, then the entire sum is divided by 2. Each displayed line item (SSS, PhilHealth, Pag-IBIG, WHT) on the payslip is half of its monthly equivalent, so all lines add up exactly to the total.

### 10.2 Monthly

```
statutoryDeductedThisSlip = SSS + PhilHealth + Pag-IBIG + MONTHLY_WHT
```

---

## 11. Net Pay

```
netPay = serviceFee - statutoryDeductedThisSlip
         (rounded to 2 decimals, HALF_UP)
```

---

## 12. Worked Examples

The following examples trace every computation step using real data from the HR reference spreadsheet (`files/Sample computation - Sheet1.csv`). Each step shows the input, the formula applied, and the resulting value.

---

### 12.1 Semi-Monthly Example -- Php 21,000/month, `semi_1`

**Inputs:**
- Monthly rate: Php 21,000.00
- Period: `semi_1` (days 1-15)
- Attendance: 80 work hours, 4 OT hours, 33 late/undertime minutes
- No adjustments, no recurring deductions

#### Step 1: Rate Derivation

| Step | Formula | Result |
|---|---|---|
| factorRate | (employee default) | 20 |
| dailyRate | 21,000 / 20 | Php 1,050.00 |
| hourlyRate | 1,050 / 8 | Php 131.25 |
| perMinuteRate | 131.25 / 60 | Php 2.1875 |

#### Step 2: Earnings

| Item | Formula | Result |
|---|---|---|
| basicPay | 131.25 * 80 | Php 10,500.00 |
| overtimePay | 131.25 * 4 * 1.0 | Php 525.00 |
| holidayPay | (no regular holidays in period) | Php 0.00 |
| adjustmentEarnings | (none) | Php 0.00 |
| **totalEarnings** | 10,500 + 525 + 0 + 0 | **Php 11,025.00** |

#### Step 3: Non-Statutory Deductions

| Item | Formula | Result |
|---|---|---|
| roundedPerMinuteRate | 2.1875 rounded to 2 dp | Php 2.19 |
| lateUndertimeDeduction | 2.19 * 33 | Php 72.27 |
| employeeDeductions | (none in this row) | Php 0.00 |
| combinedDeductions | 0 + 0 | Php 0.00 |
| totalNonStatutory | 0 + 72.27 | Php 72.27 |
| **serviceFee** | 11,025 - 72.27 | **Php 10,952.73** |

> Note: The CSV row shows Total Service Fee as Php 10,702.73. The difference of Php 1,250 is the "Cash Advance / Others" deduction shown separately in the CSV under non-statutory deductions, which is not present in this semi_1 trace but would appear on the `semi_2` slip where `deductionCutoff = SEMI_2`.

#### Step 4: Premium Base

| Condition | Formula | Result |
|---|---|---|
| monthlyRate < 30,000? | Yes | |
| premiumBase | MIN(21,000, 20,000) | Php 20,000.00 |

#### Step 5: Statutory Contributions (monthly amounts)

| Item | Formula | Result |
|---|---|---|
| SSS | ssstable bracket lookup (20,000 in range) | Php 1,000.00 |
| PhilHealth | 20,000 * 0.025 | Php 500.00 |
| Pag-IBIG | 20,000 * 0.02 | Php 400.00 |
| WHT tax base | 20,000 / 2 | Php 10,000.00 |
| WHT | SEMI_MONTHLY bracket at 10,000 (0% bracket) | Php 0.00 |

#### Step 6: Total Statutory Deductions (semi-monthly)

| Item | Formula | Result |
|---|---|---|
| statutoryTotal | 1,000 + 500 + 400 + 0 | Php 1,900.00 |
| **statutoryDeductedThisSlip** | 1,900 / 2 | **Php 950.00** |

#### Step 7: Net Pay

| Item | Formula | Result |
|---|---|---|
| **netPay** | 10,952.73 - 950.00 | **Php 10,002.73** |

> The CSV shows Php 8,752.73 for this row. The difference of Php 1,250 comes from the "Cash Advance / Others" (Php 1,250.00) deduction that applies on the semi_2 cutoff, which is correctly excluded from the semi_1 payslip.

---

### 12.2 Semi-Monthly Example -- Php 19,500/month, `semi_1` (Below 30k)

**Inputs:**
- Monthly rate: Php 19,500.00
- Period: `semi_1` (days 1-15)
- Attendance: 84 work hours, 0 OT, 0 late minutes
- No deductions

#### Step 1: Rate Derivation

| Step | Formula | Result |
|---|---|---|
| dailyRate | 19,500 / 20 | Php 975.00 |
| hourlyRate | 975 / 8 | Php 121.875, rounded to 6 dp |
| perMinuteRate | 121.875 / 60 | Php 2.03125 |

#### Step 2: Earnings

| Item | Formula | Result |
|---|---|---|
| basicPay | (hourlyRate unrounded) * 84 = 121.875 * 84 | Php 10,237.50 |
| overtimePay | (no OT) | Php 0.00 |
| holidayPay | (none) | Php 0.00 |
| **totalEarnings** | | **Php 10,237.50** |

#### Step 3: Non-Statutory Deductions

| Item | Result |
|---|---|
| lateUndertimeDeduction | Php 0.00 |
| employeeDeductions | Php 0.00 |
| **serviceFee** | **Php 10,237.50** |

#### Step 4: Premium Base

| Condition | Result |
|---|---|
| monthlyRate < 30,000? | Yes |
| premiumBase | MIN(19,500, 20,000) = Php 19,500.00 |

> Note: premiumBase = 19,500 (not capped) because 19,500 is already below 20,000.

#### Step 5: Statutory Contributions

| Item | Formula | Result |
|---|---|---|
| SSS | ssstable bracket (19,500 in bracket) | Php 975.00 |
| PhilHealth | 19,500 * 0.025 | Php 487.50 |
| Pag-IBIG | 19,500 * 0.02 | Php 390.00 |
| WHT tax base | 19,500 / 2 | Php 9,750.00 |
| WHT | SEMI_MONTHLY bracket at 9,750 (below first threshold) | Php 0.00 |

#### Step 6: Total Statutory Deductions

| Item | Formula | Result |
|---|---|---|
| statutoryTotal | 975 + 487.50 + 390 + 0 | Php 1,852.50 |
| **statutoryDeductedThisSlip** | 1,852.50 / 2 | **Php 926.25** |

#### Step 7: Net Pay

| Item | Formula | Result |
|---|---|---|
| **netPay** | 10,237.50 - 926.25 | **Php 9,311.25** |

Matches CSV row exactly (Php 9,311.25).

---

### 12.3 Semi-Monthly Example -- Php 21,000/month, `semi_1`, with Employee Deduction

**Inputs:**
- Monthly rate: Php 21,000.00
- Period: `semi_1`
- Attendance: 80 work hours, 0 OT, 0 late minutes
- One recurring deduction: Php 1,250.00 on `SEMI_2` cutoff

#### Key difference from 12.1:

The Php 1,250 deduction has `deductionCutoff = SEMI_2`, so it is **not** deducted on `semi_1`. The `semi_1` payslip shows Php 0 employee deductions. The same deduction appears on the `semi_2` payslip for this employee.

---

### 12.4 Monthly Example -- Php 30,000/month

**Inputs:**
- Monthly rate: Php 30,000.00
- Period: `monthly` (full month)
- Attendance: full month (no partial hours shown in row; totalEarnings = 26,768.73 includes Php 10,000 adjustments)
- Deductions: Php 2,583.81

#### Step 1: Rate Derivation

| Step | Formula | Result |
|---|---|---|
| dailyRate | 30,000 / 20 | Php 1,379.31 |
| hourlyRate | 1,379.31 / 8 | Php 172.41375 |
| perMinuteRate | 172.41375 / 60 | Php 2.8735625 |

#### Step 2: Earnings

| Item | Result |
|---|---|
| basicPay | Php 15,000.00 |
| overtimePay | Php 0.00 |
| adjustmentEarnings | Php 10,000.00 |
| holidayPay | Php 0.00 |
| **totalEarnings** | **Php 26,768.73** |

#### Step 3: Non-Statutory Deductions

| Item | Result |
|---|---|
| lateUndertimeDeduction | Php 0.00 |
| employeeDeductions | Php 2,583.81 |
| **serviceFee** | **Php 24,184.92** |

#### Step 4: Premium Base

| Condition | Result |
|---|---|
| monthlyRate < 30,000? | No (exactly 30,000 is not less than 30,000) |
| premiumBase | Php 30,000.00 |

> At exactly Php 30,000, the `premiumBase = monthlyRate` (no cap applied). SSS bracket ceiling in 2024 is Php 32,999, so 30,000 falls in the SSS bracket.

#### Step 5: Statutory Contributions (full monthly -- no /2)

| Item | Formula | Result |
|---|---|---|
| SSS | ssstable bracket (30,000) | Php 1,500.00 |
| PhilHealth | 30,000 * 0.025 | Php 750.00 |
| Pag-IBIG | 30,000 * 0.02 | Php 600.00 |
| WHT tax base | 30,000 (no division) | Php 30,000.00 |
| WHT | MONTHLY bracket at 30,000 | Php 687.45 |

#### Step 6: Total Statutory Deductions (monthly -- no division)

| Item | Formula | Result |
|---|---|---|
| **statutoryDeductedThisSlip** | 1,500 + 750 + 600 + 687.45 | **Php 3,537.45** |

#### Step 7: Net Pay

| Item | Formula | Result |
|---|---|---|
| **netPay** | 24,184.92 - 3,537.45 | **Php 20,647.47** |

---

### 12.5 Quick Reference -- Semi-Monthly Statutory Contributions

This table shows the statutory contribution amounts for common salary levels on a semi-monthly payslip (i.e., the **deducted per-slip amount = monthly value / 2**).

| Salary | premiumBase | SSS | PhilHealth | Pag-IBIG | WHT (semi base) | Total Stat / Slip |
|---|---|---|---|---|---|---|
| Php 19,500 | Php 19,500 | Php 975.00 | Php 487.50 | Php 390.00 | Php 0.00 (base: 9,750) | Php 926.25 |
| Php 20,000 | Php 20,000 | Php 1,000.00 | Php 500.00 | Php 400.00 | Php 0.00 (base: 10,000) | Php 950.00 |
| Php 21,000 | Php 20,000 (capped) | Php 1,000.00 | Php 500.00 | Php 400.00 | Php 0.00 (base: 10,000) | Php 950.00 |
| Php 30,000 | Php 30,000 | Php 1,500.00 | Php 750.00 | Php 600.00 | Php 0.00 (base: 15,000) | Php 1,425.00 |
| Php 37,000 | Php 37,000 | Php 1,750.00 | Php 925.00 | Php 740.00 | Php 1,304.10 (base: 18,500) | Php 2,359.55 |

---

## 13. Known Issues

| # | Item | Status |
|---|---|---|
| 1 | OT multiplier is **1.0x** for both Regular and Job Order employees. Philippine DOLE rules specify 1.25x for regular employees. | Pending fix |
| 2 | Special Non-Working Holiday pay is **not** compensated (only REGULAR holidays trigger pay) | Not implemented |
| 4 | Attendance query loads **all historical records** then filters in-memory (N+1 / unbounded query risk) | Pending fix |
| 5 | `compensationTo` column in tax table is not used in bracket selection | By design |
| 6 | Employer shares of SSS/PhilHealth/Pag-IBIG are not computed | Not implemented |

---

## 14. Key Files

| File | Purpose |
|---|---|
| `src/main/java/digital8/payroll/services/PayrollService.java` | Core payroll computation |
| `src/main/java/digital8/payroll/entities/Employees.java` | Employee master record |
| `src/main/java/digital8/payroll/entities/Attendance.java` | Attendance records |
| `src/main/java/digital8/payroll/entities/EmployeeDeductions.java` | Employee deduction assignments |
| `src/main/java/digital8/payroll/entities/EmployeeAdjustments.java` | Employee adjustment assignments |
| `src/main/java/digital8/payroll/entities/PayrollItems.java` | Payroll output/payslip |
| `src/main/java/digital8/payroll/entities/Holiday.java` | Holiday calendar |
| `src/main/java/digital8/payroll/entities/TaxTable.java` | WHT bracket table |
| `src/main/java/digital8/payroll/entities/SssTable.java` | SSS contribution table |
| `src/main/resources/templates/html/payroll.html` | Payroll page (Thymeleaf) |
| `src/main/resources/static/js/payroll.js` | Payroll page (API-driven) |
| `src/main/java/digital8/payroll/controllers/payrollViewController.java` | Payroll page controller |
| `src/main/java/digital8/payroll/controllers/PayrollApiController.java` | Payroll API controller |

---

## 15. Superseded Documents

The following documents are **superseded by this guide**:

- `docs/payroll-semi-monthly.md` -- outdated; replaced by this guide
- `PAYROLL_COMPUTATION_CHANGELOG.md` -- outdated; replaced by this guide
