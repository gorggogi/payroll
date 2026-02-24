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
import digital8.payroll.entities.PhilhealthTable;
import digital8.payroll.entities.TaxTable;
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
public class PayrollService { // used by payrollController and payrollViewController

    private static final int SCALE = 2;
    private static final RoundingMode ROUND = RoundingMode.HALF_UP;
    private static final BigDecimal HOURS_PER_MONTH = new BigDecimal("208");
    private static final BigDecimal OVERTIME_MULTIPLIER = new BigDecimal("1.5");
    private static final BigDecimal PAGIBIG_RATE = new BigDecimal("0.02");
    private static final BigDecimal PAGIBIG_CAP_MONTHLY = new BigDecimal("100");

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
        Optional<Employees> empOpt = employeeRepository.findById(empId);
        if (empOpt.isEmpty()) return new ArrayList<>();

        Employees emp = empOpt.get();
        BigDecimal monthlySalary = emp.getBasicSalary() != null ? emp.getBasicSalary() : BigDecimal.ZERO;

        String effectivePeriod = (period != null && !period.isBlank()) ? period : (emp.getPayType() != null ? emp.getPayType() : "monthly");
        boolean isBiweekly = "biweekly".equalsIgnoreCase(effectivePeriod);
        BigDecimal basicPay = isBiweekly ? monthlySalary.divide(BigDecimal.valueOf(2), SCALE, ROUND) : monthlySalary.setScale(SCALE, ROUND);

        Month month = null;
        if (monthName != null && !monthName.isBlank()) {
            try {
                month = Month.valueOf(monthName.toUpperCase());
            } catch (Exception e) {
                month = null;
            }
        }
        int year = java.time.LocalDate.now().getYear();
        if (month == null) {
            month = java.time.LocalDate.now().getMonth();
        }

        List<Attendance> records = attendanceRepository.findByEmployeeIdOrderByDateDesc(empId);
        BigDecimal totalOvertime = BigDecimal.ZERO;
        for (Attendance a : records) {
            if (a.getAttendance_date() != null && a.getAttendance_date().getMonth() != month) continue;
            if (a.getOvertime_hours() != null) totalOvertime = totalOvertime.add(a.getOvertime_hours());
        }

        BigDecimal hourlyRate = HOURS_PER_MONTH.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : monthlySalary.divide(HOURS_PER_MONTH, 6, ROUND);
        BigDecimal overtimePay = totalOvertime.multiply(hourlyRate).multiply(OVERTIME_MULTIPLIER).setScale(SCALE, ROUND);
        BigDecimal holidayPay = BigDecimal.ZERO;
        BigDecimal allowances = BigDecimal.ZERO;
        BigDecimal grossPay = basicPay.add(overtimePay).add(holidayPay).add(allowances).setScale(SCALE, ROUND);

        BigDecimal monthlyForBracket = monthlySalary;
        BigDecimal sss = computeSss(monthlyForBracket, year);
        BigDecimal philhealth = computePhilhealth(monthlyForBracket, year);
        BigDecimal pagibig = computePagibig(basicPay, isBiweekly);
        BigDecimal taxable = grossPay.subtract(sss).subtract(philhealth).subtract(pagibig).max(BigDecimal.ZERO);
        BigDecimal tax = computeTax(taxable, year);

        YearMonth ym = YearMonth.of(year, month);
        BigDecimal otherDeductions = computeOtherDeductions(empId, ym.atDay(1), ym.atEndOfMonth());

        BigDecimal totalDeductions = sss.add(philhealth).add(pagibig).add(tax).add(otherDeductions).setScale(SCALE, ROUND);
        BigDecimal netPay = grossPay.subtract(totalDeductions).setScale(SCALE, ROUND);

        PayrollItems item = new PayrollItems();
        item.setEmployeeId(empId);
        item.setBasicPay(basicPay);
        item.setOvertimePay(overtimePay);
        item.setHolidayPay(holidayPay);
        item.setAllowances(allowances);
        item.setGrossPay(grossPay);
        item.setSss(sss);
        item.setPhilhealth(philhealth);
        item.setPagibig(pagibig);
        item.setTax(tax);
        item.setOtherDeductions(otherDeductions);
        item.setTotalDeductions(totalDeductions);
        item.setNetPay(netPay);

        List<PayrollItems> out = new ArrayList<>();
        out.add(item);
        return out;
    }

    private BigDecimal computeSss(BigDecimal monthlySalary, int year) {
        List<SssTable> rows = sssTableRepository.findByEffectiveYearOrderByRangeFromAsc(year);
        if (rows == null || rows.isEmpty()) return BigDecimal.ZERO;
        for (SssTable row : rows) {
            if (row.getRangeFrom() != null && row.getRangeTo() != null && row.getEmployeeShare() != null) {
                if (monthlySalary.compareTo(row.getRangeFrom()) >= 0 && monthlySalary.compareTo(row.getRangeTo()) <= 0) {
                    return row.getEmployeeShare().setScale(SCALE, ROUND);
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal computePhilhealth(BigDecimal monthlySalary, int year) {
        List<PhilhealthTable> rows = philhealthTableRepository.findByEffectiveYearOrderByRangeFromAsc(year);
        if (rows == null || rows.isEmpty()) return BigDecimal.ZERO;
        for (PhilhealthTable row : rows) {
            if (row.getRangeFrom() != null && row.getRangeTo() != null && row.getEmployeeShare() != null) {
                if (monthlySalary.compareTo(row.getRangeFrom()) >= 0 && monthlySalary.compareTo(row.getRangeTo()) <= 0) {
                    return row.getEmployeeShare().setScale(SCALE, ROUND);
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal computePagibig(BigDecimal basicPay, boolean isBiweekly) {
        BigDecimal cap = isBiweekly ? PAGIBIG_CAP_MONTHLY.divide(BigDecimal.valueOf(2), SCALE, ROUND) : PAGIBIG_CAP_MONTHLY;
        BigDecimal contribution = basicPay.multiply(PAGIBIG_RATE).setScale(SCALE, ROUND);
        return contribution.min(cap);
    }

    private BigDecimal computeTax(BigDecimal taxableIncome, int year) {
        List<TaxTable> rows = taxTableRepository.findByEffectiveYearOrderByCompensationFromAsc(year);
        if (rows == null || rows.isEmpty()) return BigDecimal.ZERO;
        for (TaxTable row : rows) {
            if (row.getCompensationFrom() == null || row.getCompensationTo() == null) continue;
            if (taxableIncome.compareTo(row.getCompensationFrom()) >= 0 && taxableIncome.compareTo(row.getCompensationTo()) <= 0) {
                BigDecimal excess = taxableIncome.subtract(row.getCompensationFrom());
                BigDecimal taxOnExcess = (row.getTaxRate() != null ? excess.multiply(row.getTaxRate()) : BigDecimal.ZERO).setScale(SCALE, ROUND);
                BigDecimal base = row.getAdditionalTax() != null ? row.getAdditionalTax() : BigDecimal.ZERO;
                return base.add(taxOnExcess).setScale(SCALE, ROUND);
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal computeOtherDeductions(Integer employeeId, LocalDate periodStart, LocalDate periodEnd) {
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

    public List<DeductionBreakdownItem> getOtherDeductionsBreakdown(Integer employeeId, LocalDate periodStart, LocalDate periodEnd) {
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
                if (d.isPresent() && d.get().getDeductionName() != null) name = d.get().getDeductionName();
            }
            DeductionBreakdownItem item = new DeductionBreakdownItem();
            item.setDeductionName(name);
            item.setAmount(ed.getAmount().setScale(SCALE, ROUND));
            result.add(item);
        }
        return result;
    }
}
