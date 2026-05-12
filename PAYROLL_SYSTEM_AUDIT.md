# Payroll System Full Audit Report

**Project:** Spring Boot 4.0.2 / Java 21 / Thymeleaf / MySQL / Flyway
**Date:** May 12, 2026
**Scope:** All 29 entities, 28 repositories, 10 services, 18 controllers, 24 HTML templates, 9 JS files, 9 CSS files

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

### 4.2 Biweekly Payroll Silently Runs Over the Full Month

**File:** `src/main/java/digital8/payroll/services/PayrollService.java` lines 118–119

```java
// TODO: define start/end; placeholder = whole month
return new PayPeriod(ym.atDay(1), ym.atEndOfMonth(), false);
```

Any biweekly payroll run computes over the entire month, not the intended two-week period. The `isBiweekly` flag in the returned `PayPeriod` is `false`, making the period identical to monthly.

**Fix:** Implement actual biweekly period splitting by adding a `biweeklyPeriod` parameter (1 or 2) and computing start/end dates accordingly.

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

### 4.9 Bulk Test Data Insert Has No Cleanup

**File:** `test-data/100-employees.sql`

Bulk test data insert without `DELETE FROM employees WHERE ...` or transaction wrapping. Running this on a staging or production DB will duplicate or conflict with existing employee numbers.

**Fix:** Wrap in a transaction with `DELETE FROM employees WHERE employee_number LIKE 'TEST%';` at the start, or add a guard that skips if data already exists.

---

## 5. Dead Code and Stubs

### 5.1 `attendanceController` — `/time-adjustments` Is a Stub

**File:** `controllers/attendanceController.java` lines 492–501

The GET handler for `/admin/attendance/time-adjustments` populates no model attributes and renders `html/time-adjustments` empty. The POST handler for `/admin/attendance/time-adjustments/upload` may be missing entirely.

**Fix:** Either implement the feature or remove the route and template.

---

### 5.2 Commented-Out `backfill-accounts` Endpoint

**File:** `controllers/AdminEmployeeApiController.java` lines 39–44

A dead commented-out block remains in source.

**Fix:** Remove the commented block.

---

### 5.3 Unused `@Autowired PayrollService`

**File:** `controllers/EmployeeViewController.java` line 47

`PayrollService` is autowired but never referenced in this controller.

**Fix:** Remove the unused field.

---

### 5.4 Unused `@Autowired EmployeeService`

**File:** `controllers/settingsController.java` line 24

`EmployeeService` is autowired but never referenced.

**Fix:** Remove the unused field.

---

### 5.5 `allowances` Always Zero

**File:** `src/main/java/digital8/payroll/services/PayrollService.java` line 366

```java
BigDecimal allowances = BigDecimal.ZERO; // placeholder
```

Always zero, stored in `PayrollItems` but never computed from any data source.

**Fix:** Remove from `PayrollItems` if not needed, or implement from an allowances data source.

---

### 5.6 `cashAdvance` Always Zero

**File:** `src/main/java/digital8/payroll/services/PayrollService.java` line 395

```java
item.setCashAdvance(BigDecimal.ZERO); // deprecated: kept for backward compat
```

Deprecated field hardcoded to zero.

**Fix:** Remove from `PayrollItems` if not needed.

---

### 5.7 README.md Contains Java Source Code

**File:** `README.md`

The file appears to contain Java source code (`package digital8.payroll;`, `import ...`, class declarations) before the actual markdown content. Likely an accidental paste.

**Fix:** Remove the accidental source code from the README.

---

## 6. Integration Gaps

### 6.1 Special/Observance Holidays Not Compensated

**File:** `src/main/java/digital8/payroll/services/PayrollService.java` lines 206–272

Only `holidayType == "REGULAR"` triggers holiday pay. Special holidays (common in the Philippines) are ignored, even if company policy pays for them.

**Fix:** Add a config flag per holiday type or extend the holiday type enum to handle special holidays with configurable pay rates.

---

### 6.2 No Employer Share of Statutory Contributions

**File:** `src/main/java/digital8/payroll/services/PayrollService.java`

Only employee SSS/PhilHealth/Pag-IBIG shares are computed. Employer contributions (SSS ER, PhilHealth ER, Pag-IBIG ER) are not calculated, understating total payroll cost and leaving a gap in employer-side reporting.

**Fix:** Add `computeEmployerSss()`, `computeEmployerPhilhealth()`, `computeEmployerPagibig()` methods to `PayrollService`.

---

### 6.3 N+1 Query Problem in Deduction and Adjustment Breakdowns

**File:** `src/main/java/digital8/payroll/services/PayrollService.java`

**Status:** RESOLVED

**Problem:** Three methods called `findById()` inside loops — one DB round-trip per employee deduction or adjustment row:

- `getDeductionsBreakdown` — line ~657: `deductionsRepository.findById(ed.getDeductionId())` inside the loop
- `getAdjustmentsBreakdown` — line ~618: `adjustmentsRepository.findById(ea.getAdjustmentId())` inside the loop
- `computeAdjustmentsSplit` — line ~581: `adjustmentsRepository.findById(ea.getAdjustmentId())` inside the loop

For an employee with 20 deductions and 10 adjustments, this generated 31 separate queries instead of 3.

**Fix applied:** Each method now batch-fetches all referenced type IDs via `findAllById()` once before the loop, then looks them up from an in-memory `Map`:

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

---

### 6.4 No 13th-Month Pay / Final Pay / Terminal Leave Calculation

**File:** `src/main/java/digital8/payroll/services/PayrollService.java`

The service handles only regular payroll runs. Separated employees' terminal pay, 13th-month pay, and back pay are not computed.

**Fix:** Add separate service methods for these if the system needs to handle separated employees.

---

### 6.5 Premium Table UI Shows Hardcoded Fallback Rates

**File:** `controllers/PremiumTableAdminController.java` lines 45, 48

The admin UI displays `2.50%` for PhilHealth and `2.00%` for Pag-IBIG even when the DB tables are empty. The `computePhilhealth()` and `computePagibig()` service methods also use these as fallbacks, so payroll will appear to work but may use stale statutory rates.

**Fix:** Show a warning in the admin UI when tables are empty rather than displaying hardcoded rates. Log an alert when fallback rates are used in payroll computation.

---

### 6.6 Statutory Deductions Applied to All Employment Types

**File:** `src/main/java/digital8/payroll/services/PayrollService.java` lines 331–336

SSS/PhilHealth/Pag-IBIG are computed for ALL employees with a comment acknowledging "HR policy." There is no runtime configuration to exempt certain employment types (e.g., some Job Order workers may be exempt from SSS under specific conditions).

**Fix:** Add a per-employee or per-employment-type exemption flag to `Employees`.

---

### 6.7 Flyway Migrations V1–V9 Missing

**File:** `src/main/resources/db/migration/`

Only V10–V13 exist. V1–V9 (which likely contain the core employee/department/attendance schema) are missing. The base schema was either created manually or lives in a separate project. Any new developer must manually reproduce the schema.

**Fix:** Either commit the missing V1–V9 migrations or document the schema setup process in the README.

---

## 7. UI/UX Issues

### 7.1 Inline JS Conflicts with `deductions.js`

**File:** `static/js/deductions.js` vs `templates/html/deductions.html`

`deductions.html` has an inline `<script>` block that correctly sets `editDeductionCutoff` in the modal. However, `deductions.js` defines `openEditDeductionModal()` without setting the cutoff field. If the JS file ever fully replaces the inline function, cutoff editing breaks silently.

**Fix:** Move all modal logic into `deductions.js` and remove the inline script block from the HTML template.

---

### 7.2 `NavIndicatorController` — No Caching on Polling Endpoint

**File:** `controllers/NavIndicatorController.java`

`GET /api/nav-indicators` is polled by the frontend for badge counts. Without HTTP cache headers, every page load triggers a fresh DB query.

**Fix:** Add `@Cacheable` or set `Cache-Control: max-age=30` headers on the response.

---

### 7.3 Premium Table Bulk Delete Has No Confirmation

**File:** `templates/html/premiumTables.html`

Bulk SSS and tax table deletes post directly to the API with no confirmation dialog. An admin could accidentally wipe large portions of rate tables.

**Fix:** Add a JavaScript confirmation dialog before bulk delete.

---

### 7.4 All Controllers Lack Consistent `@PreAuthorize` Usage

Many endpoints use manual `instanceof Users` checks or rely entirely on Spring Security URL config. This is harder to audit than explicit method-level `@PreAuthorize` annotations.

**Fix:** Audit the Spring Security URL config and add `@PreAuthorize` annotations to all mutation endpoints.

---

## 8. Minor / Style Issues

### 8.1 `LeaveBalanceId.hashCode()` Uses Addition

**File:** `entities/LeaveBalanceId.java`

```java
public int hashCode() {
    return employeeId.hashCode() + leaveTypeId.hashCode();
}
```

Addition can cause hash collisions. Should use `Objects.hash(employeeId, leaveTypeId)`.

---

### 8.2 Inconsistent Table Naming Convention

**Files:** `entities/Adjustments.java`, `entities/EmployeeAdjustments.java`, `entities/EmployeeDeductions.java`

Catalog table uses snake_case (`adjustments`), per-employee table uses none (`employeeadjustments`). Other per-employee tables use `employeedeductions`, `leaverequests`, `overtime_request`. No consistent naming convention.

**Fix:** Pick a convention (e.g., `employee_adjustments` with underscore) and apply consistently across all per-employee tables.

---

### 8.3 `adjustmentTypeMap` Null Key Risk

**File:** `controllers/AdjustmentsViewController.java`

`adjustmentTypeMap.get()` returns `null` if an `EmployeeAdjustments` record has an `adjustmentId` not in the types list. The filter `type.equals(null)` silently never matches, so orphan adjustment records are excluded from display without warning.

**Fix:** Log a warning or surface in the model when orphan records are found.

---

### 8.4 Exception Message Leaked to User in `saveScheduleTemplateDays`

**File:** `controllers/attendanceController.java` line 271

```java
redirectAttributes.addFlashAttribute("error", e.getMessage());
```

Raw exception message appended to flash attribute. If the exception is a SQL error or internal path, it leaks to the user.

**Fix:** Replace with a generic "An error occurred while saving the schedule" message.

---

### 8.5 Inconsistent Default Cutoff Values

**Files:** `entities/EmployeeAdjustments.java` vs `entities/EmployeeDeductions.java`

`applyOnCutoff` defaults to `"BOTH"`, `deductionCutoff` defaults to `"SEMI_2"`. An admin assigning both to an employee would expect symmetry.

**Fix:** Document the asymmetry clearly in the UI, or align defaults.

---

### 8.6 JPA Default Not Enforced at DB Level

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

### 8.7 Hardcoded Tax Fallback Formula

**File:** `src/main/java/digital8/payroll/services/PayrollService.java` lines 65–66

```java
private static final BigDecimal TAX_RATE_SIMPLIFIED = new BigDecimal("0.10");
private static final BigDecimal TAX_CONSTANT = new BigDecimal("2395.90");
```

The simplified withholding tax formula (`gross × 10% − 2395.90`) is hardcoded as a last-resort fallback. The constant looks like a specific BIR bracket's "withholding constant" but hardcoding it means it won't track law changes automatically.

**Fix:** Document this as a known fixed fallback. Ideally, the constant should come from a configuration table.

---

## 9. Entity and Repository Analysis

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

### 9.2 Entities Missing Proper Relationship Annotations

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

### 9.3 Missing Inverse `@OneToMany` Relationships

| Parent Entity | Missing Inverse |
|---|---|
| `Departments` | `@OneToMany → Employees` |
| `Positions` | `@OneToMany → Employees` |
| `Roles` | `@OneToMany → Users` |
| `Deductions` | `@OneToMany → EmployeeDeductions` |
| `Adjustments` | `@OneToMany → EmployeeAdjustments` |
| `Payroll` | `@OneToMany → PayrollItems` |
| `WeeklyScheduleTemplate` | `@OneToMany → WeeklyScheduleTemplateDay`, `@OneToMany → EmployeeScheduleAssignment` |

### 9.4 Repository Adequacy Assessment

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
| `Employees.java` | `employeeId` | Yes | `email` is `@Transient`; inconsistent with canonical design |
| `Holiday.java` | `holidayId` | None | OK |
| `LeaveBalance.java` | Composite | Yes | `hashCode()` uses addition |
| `LeaveBalanceId.java` | (embeddable) | N/A | `hashCode()` uses addition |
| `LeaveRequests.java` | `leaveRequestId` | Yes | `approved_by` bare Integer |
| `LeaveTypes.java` | `leaveTypeId` | None | No inverse `@OneToMany` |
| `OvertimeRequest.java` | `overtimeRequestId` | Yes (partial) | `approvedByUserId` bare Integer |
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

## Summary

| Category | Count | Highest Severity |
|---|---|---|
| Critical issues (payroll correctness) | 7 | OT multiplier = 1.0, balance cards zero, leave type disabled |
| Security vulnerabilities | 5 | `testController` PII exposure |
| Bugs and logic errors | 9 | NPE on null endDate, wrong `emp_id` |
| Dead code and stubs | 7 | `/time-adjustments` stub, `testController` |
| Integration gaps | 7 | No employer contributions, missing V1–V9 migrations |
| UI/UX issues | 4 | `forgetPassword` empty, inline JS conflict |
| Minor/style issues | 7 | Hash collision risk, naming inconsistency |
| **Total findings** | **46** | |
