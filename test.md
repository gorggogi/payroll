# Manual Website Test Plan — Recurring Deductions with Start/End Dates

## Prerequisites

Create these deductions for a test employee on the **Deductions** management page before running the tests.

---

## Group A: Recurring Deduction — Active All Year (Jan 1 – Dec 31)


| Field          | Value               |
| -------------- | ------------------- |
| Deduction Name | `TEST-RECUR-ACTIVE` |
| Amount         | 1,500.00            |
| Is Recurring?  | Yes                 |
| monthlyCutoff  | BOTH                |
| Start Date     | `2026-01-01`        |
| End Date       | `2026-12-31`        |


### Test A1 — today = May 10 (SEMI_1)

- `TEST-RECUR-ACTIVE` should show **1,500.00** in monthlyCutoff.
- Reason: active all year, BOTH cutoff applies every period.

### Test A2 — today = May 20 (SEMI_2)

- `TEST-RECUR-ACTIVE` should show **1,500.00** in monthlyCutoff.
- Reason: BOTH applies every period regardless of today.

---

## Group B: Recurring Deduction — Starts Mid-Year (May 1 – Dec 31)


| Field          | Value                   |
| -------------- | ----------------------- |
| Deduction Name | `TEST-RECUR-STARTS-MAY` |
| Amount         | 800.00                  |
| Is Recurring?  | Yes                     |
| monthlyCutoff  | SEMI_1                  |
| Start Date     | `2026-05-01`            |
| End Date       | `2026-12-31`            |


### Test B1 — today = May 10, SEMI_1, after start date

- `TEST-RECUR-STARTS-MAY` should show **800.00** in monthlyCutoff.
- Reason: today=May 10 is in SEMI_1, startDate=May 1 has already passed, endDate is in the future.

### Test B2 — today = April 10, before start date

- `TEST-RECUR-STARTS-MAY` should show **0.00** (or be absent).
- Reason: startDate (May 1) is in the future — deduction is not yet active.

### Test B3 — today = January 1, 2027, after end date

- `TEST-RECUR-STARTS-MAY` should show **0.00** (or be absent).
- Reason: endDate (Dec 31, 2026) has passed — deduction is expired.

---

## Group C: Recurring Deduction — Expires Mid-Year (Jan 1 – June 15)


| Field          | Value                     |
| -------------- | ------------------------- |
| Deduction Name | `TEST-RECUR-EXPIRES-JUNE` |
| Amount         | 600.00                    |
| Is Recurring?  | Yes                       |
| monthlyCutoff  | SEMI_1                    |
| Start Date     | `2026-01-01`              |
| End Date       | `2026-06-15`              |


### Test C1 — today = May 10 (SEMI_1), active period

- `TEST-RECUR-EXPIRES-JUNE` should show **600.00** in monthlyCutoff.
- Reason: within [startDate, endDate], today is SEMI_1, SEMI_1 cutoff matches.

### Test C2 — today = June 10 (SEMI_1), before end date

- `TEST-RECUR-EXPIRES-JUNE` should show **0.00** (or be absent).
- Reason: June 10 is SEMI_2, SEMI_1 cutoff does not apply this period, even though endDate hasn't passed yet.

### Test C3 — today = June 20 (SEMI_2), after end date

- `TEST-RECUR-EXPIRES-JUNE` should show **0.00**.
- Reason: endDate (June 15) has passed.

---

## Group D: Recurring Deduction — No End Date (Open-Ended)


| Field          | Value                  |
| -------------- | ---------------------- |
| Deduction Name | `TEST-RECUR-OPEN`      |
| Amount         | 400.00                 |
| Is Recurring?  | Yes                    |
| monthlyCutoff  | SEMI_2                 |
| Start Date     | `2026-01-01`           |
| End Date       | *(leave blank / null)* |


### Test D1 — today = May 10 (SEMI_1)

- `TEST-RECUR-OPEN` should show **0.00** in monthlyCutoff.
- Reason: no endDate (indefinite), but today is SEMI_1 and cutoff is SEMI_2.

### Test D2 — today = May 20 (SEMI_2)

- `TEST-RECUR-OPEN` should show **400.00** in monthlyCutoff.
- Reason: today is SEMI_2, SEMI_2 cutoff matches.

---

## Group E: One-Time Deduction — Start Date Boundary


| Field          | Value                  |
| -------------- | ---------------------- |
| Deduction Name | `TEST-ONETIME`         |
| Amount         | 500.00                 |
| Is Recurring?  | No                     |
| Start Date     | `2026-05-15`           |
| End Date       | *(leave blank / null)* |


### Test E1 — today = May 10 (SEMI_1), before one-time start date

- `TEST-ONETIME` should show **0.00** in monthlyCutoff.
- Reason: one-time startDate (May 15) is in the future relative to May 10.

### Test E2 — today = May 15 (SEMI_1 boundary, equals start date)

- `TEST-ONETIME` should show **500.00** in monthlyCutoff.
- Reason: startDate falls exactly on the period boundary (SEMI_1 = May 1–15), included.

### Test E3 — today = May 16 (SEMI_2), after one-time start date

- `TEST-ONETIME` should show **0.00** in monthlyCutoff for the *next* SEMI_2 period.
- Reason: startDate (May 15) is in SEMI_1, not SEMI_2 — appears in current month's cutoff but not the next SEMI_2 period.

---

## Summary Table


| Test | today       | Deduction Type | Cutoff | Start Date | End Date | Expected monthlyCutoff |
| ---- | ----------- | -------------- | ------ | ---------- | -------- | ---------------------- |
| A1   | May 10      | Recurring      | BOTH   | Jan 1      | Dec 31   | 1,500.00               |
| A2   | May 20      | Recurring      | BOTH   | Jan 1      | Dec 31   | 1,500.00               |
| B1   | May 10      | Recurring      | SEMI_1 | May 1      | Dec 31   | 800.00                 |
| B2   | Apr 10      | Recurring      | SEMI_1 | May 1      | Dec 31   | 0.00                   |
| B3   | Jan 1, 2027 | Recurring      | SEMI_1 | May 1      | Dec 31   | 0.00                   |
| C1   | May 10      | Recurring      | SEMI_1 | Jan 1      | Jun 15   | 600.00                 |
| C2   | Jun 10      | Recurring      | SEMI_1 | Jan 1      | Jun 15   | 0.00                   |
| C3   | Jun 20      | Recurring      | SEMI_1 | Jan 1      | Jun 15   | 0.00                   |
| D1   | May 10      | Recurring      | SEMI_2 | Jan 1      | *(null)* | 0.00                   |
| D2   | May 20      | Recurring      | SEMI_2 | Jan 1      | *(null)* | 400.00                 |
| E1   | May 10      | One-time       | —      | May 15     | *(null)* | 0.00                   |
| E2   | May 15      | One-time       | —      | May 15     | *(null)* | 500.00                 |
| E3   | May 16      | One-time       | —      | May 15     | *(null)* | 0.00                   |


---

## Period Reference


| Period | Date Range                  |
| ------ | --------------------------- |
| SEMI_1 | Day 1 – Day 15 of the month |
| SEMI_2 | Day 16 – End of month       |


## How to Run

1. Set up the test employee with the deductions listed above.
2. Navigate to the **Deductions** balances/process view for that employee.
3. Check the `monthlyCutoff` column for each scenario.
4. To change `today`, adjust the system date or use a test account with a different payroll reference date.

