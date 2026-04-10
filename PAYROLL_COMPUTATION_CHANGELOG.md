# Payroll System Computation Changelog
**Last Updated:** April 2026
**Purpose:** Document the current implementation of payroll computation logic in `PayrollService.java`, serving as the single source of truth for what the system actually does.

---

## 1. Core Rate Constants & Rules

All rate derivation follows fixed constants hardcoded in `PayrollService.java`:

| Constant | Value | Formula |
|---|---|---|
| Working days/month | 20 | Fixed |
| Hours/day | 8 | Fixed |
| Daily Rate | `monthlyRate / 20` | |
| Hourly Rate | `dailyRate / 8` | |
| Per-Minute Rate | `hourlyRate / 60` | Rounded to 2 decimals **before** multiplying against minutes |

> The per-minute rate is rounded to 2 decimal places early (e.g. ₱2.19 instead of ₱2.1875) to match HR's spreadsheet behavior.

---

## 2. Employment Type

The system supports two employment types: `Regular` and `Job Order`.

| | Regular | Job Order |
|---|---|---|
| SSS | Bracket lookup (premiumBase) | Same — all employees contribute |
| PhilHealth | % formula (premiumBase) | Same |
| Pag-IBIG | % formula (premiumBase) | Same |
| WHT | Bracket table (premiumBase) | Same |
| OT Multiplier | **1.0x (⚠️ pending fix to 1.25x)** | 1.0x |

> **Note:** The original design intended Job Order employees to pay 5% flat EWT and no government contributions. This was revised — HR policy now applies government contributions to all employees regardless of type. The bracket table (not simplified formula) is used for WHT for all employees.

---

## 3. Premium Base (Salary Splitting)

The `premiumBase` is used as the taxable base for all four statutory deductions (SSS, PhilHealth, Pag-IBIG, WHT):

```
if monthlyRate < ₱30,000:
    premiumBase = min(monthlyRate, ₱20,000)   // capped at ₱20k
else:
    premiumBase = monthlyRate
```

**Why:** Employees earning below ₱30,000 are treated as having their salary split into a ₱20,000 basic component (subject to government contributions) and a non-taxable allowance portion. This matches HR's spreadsheet behavior exactly.

---

## 4. Statutory Contributions

### SSS
Computed via bracket lookup in `ssstable` using `premiumBase` and current year.
Based on SSS Circular No. 2024-006 (14%–15% contribution bands up to ₱35,000 MSC ceiling).

### PhilHealth
```
PhilHealth = (premiumBase × 5%) / 2    // 2.5% employee share
```

### Pag-IBIG
```
Pag-IBIG = premiumBase × 2%
```

### Withholding Tax (WHT)

**Semi-monthly:**
```
semiBase = premiumBase / 2
SEMI_WHT = SEMI_MONTHLY_table(semiBase)
```

**Monthly:**
```
MONTHLY_WHT = MONTHLY_table(premiumBase)
```

> **Critical:** Use `premiumBase / 2`, NOT `monthlyRate / 2`. Sub-30k employees have `premiumBase = ₱20,000`, so `semiBase = ₱10,000`, which stays in the 0% bracket → WHT = ₱0. Using `monthlyRate / 2` incorrectly pushes these employees into a taxable bracket. (This was an active bug, fixed April 2026.)

---

## 5. Total Statutory Deductions (Final Formula)

### Semi-monthly
```
Total = (SSS + PhilHealth + Pag-IBIG + SEMI_WHT) / 2
```
- All four values are summed first, then the entire sum is divided by 2
- **Matches HR's spreadsheet exactly** — verified against all rows in `files/Sample computation - Sheet1.csv`
- Each line item displayed on the payslip = the full value divided by 2, so all displayed lines sum to the total

### Monthly
```
Total = SSS + PhilHealth + Pag-IBIG + MONTHLY_WHT
```

---

## 6. Non-Statutory Deductions

All employee deductions (loans, cash advances, union dues, etc.) are handled identically with no type-based branching:

- Stored in `employeedeductions` table with `startDate`, `endDate`, `isRecurring`, `amount`
- **Recurring deductions:** included if the cutoff date range overlaps the deduction's active window (`startDate ≤ periodEnd AND endDate ≥ periodStart`)
- **One-time deductions:** included only if `startDate` falls within the cutoff window
- Named breakdown per deduction type appears on the payslip

> **Known issue (⚠️ pending fix):** Recurring deductions are currently included on **both** `semi_1` and `semi_2` cutoffs for semi-monthly pay. The intended behavior is to deduct once per month (second cutoff only). Fix pending.

---

## 7. Net Pay Formula

```
Net Pay = Service Fee - Total Statutory Deductions

where:
  Service Fee = Total Earnings - Total Non-Statutory Deductions
  Total Earnings = Basic Pay + Overtime Pay + Holiday Pay + Adjustment Earnings
```

---

## 8. UI / Payslip Display

Both `payroll.html` (Thymeleaf) and `payroll.js` (API/compute-button path) display contributions consistently:

| Line | Semi-monthly display | Monthly display |
|---|---|---|
| SSS | `item.sss / 2` | `item.sss` |
| PhilHealth | `item.philhealth / 2` | `item.philhealth` |
| Pag-IBIG | `item.pagibig / 2` | `item.pagibig` |
| Withholding Tax | `item.tax / 2` | `item.tax` |
| **Total** | `semiMonthlyContributions` | `totalDeductions` |

> `item.sss/philhealth/pagibig` store monthly amounts. `item.tax` stores the SEMI_WHT value for semi-monthly payslips, or MONTHLY_WHT for monthly. All four displayed lines are halved for semi-monthly so they add up exactly to the total.

---

## 9. Known Pending Items

| # | Item | Status |
|---|---|---|
| 1 | OT multiplier for Regular employees (should be 1.25x, currently 1.0x) | ⚠️ Pending |
| 2 | Recurring deductions deducted on both semi_1 and semi_2 (should be once per month) | ⚠️ Pending |
| 3 | Adjustment Earnings not yet wired to payroll (Bonuses/Adjustments page is a stub) | ⚠️ Pending |
| 4 | Special Non-Working Holiday pay (30% premium for unworked SNWHs) | ⚠️ Pending |

---

## 10. DB Schema (payrollitems table)

Key columns persisted per payslip computation:

| Column | Description |
|---|---|
| `daily_rate`, `hourly_rate`, `per_minute_rate` | Rate breakdown |
| `total_worked_hours`, `total_ot_hours` | Hours breakdown |
| `late_undertime_minutes`, `late_undertime_deduction` | Late/undertime |
| `adjustment_earnings`, `adjustment_deductions` | Earnings adjustments and employee deductions |
| `service_fee` | Total earnings minus non-statutory deductions |
| `semi_monthly_contributions` | Total statutory deducted this slip |
| `employment_type` | `Regular` or `Job Order` |
