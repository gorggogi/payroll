package digital8.payroll.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import digital8.payroll.repositories.EmployeeRepository;
import digital8.payroll.repositories.AttendanceRepository;
import digital8.payroll.repositories.SssTableRepository;
import digital8.payroll.repositories.PhilhealthTableRepository;
import digital8.payroll.repositories.TaxTableRepository;
import digital8.payroll.repositories.EmployeeDeductionsRepository;
import digital8.payroll.repositories.DeductionsRepository;
import digital8.payroll.dto.DeductionBreakdownItem;
import digital8.payroll.entities.Deductions;
import digital8.payroll.entities.PayrollItems;
import digital8.payroll.entities.Employees;
import digital8.payroll.entities.Attendance;
import digital8.payroll.entities.SssTable;
import digital8.payroll.entities.EmployeeDeductions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PayrollService { 

    private static final int SCALE = 2;
    private static final RoundingMode ROUND = RoundingMode.HALF_UP;

    // Rate derivation constants
    private static final BigDecimal DEFAULT_PAY_FACTOR = new BigDecimal("20");
    private static final BigDecimal HOURS_PER_DAY = new BigDecimal("8");
    private static final BigDecimal MINUTES_PER_HOUR = new BigDecimal("60");

    // OT multipliers by employment type
    private static final BigDecimal OT_MULTIPLIER_JOB_ORDER = new BigDecimal("1.0");
    private static final BigDecimal OT_MULTIPLIER_REGULAR = new BigDecimal("1.25");

    // Statutory deduction constants
    private static final BigDecimal PAGIBIG_RATE = new BigDecimal("0.02");
    private static final BigDecimal PHILHEALTH_RATE = new BigDecimal("0.05");
    private static final BigDecimal PHILHEALTH_SPLIT = new BigDecimal("2");

    // Salary splitting for below-30k employees
    private static final BigDecimal SALARY_THRESHOLD = new BigDecimal("30000");
    private static final BigDecimal PREMIUM_BASE_CAP = new BigDecimal("20000");

    // Simplified withholding tax formula constants
    private static final BigDecimal TAX_RATE_SIMPLIFIED = new BigDecimal("0.10");
    private static final BigDecimal TAX_CONSTANT = new BigDecimal("2395.90");

    // Job Order EWT rate
    private static final BigDecimal JOB_ORDER_EWT_RATE = new BigDecimal("0.05");

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
    private TaxTableRepository taxTableRepository;
    @Autowired
    private EmployeeDeductionsRepository employeeDeductionsRepository;
    @Autowired
    private DeductionsRepository deductionsRepository;

    public List<PayrollItems> computePayroll(Integer empId, String period, String monthName) {
        return computePayroll(empId, period, monthName, null);
    }

    public List<PayrollItems> computePayroll(Integer empId, String period, String monthName, Integer yearParam) {
        Optional<Employees> empOpt = employeeRepository.findById(empId);
        if (empOpt.isEmpty()) return new ArrayList<>();

        Employees emp = empOpt.get();
        BigDecimal monthlyRate = emp.getBasicSalary() != null ? emp.getBasicSalary() : BigDecimal.ZERO;
        String empType = emp.getEmploymentType() != null ? emp.getEmploymentType().trim() : "Regular";
        boolean isJobOrder = "Job Order".equalsIgnoreCase(empType) || "JobOrder".equalsIgnoreCase(empType);

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

        // --- 3. Aggregate attendance data for the period ---
        List<Attendance> records = attendanceRepository.findByEmployeeIdOrderByDateDesc(empId);
        BigDecimal totalWorkedHours = BigDecimal.ZERO;
        BigDecimal totalOtHours = BigDecimal.ZERO;
        int totalLateUndertimeMinutes = 0;

        for (Attendance a : records) {
            if (a.getAttendance_date() == null) continue;
            if (a.getAttendance_date().getMonth() != month || a.getAttendance_date().getYear() != year) continue;

            if (a.getWork_hours() != null) totalWorkedHours = totalWorkedHours.add(a.getWork_hours());
            if (a.getOvertime_hours() != null) totalOtHours = totalOtHours.add(a.getOvertime_hours());
            if (a.getLate_minutes() != null) totalLateUndertimeMinutes += a.getLate_minutes();
            if (a.getUndertime_minutes() != null) totalLateUndertimeMinutes += a.getUndertime_minutes();
        }

        // --- 4. Compute Earnings ---
        // Basic Pay = Hourly Rate × Total Hours
        BigDecimal basicPay = hourlyRate.multiply(totalWorkedHours).setScale(SCALE, ROUND);

        // Overtime Pay = Hourly Rate × Total OT Hours × OT Multiplier
        BigDecimal otMultiplier = isJobOrder ? OT_MULTIPLIER_JOB_ORDER : OT_MULTIPLIER_REGULAR;
        BigDecimal overtimePay = hourlyRate.multiply(totalOtHours).multiply(otMultiplier).setScale(SCALE, ROUND);

        // Adjustment Earnings (from EmployeeDeductions marked as earnings-type)
        YearMonth ym = YearMonth.of(year, month);
        BigDecimal adjustmentEarnings = BigDecimal.ZERO; // placeholder for future earnings adjustments

        // Total Earnings = Basic Pay + Overtime + Adjustment Earnings
        BigDecimal totalEarnings = basicPay.add(overtimePay).add(adjustmentEarnings).setScale(SCALE, ROUND);

        // --- 5. Compute Non-Statutory Deductions ---
        // Late/Undertime deduction = Total Late/Undertime Minutes × (Rounded) Per Minute Rate
        // Matches the spreadsheet behavior which rounds the per minute rate early (e.g. 2.19 instead of 2.1875)
        BigDecimal roundedPerMinuteRate = perMinuteRate.setScale(SCALE, ROUND);
        BigDecimal lateUndertimeDeduction = roundedPerMinuteRate
                .multiply(new BigDecimal(totalLateUndertimeMinutes))
                .setScale(SCALE, ROUND);

        // All employee deductions from EmployeeDeductions table
        BigDecimal adjustmentDeductions = computeEmployeeDeductions(empId, ym.atDay(1), ym.atEndOfMonth());

        // Total Deductions (non-statutory) = Employee Deductions + Late/Undertime
        BigDecimal totalNonStatutoryDeductions = adjustmentDeductions
                .add(lateUndertimeDeduction)
                .setScale(SCALE, ROUND);

        // Total Service Fee = Total Earnings - Total Non-Statutory Deductions
        BigDecimal serviceFee = totalEarnings.subtract(totalNonStatutoryDeductions).setScale(SCALE, ROUND);

        // --- 6. Compute Statutory Deductions ---
        // Determine premium base (salary splitting for below 30k)
        BigDecimal premiumBase = monthlyRate.compareTo(SALARY_THRESHOLD) < 0 ? PREMIUM_BASE_CAP : monthlyRate;

        BigDecimal sss;
        BigDecimal philhealth;
        BigDecimal pagibig;
        BigDecimal tax;

        if (isJobOrder) {
            // Job Order: no SSS, PhilHealth, Pag-IBIG
            sss = BigDecimal.ZERO;
            philhealth = BigDecimal.ZERO;
            pagibig = BigDecimal.ZERO;
            // Job Order tax = 5% EWT on total earnings
            tax = totalEarnings.multiply(JOB_ORDER_EWT_RATE).setScale(SCALE, ROUND);
        } else {
            // Regular: compute all statutory deductions
            sss = computeSss(premiumBase, year);
            philhealth = computePhilhealth(premiumBase);
            pagibig = computePagibig(premiumBase);
            // Simplified withholding tax: Monthly Rate × 10% - 2,395.90
            tax = computeWithholdingTax(monthlyRate);
        }

        // Semi-monthly contributions = (SSS + PhilHealth + HDMF + Tax) / 2
        BigDecimal statutoryTotal = sss.add(philhealth).add(pagibig).add(tax);
        BigDecimal semiMonthlyContributions = statutoryTotal.divide(SEMI_MONTHLY_DIVISOR, SCALE, ROUND);

        // --- 7. Net Pay ---
        // Net Pay = Service Fee - Semi-monthly Contributions
        BigDecimal netPay = serviceFee.subtract(semiMonthlyContributions).setScale(SCALE, ROUND);

        // --- 8. For backward compatibility, compute existing fields ---
        BigDecimal holidayPay = BigDecimal.ZERO; // placeholder
        BigDecimal allowances = BigDecimal.ZERO; // placeholder
        BigDecimal grossPay = totalEarnings; // totalEarnings is the new grossPay
        BigDecimal totalDeductions = totalNonStatutoryDeductions.add(semiMonthlyContributions).setScale(SCALE, ROUND);

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
        item.setAdjustmentDeductions(adjustmentDeductions.setScale(SCALE, ROUND));
        item.setOtherDeductions(totalNonStatutoryDeductions);

        // Service fee
        item.setServiceFee(serviceFee);

        // Statutory deductions
        item.setSss(sss);
        item.setPhilhealth(philhealth);
        item.setPagibig(pagibig);
        item.setTax(tax);
        item.setSemiMonthlyContributions(semiMonthlyContributions);

        // Totals
        item.setTotalDeductions(totalDeductions);
        item.setNetPay(netPay);

        // Employment type
        item.setEmploymentType(empType);

        List<PayrollItems> out = new ArrayList<>();
        out.add(item);
        return out;
    }

    /**
     * SSS: bracket lookup using premium base on the ssstable.
     */
    private BigDecimal computeSss(BigDecimal premiumBase, int year) {
        List<SssTable> rows = sssTableRepository.findByEffectiveYearOrderByRangeFromAsc(year);
        if (rows == null || rows.isEmpty()) return BigDecimal.ZERO;
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
     * PhilHealth: flat formula (premiumBase × 5%) / 2
     */
    private BigDecimal computePhilhealth(BigDecimal premiumBase) {
        return premiumBase.multiply(PHILHEALTH_RATE)
                .divide(PHILHEALTH_SPLIT, SCALE, ROUND);
    }

    /**
     * Pag-IBIG (HDMF): flat 2% of premium base, no cap.
     */
    private BigDecimal computePagibig(BigDecimal premiumBase) {
        return premiumBase.multiply(PAGIBIG_RATE).setScale(SCALE, ROUND);
    }

    /**
     * Withholding Tax (simplified formula): Monthly Rate × 10% - 2,395.90
     * Returns 0 if result is negative (salary below tax threshold).
     */
    private BigDecimal computeWithholdingTax(BigDecimal monthlyRate) {
        BigDecimal tax = monthlyRate.multiply(TAX_RATE_SIMPLIFIED)
                .subtract(TAX_CONSTANT)
                .setScale(SCALE, ROUND);
        return tax.max(BigDecimal.ZERO);
    }

    /**
     * Computes the total of ALL employee deductions for the given period.
     * No type filtering — every deduction assigned to an employee is included.
     */
    private BigDecimal computeEmployeeDeductions(Integer employeeId, LocalDate periodStart, LocalDate periodEnd) {
        List<EmployeeDeductions> list = employeeDeductionsRepository.findByEmployeeId(employeeId);
        if (list == null) return BigDecimal.ZERO;
        BigDecimal sum = BigDecimal.ZERO;
        for (EmployeeDeductions ed : list) {
            if (ed.getAmount() == null) continue;

            boolean recurring = Boolean.TRUE.equals(ed.getIsRecurring());
            if (recurring) {
                if (ed.getStartDate() != null && ed.getStartDate().isAfter(periodEnd)) continue;
                if (ed.getEndDate() != null && ed.getEndDate().isBefore(periodStart)) continue;
            } else {
                if (ed.getStartDate() == null || ed.getStartDate().isAfter(periodEnd) || ed.getStartDate().isBefore(periodStart)) continue;
            }
            sum = sum.add(ed.getAmount());
        }
        return sum.setScale(SCALE, ROUND);
    }

    /**
     * Returns a named breakdown of ALL employee deductions for the given period.
     * Each deduction is listed individually by its name from the Deductions catalog.
     */
    public List<DeductionBreakdownItem> getDeductionsBreakdown(Integer employeeId, LocalDate periodStart, LocalDate periodEnd) {
        List<EmployeeDeductions> list = employeeDeductionsRepository.findByEmployeeId(employeeId);
        List<DeductionBreakdownItem> result = new ArrayList<>();
        if (list == null) return result;
        for (EmployeeDeductions ed : list) {
            if (ed.getAmount() == null) continue;
            boolean recurring = Boolean.TRUE.equals(ed.getIsRecurring());
            if (recurring) {
                if (ed.getStartDate() != null && ed.getStartDate().isAfter(periodEnd)) continue;
                if (ed.getEndDate() != null && ed.getEndDate().isBefore(periodStart)) continue;
            } else {
                if (ed.getStartDate() == null || ed.getStartDate().isAfter(periodEnd) || ed.getStartDate().isBefore(periodStart)) continue;
            }
            String name = "Other";
            if (ed.getDeductionId() != null) {
                Optional<Deductions> d = deductionsRepository.findById(ed.getDeductionId());
                if (d.isPresent() && d.get().getDeductionName() != null) {
                    name = d.get().getDeductionName();
                }
            }

            DeductionBreakdownItem item = new DeductionBreakdownItem();
            item.setDeductionName(name);
            item.setAmount(ed.getAmount().setScale(SCALE, ROUND));
            result.add(item);
        }
        return result;
    }
}
