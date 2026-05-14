package digital8.payroll.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import digital8.payroll.repositories.EmployeeRepository;
import digital8.payroll.repositories.AttendanceRepository;
import digital8.payroll.repositories.SssTableRepository;
import digital8.payroll.repositories.PhilhealthTableRepository;
import digital8.payroll.repositories.TaxTableRepository;
import digital8.payroll.repositories.EmployeeDeductionsRepository;
import digital8.payroll.repositories.EmployeeAdjustmentsRepository;
import digital8.payroll.repositories.AdjustmentsRepository;
import digital8.payroll.repositories.PagibigTableRepository;
import digital8.payroll.repositories.DeductionsRepository;
import digital8.payroll.repositories.EmployeeScheduleAssignmentRepository;
import digital8.payroll.repositories.WeeklyScheduleTemplateDayRepository;
import digital8.payroll.dto.DeductionBreakdownItem;
import digital8.payroll.entities.Adjustments;
import digital8.payroll.entities.EmployeeAdjustments;
import digital8.payroll.entities.Deductions;
import digital8.payroll.entities.PayrollItems;
import digital8.payroll.entities.Employees;
import digital8.payroll.entities.Attendance;
import digital8.payroll.entities.EmployeeScheduleAssignment;
import digital8.payroll.entities.SssTable;
import digital8.payroll.entities.TaxTable;
import digital8.payroll.entities.EmployeeDeductions;
import digital8.payroll.entities.PagibigTable;
import digital8.payroll.entities.PhilhealthTable;
import digital8.payroll.entities.WeeklyScheduleTemplateDay;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import digital8.payroll.entities.Holiday;
import java.util.HashSet;
import java.util.Set;

@Service
public class PayrollService {

    private static final int SCALE = 2;
    private static final RoundingMode ROUND = RoundingMode.HALF_UP;

    // Rate derivation constants
    private static final BigDecimal DEFAULT_PAY_FACTOR = new BigDecimal("20");
    private static final BigDecimal HOURS_PER_DAY = new BigDecimal("8");
    private static final BigDecimal MINUTES_PER_HOUR = new BigDecimal("60");

    // Default OT multiplier used when an employee has no per-employee override
    private static final BigDecimal OT_MULTIPLIER_DEFAULT = new BigDecimal("1.0");

    // Statutory deduction constants
    private static final BigDecimal PAGIBIG_RATE = new BigDecimal("0.02");
    private static final BigDecimal PHILHEALTH_RATE = new BigDecimal("0.025");

    // Salary splitting for below-30k employees
    private static final BigDecimal SALARY_THRESHOLD = new BigDecimal("30000");
    private static final BigDecimal PREMIUM_BASE_CAP = new BigDecimal("20000");

    // Simplified withholding tax formula constants
    private static final BigDecimal TAX_RATE_SIMPLIFIED = new BigDecimal("0.10");
    private static final BigDecimal TAX_CONSTANT = new BigDecimal("2395.90");

    // Semi-monthly divisor
    private static final BigDecimal SEMI_MONTHLY_DIVISOR = new BigDecimal("2");

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private SssTableRepository sssTableRepository;
    @Autowired
    private PhilhealthTableRepository philhealthTableRepository;
    @Autowired
    private PagibigTableRepository pagibigTableRepository;
    @Autowired
    private TaxTableRepository taxTableRepository;
    @Autowired
    private EmployeeDeductionsRepository employeeDeductionsRepository;
    @Autowired
    private EmployeeAdjustmentsRepository employeeAdjustmentsRepository;
    @Autowired
    private AdjustmentsRepository adjustmentsRepository;
    @Autowired
    private DeductionsRepository deductionsRepository;
    @Autowired
    private EmployeeScheduleAssignmentRepository employeeScheduleAssignmentRepository;
    @Autowired
    private WeeklyScheduleTemplateDayRepository weeklyScheduleTemplateDayRepository;
    @Autowired
    private HolidayCalendarService holidayCalendarService;

    private static final class PayPeriod {
        final LocalDate start;
        final LocalDate end;
        final boolean semiMonthly; // true → divide statutory by 2

        PayPeriod(LocalDate start, LocalDate end, boolean semiMonthly) {
            this.start = start;
            this.end = end;
            this.semiMonthly = semiMonthly;
        }
    }

    private PayPeriod resolvePayPeriod(String period, YearMonth ym) {
        String p = period == null ? "" : period.trim().toLowerCase();
        if ("semi_1".equals(p) || "semi-monthly-1".equals(p)) {
            return new PayPeriod(ym.atDay(1), ym.atDay(15), true);
        }
        if ("semi_2".equals(p) || "semi-monthly-2".equals(p)) {
            return new PayPeriod(ym.atDay(16), ym.atEndOfMonth(), true);
        }
        // monthly, empty, or unknown → full calendar month, full statutory (no ÷2)
        return new PayPeriod(ym.atDay(1), ym.atEndOfMonth(), false);
    }

    private boolean eligibleForRegularHolidayPay(Employees emp) {
        return emp != null && emp.isHolidayPayEligible();
    }

    /**
     * Pay cutoff bounds for UI (deduction breakdown, etc.).
     */
    public LocalDate[] getPayrollPeriodBounds(int year, Month month, String period) {
        PayPeriod pp = resolvePayPeriod(period, YearMonth.of(year, month));
        return new LocalDate[] { pp.start, pp.end };
    }

    public List<PayrollItems> computePayroll(Integer empId, String period, String monthName) {
        return computePayroll(empId, period, monthName, null);
    }

    public List<PayrollItems> computePayroll(Integer empId, String period, String monthName, Integer yearParam) {
        Optional<Employees> empOpt = employeeRepository.findById(empId);
        if (empOpt.isEmpty())
            return new ArrayList<>();

        Employees emp = empOpt.get();
        BigDecimal monthlyRate = emp.getBasicSalary() != null ? emp.getBasicSalary() : BigDecimal.ZERO;

        // --- 1. Rate Derivation ---
        // Daily Rate = Monthly Rate / Factor
        BigDecimal factor = emp.getFactorRate();
        if (factor == null || factor.compareTo(BigDecimal.ZERO) <= 0) {
            factor = DEFAULT_PAY_FACTOR;
        }
        BigDecimal dailyRate = monthlyRate.divide(factor, 6, ROUND);
        // Hourly Rate = Daily Rate / 8
        BigDecimal hourlyRate = dailyRate.divide(HOURS_PER_DAY, 6, ROUND);
        // Per Minute Rate = Hourly Rate / 60
        BigDecimal perMinuteRate = hourlyRate.divide(MINUTES_PER_HOUR, 6, ROUND);

        // --- 2. Determine pay period ---
        Month month = null;
        if (monthName != null && !monthName.isBlank()) {
            try {
                month = Month.valueOf(monthName.toUpperCase());
            } catch (Exception e) {
                month = null;
            }
        }
        int year = (yearParam != null) ? yearParam : LocalDate.now().getYear();
        if (month == null) {
            month = LocalDate.now().getMonth();
        }

        YearMonth ym = YearMonth.of(year, month);
        PayPeriod pp = resolvePayPeriod(period, ym);
        LocalDate periodStart = pp.start;
        LocalDate periodEnd = pp.end;
        boolean semiMonthly = pp.semiMonthly;

        // --- 3. Aggregate attendance data for the period ---
        List<Attendance> records = attendanceRepository.findByEmployeeIdOrderByDateDesc(empId);
        BigDecimal totalWorkedHours = BigDecimal.ZERO;
        BigDecimal totalOtHours = BigDecimal.ZERO;
        int totalLateUndertimeMinutes = 0;

        for (Attendance a : records) {
            LocalDate d = a.getAttendance_date();
            if (d == null)
                continue;
            if (d.isBefore(periodStart) || d.isAfter(periodEnd))
                continue;

            AttendanceMetrics metrics = resolveAttendanceMetrics(a);
            totalWorkedHours = totalWorkedHours.add(metrics.workHours);
            totalOtHours = totalOtHours.add(metrics.overtimeHours);
            totalLateUndertimeMinutes += metrics.lateMinutes;
            totalLateUndertimeMinutes += metrics.undertimeMinutes;
        }

        // --- 3b. Regular holiday pay (temporary rule) ---
        // Rule requested:
        // - If employee worked on a REGULAR holiday: pay becomes double for ALL hours
        // that day (work + overtime).
        // Since basic/overtime already pay 1x, we add 1x as holidayPay premium for
        // those hours.
        // - If employee did NOT work on a REGULAR holiday: pay 1x daily rate for that
        // day.
        BigDecimal holidayPay = BigDecimal.ZERO;

        if (eligibleForRegularHolidayPay(emp)) {

            List<Holiday> holidaysInPeriod = holidayCalendarService.activeHolidaysInRange(
                    periodStart,
                    periodEnd);

            Set<LocalDate> regularHolidayDates = new HashSet<>();
            for (Holiday h : holidaysInPeriod) {
                if (Holiday.TYPE_REGULAR.equalsIgnoreCase(h.getHolidayType())) {
                    regularHolidayDates.add(h.getHolidayDate());
                }
            }

            // Track which dates have a clock-in (time_in != null)
            Set<LocalDate> clockInDates = new HashSet<>();

            BigDecimal regularHolidayWorkedHours = BigDecimal.ZERO;

            for (Attendance a : records) {
                LocalDate d = a.getAttendance_date();
                if (d == null)
                    continue;
                if (d.isBefore(periodStart) || d.isAfter(periodEnd))
                    continue;

                boolean clockedIn = a.getTime_in() != null;
                if (clockedIn) {
                    clockInDates.add(d);
                }

                if (!clockedIn)
                    continue;
                if (!regularHolidayDates.contains(d))
                    continue;

                AttendanceMetrics metrics = resolveAttendanceMetrics(a);
                BigDecimal wh = metrics.workHours;
                BigDecimal ot = metrics.overtimeHours;

                // Optional safety: only count if there are positive hours
                // if (wh.add(ot).compareTo(BigDecimal.ZERO) <= 0) continue;

                regularHolidayWorkedHours = regularHolidayWorkedHours.add(wh).add(ot);
            }

            // Premium for worked regular-holiday hours: +1x hourly (to make total 2x)
            holidayPay = holidayPay.add(hourlyRate.multiply(regularHolidayWorkedHours));

            // Pay for unworked regular holidays: +1x daily rate
            for (LocalDate hd : regularHolidayDates) {
                if (!clockInDates.contains(hd)) {
                    holidayPay = holidayPay.add(dailyRate);
                }
            }

            holidayPay = holidayPay.setScale(SCALE, ROUND);

        }
        // --- 4. Compute Earnings ---
        // Basic Pay = Hourly Rate × Total Hours
        BigDecimal basicPay = hourlyRate.multiply(totalWorkedHours).setScale(SCALE, ROUND);

        // Overtime Pay = Hourly Rate × Total OT Hours × OT Multiplier
        // Use per-employee override if set; fall back to system defaults by employment type
        BigDecimal employeeOtMultiplier = emp.getOtMultiplier();
        BigDecimal otMultiplier = (employeeOtMultiplier != null && employeeOtMultiplier.compareTo(BigDecimal.ZERO) > 0)
            ? employeeOtMultiplier
            : OT_MULTIPLIER_DEFAULT;
        BigDecimal overtimePay = hourlyRate.multiply(totalOtHours).multiply(otMultiplier).setScale(SCALE, ROUND);

        // Adjustment Earnings — from employee_adjustments where adjustmentType = 'EARNINGS'
        BigDecimal[] adjSplit = computeAdjustmentsSplit(empId, periodStart, periodEnd, period);
        BigDecimal adjustmentEarnings = adjSplit[0];
        BigDecimal adjustmentDeductions = adjSplit[1];

        // Total Earnings = Basic Pay + Overtime + Adjustment Earnings
        BigDecimal totalEarnings = basicPay
                .add(overtimePay)
                .add(holidayPay)    
                .add(adjustmentEarnings)
                .setScale(SCALE, ROUND);

        // --- 5. Compute Non-Statutory Deductions ---
        // Late/Undertime deduction = Total Late/Undertime Minutes × (Rounded) Per
        // Minute Rate
        // Matches the spreadsheet behavior which rounds the per minute rate early (e.g.
        // 2.19 instead of 2.1875)
        BigDecimal roundedPerMinuteRate = perMinuteRate.setScale(SCALE, ROUND);
        BigDecimal lateUndertimeDeduction = roundedPerMinuteRate
                .multiply(new BigDecimal(totalLateUndertimeMinutes))
                .setScale(SCALE, ROUND);

        // Employee deductions active for this pay cutoff
        BigDecimal employeeDeductions = computeEmployeeDeductions(empId, periodStart, periodEnd, period);
        // adjustmentDeductions already computed above from employee_adjustments
        BigDecimal combinedDeductions = employeeDeductions.add(adjustmentDeductions);

        // Total Deductions (non-statutory) = Employee Deductions + Adjustment Deductions + Late/Undertime
        BigDecimal totalNonStatutoryDeductions = combinedDeductions
                .add(lateUndertimeDeduction)
                .setScale(SCALE, ROUND);

        // Total Service Fee = Total Earnings - Total Non-Statutory Deductions
        BigDecimal serviceFee = totalEarnings.subtract(totalNonStatutoryDeductions).setScale(SCALE, ROUND);

        // --- 6. Compute Statutory Deductions ---
        // Determine premium base for contributions.
        // HR rule:
        // - salary below 30k: treat as 20k basic + allowance (non-taxable), so premiums
        // are computed on basic only (<=20k)
        // - salary 30k and above: premiums computed on full salary
        BigDecimal premiumBase = monthlyRate.compareTo(SALARY_THRESHOLD) < 0
                ? monthlyRate.min(PREMIUM_BASE_CAP)
                : monthlyRate;

        BigDecimal sss;
        BigDecimal philhealth;
        BigDecimal pagibig;
        BigDecimal tax;

        // HR policy: ALL employees are subject to government contributions
        // (SSS/PhilHealth/HDMF),
        // regardless of employment type.
        sss = computeSss(premiumBase, year);
        philhealth = computePhilhealth(premiumBase, year);
        pagibig = computePagibig(premiumBase, year);

        // WHT: for semi-monthly, use SEMI_MONTHLY table on premiumBase/2.
        // For monthly, use MONTHLY table on premiumBase.
        // Using premiumBase (not monthlyRate) ensures sub-30k employees (capped at ₱20k)
        // stay in the 0% bracket → WHT = 0, matching HR's sample computation.
        if (semiMonthly) {
            BigDecimal semiBase = premiumBase.divide(SEMI_MONTHLY_DIVISOR, SCALE, ROUND);
            tax = computeWithholdingTaxFromTable(semiBase, year, "SEMI_MONTHLY");
        } else {
            tax = computeWithholdingTaxFromTable(premiumBase, year, "MONTHLY");
        }

        // Total statutory deducted this slip:
        // Semi-monthly: (SSS + PhilHealth + Pag-IBIG + SEMI_WHT) / 2
        //   → WHT is the semi-monthly bracket value; the entire sum is divided by 2
        //   → matches HR's spreadsheet: all four columns summed then halved
        // Monthly: SSS + PhilHealth + Pag-IBIG + MONTHLY_WHT
        BigDecimal statutoryTotal = sss.add(philhealth).add(pagibig).add(tax);
        BigDecimal statutoryDeductedThisSlip;
        if (semiMonthly) {
            statutoryDeductedThisSlip = statutoryTotal.divide(SEMI_MONTHLY_DIVISOR, SCALE, ROUND);
        } else {
            statutoryDeductedThisSlip = statutoryTotal;
        }

        // --- 7. Net Pay ---
        BigDecimal netPay = serviceFee.subtract(statutoryDeductedThisSlip).setScale(SCALE, ROUND);

        // --- 8. For backward compatibility, compute existing fields ---
        BigDecimal allowances = BigDecimal.ZERO; // placeholder
        BigDecimal grossPay = totalEarnings; // totalEarnings is the new grossPay
        BigDecimal totalDeductions = totalNonStatutoryDeductions.add(statutoryDeductedThisSlip).setScale(SCALE, ROUND);

        // --- 9. Build PayrollItem ---
        PayrollItems item = new PayrollItems();
        item.setEmployeeId(empId);

        // Rate breakdown
        item.setDailyRate(dailyRate.setScale(SCALE, ROUND));
        item.setHourlyRate(hourlyRate.setScale(SCALE, ROUND));
        item.setPerMinuteRate(perMinuteRate.setScale(SCALE, ROUND));

        // Hours
        item.setTotalWorkedHours(totalWorkedHours.setScale(SCALE, ROUND));
        item.setTotalOtHours(totalOtHours.setScale(SCALE, ROUND));

        // Earnings
        item.setBasicPay(basicPay);
        item.setOvertimePay(overtimePay);
        item.setHolidayPay(holidayPay);
        item.setAllowances(allowances);
        item.setAdjustmentEarnings(adjustmentEarnings.setScale(SCALE, ROUND));
        item.setTotalEarnings(totalEarnings);
        item.setGrossPay(grossPay);

        // Non-statutory deductions
        item.setLateUndertimeMinutes(totalLateUndertimeMinutes);
        item.setLateUndertimeDeduction(lateUndertimeDeduction);
        item.setCashAdvance(BigDecimal.ZERO); // deprecated: kept for backward compat
        item.setAdjustmentDeductions(combinedDeductions.setScale(SCALE, ROUND));
        item.setOtherDeductions(totalNonStatutoryDeductions);

        // Service fee
        item.setServiceFee(serviceFee);

        // Statutory deductions
        item.setSss(sss);
        item.setPhilhealth(philhealth);
        item.setPagibig(pagibig);
        item.setTax(tax);
        item.setSemiMonthlyContributions(statutoryDeductedThisSlip);

        // Totals
        item.setTotalDeductions(totalDeductions);
        item.setNetPay(netPay);

        // Employment type
        String empType = emp.getEmploymentType() != null ? emp.getEmploymentType().trim() : "Regular";
        item.setEmploymentType(empType);

        List<PayrollItems> out = new ArrayList<>();
        out.add(item);
        return out;
    }

    private AttendanceMetrics resolveAttendanceMetrics(Attendance attendance) {
        if (attendance == null) {
            return AttendanceMetrics.zero();
        }

        AttendanceMetrics fallback = AttendanceMetrics.zero();
        LocalTime timeIn = attendance.getTime_in();
        LocalTime timeOut = attendance.getTime_out();
        if (timeIn != null && timeOut != null) {
            BigDecimal rawWorkedHours = minutesToHours(clockMinutesBetween(timeIn, timeOut));
            BigDecimal storedOvertime = preferStoredDecimal(
                    attendance.getOvertime_hours(),
                    BigDecimal.ZERO.setScale(SCALE, ROUND));
            fallback = new AttendanceMetrics(
                    preferStoredDecimal(attendance.getWork_hours(), rawWorkedHours),
                    storedOvertime,
                    preferStoredInt(attendance.getLate_minutes(), 0),
                    preferStoredInt(attendance.getUndertime_minutes(), 0));

            if (isRestDay(attendance.getEmployeeId(), attendance.getAttendance_date())) {
                return new AttendanceMetrics(
                        BigDecimal.ZERO.setScale(SCALE, ROUND),
                        storedOvertime,
                        0,
                        0);
            }

            Optional<WeeklyScheduleTemplateDay> shiftDayOpt = findAssignedShiftDay(
                    attendance.getEmployeeId(),
                    attendance.getAttendance_date());
            if (shiftDayOpt.isPresent()) {
                WeeklyScheduleTemplateDay shiftDay = shiftDayOpt.get();
                AttendanceMetrics computed = computeAttendanceMetrics(
                        timeIn,
                        timeOut,
                        shiftDay.getTimeIn(),
                        shiftDay.getTimeOut());
                return new AttendanceMetrics(
                        computed.workHours,
                        resolveOvertimeHours(attendance.getOvertime_hours(), computed.overtimeHours),
                        computed.lateMinutes,
                        computed.undertimeMinutes);
            }
        }

        return fallback;
    }

    private boolean isRestDay(Integer employeeId, LocalDate date) {
        if (employeeId == null || date == null) {
            return false;
        }
        Optional<EmployeeScheduleAssignment> assignmentOpt = resolveShiftAssignment(employeeId, date);
        if (assignmentOpt.isEmpty()) {
            return false;
        }
        return weeklyScheduleTemplateDayRepository.findByTemplateIdAndDayOfWeek(
                assignmentOpt.get().getTemplateId(),
                date.getDayOfWeek().getValue())
                .map(WeeklyScheduleTemplateDay::isRestDay)
                .orElse(false);
    }

    private Optional<WeeklyScheduleTemplateDay> findAssignedShiftDay(Integer employeeId, LocalDate date) {
        if (employeeId == null || date == null) {
            return Optional.empty();
        }

        Optional<EmployeeScheduleAssignment> assignmentOpt = resolveShiftAssignment(employeeId, date);
        if (assignmentOpt.isEmpty()) {
            return Optional.empty();
        }

        Optional<WeeklyScheduleTemplateDay> dayOpt =
                weeklyScheduleTemplateDayRepository.findByTemplateIdAndDayOfWeek(
                        assignmentOpt.get().getTemplateId(),
                        date.getDayOfWeek().getValue());
        if (dayOpt.isEmpty()) {
            return Optional.empty();
        }

        WeeklyScheduleTemplateDay day = dayOpt.get();
        if (day.isRestDay() || day.getTimeIn() == null || day.getTimeOut() == null) {
            return Optional.empty();
        }
        return Optional.of(day);
    }

    private Optional<EmployeeScheduleAssignment> resolveShiftAssignment(Integer employeeId, LocalDate date) {
        if (employeeId == null || date == null) {
            return Optional.empty();
        }
        Optional<EmployeeScheduleAssignment> exact =
                employeeScheduleAssignmentRepository.findByEmployeeIdAndScheduleYearAndScheduleMonth(
                        employeeId, date.getYear(), date.getMonthValue());
        if (exact.isPresent()) {
            return exact;
        }
        return employeeScheduleAssignmentRepository.findLatestAssignmentOnOrBefore(
                employeeId, date.getYear(), date.getMonthValue());
    }

    private AttendanceMetrics computeAttendanceMetrics(
            LocalTime actualIn,
            LocalTime actualOut,
            LocalTime shiftIn,
            LocalTime shiftOut) {
        if (actualIn == null || actualOut == null || shiftIn == null || shiftOut == null) {
            return AttendanceMetrics.zero();
        }

        long shiftStart = shiftIn.toSecondOfDay() / 60L;
        long shiftDuration = clockMinutesBetween(shiftIn, shiftOut);
        if (shiftDuration <= 0) {
            return AttendanceMetrics.zero();
        }
        long shiftEnd = shiftStart + shiftDuration;

        long actualStart = normalizeMinuteToTarget(actualIn, shiftStart);
        long actualDuration = clockMinutesBetween(actualIn, actualOut);
        if (actualDuration <= 0) {
            return AttendanceMetrics.zero();
        }
        long actualEnd = actualStart + actualDuration;

        long regularMinutes = Math.max(0L, Math.min(actualEnd, shiftEnd) - Math.max(actualStart, shiftStart));
        long lateMinutes = Math.max(0L, Math.min(actualStart, shiftEnd) - shiftStart);
        long undertimeMinutes = Math.max(0L, shiftEnd - Math.max(actualEnd, shiftStart));
        long overtimeMinutes = Math.max(0L, actualEnd - Math.max(shiftEnd, actualStart));

        return new AttendanceMetrics(
                minutesToHours(regularMinutes),
                minutesToHours(overtimeMinutes),
                safeIntMinutes(lateMinutes),
                safeIntMinutes(undertimeMinutes));
    }

    private BigDecimal preferStoredDecimal(BigDecimal stored, BigDecimal computed) {
        if (stored != null && stored.compareTo(BigDecimal.ZERO) > 0) {
            return stored.setScale(SCALE, ROUND);
        }
        return computed != null ? computed.setScale(SCALE, ROUND) : BigDecimal.ZERO.setScale(SCALE, ROUND);
    }

    private int preferStoredInt(Integer stored, int computed) {
        if (stored != null && stored > 0) {
            return stored;
        }
        return Math.max(0, computed);
    }

    private int safeIntMinutes(long minutes) {
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, minutes));
    }

    private BigDecimal resolveOvertimeHours(BigDecimal storedOvertime, BigDecimal computedOvertime) {
        BigDecimal stored = storedOvertime != null
                ? storedOvertime.setScale(SCALE, ROUND)
                : BigDecimal.ZERO.setScale(SCALE, ROUND);
        BigDecimal computed = computedOvertime != null
                ? computedOvertime.setScale(SCALE, ROUND)
                : BigDecimal.ZERO.setScale(SCALE, ROUND);
        return stored.max(computed);
    }

    private long clockMinutesBetween(LocalTime start, LocalTime end) {
        long minutes = ChronoUnit.MINUTES.between(start, end);
        if (minutes < 0) {
            minutes += 24L * 60L;
        }
        return minutes;
    }

    private long normalizeMinuteToTarget(LocalTime time, long targetMinute) {
        long baseMinute = time.toSecondOfDay() / 60L;
        long best = baseMinute;
        long bestDistance = Math.abs(baseMinute - targetMinute);
        long[] offsets = new long[] { -24L * 60L, 24L * 60L };
        for (long offset : offsets) {
            long candidate = baseMinute + offset;
            long distance = Math.abs(candidate - targetMinute);
            if (distance < bestDistance) {
                best = candidate;
                bestDistance = distance;
            }
        }
        return best;
    }

    private BigDecimal minutesToHours(long minutes) {
        return BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), SCALE, ROUND);
    }

    private static final class AttendanceMetrics {
        private final BigDecimal workHours;
        private final BigDecimal overtimeHours;
        private final int lateMinutes;
        private final int undertimeMinutes;

        private AttendanceMetrics(
                BigDecimal workHours,
                BigDecimal overtimeHours,
                int lateMinutes,
                int undertimeMinutes) {
            this.workHours = workHours != null
                    ? workHours.setScale(SCALE, ROUND)
                    : BigDecimal.ZERO.setScale(SCALE, ROUND);
            this.overtimeHours = overtimeHours != null
                    ? overtimeHours.setScale(SCALE, ROUND)
                    : BigDecimal.ZERO.setScale(SCALE, ROUND);
            this.lateMinutes = Math.max(0, lateMinutes);
            this.undertimeMinutes = Math.max(0, undertimeMinutes);
        }

        private static AttendanceMetrics zero() {
            return new AttendanceMetrics(
                    BigDecimal.ZERO.setScale(SCALE, ROUND),
                    BigDecimal.ZERO.setScale(SCALE, ROUND),
                    0,
                    0);
        }
    }

    /**
     * SSS: bracket lookup using premium base on the ssstable.
     */
    private BigDecimal computeSss(BigDecimal premiumBase, int year) {
        List<SssTable> rows = sssTableRepository.findByEffectiveYearOrderByRangeFromAsc(year);
        if (rows == null || rows.isEmpty())
            return BigDecimal.ZERO;
        for (SssTable row : rows) {
            if (row.getRangeFrom() != null && row.getRangeTo() != null && row.getEmployeeShare() != null) {
                if (premiumBase.compareTo(row.getRangeFrom()) >= 0 && premiumBase.compareTo(row.getRangeTo()) <= 0) {
                    return row.getEmployeeShare().setScale(SCALE, ROUND);
                }
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * PhilHealth: flat formula (premiumBase × Rate)
     * Fetched dynamically from table, falling back to 2.5% if unconfigured.
     */
    private BigDecimal computePhilhealth(BigDecimal premiumBase, int year) {
        BigDecimal activeRate = PHILHEALTH_RATE;
        List<PhilhealthTable> rates = philhealthTableRepository.findByEffectiveYearOrderByRangeFromAsc(year);
        if (rates != null && !rates.isEmpty() && rates.get(0).getEmployeeShare() != null) {
            activeRate = rates.get(0).getEmployeeShare().divide(new BigDecimal("100"), 4, ROUND);
        }
        return premiumBase.multiply(activeRate).setScale(SCALE, ROUND);
    }

    /**
     * Pag-IBIG (HDMF): flat formula (premiumBase × Rate)
     * Fetched dynamically from table, falling back to 2.0% if unconfigured.
     */
    private BigDecimal computePagibig(BigDecimal premiumBase, int year) {
        BigDecimal activeRate = PAGIBIG_RATE;
        List<PagibigTable> rates = pagibigTableRepository.findByEffectiveYearOrderByRangeFromAsc(year);
        if (rates != null && !rates.isEmpty() && rates.get(0).getEmployeeShare() != null) {
            activeRate = rates.get(0).getEmployeeShare().divide(new BigDecimal("100"), 4, ROUND);
        }
        return premiumBase.multiply(activeRate).setScale(SCALE, ROUND);
    }

    private BigDecimal withholdingTaxFromBracketRows(BigDecimal taxablePay, List<TaxTable> rows) {
        if (rows == null || rows.isEmpty())
            return BigDecimal.ZERO;
        TaxTable bracket = null;
        for (TaxTable r : rows) {
            if (r.getCompensationFrom() == null || r.getTaxRate() == null || r.getAdditionalTax() == null)
                continue;
            if (taxablePay.compareTo(r.getCompensationFrom()) >= 0) {
                if (bracket == null || r.getCompensationFrom().compareTo(bracket.getCompensationFrom()) > 0) {
                    bracket = r;
                }
            }
        }
        if (bracket == null)
            return BigDecimal.ZERO;
        BigDecimal excess = taxablePay.subtract(bracket.getCompensationFrom());
        return bracket.getAdditionalTax()
                .add(bracket.getTaxRate().multiply(excess))
                .setScale(SCALE, ROUND)
                .max(BigDecimal.ZERO);
    }

    /**
     * Withholding tax: SEMI_MONTHLY brackets on cutoff gross, or MONTHLY on
     * period/salary base.
     */
    private BigDecimal computeWithholdingTaxFromTable(BigDecimal taxablePay, int year, String payFrequency) {
        if (taxablePay == null || taxablePay.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        List<TaxTable> rows = taxTableRepository.findByEffectiveYearAndPayFrequencyOrderByCompensationFromAsc(year,
                payFrequency);
        if (rows == null || rows.isEmpty()) {
            rows = taxTableRepository.findByEffectiveYearAndPayFrequencyOrderByCompensationFromAsc(year - 1,
                    payFrequency);
        }
        if ((rows == null || rows.isEmpty()) && "SEMI_MONTHLY".equals(payFrequency)) {
            List<TaxTable> m = taxTableRepository.findByEffectiveYearAndPayFrequencyOrderByCompensationFromAsc(year,
                    "MONTHLY");
            if (m == null || m.isEmpty()) {
                m = taxTableRepository.findByEffectiveYearAndPayFrequencyOrderByCompensationFromAsc(year - 1,
                        "MONTHLY");
            }
            if (m != null && !m.isEmpty()) {
                BigDecimal impliedMonthly = taxablePay.multiply(SEMI_MONTHLY_DIVISOR);
                return withholdingTaxFromBracketRows(impliedMonthly, m)
                        .divide(SEMI_MONTHLY_DIVISOR, SCALE, ROUND);
            }
            return BigDecimal.ZERO;
        }
        if (rows == null || rows.isEmpty()) {
            BigDecimal tax = taxablePay.multiply(TAX_RATE_SIMPLIFIED)
                    .subtract(TAX_CONSTANT)
                    .setScale(SCALE, ROUND);
            return tax.max(BigDecimal.ZERO);
        }
        return withholdingTaxFromBracketRows(taxablePay, rows);
    }

    /**
     * Computes the total of ALL employee deductions for the given period.
     * For recurring deductions, also checks deductionCutoff vs the current pay period:
     *   SEMI_1 = only on first cutoff (semi_1)
     *   SEMI_2 = only on second cutoff (semi_2)  [default]
     *   BOTH   = every cutoff
     * One-time deductions are not affected by deductionCutoff.
     */
    // Visible for unit-testing subclass — do not call from outside PayrollService
    BigDecimal computeEmployeeDeductions(Integer employeeId, LocalDate periodStart, LocalDate periodEnd, String period) {
        List<EmployeeDeductions> list = employeeDeductionsRepository.findByEmployeeId(employeeId);
        if (list == null)
            return BigDecimal.ZERO;
        BigDecimal sum = BigDecimal.ZERO;
        for (EmployeeDeductions ed : list) {
            if (ed.getAmount() == null)
                continue;

            boolean recurring = Boolean.TRUE.equals(ed.getIsRecurring());
            if (recurring) {
                if (ed.getStartDate() != null && ed.getStartDate().isAfter(periodEnd))
                    continue;
                if (ed.getEndDate() != null && ed.getEndDate().isBefore(periodStart))
                    continue;
                if (!matchesCutoff(period, ed.getDeductionCutoff(), "SEMI_2")) continue;
            } else {
                if (ed.getStartDate() == null || ed.getStartDate().isAfter(periodEnd)
                        || ed.getStartDate().isBefore(periodStart))
                    continue;
            }
            sum = sum.add(ed.getAmount());
        }
        return sum.setScale(SCALE, ROUND);
    }

    /**
     * Splits EmployeeAdjustments for the given period into [earningsSum, deductionsSum].
     * Applies the same cutoff + date-range filtering as computeEmployeeDeductions.
     */
    // Visible for unit-testing subclass — do not call from outside PayrollService
    BigDecimal[] computeAdjustmentsSplit(Integer employeeId, LocalDate periodStart, LocalDate periodEnd, String period) {
        List<EmployeeAdjustments> list = employeeAdjustmentsRepository.findByEmployeeId(employeeId);
        BigDecimal earnings = BigDecimal.ZERO;
        BigDecimal deductions = BigDecimal.ZERO;
        if (list == null) return new BigDecimal[]{ earnings, deductions };
        Set<Integer> adjIds = list.stream()
            .map(EmployeeAdjustments::getAdjustmentId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Integer, Adjustments> adjMap = adjIds.isEmpty() ? Collections.emptyMap()
            : adjustmentsRepository.findAllById(adjIds).stream()
                .collect(Collectors.toMap(Adjustments::getAdjustmentId, a -> a));
        for (EmployeeAdjustments ea : list) {
            if (ea.getAmount() == null) continue;
            boolean recurring = Boolean.TRUE.equals(ea.getIsRecurring());
            if (recurring) {
                if (ea.getStartDate() != null && ea.getStartDate().isAfter(periodEnd)) continue;
                if (ea.getEndDate() != null && ea.getEndDate().isBefore(periodStart)) continue;
                if (!matchesCutoff(period, ea.getApplyOnCutoff(), "BOTH")) continue;
            } else {
                if (ea.getStartDate() == null || ea.getStartDate().isAfter(periodEnd)
                        || ea.getStartDate().isBefore(periodStart)) continue;
            }
            // Resolve type from catalog
            String adjType = "Earnings";
            if (ea.getAdjustmentId() != null) {
                Adjustments a = adjMap.get(ea.getAdjustmentId());
                if (a != null && a.getAdjustmentType() != null) {
                    adjType = a.getAdjustmentType();
                }
            }
            if ("Deduction".equalsIgnoreCase(adjType)) {
                deductions = deductions.add(ea.getAmount());
            } else {
                earnings = earnings.add(ea.getAmount());
            }
        }
        return new BigDecimal[]{ earnings.setScale(SCALE, ROUND), deductions.setScale(SCALE, ROUND) };
    }

    /**
     * Returns a named breakdown of ALL adjustments for the given period (for payslip display).
     */
    public List<DeductionBreakdownItem> getAdjustmentsBreakdown(Integer employeeId, LocalDate periodStart,
            LocalDate periodEnd, String period) {
        List<EmployeeAdjustments> list = employeeAdjustmentsRepository.findByEmployeeId(employeeId);
        List<DeductionBreakdownItem> result = new ArrayList<>();
        if (list == null) return result;
        Set<Integer> adjIds = list.stream()
            .map(EmployeeAdjustments::getAdjustmentId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Integer, Adjustments> adjMap = adjIds.isEmpty() ? Collections.emptyMap()
            : adjustmentsRepository.findAllById(adjIds).stream()
                .collect(Collectors.toMap(Adjustments::getAdjustmentId, a -> a));
        for (EmployeeAdjustments ea : list) {
            if (ea.getAmount() == null) continue;
            boolean recurring = Boolean.TRUE.equals(ea.getIsRecurring());
            if (recurring) {
                if (ea.getStartDate() != null && ea.getStartDate().isAfter(periodEnd)) continue;
                if (ea.getEndDate() != null && ea.getEndDate().isBefore(periodStart)) continue;
                if (!matchesCutoff(period, ea.getApplyOnCutoff(), "BOTH")) continue;
            } else {
                if (ea.getStartDate() == null || ea.getStartDate().isAfter(periodEnd)
                        || ea.getStartDate().isBefore(periodStart)) continue;
            }
            DeductionBreakdownItem item = new DeductionBreakdownItem();
            // Resolve name from type catalog (mirrors getDeductionsBreakdown)
            String name = "Other";
            if (ea.getAdjustmentId() != null) {
                Adjustments a = adjMap.get(ea.getAdjustmentId());
                if (a != null && a.getAdjustmentName() != null) {
                    name = a.getAdjustmentName();
                }
            }
            item.setDeductionName(name);
            item.setAmount(ea.getAmount().setScale(SCALE, ROUND));
            result.add(item);
        }
        return result;
    }

    /**
     * Returns a named breakdown of ALL employee deductions for the given period.
     * Applies the same cutoff filtering as computeEmployeeDeductions.
     */
    public List<DeductionBreakdownItem> getDeductionsBreakdown(Integer employeeId, LocalDate periodStart,
            LocalDate periodEnd, String period) {
        List<EmployeeDeductions> list = employeeDeductionsRepository.findByEmployeeId(employeeId);
        List<DeductionBreakdownItem> result = new ArrayList<>();
        if (list == null)
            return result;
        Set<Integer> dedIds = list.stream()
            .map(EmployeeDeductions::getDeductionId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Integer, Deductions> dedMap = dedIds.isEmpty() ? Collections.emptyMap()
            : deductionsRepository.findAllById(dedIds).stream()
                .collect(Collectors.toMap(Deductions::getDeductionId, d -> d));
        for (EmployeeDeductions ed : list) {
            if (ed.getAmount() == null)
                continue;
            boolean recurring = Boolean.TRUE.equals(ed.getIsRecurring());
            if (recurring) {
                if (ed.getStartDate() != null && ed.getStartDate().isAfter(periodEnd))
                    continue;
                if (ed.getEndDate() != null && ed.getEndDate().isBefore(periodStart))
                    continue;
                if (!matchesCutoff(period, ed.getDeductionCutoff(), "SEMI_2")) continue;
            } else {
                if (ed.getStartDate() == null || ed.getStartDate().isAfter(periodEnd)
                        || ed.getStartDate().isBefore(periodStart))
                    continue;
            }
            String name = "Other";
            if (ed.getDeductionId() != null) {
                Deductions d = dedMap.get(ed.getDeductionId());
                if (d != null && d.getDeductionName() != null) {
                    name = d.getDeductionName();
                }
            }

            DeductionBreakdownItem item = new DeductionBreakdownItem();
            item.setDeductionName(name);
            item.setAmount(ed.getAmount().setScale(SCALE, ROUND));
            result.add(item);
        }
        return result;
    }

    /**
     * Returns true if the given cutoff applies for the current period.
     * - BOTH always matches.
     * - Monthly periods always match (cutoff only applies to semi-monthly).
     * - For semi-monthly periods, the cutoff must match the period (case-insensitive).
     * - null period falls back to monthly behavior (all cutoffs match).
     */
    private boolean matchesCutoff(String period, String cutoff, String defaultCutoff) {
        if (cutoff == null) cutoff = defaultCutoff;
        if ("BOTH".equalsIgnoreCase(cutoff)) return true;
        String p = period == null ? "" : period.trim().toLowerCase();
        if (!("semi_1".equals(p) || "semi-monthly-1".equals(p)
            || "semi_2".equals(p) || "semi-monthly-2".equals(p))) {
            return true;
        }
        if ("semi_1".equals(p) || "semi-monthly-1".equals(p)) {
            return "SEMI_1".equalsIgnoreCase(cutoff);
        }
        if ("semi_2".equals(p) || "semi-monthly-2".equals(p)) {
            return "SEMI_2".equalsIgnoreCase(cutoff);
        }
        return true;
    }
}
