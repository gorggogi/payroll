package digital8.payroll.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import digital8.payroll.entities.Attendance;
import digital8.payroll.entities.Deductions;
import digital8.payroll.entities.EmployeeDeductions;
import digital8.payroll.entities.Employees;
import digital8.payroll.entities.PayrollItems;
import digital8.payroll.entities.SssTable;
import digital8.payroll.entities.TaxTable;
import digital8.payroll.repositories.AttendanceRepository;
import digital8.payroll.repositories.DeductionsRepository;
import digital8.payroll.repositories.EmployeeDeductionsRepository;
import digital8.payroll.repositories.EmployeeRepository;
import digital8.payroll.repositories.PagibigTableRepository;
import digital8.payroll.repositories.PhilhealthTableRepository;
import digital8.payroll.repositories.SssTableRepository;
import digital8.payroll.repositories.TaxTableRepository;

/**
 * Integration-style test using real data from Arpil10.sql.
 *
 * Employee 119 — "Charlene Dilig"
 *   Employment type : Job Order
 *   Basic salary    : 39,000 / mo
 *   Factor rate     : 20
 *
 * Semi-monthly period used: MARCH 2026
 *   semi_1 (Mar  1–15) : 76 worked hours, 0 late, 0 OT  → basic pay 18,525.00
 *   semi_2 (Mar 16–31) : 84 worked hours, 0 late, 0 OT  → basic pay 20,475.00
 *
 * Deduction: SSS Loan ₱1,000 with cutoff = SEMI_2
 *
 * Expected:
 *   semi_1 → adjustmentDeductions = 0.00     (SEMI_2 loan skipped)
 *   semi_2 → adjustmentDeductions = 1,000.00  (SEMI_2 loan applied)
 *
 * Statutory (same both periods):
 *   premiumBase = 39,000 (≥ 30k, no cap)
 *   SSS          = 1,750.00
 *   PhilHealth   = 39,000 × 5% / 2 = 975.00
 *   Pag-IBIG     = 39,000 × 2%     = 780.00
 *   SEMI_WHT     = semiBase 19,500 → bracket [16667–33332, 20%, 937.50]
 *               = 937.50 + 0.20 × (19,500 − 16,667) = 937.50 + 566.60 = 1,504.10
 *   Semi-monthly contributions = (1750 + 975 + 780 + 1504.10) / 2 = 2,504.55
 *
 * Net pay:
 *   semi_1 (no deduction) = 18,525.00 − 2,504.55 = 16,020.45
 *   semi_2 (with ₱1k)     = 20,475.00 − 1,000.00 − 2,504.55 = 16,970.45
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PayrollCutoffPayslipTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private AttendanceRepository attendanceRepository;
    @Mock private SssTableRepository sssTableRepository;
    @Mock private TaxTableRepository taxTableRepository;
    @Mock private PhilhealthTableRepository philhealthTableRepository;
    @Mock private PagibigTableRepository pagibigTableRepository;
    @Mock private EmployeeDeductionsRepository employeeDeductionsRepository;
    @Mock private DeductionsRepository deductionsRepository;
    @Mock private HolidayCalendarService holidayCalendarService;

    @InjectMocks
    private PayrollService payrollService;

    private static final int EMP_ID = 119;

    // ── Attendance records from the SQL dump (employee 119, March 2026) ─────
    private static final Object[][] ATT_DATA = {
        // { date, workHours, lateMin, undertimeMin, otHours }
        { LocalDate.of(2026, 3,  2), 8.00, 0, 0, 0.00 },
        { LocalDate.of(2026, 3,  3), 8.00, 0, 0, 0.00 },
        { LocalDate.of(2026, 3,  4), 8.00, 0, 0, 0.00 },
        { LocalDate.of(2026, 3,  5), 8.00, 0, 0, 0.00 },
        { LocalDate.of(2026, 3,  6), 8.00, 0, 0, 0.00 },
        { LocalDate.of(2026, 3,  9), 8.00, 0, 0, 0.00 },
        { LocalDate.of(2026, 3, 10), 8.00, 0, 0, 0.00 },
        { LocalDate.of(2026, 3, 11), 8.00, 0, 0, 0.00 },
        { LocalDate.of(2026, 3, 12), 8.00, 0, 0, 0.00 },
        { LocalDate.of(2026, 3, 13), 4.00, 0, 0, 0.00 }, // half-day
        { LocalDate.of(2026, 3, 16), 8.00, 0, 0, 0.00 },
        { LocalDate.of(2026, 3, 17), 8.00, 0, 0, 0.00 },
        { LocalDate.of(2026, 3, 18), 8.00, 0, 0, 0.00 },
        { LocalDate.of(2026, 3, 19), 8.00, 0, 0, 0.00 },
        { LocalDate.of(2026, 3, 20), 8.00, 0, 0, 0.00 },
        { LocalDate.of(2026, 3, 23), 8.00, 0, 0, 0.00 },
        { LocalDate.of(2026, 3, 24), 8.00, 0, 0, 0.00 },
        { LocalDate.of(2026, 3, 25), 8.00, 0, 0, 0.00 },
        { LocalDate.of(2026, 3, 26), 8.00, 0, 0, 0.00 },
        { LocalDate.of(2026, 3, 27), 8.00, 0, 0, 0.00 },
        { LocalDate.of(2026, 3, 30), 4.00, 0, 0, 0.00 }, // half-day
    };

    @BeforeEach
    void setUp() {
        // Employee 119
        Employees emp = new Employees();
        emp.setEmploymentType("Job Order");
        emp.setBasicSalary(new BigDecimal("39000"));
        when(employeeRepository.findById(EMP_ID)).thenReturn(Optional.of(emp));

        // All attendance
        List<Attendance> attList = new ArrayList<>();
        for (Object[] row : ATT_DATA) {
            Attendance a = new Attendance();
            a.setAttendance_date((LocalDate) row[0]);
            a.setWork_hours(BigDecimal.valueOf((double) row[1]));
            a.setLate_minutes((int) row[2]);
            a.setUndertime_minutes((int) row[3]);
            a.setOvertime_hours(BigDecimal.valueOf((double) row[4]));
            attList.add(a);
        }
        when(attendanceRepository.findByEmployeeIdOrderByDateDesc(EMP_ID)).thenReturn(attList);

        // SSS table — 2026 top bracket covers 39,000 → employee share 1,750
        SssTable sssRow = new SssTable();
        sssRow.setRangeFrom(new BigDecimal("34750.00"));
        sssRow.setRangeTo(new BigDecimal("999999.99"));
        sssRow.setEmployeeShare(new BigDecimal("1750.00"));
        when(sssTableRepository.findByEffectiveYearOrderByRangeFromAsc(anyInt()))
                .thenReturn(List.of(sssRow));

        // Tax table — SEMI_MONTHLY brackets for 2026 (from Arpil10.sql rows 13–18)
        when(taxTableRepository.findByEffectiveYearAndPayFrequencyOrderByCompensationFromAsc(
                anyInt(), eq("SEMI_MONTHLY")))
                .thenReturn(semiMonthlyTaxTable2026());

        // Holidays → none (keeps the math simple)
        when(holidayCalendarService.activeHolidaysInRange(any(), any()))
                .thenReturn(List.of());

        // Deduction definition
        Deductions sssLoanDef = new Deductions();
        sssLoanDef.setDeductionName("SSS Loan");
        sssLoanDef.setDeductionType("Loan");
        when(deductionsRepository.findById(anyInt())).thenReturn(Optional.of(sssLoanDef));
    }

    // ── Tests ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("semi_1: SEMI_2 loan NOT deducted → adjustmentDeductions=0, net=16,020.45")
    void semi1_semi2Loan_isNotApplied() {
        stubDeductionWithCutoff("SEMI_2");

        List<PayrollItems> result =
                payrollService.computePayroll(EMP_ID, "semi_1", "MARCH", 2026);

        assertEquals(1, result.size());
        PayrollItems item = result.get(0);

        assertBD("18525.00", item.getBasicPay(),        "basicPay");
        assertBD("0.00",     item.getAdjustmentDeductions(), "adjustmentDeductions");
        assertBD("2504.55",  item.getSemiMonthlyContributions(), "semiMonthlyContributions");
        assertBD("16020.45", item.getNetPay(),           "netPay");
    }

    @Test
    @DisplayName("semi_2: SEMI_2 loan IS deducted → adjustmentDeductions=1000, net=16,970.45")
    void semi2_semi2Loan_isApplied() {
        stubDeductionWithCutoff("SEMI_2");

        List<PayrollItems> result =
                payrollService.computePayroll(EMP_ID, "semi_2", "MARCH", 2026);

        assertEquals(1, result.size());
        PayrollItems item = result.get(0);

        assertBD("20475.00", item.getBasicPay(),         "basicPay");
        assertBD("1000.00",  item.getAdjustmentDeductions(), "adjustmentDeductions");
        assertBD("2504.55",  item.getSemiMonthlyContributions(), "semiMonthlyContributions");
        assertBD("16970.45", item.getNetPay(),            "netPay");
    }

    @Test
    @DisplayName("semi_1: BOTH cutoff loan IS deducted → adjustmentDeductions=1000")
    void semi1_bothCutoffLoan_isApplied() {
        stubDeductionWithCutoff("BOTH");

        List<PayrollItems> result =
                payrollService.computePayroll(EMP_ID, "semi_1", "MARCH", 2026);

        assertEquals(1, result.size());
        assertBD("1000.00", result.get(0).getAdjustmentDeductions(), "adjustmentDeductions");
    }

    @Test
    @DisplayName("semi_2: BOTH cutoff loan IS deducted → adjustmentDeductions=1000")
    void semi2_bothCutoffLoan_isApplied() {
        stubDeductionWithCutoff("BOTH");

        List<PayrollItems> result =
                payrollService.computePayroll(EMP_ID, "semi_2", "MARCH", 2026);

        assertEquals(1, result.size());
        assertBD("1000.00", result.get(0).getAdjustmentDeductions(), "adjustmentDeductions");
    }

    @Test
    @DisplayName("semi_2: SEMI_1 loan NOT deducted → adjustmentDeductions=0")
    void semi2_semi1Loan_isNotApplied() {
        stubDeductionWithCutoff("SEMI_1");

        List<PayrollItems> result =
                payrollService.computePayroll(EMP_ID, "semi_2", "MARCH", 2026);

        assertEquals(1, result.size());
        assertBD("0.00", result.get(0).getAdjustmentDeductions(), "adjustmentDeductions");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void stubDeductionWithCutoff(String cutoff) {
        EmployeeDeductions ed = new EmployeeDeductions();
        ed.setDeductionId(101);
        ed.setAmount(new BigDecimal("1000.00"));
        ed.setIsRecurring(true);
        ed.setStartDate(LocalDate.of(2026, 3, 1));
        ed.setEndDate(LocalDate.of(2026, 3, 31));
        ed.setDeductionCutoff(cutoff);
        when(employeeDeductionsRepository.findByEmployeeId(EMP_ID)).thenReturn(List.of(ed));
    }

    private void assertBD(String expected, BigDecimal actual, String label) {
        assertEquals(0,
            new BigDecimal(expected).compareTo(actual),
            label + ": expected " + expected + " but got " + actual);
    }

    /**
     * SEMI_MONTHLY tax brackets from the SQL dump (2026, rows 13–18).
     * semiBase for emp 119 = 39000/2 = 19,500 → falls in bracket 15.
     * SEMI_WHT = 937.50 + 0.20 * (19500 - 16667) = 937.50 + 566.60 = 1,504.10
     */
    private List<TaxTable> semiMonthlyTaxTable2026() {
        return List.of(
            taxRow(1.00,       10416.99, 0.00, 0.00),
            taxRow(10417.00,   16666.99, 0.15, 0.00),
            taxRow(16667.00,   33332.99, 0.20, 937.50),
            taxRow(33333.00,   83332.99, 0.25, 4270.70),
            taxRow(83333.00,  333332.99, 0.30, 16770.70),
            taxRow(333333.00, 9999999.99,0.35, 91770.70)
        );
    }

    private TaxTable taxRow(double from, double to, double rate, double additional) {
        TaxTable t = new TaxTable();
        t.setCompensationFrom(BigDecimal.valueOf(from));
        t.setCompensationTo(BigDecimal.valueOf(to));
        t.setTaxRate(BigDecimal.valueOf(rate));
        t.setAdditionalTax(BigDecimal.valueOf(additional));
        t.setPayFrequency("SEMI_MONTHLY");
        return t;
    }
}
