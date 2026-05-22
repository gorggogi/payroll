# Daily Rate & Net Pay Comparison (with Overtime)

**January 2026 | Monthly Salary: 20,000**

> January 2026 calendar working days = **20 days**
>
> - Employee A: Mon-Fri = **20 scheduled days**
> - Employee B: Mon-Wed-Fri = **12 scheduled days**
>
> Both employees work **8 hours rest day overtime** in January.
>
> OT multiplier used: **1.0** (from `OT_MULTIPLIER_DEFAULT` in PayrollService)

---

## Daily Rate

| Employee | Schedule-Based | Fixed Per-Month (Calendar) |
|---|---|---|
| **Employee A (M-F)** | 20,000 / 20 = **1,000.00** | 20,000 / 20 = **1,000.00** |
| **Employee B (M-W-F)** | 20,000 / 12 = **1,666.67** | 20,000 / 20 = **1,000.00** |

---

## Hourly Rate

| Employee | Schedule-Based | Fixed Per-Month (Calendar) |
|---|---|---|
| **Employee A (M-F)** | 1,000 / 8 = **125.00** | 1,000 / 8 = **125.00** |
| **Employee B (M-W-F)** | 1,666.67 / 8 = **208.33** | 1,000 / 8 = **125.00** |

---

## Rest Day OT Pay (8 hours)

| Employee | Schedule-Based | Fixed Per-Month (Calendar) |
|---|---|---|
| **Employee A (M-F)** | 125 * 8 * 1.0 = **1,000.00** | 125 * 8 * 1.0 = **1,000.00** |
| **Employee B (M-W-F)** | 208.33 * 8 * 1.0 = **1,666.64** | 125 * 8 * 1.0 = **1,000.00** |

---

## Total Pay (Basic + OT)

| Employee | Schedule-Based | Fixed Per-Month (Calendar) |
|---|---|---|
| **Employee A (M-F)** | 20,000 + 1,000 = **21,000.00** | 20,000 + 1,000 = **21,000.00** |
| **Employee B (M-W-F)** | 20,000 + 1,666.64 = **21,666.64** | 12,000 + 1,000 = **13,000.00** |

---

## Summary

| | Schedule-Based | Fixed Per-Month (Calendar) |
|---|---|---|
| Daily rate | **Varies by employee** | **Same for everyone** |
| OT rate | **Varies by employee** | **Same for everyone** |
| Employee A total (20 days + OT) | 21,000 | 21,000 |
| Employee B total (12 days + OT) | 21,666.64 | 13,000 |

---

## Key Insight

Under **schedule-based**, Employee B earns **666.64 more** than Employee A despite working fewer days, purely because their daily rate is inflated (+67%). The company pays significantly more in OT for alternating-schedule employees.

Under **fixed per-month**, the daily rate is the same for everyone. Employee B earns significantly less (13,000) because they only worked 12 days — but the OT rate is fair and consistent with everyone else in the company.
