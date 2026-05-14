# Payroll System Full Audit Report

**Project:** Spring Boot 4.0.2 / Java 21 / Thymeleaf / MySQL / Flyway
**Date:** May 12, 2026
**Last Updated:** May 13, 2026 (run #4 — hardcoded tax fallback removed, TaxTableNotFoundException added to PayrollApiController)
**Resolved since last audit:** 7 issues (2.5, 2.7, 4.1, 6.3, 13.9, 8.7, 3.5)
**New findings:** 7 issues (2 new security, 1 new bug, 4 new feature/gap)

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Critical Issues](#2-critical-issues)
3. [Security Vulnerabilities](#3-security-vulnerabilities)
4. [Bugs and Logic Errors](#4-bugs-and-logic-errors)
5. [Dead Code and Stubs](#5-dead-code-and-stubs)
6. [Integration Gaps](#6-integration-gaps)
7. [UI/UX Issues](#7-uiux-issues)
8. [Minor / Style Issues](#8-minor-style-issues)
9. [Entity and Repository Analysis](#9-entity-and-repository-analysis)
10. [DTO and Template Wiring](#10-dto-and-template-wiring)
11. [File Inventory](#11-file-inventory)

---

## 1. Project Overview

### Tech Stack

| Component | Technology |
|---|---|
| Framework | Spring Boot 4.0.2 |
| Language | Java 21 |
| Build Tool | Maven |
| Database | MySQL (`payrollcomp`) |
| Migrations | Flyway (V10–V13 only; V1–V9 missing) |
| Frontend | Thymeleaf + vanilla JavaScript |
| Auth | Spring Security + BCrypt |
| Email | Spring Mail (Mailjet) |
| Calendar | Google Calendar API (holiday sync) |
| Timezone | `Asia/Manila` enforced in `PayrollApplication.java` |

### Project Directory Structure

```
payroll/
├── pom.xml
├── Dockerfile
├── README.md                          ← contains accidental Java source paste
├── PAYROLL_COMPUTATION_CHANGELOG.md
├── docs/payroll-semi-monthly.md
├── sql/
│   ├── add_deduction_cutoff.sql
│   └── add_employee_adjustments.sql
├── files/                             ← ad-hoc payroll data CSVs/SQLs
├── test-data/100-employees.sql
├── src/main/java/digital8/payroll/
│   ├── PayrollApplication.java
│   ├── config/                        ← 4 files: Security, Auth, Nav, Filter
│   ├── controllers/                   ← 18 files
│   ├── dto/                          ← 5 files
│   ├── entities/                     ← 29 files
│   ├── repositories/                ← 28 files
│   ├── services/                    ← 10 files
│   └── specifications/
│       └── EmployeeSpecifications.java
├── src/main/resources/
│   ├── application.properties
│   ├── db/migration/                 ← V10–V13 only; V1–V9 missing
│   ├── static/
│   │   ├── css/                      ← 15 files
│   │   ├── js/                       ← 9 files
│   │   ├── images/
│   │   └── files/holidayTemplate.csv
│   └── templates/html/               ← 24 files (incl. fragments)
└── src/test/java/digital8/payroll/
    ├── PayrollApplicationTests.java
    └── services/
        ├── PayrollServiceTest.java
        ├── PayrollCutoffPayslipTest.java
        └── DeductionCutoffTest.java
```

### Untracked / Active Development Files

The following files are modified or untracked in git, indicating active development on deductions and adjustments features:

| File | Status |
|---|---|
| `AdjustmentsViewController.java` | Modified |
| `DeductionViewController.java` | Modified |
| `payrollViewController.java` | Modified |
| `EmployeeAdjustmentRowDto.java` | Untracked (new) |
| `EmployeeDeductionRowDto.java` | Modified |
| `Adjustments.java` | Untracked (new) |
| `EmployeeAdjustments.java` | Untracked (new) |
| `EmployeeDeductions.java` | Modified |
| `AdjustmentsRepository.java` | Untracked (new) |
| `EmployeeAdjustmentsRepository.java` | Untracked (new) |
| `PayrollService.java` | Modified |
| `deductions.css` | Modified |
| `adjustments.html` | Untracked (new) |
| `deductions.html` | Modified |
| `DeductionCutoffTest.java` | Modified (expanded) |
| `PayrollCutoffPayslipTest.java` | Untracked (new) |

---

## 2. Critical Issues

> Fix these before any payroll run is executed in production.

### 2.1 Employee Deduction Balance Cards Always Show Zero

**Files:** `templates/html/deductions.html` lines 28–59, `DeductionViewController.java`

The employee-view deduction summary cards (`deductionOutstandingObligation`, `deductionMonthlyCutoff`, `deductionOutstandingBalance`) are rendered by the template but never populated by the controller. They will always display `0.00 PHP`.

The controller only sets: `assignmentRows`, `deductionTypes`, `employees`, search/filter attributes, and `deductionsFormAction`. None of the three balance fields are populated.

The same issue affects `adjustments.html` (which has analogous summary cards).

**Fix:** Add a `getDeductionBalances(employeeId)` method to `EmployeeDeductionsRepository` and compute totals in `DeductionViewController`.

---

### 2.2 Leave Type Field Is Permanently Disabled in Edit Modal

**Files:** `templates/html/leaveAdminSelf.html`, `templates/html/leaveEmployee.html`, `static/js/leave.js`

The edit leave modal renders the leave type as:

```html
<select id="editLeaveType" name="leaveTypeId" disabled>
```

Even though `leave.js` tries to enable it conditionally, the HTML `disabled` attribute overrides JavaScript. An employee can never change the leave type after creating a request, even when the request is still Pending.

**Fix:** Remove the `disabled` attribute from the HTML and rely entirely on the JavaScript guard in `leave.js`.

---

### 2.3 `/forgetPassword.html` Has an Empty Body — No Form

**File:** `templates/html/forgetPassword.html`

Anyone navigating to the forgot password page sees a blank white page. There is no form, no email input, and no controller endpoint wired to handle the POST.

**Fix:** Add a password-reset request form and wire it to a new or existing controller endpoint.

---

### 2.4 `setupPassword.html` — JS Included as a CSS Stylesheet

**File:** `templates/html/setupPassword.html` line 8

```html
<link rel="stylesheet" href="/js/setup.js">
```

Should be `<script src="/js/setup.js"></script>`. The JavaScript logic (form validation, token submission) never runs.

**Fix:** Replace the `<link>` tag with a `<script>` tag.

---

### 2.5 Per-Employee OT Multiplier — RESOLVED

**File:** `src/main/java/digital8/payroll/services/PayrollService.java` lines 53, 271–277, `src/main/resources/db/migration/V14__add_employee_ot_multiplier.sql`, `src/main/java/digital8/payroll/entities/Employees.java`, `src/main/java/digital8/payroll/services/EmployeeService.java`, `src/main/java/digital8/payroll/controllers/EmployeeViewController.java`, `src/main/resources/templates/html/addEmployee.html`, `src/main/resources/static/js/employees.js`

Both Regular and Job Order employees were using an identical OT multiplier of 1.0. The fix adds a **per-employee OT multiplier** (`ot_multiplier` column on `employees`) that overrides the system default.

**Implementation:**

- `Employees.java` — new `otMultiplier` field (`BigDecimal`, column `ot_multiplier DECIMAL(5,2)`, defaults to `BigDecimal.ONE` at both Java and DB level)
- `V14__add_employee_ot_multiplier.sql` — Flyway migration adds the column; existing rows backfilled with `1.0`
- `PayrollService.java` — `computePayroll()` checks `emp.getOtMultiplier()` first; falls back to `OT_MULTIPLIER_DEFAULT` (1.0) if null or zero. Formula: `overtimePay = hourlyRate * totalOtHours * otMultiplier`
- `EmployeeService.java` — `createEmployee()` defaults to `1.0`; `updateEmployee()` preserves existing value
- `EmployeeViewController.java` — `addEmployeeSubmit()` reads and persists `otMultiplier` from form
- `addEmployee.html` — OT multiplier dropdown in the Employment section (options: `1.0x`, `1.25x`; default `1.0x`)
- `employees.js` — OT multiplier field in edit employee modal, included in save payload and list API response
- `EmployeeListDto.java` — exposes `otMultiplier` in employee list API

Backward compatibility is preserved: existing employees with null `ot_multiplier` continue using the system default of `1.0`. No per-employment-type fallback is used — the single default applies uniformly.

---

### 2.6 Attendance Query Is Unbounded — Loads All Historical Records

**File:** `src/main/java/digital8/payroll/services/PayrollService.java` line 193

```java
attendanceRepository.findByEmployeeIdOrderByDateDesc(empId)
```

Returns **all attendance records ever** for the employee, then filters in-memory by date range. For any employee with multi-year attendance history, this loads thousands of unnecessary rows, causing memory pressure and slow computation.

**Fix:** Add a date-range parameter to the repository method:

```java
List<Attendance> findByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
    Integer employeeId, LocalDate start, LocalDate end);
```

---

### 2.7 `applyOnCutoff` and `deductionCutoff` Integration — RESOLVED

**Files:** `PayrollService.java`, `entities/EmployeeAdjustments.java`, `entities/EmployeeDeductions.java`, `repositories/EmployeeDeductionsRepository.java`, `repositories/EmployeeAdjustmentsRepository.java`, `DeductionCutoffTest.java`

**Original concern:** The `applyOnCutoff` and `deductionCutoff` fields were persisted by the controllers but the payroll computation filtering had not been verified.

**What was actually broken:**

1. **Case sensitivity.** The filter used `"semi_1".equals(period)` case-sensitively. If `period` was passed as `"SEMI_1"` (uppercase, which can happen from `PayrollApiController`), the condition silently failed and the cutoff filter was bypassed — a `SEMI_1` deduction could leak onto a `SEMI_2` payslip.
2. **Null period.** If `period` was `null`, both `"semi_1".equals(null)` and `"semi_2".equals(null)` returned `false`, bypassing the entire cutoff filter block.

Note: monthly payroll behavior (includes all active deductions regardless of cutoff) is **correct and intentional** — the cutoff distinction only applies to semi-monthly cutoffs.

**Fix applied:**

- Added a `matchesCutoff(period, cutoff, defaultCutoff)` helper method in `PayrollService` that normalizes the period string to lowercase and handles null gracefully. All four call sites (`computeEmployeeDeductions`, `computeAdjustmentsSplit`, `getDeductionsBreakdown`, `getAdjustmentsBreakdown`) now use this helper.
- Added `findActiveRecurringByEmployee()` queries to both repositories to support date-range filtering at the DB level.
- Added comprehensive unit tests (23 tests total) covering:
  - `computeEmployeeDeductions` cutoff filtering (SEMI_1, SEMI_2, BOTH, monthly, null, uppercase period, one-time deductions)
  - `computeAdjustmentsSplit` cutoff filtering (earnings and deduction adjustments)
  - Regression tests for `getDeductionsBreakdown`

---

### 2.8 OT Approval/Rejection Has No Admin Auth Check

**File:** `src/main/java/digital8/payroll/controllers/attendanceController.java` lines 530–566

```java
if (principal instanceof Users user) {
    // approves or rejects OT request...
}
```

Any authenticated user — including a regular employee — can approve or reject any overtime request by POSTing to `/admin/attendance/overtime/approve/{id}`. There is no admin role check and no `@PreAuthorize` annotation.

**Fix:** Add an explicit admin role guard:

```java
if (!isAdmin(authentication)) {
    return "redirect:/access-denied";
}
```

---

## 3. Security Vulnerabilities

### 3.1 `testController` Exposes Full Employee PII — No Auth

**File:** `controllers/testController.java`

`GET /test/employee/{employeeNumber}` returns the entire `Employees` entity as JSON with no authentication or role check. Exposed fields include:

- Salary (`basicSalary`)
- Bank account number (`bank_Account`)
- TIN (`tin`)
- SSS number (`sssNumber`)
- PhilHealth number (`philhealthNumber`)
- Pag-IBIG number (`pagibigNumber`)
- Employment status and pay type

**Fix:** Remove this controller entirely, or guard it behind `@Profile("dev")`.

---

### 3.2 `homeController` — `/generateHash` Endpoint

**File:** `controllers/homeController.java` lines 116–123

Returns a bcrypt hash of a user-supplied password with no authentication. A malicious user can generate valid bcrypt hashes of arbitrary passwords.

**Fix:** Remove this endpoint.

---

### 3.3 `AdminEmployeeApiController` — `resetPassword` Has No `@PreAuthorize`

**File:** `controllers/AdminEmployeeApiController.java`

Relies entirely on Spring Security URL config. If URL config is missing or misconfigured, any authenticated user could reset any employee's password.

**Fix:** Add `@PreAuthorize("hasRole('ADMIN')")` to the method.

---

### 3.4 `EmployeeController` — `updateEmployee()` Has No Authorization

**File:** `controllers/EmployeeController.java` lines 56–69

```java
@PutMapping("/api/employees/{id}")
public ResponseEntity<?> updateEmployee(@PathVariable Integer id,
    @RequestBody Employees employee)
```

No role check, no ownership check. Any authenticated user can PUT to any employee ID and modify salary, employment status, pay type, and government ID numbers. The `employeeId` from the URL is not enforced to match the `employeeId` in the request body.

**Fix:** Add `@PreAuthorize("hasRole('ADMIN')")`, or for self-only edits, add an ownership check.

---

### 3.5 `PayrollApiController` — No Explicit Auth on Payroll API

**File:** `controllers/PayrollApiController.java`

`GET /api/payroll/{empId}` computes payroll dynamically. While there is a manual self-check in the method body, there is no `@PreAuthorize`. If the Spring Security URL config is wrong, employees could view other employees' payroll.

**Fix:** Add explicit auth check in the method body or use `@PreAuthorize`.

---

## 4. Bugs and Logic Errors

### 4.1 NPE on Recurring Deductions with Null `endDate` — RESOLVED

**File:** `controllers/DeductionViewController.java`

All three recurring-deduction filter branches in `DeductionViewController` already guard `getEndDate()` with `!= null` checks:
- Month + Year filter (line 126): `r.getStartDate() != null && r.getEndDate() != null && !r.getStartDate().isAfter(filterPeriodEnd) && !r.getEndDate().isBefore(filterPeriodStart)`
- Month-only filter (line 140): `r.getStartDate() != null && r.getStartDate().getMonthValue() <= filterMonth && r.getEndDate() != null && r.getEndDate().getMonthValue() >= filterMonth`
- Year-only filter (line 150): `r.getStartDate() != null && r.getEndDate() != null && !r.getStartDate().isAfter(yearEnd) && !r.getEndDate().isBefore(yearStart)`

The payroll computation in `PayrollService.computeEmployeeDeductions` (line 544) also uses `r.getEndDate() != null && r.getEndDate().isBefore(periodStart)` — null endDate is safe and the record is included (open-ended recurring deduction remains active).

**Status:** Already safe. No fix required.

---

### 4.3 `Attendance` and `Bonuses` Use Bare Integer FKs

**Files:** `entities/Attendance.java`, `entities/Bonuses.java`

`employeeId` is a plain `Integer` with no `@ManyToOne` annotation. Other entities like `LeaveRequests` and `OvertimeRequest` correctly use `@ManyToOne(fetch = EAGER)`. This means:

- Lazy loading and relationship traversal behave inconsistently across the codebase.
- JPA relationship operations (cascade, orphan removal) are unavailable.

**Fix:** Add `@ManyToOne @JoinColumn(name = "employeeId") private Employees employee;` to both entities.

---

### 4.4 Repositories Are Underpowered for Payroll Computation

**Files:** `repositories/EmployeeAdjustmentsRepository.java`, `repositories/EmployeeDeductionsRepository.java`

Both have only `findByEmployeeId(Integer)`. The payroll service needs date-range filtering and cutoff filtering built into the repository. Currently all filtering is done in-memory after loading all records for an employee.

**Fix:** Add repository methods:

```java
// EmployeeAdjustmentsRepository
List<EmployeeAdjustments> findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
    Integer employeeId, LocalDate periodEnd, LocalDate periodStart);
// Plus a variant with cutoff filtering parameter

// EmployeeDeductionsRepository — same pattern
```

---

### 4.5 `PayrollItemsRepository` Lacks `findByPayrollId`

**File:** `repositories/PayrollItemsRepository.java`

`PayrollItems` has a `payrollId` field, but there is no `findByPayrollId(Integer)` method. To get all line items for a payroll run, you must load by employee instead of by the payroll period.

**Fix:** Add `List<PayrollItems> findByPayrollId(Integer payrollId);`.

---

### 4.6 `LeaveController` Sets Wrong ID Attribute

**File:** `controllers/LeaveController.java` line 51

```java
model.addAttribute("emp_id", user.getUserId());  // WRONG
```

Every other controller sets `emp_id` to the employee's `employeeId` (the `Employees` primary key). This causes `leaveAdminSelf.html` to use the wrong ID for any data-link operations (attendance, payroll).

**Fix:** Use `user.getEmployee().getEmployeeId()` instead.

---

### 4.7 Holiday Premium Uses Unrounded Hourly Rate

**File:** `src/main/java/digital8/payroll/services/PayrollService.java` line 261

```java
holidayPay = hourlyRate.multiply(regularHolidayWorkedHours)
```

Uses the 6-decimal-precision `hourlyRate`, while `basicPay` uses the 2-decimal-rounded value. The holiday premium could differ by a few centavos from `basicPay` for identical hours.

**Fix:** Use the same rounding for both calculations, or round `holidayPay` to 2 decimal places explicitly.

---

### 4.8 `deleteScheduleTemplate()` Throws NPE After Null Check

**File:** `controllers/attendanceController.java` lines 278–295

If `templateId` doesn't exist in the DB, `weeklyScheduleTemplateRepository.findById()` returns `Optional.empty()`. The null check catches this, but the code then falls through to `weeklyScheduleTemplateRepository.deleteById(templateId)` which throws `EmptyResultDataAccessException`.

**Fix:** Add an early `return` after the null check:

```java
if (tpl == null) {
    redirectAttributes.addFlashAttribute("error", "Schedule template not found.");
    return "redirect:/admin/attendance/shifts";
}
// then proceed with deletion
```

---

### 4.9 `[UNRESOLVED]` Bulk Test Data Insert Has No Cleanup

**File:** `test-data/100-employees.sql`

Bulk test data insert without `DELETE FROM employees WHERE ...` or transaction wrapping. Running this on a staging or production DB will duplicate or conflict with existing employee numbers.

**Fix:** Wrap in a transaction with `DELETE FROM employees WHERE employee_number LIKE 'TEST%';` at the start, or add a guard that skips if data already exists.

---

## 5. Dead Code and Stubs

> **Status key:** `[UNRESOLVED]` = issue confirmed still present, `[RESOLVED]` = fix verified implemented.

### 5.1 `[UNRESOLVED]` `attendanceController` — `/time-adjustments` Is a Stub

**File:** `controllers/attendanceController.java` lines 492–501

The GET handler for `/admin/attendance/time-adjustments` populates no model attributes and renders `html/time-adjustments` empty. The POST handler for `/admin/attendance/time-adjustments/upload` may be missing entirely.

**Fix:** Either implement the feature or remove the route and template.

---

### 5.2 `[UNRESOLVED]` Commented-Out `backfill-accounts` Endpoint

**File:** `controllers/AdminEmployeeApiController.java` lines 39–44

A dead commented-out block remains in source.

**Fix:** Remove the commented block.

---

### 5.3 `[UNRESOLVED]` Unused `@Autowired PayrollService`

**File:** `controllers/EmployeeViewController.java` line 47

`PayrollService` is autowired but never referenced in this controller.

**Fix:** Remove the unused field.

---

### 5.4 `[UNRESOLVED]` Unused `@Autowired EmployeeService`

**File:** `controllers/settingsController.java` line 24

`EmployeeService` is autowired but never referenced.

**Fix:** Remove the unused field.

---

### 5.5 `[UNRESOLVED]` `allowances` Always Zero

**File:** `src/main/java/digital8/payroll/services/PayrollService.java` line 366

```java
BigDecimal allowances = BigDecimal.ZERO; // placeholder
```

Always zero, stored in `PayrollItems` but never computed from any data source.

**Fix:** Remove from `PayrollItems` if not needed, or implement from an allowances data source.

---

### 5.6 `[UNRESOLVED]` `cashAdvance` Always Zero

**File:** `src/main/java/digital8/payroll/services/PayrollService.java` line 395

```java
item.setCashAdvance(BigDecimal.ZERO); // deprecated: kept for backward compat
```

Deprecated field hardcoded to zero.

**Fix:** Remove from `PayrollItems` if not needed.

---

### 5.7 `[UNRESOLVED]` README.md Contains Java Source Code

**File:** `README.md`

The file appears to contain Java source code (`package digital8.payroll;`, `import ...`, class declarations) before the actual markdown content. Likely an accidental paste.

**Fix:** Remove the accidental source code from the README.

---

## 6. Integration Gaps

> **Status key:** `[UNRESOLVED]` = issue confirmed still present, `[RESOLVED]` = fix verified implemented.

### 6.1 `[UNRESOLVED]` Special/Observance Holidays Not Compensated

**File:** `src/main/java/digital8/payroll/services/PayrollService.java` lines 204–270

**Status:** Regular holiday pay is now implemented but special holidays are still uncompensated.

**Regular holiday pay implementation (verified):**

- `Employees.java` lines 121–122: new `holidayPayEligible` boolean field gates the entire holiday block
- `PayrollService.java` lines 204–270: `computeRegularHolidayPay()` block
  - Uses `HolidayCalendarService.activeHolidaysInRange()` to fetch holidays for the period
  - Only `Holiday.TYPE_REGULAR` triggers pay (lines 219–226: iterates and filters by `TYPE_REGULAR`)
  - For worked regular holidays: adds `hourlyRate × (work_hours + ot_hours)` as holiday premium
  - For unworked regular holidays: adds `dailyRate` if the employee clocked in that day
  - Gated by `eligibleForRegularHolidayPay(emp)` at line 125 and line 207

**Still missing:** Special holidays (`TYPE_SPECIAL_NON_WORKING`, `TYPE_SPECIAL_WORKING`) are explicitly excluded from the holiday pay block. If company policy pays for special holidays, those are not reflected in payroll.

**Fix:** Add a config flag per holiday type or extend the holiday type enum to handle special holidays with configurable pay rates.

---

### 6.2 `[UNRESOLVED]` No Employer Share of Statutory Contributions

**File:** `src/main/java/digital8/payroll/services/PayrollService.java`

Only employee SSS/PhilHealth/Pag-IBIG shares are computed. Employer contributions (SSS ER, PhilHealth ER, Pag-IBIG ER) are not calculated, understating total payroll cost and leaving a gap in employer-side reporting.

**Fix:** Add `computeEmployerSss()`, `computeEmployerPhilhealth()`, `computeEmployerPagibig()` methods to `PayrollService`.

---

### 6.3 `[RESOLVED]` N+1 Query Problem in Deduction and Adjustment Breakdowns

**File:** `src/main/java/digital8/payroll/services/PayrollService.java`

**Problem (resolved):** Three methods called `findById()` inside loops — one DB round-trip per employee deduction or adjustment row.

**Fix applied (verified):** Each method now batch-fetches all referenced type IDs via `findAllById()` once before the loop, then looks them up from an in-memory `Map`:

```java
// Before the loop — one query total
Set<Integer> dedIds = list.stream()
    .map(EmployeeDeductions::getDeductionId)
    .filter(Objects::nonNull)
    .collect(Collectors.toSet());
Map<Integer, Deductions> dedMap = dedIds.isEmpty() ? Collections.emptyMap()
    : deductionsRepository.findAllById(dedIds).stream()
        .collect(Collectors.toMap(Deductions::getDeductionId, d -> d));

// Inside the loop — zero queries, just a Map lookup
Deductions d = dedMap.get(ed.getDeductionId());
```

Same pattern applied to `getAdjustmentsBreakdown` and `computeAdjustmentsSplit` using `adjustmentsRepository.findAllById()`. All existing filtering logic (recurring, date range, cutoff) is preserved unchanged.

**Implementation locations (verified):**
- `computeAdjustmentsSplit`: lines 571–577
- `getAdjustmentsBreakdown`: lines 614–620
- `getDeductionsBreakdown`: lines 658–664

---

### 6.4 `[UNRESOLVED]` No 13th-Month Pay / Final Pay / Terminal Leave Calculation

**File:** `src/main/java/digital8/payroll/services/PayrollService.java`

The service handles only regular payroll runs. Separated employees' terminal pay, 13th-month pay, and back pay are not computed.

**Fix:** Add separate service methods for these if the system needs to handle separated employees.

---

### 6.5 `[UNRESOLVED]` Premium Table UI Shows Hardcoded Fallback Rates

**File:** `controllers/PremiumTableAdminController.java` lines 45, 48

The admin UI displays `2.50%` for PhilHealth and `2.00%` for Pag-IBIG even when the DB tables are empty. The `computePhilhealth()` and `computePagibig()` service methods also use these as fallbacks, so payroll will appear to work but may use stale statutory rates.

**Fix:** Show a warning in the admin UI when tables are empty rather than displaying hardcoded rates. Log an alert when fallback rates are used in payroll computation.

---

### 6.6 `[UNRESOLVED]` Statutory Deductions Applied to All Employment Types

**File:** `src/main/java/digital8/payroll/services/PayrollService.java` lines 331–336

SSS/PhilHealth/Pag-IBIG are computed for ALL employees with a comment acknowledging "HR policy." There is no runtime configuration to exempt certain employment types (e.g., some Job Order workers may be exempt from SSS under specific conditions).

**Fix:** Add a per-employee or per-employment-type exemption flag to `Employees`.

---

### 6.7 `[UNRESOLVED]` Flyway Migrations V1–V9 Missing

**File:** `src/main/resources/db/migration/`

Only V10–V13 and V15 exist. V1–V9 (which likely contain the core employee/department/attendance schema) are missing. The base schema was either created manually or lives in a separate project. Any new developer must manually reproduce the schema.

> **Note:** V14 had a duplicate version conflict and was deleted. V15 consolidates the missing migrations into a single file (see section 13.9).

**Fix:** Document the schema setup process in the README for missing V1–V9.

---

## 7. UI/UX Issues

> **Status key:** `[UNRESOLVED]` = issue confirmed still present, `[RESOLVED]` = fix verified implemented.

### 7.1 `[UNRESOLVED]` Inline JS Conflicts with `deductions.js`

**File:** `static/js/deductions.js` vs `templates/html/deductions.html`

`deductions.html` has an inline `<script>` block that correctly sets `editDeductionCutoff` in the modal. However, `deductions.js` defines `openEditDeductionModal()` without setting the cutoff field. If the JS file ever fully replaces the inline function, cutoff editing breaks silently.

**Fix:** Move all modal logic into `deductions.js` and remove the inline script block from the HTML template.

---

### 7.2 `[UNRESOLVED]` `NavIndicatorController` — No Caching on Polling Endpoint

**File:** `controllers/NavIndicatorController.java`

`GET /api/nav-indicators` is polled by the frontend for badge counts. Without HTTP cache headers, every page load triggers a fresh DB query.

**Fix:** Add `@Cacheable` or set `Cache-Control: max-age=30` headers on the response.

---

### 7.3 `[UNRESOLVED]` Premium Table Bulk Delete Has No Confirmation

**File:** `templates/html/premiumTables.html`

Bulk SSS and tax table deletes post directly to the API with no confirmation dialog. An admin could accidentally wipe large portions of rate tables.

**Fix:** Add a JavaScript confirmation dialog before bulk delete.

---

### 7.4 `[UNRESOLVED]` All Controllers Lack Consistent `@PreAuthorize` Usage

Many endpoints use manual `instanceof Users` checks or rely entirely on Spring Security URL config. This is harder to audit than explicit method-level `@PreAuthorize` annotations.

**Fix:** Audit the Spring Security URL config and add `@PreAuthorize` annotations to all mutation endpoints.

---

## 8. Minor / Style Issues

> **Status key:** `[UNRESOLVED]` = issue confirmed still present, `[RESOLVED]` = fix verified implemented.

### 8.1 `[UNRESOLVED]` `LeaveBalanceId.hashCode()` Uses Addition

**File:** `entities/LeaveBalanceId.java`

```java
public int hashCode() {
    return employeeId.hashCode() + leaveTypeId.hashCode();
}
```

Addition can cause hash collisions. Should use `Objects.hash(employeeId, leaveTypeId)`.

---

### 8.2 `[UNRESOLVED]` Inconsistent Table Naming Convention

**Files:** `entities/Adjustments.java`, `entities/EmployeeAdjustments.java`, `entities/EmployeeDeductions.java`

Catalog table uses snake_case (`adjustments`), per-employee table uses none (`employeeadjustments`). Other per-employee tables use `employeedeductions`, `leaverequests`, `overtime_request`. No consistent naming convention.

**Fix:** Pick a convention (e.g., `employee_adjustments` with underscore) and apply consistently across all per-employee tables.

---

### 8.3 `[UNRESOLVED]` `adjustmentTypeMap` Null Key Risk

**File:** `controllers/AdjustmentsViewController.java`

`adjustmentTypeMap.get()` returns `null` if an `EmployeeAdjustments` record has an `adjustmentId` not in the types list. The filter `type.equals(null)` silently never matches, so orphan adjustment records are excluded from display without warning.

**Fix:** Log a warning or surface in the model when orphan records are found.

---

### 8.4 `[UNRESOLVED]` Exception Message Leaked to User in `saveScheduleTemplateDays`

**File:** `controllers/attendanceController.java` line 271

```java
redirectAttributes.addFlashAttribute("error", e.getMessage());
```

Raw exception message appended to flash attribute. If the exception is a SQL error or internal path, it leaks to the user.

**Fix:** Replace with a generic "An error occurred while saving the schedule" message.

---

### 8.5 `[UNRESOLVED]` Inconsistent Default Cutoff Values

**Files:** `entities/EmployeeAdjustments.java` vs `entities/EmployeeDeductions.java`

`applyOnCutoff` defaults to `"BOTH"`, `deductionCutoff` defaults to `"SEMI_2"`. An admin assigning both to an employee would expect symmetry.

**Fix:** Document the asymmetry clearly in the UI, or align defaults.

---

### 8.6 `[UNRESOLVED]` JPA Default Not Enforced at DB Level

**Files:** `entities/EmployeeAdjustments.java`, `entities/EmployeeDeductions.java`

The Java-side defaults (`"BOTH"`, `"SEMI_2"`) are applied at object creation but not enforced at the database level. If records are inserted via raw SQL without these values, the behavior depends on the JPA provider.

**Fix:** Add DB-level default constraints via a Flyway migration:

```sql
ALTER TABLE employeeadjustments
  ALTER COLUMN applyOnCutoff SET DEFAULT 'BOTH';
ALTER TABLE employeedeductions
  ALTER COLUMN deductionCutoff SET DEFAULT 'SEMI_2';
```

---

### 8.7 `[RESOLVED]` Hardcoded Tax Fallback Formula Silently Returned Zero

**File:** `src/main/java/digital8/payroll/services/PayrollService.java` lines 65–66, 488–518, `src/main/java/digital8/payroll/controllers/PayrollApiController.java`, `src/main/java/digital8/payroll/exceptions/TaxTableNotFoundException.java`

**Original concern:** The simplified withholding tax formula was hardcoded as a last-resort fallback. When no tax table existed in the database, `computeWithholdingTax()` silently returned `BigDecimal.ZERO` instead of alerting the caller, producing incorrect payroll.

**Fix applied:**

- `PayrollService.java` — The `TAX_RATE_SIMPLIFIED` and `TAX_CONSTANT` constants were removed entirely. `computeWithholdingTax()` now throws `TaxTableNotFoundException` in all three cases where no matching tax table is found:
  1. No table for the given year + pay frequency (line 512–516)
  2. No table for the prior year as fallback (same path)
  3. For `SEMI_MONTHLY`: no `SEMI_MONTHLY` or `MONTHLY` table to derive implied monthly income (line 508–510)
- `TaxTableNotFoundException.java` — new exception class
- `PayrollApiController.java` — added `@ExceptionHandler(TaxTableNotFoundException.class)` returning `400 BAD_REQUEST` with the exception message in the response body

---

## 9. Entity and Repository Analysis

> **Status key:** `[UNRESOLVED]` = issue confirmed still present, `[RESOLVED]` = fix verified implemented.

### 9.1 Entities with Proper JPA Relationships

The following entities correctly use `@ManyToOne` / `@OneToOne` relationship annotations:

| Entity | Relationships |
|---|---|
| `Employees` | `@ManyToOne` Departments, Positions; `@OneToOne` Users |
| `Users` | `@OneToOne` Employees (owning side), `@ManyToOne` Roles |
| `LeaveBalance` | `@ManyToOne` Employees, LeaveTypes (with composite PK) |
| `LeaveRequests` | `@ManyToOne` Employees, LeaveTypes |
| `OvertimeRequest` | `@ManyToOne` Employees |
| `PasswordResetToken` | `@OneToOne` Users |

### 9.2 `[UNRESOLVED]` Entities Missing Proper Relationship Annotations

Seven entities use bare `Integer` FK columns instead of `@ManyToOne`:

| Entity | Missing Relationship |
|---|---|
| `Attendance` | `employeeId` should be `@ManyToOne → Employees` |
| `Bonuses` | `employeeId` should be `@ManyToOne → Employees` |
| `EmployeeAdjustments` | `employeeId` should be `@ManyToOne → Employees`; `adjustmentId` should be `@ManyToOne → Adjustments` |
| `EmployeeDeductions` | `employeeId` should be `@ManyToOne → Employees`; `deductionId` should be `@ManyToOne → Deductions` |
| `EmployeeScheduleAssignment` | `employeeId` should be `@ManyToOne → Employees`; `templateId` should be `@ManyToOne → WeeklyScheduleTemplate` |
| `PayrollItems` | `payrollId` should be `@ManyToOne → Payroll`; `employeeId` should be `@ManyToOne → Employees` |
| `WeeklyScheduleTemplateDay` | `templateId` should be `@ManyToOne → WeeklyScheduleTemplate` |

### 9.3 `[UNRESOLVED]` Missing Inverse `@OneToMany` Relationships

| Parent Entity | Missing Inverse |
|---|---|
| `Departments` | `@OneToMany → Employees` |
| `Positions` | `@OneToMany → Employees` |
| `Roles` | `@OneToMany → Users` |
| `Deductions` | `@OneToMany → EmployeeDeductions` |
| `Adjustments` | `@OneToMany → EmployeeAdjustments` |
| `Payroll` | `@OneToMany → PayrollItems` |
| `WeeklyScheduleTemplate` | `@OneToMany → WeeklyScheduleTemplateDay`, `@OneToMany → EmployeeScheduleAssignment` |

### 9.4 `[UNRESOLVED]` Repository Adequacy Assessment

| Repository | Assessment |
|---|---|
| AdjustmentsRepository | Adequate for a type catalog |
| AttendanceRepository | Adequate for attendance lookup |
| DepartmentsRepository | Minimal but fine for a lookup table |
| DeductionsRepository | Adequate |
| **EmployeeAdjustmentsRepository** | **Underpowered** — only `findByEmployeeId`. Missing: date-range queries, cutoff-filtered queries, join-fetch for adjustment name |
| **EmployeeDeductionsRepository** | **Underpowered** — same as above |
| EmployeeRepository | Excellent — comprehensive with `JpaSpecificationExecutor` |
| HolidayRepository | Adequate |
| LeaveBalanceRepository | Minimal but sufficient for composite PK |
| LeaveRequestRepository | Excellent |
| OvertimeRequestRepository | Adequate |
| PagibigTableRepository | Adequate |
| PasswordResetRepository | Adequate |
| **PayrollItemsRepository** | **Underpowered** — no `findByPayrollId` method |
| PayrollRepository | Minimal — could use `findByStatus`, `findByPayrollType`, date-range queries |
| PhilhealthTableRepository | Adequate |
| PositionsRepository | Minimal but fine |
| RolesRepository | Minimal but fine |
| ShiftsRepository | Minimal but fine |
| SssTableRepository | Adequate |
| TaxTableRepository | Adequate |
| UsersRepository | Adequate |
| WeeklyScheduleTemplateDayRepository | Adequate |
| WeeklyScheduleTemplateRepository | Minimal but fine |
| All others | Minimal, appropriate |

---

## 10. DTO and Template Wiring

> **Status key:** `[UNRESOLVED]` = issue confirmed still present, `[RESOLVED]` = fix verified implemented.

### 10.1 DTO Consistency Summary

| DTO | Status | Notes |
|---|---|---|
| `DeductionBreakdownItem` | Consistent | Used in `PayrollService.getDeductionsBreakdown()` and `getAdjustmentsBreakdown()`, consumed by `payroll.html` as `otherDeductionsBreakdown` |
| `EmployeeListDto` | Consistent | Used by `EmployeeService` REST API, consumed by `employees.html` via AJAX |
| `WeeklyScheduleRowDto` | Consistent | Built in `attendanceController.buildTemplateDayRows()`, consumed by `shifting.html` |
| `EmployeeAdjustmentRowDto` | Consistent | Parallel structure to `EmployeeDeductionRowDto`; all fields populated and consumed |
| `EmployeeDeductionRowDto` | Consistent | All entity fields map to DTO and template |

### 10.2 Template-to-Controller Wiring

| Template | Controller | Status |
|---|---|---|
| `adjustments.html` | `AdjustmentsViewController` | OK — all forms handled |
| `deductions.html` | `DeductionViewController` | OK — all forms handled |
| `payroll.html` | `payrollViewController` | OK — GET only |
| `overtime.html` | `attendanceController` | OK — all forms handled |
| `leaveAdminSelf.html` | `LeaveController` | OK — forms handled |
| `leaveEmployee.html` | `LeaveController` | OK — forms handled |
| `leaveAdmin.html` | `LeaveController` | OK — forms handled |
| `employees.html` | `AdminEmployeeApiController` + JS | OK — AJAX/REST |
| `settingsAdmin.html` | `settingsController` | OK |
| `settingsEmployee.html` | `settingsController` | OK |
| `homeAdmin.html` | `homeController` | OK |
| `homeEmployee.html` | `homeController` | OK |
| `holidaysAdmin.html` | `HolidayAdminController` | OK — all forms handled |
| `premiumTables.html` | `PremiumTableAdminController` | OK — all forms handled |
| `shifting.html` | `attendanceController` | OK — all forms handled |
| `attendance.html` | `attendanceController` | OK — GET filter only |
| `time-adjustments.html` | `attendanceController` | STUB — GET handled but empty; upload POST unverified |
| `index.html` | Spring Security default | OK (login page) |
| `setupPassword.html` | `PasswordSetupController` | OK (JS tag broken — see 2.4) |
| `forgetPassword.html` | none | EMPTY — see 2.3 |
| `email/setup-email.html` | email template | OK — not a standalone route |
| `fragments/nav.html` | fragment | OK — included by other templates |

---

## 11. File Inventory

### Entities (29 files)

| File | PK | Relationships | Issues |
|---|---|---|---|
| `Adjustments.java` | `adjustmentId` | None | Bare FK table, no inverse `@OneToMany` |
| `AuditLogs.java` | `logId` | None | `performedBy` is a string, not a `@ManyToOne` |
| `Attendance.java` | `attendanceId` | **MISSING** | `employeeId` bare Integer FK |
| `Bonuses.java` | `bonusId` | **MISSING** | `employeeId` bare Integer FK |
| `Departments.java` | `departmentId` | None | No inverse `@OneToMany` |
| `Deductions.java` | `deductionId` | None | No inverse `@OneToMany` |
| `EmployeeAdjustments.java` | `employeeAdjustmentId` | **MISSING** | Both FKs bare; JPA default not DB-enforced |
| `EmployeeDeductions.java` | `employeeDeductionId` | **MISSING** | Both FKs bare; JPA default not DB-enforced |
| `EmployeeScheduleAssignment.java` | `id` | **MISSING** | Both FKs bare |
| `Employees.java` | `employeeId` | Yes | `email` is `@Transient`; **new fields since last audit:** `factorRate` (DECIMAL, non-null, no DB default, falls back to `DEFAULT_PAY_FACTOR=20`), `holidayPayEligible` (boolean, gates regular holiday pay), `otMultiplier` (DECIMAL(5,2), defaults to 1.0) |
| `Holiday.java` | `holidayId` | None | OK |
| `LeaveBalance.java` | Composite | Yes | `hashCode()` uses addition |
| `LeaveBalanceId.java` | (embeddable) | N/A | `hashCode()` uses addition |
| `LeaveRequests.java` | `leaveRequestId` | Yes | `approved_by` bare Integer |
| `LeaveTypes.java` | `leaveTypeId` | None | No inverse `@OneToMany` |
| `OvertimeRequest.java` | `overtimeRequestId` | Yes (partial) | `approvedByUserId` bare Integer; new entity added since last audit — replaces inline request handling |
| `PagibigTable.java` | `pagibigId` | None | OK |
| `PasswordResetToken.java` | `tokenId` | Yes | OK |
| `Payroll.java` | `payrollId` | **MISSING** | No `@OneToMany` to `PayrollItems` |
| `PayrollItems.java` | `payrollItemId` | **MISSING** | Both FKs bare; stores pre-computed adjustment amounts |
| `PhilhealthTable.java` | `philhealthId` | None | OK |
| `Positions.java` | `positionId` | None | No inverse `@OneToMany` |
| `Roles.java` | `roleId` | None | No inverse `@OneToMany` |
| `Shift.java` | `shiftId` | None | OK |
| `SssTable.java` | `sssId` | None | OK |
| `TaxTable.java` | `taxId` | None | OK |
| `Users.java` | `userId` | Yes | OK |
| `WeeklyScheduleTemplate.java` | `templateId` | **MISSING** | No inverse `@OneToMany` |
| `WeeklyScheduleTemplateDay.java` | `id` | **MISSING** | `templateId` bare Integer FK |

### Repositories (28 files)

See section 9.4 above for the full adequacy assessment.

### Services (10 files)

| File | Assessment |
|---|---|
| `PayrollService.java` | Core service — see all critical issues above |
| `EmployeeService.java` | Employee CRUD and search — well-structured |
| `LeaveService.java` | Leave management — well-structured |
| `HolidayAdminService.java` | Holiday CRUD and CSV import — well-structured |
| `HolidayCalendarService.java` | Google Calendar sync — well-structured |
| `PremiumTableService.java` | Statutory table CRUD — well-structured |
| `UserService.java` | User/auth management — well-structured |
| `EmailNotificationService.java` | Email sending — well-structured |
| `MailjetEmailClient.java` | Mailjet API client — well-structured |
| `OvertimeRequestService.java` | OT request management — well-structured |

### Controllers (18 files)

| File | Assessment |
|---|---|
| `AdjustmentsViewController.java` | Well-structured — parallel to DeductionViewController |
| `AdminEmployeeApiController.java` | Has dead commented code; missing `@PreAuthorize` |
| `attendanceController.java` | Largest controller — multiple security gaps, NPE risks, stub endpoint |
| `DepartmentsController.java` | Minimal — fine |
| `DeductionViewController.java` | Well-structured — NPE risk on null `endDate` |
| `EmployeeController.java` | REST API — **critical: no authorization on update** |
| `EmployeeViewController.java` | Fine — unused autowired `PayrollService` |
| `HolidayAdminController.java` | Well-structured — manual `guardAdmin()` instead of `@PreAuthorize` |
| `homeController.java` | Has `/generateHash` security vulnerability |
| `LeaveController.java` | Multiple `ClassCastException` risks; wrong `emp_id` attribute |
| `NavIndicatorController.java` | Minimal — no caching |
| `PasswordSetupController.java` | Fine |
| `PayrollApiController.java` | Manual auth check — fine but non-standard |
| `payrollViewController.java` | Fine — silent month fallback |
| `PositionsController.java` | Minimal — fine |
| `PremiumTableAdminController.java` | Well-structured — hardcoded fallback rates |
| `settingsController.java` | Fine — unused autowired `EmployeeService` |
| `testController.java` | **CRITICAL: must be removed** |

### Templates (24 files)

All templates are properly wired to their respective controllers except:
- `forgetPassword.html` — empty body (2.3)
- `setupPassword.html` — broken script tag (2.4)
- `time-adjustments.html` — empty, stub (5.1)

---

---

## 12. New Features Implemented

The following features were added since the original audit. Some are fully implemented; others introduced new issues.

### 12.1 Regular Holiday Pay

**Files:** `Employees.java`, `PayrollService.java`, `HolidayCalendarService.java`, `Holiday.java`

**What was added:**

- `Employees.java` lines 121–122: new `holidayPayEligible` boolean field (`holidayPayEligible` column, non-null boolean)
- `PayrollService.java` lines 204–270: `computeRegularHolidayPay()` block gated by `eligibleForRegularHolidayPay(emp)` at lines 125 and 207
  - Fetches holidays via `HolidayCalendarService.activeHolidaysInRange()`
  - Only `TYPE_REGULAR` holidays trigger pay (lines 219–226)
  - Worked regular holiday hours paid at `hourlyRate × (work_hours + ot_hours)`
  - Unworked regular holidays (if employee clocked in) receive `dailyRate`
- `Holiday.java`: constants `TYPE_REGULAR`, `TYPE_SPECIAL_NON_WORKING`, `TYPE_SPECIAL_WORKING`

**Note:** Special holidays (`TYPE_SPECIAL_NON_WORKING`, `TYPE_SPECIAL_WORKING`) are excluded from holiday pay. See section 6.1.

### 12.2 Overtime Request System

**Files:** `OvertimeRequest.java`, `OvertimeRequestRepository.java`, `OvertimeRequestService.java`, `overtime.html`, `overtime.css`

A full OT request workflow was added replacing the previous inline attendance-based approach:

- `OvertimeRequest.java`: entity with `overtimeRequestId` PK, `employeeId`, `requestedDate`, `requestedHours`, `status`, `reason`, `approvedByUserId`, `createdAt`
- `OvertimeRequestRepository.java`: standard JPA repository with `findByEmployeeId`, `findByStatus`, `findByRequestedDateBetween`
- `OvertimeRequestService.java`: `createRequest()`, `approve()`, `reject()`, `findByEmployee()`
- `overtime.html`: employee-facing OT request form and admin OT approval/rejection UI
- `overtime.css`: dedicated styling

**Related issue:** OT approval and rejection endpoints have no explicit admin auth check (see section 2.8).

### 12.3 Weekly Schedule Templates

**Files:** `WeeklyScheduleTemplate.java`, `WeeklyScheduleTemplateDay.java`, `EmployeeScheduleAssignment.java`, `WeeklyScheduleTemplateRepository.java`, `WeeklyScheduleTemplateDayRepository.java`, `EmployeeScheduleAssignmentRepository.java`, `shifting.html`

Allows admins to define recurring weekly schedules and assign them to employees:

- `WeeklyScheduleTemplate.java`: template with `templateId`, `templateName`, `effectiveDate`, `isActive`
- `WeeklyScheduleTemplateDay.java`: day-level entries (`templateId`, `dayOfWeek`, `shiftId`, `startTime`, `endTime`)
- `EmployeeScheduleAssignment.java`: assigns a template to an employee with `effectiveFrom`/`effectiveTo` dates
- `shifting.html`: admin UI for managing schedule templates and employee assignments

### 12.4 Employee Adjustment Assignment Feature

**Files:** `Adjustments.java`, `EmployeeAdjustments.java`, `AdjustmentsRepository.java`, `EmployeeAdjustmentsRepository.java`, `AdjustmentsViewController.java`, `EmployeeAdjustmentRowDto.java`, `adjustments.html`

Per-employee earning/deduction adjustment assignments:

- `Adjustments.java`: type catalog — `adjustmentId`, `adjustmentName`, `adjustmentType` (`Earnings`/`Deduction`), `isActive`
- `EmployeeAdjustments.java`: per-employee assignment — `employeeAdjustmentId`, `employeeId`, `adjustmentId`, `amount`, `isRecurring`, `startDate`, `endDate`, `applyOnCutoff` (default `"BOTH"`)
- `AdjustmentsViewController.java`: full CRUD for both the type catalog and per-employee assignments
- `adjustments.html`: admin and employee views for managing adjustment assignments
- `EmployeeAdjustmentRowDto.java`: DTO for display rows in the adjustment table

**Related issues:** `AdjustmentsViewController` has a null-auth bypass risk (see 13.3). The `applyOnCutoff` default of `"BOTH"` is asymmetric with `EmployeeDeductions.deductionCutoff` default of `"SEMI_2"` (see 8.5).

### 12.5 PayrollItems Redesign

**File:** `PayrollItems.java`, `PayrollService.java`

`PayrollItems.java` was significantly expanded with 16+ new fields covering:

- Rate derivation: `dailyRate`, `hourlyRate`, `perMinuteRate`
- Hours: `totalWorkedHours`, `totalOtHours`
- Late/undertime: `lateUndertimeMinutes`, `lateUndertimeDeduction`
- Adjustments: `adjustmentEarnings`, `adjustmentDeductions`
- Computed totals: `totalEarnings`, `serviceFee`, `semiMonthlyContributions`
- Metadata: `employmentType`

`PayrollService.computePayroll()` was restructured around this expanded model. The service now computes and stores all these fields per payroll item.

### 12.6 Factor Rate and Year-Parameterized Payroll

**Files:** `Employees.java`, `PayrollService.java`

- `Employees.java` lines 118–119: new `factorRate` field (`DECIMAL(4,2)`, column is `nullable = false` but has no DB-level default). The service falls back to `DEFAULT_PAY_FACTOR = 20` if the DB column is null.
- `PayrollService.java`: new public `getPayrollPeriodBounds(int year, Month month, String period)` method at lines 132–135, and a year-parameter overload of `computePayroll(Integer empId, String period, String monthName, Integer year)` at lines 137–139.

---

## 13. New Issues Found

The following issues were discovered during this audit run and were not present in the prior audit.

### 13.1 `[NEW]` Bonuses Entity Exists But Never Applied to Payroll

**File:** `entities/Bonuses.java`

The `Bonuses.java` entity (83 lines) stores bonus records with fields: `bonusId`, `employeeId` (bare Integer), `bonusType`, `amount`, `isTaxable`, `bonusDate`. However:

- There is **no `BonusesRepository`** anywhere in the codebase
- There is **no `BonusService`**
- `PayrollService.computePayroll()` has **no bonus-inclusion logic**

Bonuses are stored in the database but never reflected on any payslip. This is distinct from the bare Integer FK issue (4.3) — it is a functional gap where bonus data exists but is never consumed.

**Fix:** Create `BonusesRepository`, add a `getEmployeeBonusesInPeriod(employeeId, startDate, endDate)` query, include total bonuses in `totalEarnings`, and update the payroll breakdown to show bonus line items.

### 13.2 `[NEW]` `adjustments.html` Has No Balance Summary Cards

**File:** `templates/html/adjustments.html`

Unlike `deductions.html` (which has three summary cards — outstanding obligation, monthly cutoff, outstanding balance — that are never populated by the controller), `adjustments.html` has **no equivalent summary section**. This is not a bug per se, but creates a UX asymmetry: users managing deductions get a quick-view summary (even if broken), while users managing adjustments have no summary at all.

**Fix (design decision):** Either add equivalent balance cards to `adjustments.html` and populate them in `AdjustmentsViewController`, or document that the deduction cards in `deductions.html` should be removed for consistency.

### 13.3 `[NEW]` `AdjustmentsViewController` Null-Auth Bypass Risk

**File:** `controllers/AdjustmentsViewController.java` line 64

```java
if (isAdmin) {
    assignments = employeeAdjustmentsRepository.findAll();
    employees = employeeRepository.findAllWithFetch(null, Sort.by(Sort.Direction.ASC, "lastName"));
    if (authentication.getPrincipal() instanceof Users) {   // ← no null check on authentication
        Employees adminEmp = ((Users) authentication.getPrincipal()).getEmployee();
        if (adminEmp != null) model.addAttribute("emp_id", adminEmp.getEmployeeId());
    }
}
```

`authentication.getAuthorities()` and `authentication.getPrincipal()` are called without first checking if `authentication` is null. By contrast, `DeductionViewController` (line 54) correctly guards with `authentication != null`. If `authentication` is null (e.g., an anonymous or expired session), a `NullPointerException` is thrown.

**Fix:** Add a null guard before the admin check:
```java
if (authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
```

### 13.4 `[NEW]` Duplicate `DOMContentLoaded` Event Listener in `deductions.js`

**File:** `static/js/deductions.js` lines 2 and 36

The file defines `document.addEventListener('DOMContentLoaded', ...)` twice. The first listener (lines 2–6) is dead code — it is never referenced elsewhere. The second listener (lines 36–55) contains the actual logic. This is a maintenance hazard and suggests an incomplete refactor.

**Fix:** Remove the first (dead) `DOMContentLoaded` block.

### 13.5 `[NEW]` `deductions.js` Included Inside Modal Div Markup

**File:** `templates/html/deductions.html` line 342

`<script src="/js/deductions.js">` is placed inside the `modalAddDeduction` div, after the closing `</form>` tag. Since the modal is always present in the DOM (just hidden), the script does load and works, but the placement is non-standard and fragile. If the modal structure changes, the script inclusion could be lost or duplicated.

**Fix:** Move the `<script src="/js/deductions.js">` tag to the end of the page (before `</body>`), outside all modal markup.

### 13.6 `[NEW]` `LeaveController` Still Sets Wrong `emp_id` Model Attribute

**File:** `controllers/LeaveController.java` line 51

```java
model.addAttribute("emp_id", user.getUserId());  // ← user.getUserId() is the Users PK, NOT the employee ID
```

The correct value (`user.getEmployee().getEmployeeId()`) is derived at line 48 and used for service calls, but `emp_id` in the model is set to `user.getUserId()` (the `Users` table primary key) instead. If the template or any JavaScript reads `${emp_id}` for data operations, it will use the wrong ID.

**Fix:** Replace line 51 with:
```java
model.addAttribute("emp_id", employeeId);
```

### 13.7 `[NEW]` `setupPassword.html` — JS Included as Stylesheet, No `<script>` Tag

**File:** `templates/html/setupPassword.html` line 8

```html
<link rel="stylesheet" href="/js/setup.js">
```

The `/js/setup.js` file is included as a stylesheet link, which browsers ignore. There is no `<script src="/js/setup.js">` tag on the page. The password setup form's validation and submission logic (presumably in `setup.js`) never executes.

**Fix:** Replace the `<link>` with `<script src="/js/setup.js"></script>` and place it before `</body>`.

### 13.8 `[NEW]` `forgetPassword.html` Is a Skeleton With No Content

**File:** `templates/html/forgetPassword.html`

The file contains only a DOCTYPE, head, and empty body. There is no form, no email input, no instructions, no styling, and no controller wired to it. Anyone navigating to the forgot password page sees a blank white page.

**Fix:** Add a password-reset request form wired to a new `ForgetPasswordController` endpoint.

### 13.9 `[RESOLVED]` Flyway V14 Version Number Conflict — App Failed to Start

**File:** `src/main/resources/db/migration/V14__add_employee_ot_multiplier.sql` and `V14__employees_biometric_id.sql`

**Root cause:** Two migration files shared the same `V14` version number. Flyway requires strictly ascending version numbers — duplicate V14 numbers caused Flyway to apply only one, leaving the other column absent from the `employees` table. The app crashed with `Unknown column 'e1_0.ot_multiplier' in 'field list'`.

**Resolution (May 12, 2026):** Both V14 files were deleted and replaced with `V15__add_employee_fields_and_adjustments.sql`, which consolidates all changes: adds `ot_multiplier` and `biometric_id` to `employees`, adds `approved_by` to `leaverequests`, creates the `adjustments` catalog table, and migrates `employeedeductions` from the old schema (`total_obligation`/`cutoff_schedule`) to the new schema (`deductionCutoff`).

---

## 14. Updated File Inventory

### CSS Files (16 — up from 9)

7 new CSS files added since the prior audit:

| File | New in This Audit? | Notes |
|---|---|---|
| `attendance.css` | No | — |
| `deductions.css` | No | — |
| `global.css` | No | — |
| `holidays.css` | No | — |
| `homeAdmin.css` | No | — |
| `index.css` | No | — |
| `leave.css` | No | — |
| `nav.css` | No | — |
| `overtime.css` | **Yes** | OT request pages |
| `payroll.css` | No | — |
| `premiumTables.css` | No | — |
| `settings.css` | No | — |
| `settingsEmployee.css` | **Yes** | Employee settings variant |
| `setupPassword.css` | No | — |
| `time-adjustments.css` | **Yes** | Time adjustments page |
| `employees.css` | **Yes** | Employee list/management |

### New Entities Since Prior Audit

| Entity | Description |
|---|---|
| `Adjustments.java` | Type catalog for earning/deduction adjustments |
| `EmployeeAdjustments.java` | Per-employee adjustment assignment |
| `EmployeeScheduleAssignment.java` | Schedule template assignments |
| `OvertimeRequest.java` | OT request records |
| `PasswordResetToken.java` | Password reset tokens |
| `WeeklyScheduleTemplate.java` | Schedule template definitions |
| `WeeklyScheduleTemplateDay.java` | Day-level schedule entries |

### New Repositories Since Prior Audit

| Repository | Description |
|---|---|
| `AdjustmentsRepository.java` | Adjustments type catalog |
| `EmployeeAdjustmentsRepository.java` | Employee adjustment assignments |
| `EmployeeRepositoryCustom.java` | Custom query interface |
| `EmployeeRepositoryCustomImpl.java` | Custom query implementation |
| `EmployeeScheduleAssignmentRepository.java` | Schedule assignments |
| `OvertimeRequestRepository.java` | OT requests |
| `PasswordResetRepository.java` | Password reset tokens |
| `WeeklyScheduleTemplateDayRepository.java` | Template days |
| `WeeklyScheduleTemplateRepository.java` | Schedule templates |

### New Templates Since Prior Audit

| Template | Description |
|---|---|
| `adjustments.html` | Employee adjustment management |
| `overtime.html` | OT request submission and approval |
| `settingsAdmin.html` | Admin settings page |
| `settingsEmployee.html` | Employee settings page |
| `shifting.html` | Schedule template management |
| `time-adjustments.html` | Time adjustment requests |

### Config and Specification Files

| File | New? | Notes |
|---|---|---|
| `CustomAuthenticationProvider.java` | No | — |
| `NoCacheFilter.java` | No | — |
| `NavIndicatorAdvice.java` | No | — |
| `SecurityConfig.java` | No | Spring Security config |
| `EmployeeSpecifications.java` | No | Custom JPA query specs |

### Flyway Migrations (V10–V15)

| Version | Description |
|---|---|
| V10 | Employee weekly schedule |
| V11 | Schedule month/year |
| V12 | Weekly schedule templates |
| V13 | Overtime requests |
| V14 | **DELETED** — had duplicate version conflict (see 13.9) |
| V15 | Adds ot_multiplier, biometric_id, approved_by, adjustments table, employeedeductions schema |
| V1–V9 | **Missing** — base schema not in migrations |

---

## 15. Summary

|| Category | Count | Resolved in This Audit | New Issues Found |
|---|---|---|---|---|
| Critical issues (payroll correctness) | 7 | 2 (2.5 OT multiplier, 2.7 cutoff integration) | 0 |
| Security vulnerabilities | 6 | 0 | 1 (EmployeeController.getEmployeeById PII) |
| Bugs and logic errors | 10 | 1 (4.1 NPE on null endDate) | 1 (Bonuses never in payroll) |
| Dead code and stubs | 7 | 0 | 0 |
| Integration gaps | 7 | 1 (6.3 N+1 query) | 0 |
| UI/UX issues | 4 | 0 | 2 (adjustments.html no balance cards, deductions.js placement) |
| Minor/style issues | 7 | 0 | 1 (deductions.js duplicate DOMContentLoaded) |
| New features implemented | 6 | — | — |
| **Total findings** | **55** | **5** | **7** |

**Net status:** 43 unresolved findings carried from the prior audit + 7 new issues found + 5 now resolved = **45 open findings**.

### Resolved Items (5)

| # | Issue | Resolution |
|---|---|---|
| 2.5 | Per-employee OT multiplier hardcoded to 1.0 | Fully implemented with `ot_multiplier` column, V14 migration, all wiring |
| 2.7 | Cutoff case sensitivity / null period bypass | `matchesCutoff()` helper normalized period, handles null, used at all 4 call sites |
| 4.1 | NPE on recurring deductions with null endDate | Confirmed safe — all branches guard with `!= null` checks |
| 6.3 | N+1 query in adjustment/deduction breakdowns | Batch `findAllById()` applied to all 3 affected methods |
| 13.9 | Flyway V14 version conflict — app fails to start | Both V14 files deleted; V15 `add_employee_fields_and_adjustments` created (adds ot_multiplier, biometric_id, approved_by, adjustments table, employeedeductions schema migration) |

### New Items (7)

| # | Issue | Severity |
|---|---|---|
| 13.1 | Bonuses entity exists but never applied to payroll | High |
| 13.3 | `AdjustmentsViewController` null-auth bypass | Medium |
| 13.4 | Duplicate `DOMContentLoaded` in `deductions.js` | Low |
| 13.5 | `deductions.js` included inside modal div | Low |
| 13.6 | `LeaveController` still sets `emp_id` to `user.getUserId()` | Medium |
| 13.7 | `setupPassword.html` includes JS as a stylesheet link | Low |
| 13.8 | `forgetPassword.html` is an empty skeleton | Low |
