package digital8.payroll.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import digital8.payroll.dto.DeductionBreakdownItem;
import digital8.payroll.entities.Adjustments;
import digital8.payroll.entities.Deductions;
import digital8.payroll.entities.EmployeeAdjustments;
import digital8.payroll.entities.EmployeeDeductions;
import digital8.payroll.repositories.AdjustmentsRepository;
import digital8.payroll.repositories.AttendanceRepository;
import digital8.payroll.repositories.DeductionsRepository;
import digital8.payroll.repositories.EmployeeAdjustmentsRepository;
import digital8.payroll.repositories.EmployeeDeductionsRepository;
import digital8.payroll.repositories.EmployeeRepository;
import digital8.payroll.repositories.PagibigTableRepository;
import digital8.payroll.repositories.PhilhealthTableRepository;
import digital8.payroll.repositories.SssTableRepository;
import digital8.payroll.repositories.TaxTableRepository;

/**
 * Targeted tests for applyOnCutoff / deductionCutoff filtering in PayrollService.
 *
 * Semi-monthly periods used in these tests:
 *   semi_1 → Jan 1–15, 2024
 *   semi_2 → Jan 16–31, 2024
 *
 * A recurring deduction whose active window covers the full month is used throughout,
 * so the only variables are deductionCutoff and period.
 *
 * The test-accessor subclass exposes private methods for direct testing without reflection.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeductionCutoffTest {

    // --- All repos must be declared so @InjectMocks wires PayrollService ---
    @Mock private EmployeeRepository employeeRepository;
    @Mock private AttendanceRepository attendanceRepository;
    @Mock private SssTableRepository sssTableRepository;
    @Mock private TaxTableRepository taxTableRepository;
    @Mock private PhilhealthTableRepository philhealthTableRepository;
    @Mock private PagibigTableRepository pagibigTableRepository;
    @Mock private EmployeeDeductionsRepository employeeDeductionsRepository;
    @Mock private EmployeeAdjustmentsRepository employeeAdjustmentsRepository;
    @Mock private AdjustmentsRepository adjustmentsRepository;
    @Mock private DeductionsRepository deductionsRepository;
    @Mock private HolidayCalendarService holidayCalendarService;

    @InjectMocks
    private TestPayrollService payrollService;

    // Test-accessor that exposes private methods for testing
    static class TestPayrollService extends PayrollService {
        BigDecimal testComputeEmployeeDeductions(Integer empId, LocalDate start, LocalDate end, String period) {
            return computeEmployeeDeductions(empId, start, end, period);
        }
        BigDecimal[] testComputeAdjustmentsSplit(Integer empId, LocalDate start, LocalDate end, String period) {
            return computeAdjustmentsSplit(empId, start, end, period);
        }
    }

    // Period boundaries
    private static final LocalDate SEMI_1_START = LocalDate.of(2024, 1, 1);
    private static final LocalDate SEMI_1_END   = LocalDate.of(2024, 1, 15);
    private static final LocalDate SEMI_2_START = LocalDate.of(2024, 1, 16);
    private static final LocalDate SEMI_2_END   = LocalDate.of(2024, 1, 31);
    private static final LocalDate MONTH_START   = LocalDate.of(2024, 1, 1);
    private static final LocalDate MONTH_END    = LocalDate.of(2024, 1, 31);

    // Deduction covers the whole month — only cutoff determines inclusion
    private static final LocalDate DEDUCTION_START = LocalDate.of(2024, 1, 1);
    private static final LocalDate DEDUCTION_END   = LocalDate.of(2024, 1, 31);

    private static final int EMP_ID = 99;
    private static final int DEDUCTION_DEF_ID = 200;
    private static final int ADJUSTMENT_DEF_ID = 300;
    private static final BigDecimal AMOUNT = new BigDecimal("1500.00");
    private static final BigDecimal ADJ_AMOUNT = new BigDecimal("500.00");

    @BeforeEach
    void stubDeductionsAndAdjustments() {
        Deductions def = new Deductions();
        def.setDeductionName("SSS Loan");
        def.setDeductionType("Loan");
        when(deductionsRepository.findById(DEDUCTION_DEF_ID)).thenReturn(Optional.of(def));

        Adjustments adjDef = new Adjustments();
        adjDef.setAdjustmentName("Transport Allowance");
        adjDef.setAdjustmentType("Earnings");
        when(adjustmentsRepository.findById(ADJUSTMENT_DEF_ID)).thenReturn(Optional.of(adjDef));
    }

    // ------------------------------------------------------------------ helpers

    private EmployeeDeductions makeRecurringDeduction(String cutoff) {
        EmployeeDeductions ed = new EmployeeDeductions();
        ed.setEmployeeId(EMP_ID);
        ed.setDeductionId(DEDUCTION_DEF_ID);
        ed.setAmount(AMOUNT);
        ed.setIsRecurring(true);
        ed.setStartDate(DEDUCTION_START);
        ed.setEndDate(DEDUCTION_END);
        ed.setDeductionCutoff(cutoff);
        return ed;
    }

    private EmployeeAdjustments makeRecurringAdjustment(String cutoff, String adjType) {
        EmployeeAdjustments ea = new EmployeeAdjustments();
        ea.setEmployeeId(EMP_ID);
        ea.setAdjustmentId(ADJUSTMENT_DEF_ID);
        ea.setAmount(ADJ_AMOUNT);
        ea.setIsRecurring(true);
        ea.setStartDate(DEDUCTION_START);
        ea.setEndDate(DEDUCTION_END);
        ea.setApplyOnCutoff(cutoff);
        return ea;
    }

    private EmployeeAdjustments makeRecurringAdjustmentDeductionType(String cutoff) {
        EmployeeAdjustments ea = new EmployeeAdjustments();
        ea.setEmployeeId(EMP_ID);
        ea.setAdjustmentId(ADJUSTMENT_DEF_ID);
        ea.setAmount(ADJ_AMOUNT);
        ea.setIsRecurring(true);
        ea.setStartDate(DEDUCTION_START);
        ea.setEndDate(DEDUCTION_END);
        ea.setApplyOnCutoff(cutoff);
        Adjustments adjDef = new Adjustments();
        adjDef.setAdjustmentName("Other Deduction");
        adjDef.setAdjustmentType("Deduction");
        when(adjustmentsRepository.findById(ADJUSTMENT_DEF_ID)).thenReturn(Optional.of(adjDef));
        return ea;
    }

    // ------------------------------------------------------------------
    // SECTION 1: computeEmployeeDeductions tests (via test accessor)
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("computeEmployeeDeductions — cutoff filtering")
    class ComputeDeductionTests {

        @Test
        @DisplayName("SEMI_2 deduction included on semi_2 period")
        void semi2Deduction_onSemi2() {
            List<EmployeeDeductions> list = new ArrayList<>();
            list.add(makeRecurringDeduction("SEMI_2"));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID)).thenReturn(list);

            BigDecimal result = payrollService.testComputeEmployeeDeductions(
                EMP_ID, SEMI_2_START, SEMI_2_END, "semi_2");

            assertEquals(AMOUNT, result);
        }

        @Test
        @DisplayName("SEMI_2 deduction skipped on semi_1 period")
        void semi2Deduction_skippedOnSemi1() {
            List<EmployeeDeductions> list = new ArrayList<>();
            list.add(makeRecurringDeduction("SEMI_2"));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID)).thenReturn(list);

            BigDecimal result = payrollService.testComputeEmployeeDeductions(
                EMP_ID, SEMI_1_START, SEMI_1_END, "semi_1");

            assertEquals(0, BigDecimal.ZERO.compareTo(result),
                "Deduction should be skipped on this period");
        }

        @Test
        @DisplayName("SEMI_1 deduction included on semi_1 period")
        void semi1Deduction_onSemi1() {
            List<EmployeeDeductions> list = new ArrayList<>();
            list.add(makeRecurringDeduction("SEMI_1"));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID)).thenReturn(list);

            BigDecimal result = payrollService.testComputeEmployeeDeductions(
                EMP_ID, SEMI_1_START, SEMI_1_END, "semi_1");

            assertEquals(AMOUNT, result);
        }

        @Test
        @DisplayName("SEMI_1 deduction skipped on semi_2 period")
        void semi1Deduction_skippedOnSemi2() {
            List<EmployeeDeductions> list = new ArrayList<>();
            list.add(makeRecurringDeduction("SEMI_1"));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID)).thenReturn(list);

            BigDecimal result = payrollService.testComputeEmployeeDeductions(
                EMP_ID, SEMI_2_START, SEMI_2_END, "semi_2");

            assertEquals(0, BigDecimal.ZERO.compareTo(result),
                "Deduction should be skipped on this period");
        }

        @Test
        @DisplayName("BOTH cutoff deduction included on both semi_1 and semi_2")
        void bothCutoff_includedBothPeriods() {
            List<EmployeeDeductions> list = new ArrayList<>();
            list.add(makeRecurringDeduction("BOTH"));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID)).thenReturn(list);

            BigDecimal semi1 = payrollService.testComputeEmployeeDeductions(
                EMP_ID, SEMI_1_START, SEMI_1_END, "semi_1");
            BigDecimal semi2 = payrollService.testComputeEmployeeDeductions(
                EMP_ID, SEMI_2_START, SEMI_2_END, "semi_2");

            assertEquals(AMOUNT, semi1);
            assertEquals(AMOUNT, semi2);
        }

        @Test
        @DisplayName("Monthly period includes all cutoffs — SEMI_2 deduction appears on monthly")
        void monthlyIncludesAllCutoffs() {
            List<EmployeeDeductions> list = new ArrayList<>();
            list.add(makeRecurringDeduction("SEMI_2"));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID)).thenReturn(list);

            BigDecimal result = payrollService.testComputeEmployeeDeductions(
                EMP_ID, MONTH_START, MONTH_END, "monthly");

            assertEquals(AMOUNT, result, "Monthly payroll should include all active deductions regardless of cutoff");
        }

        @Test
        @DisplayName("Uppercase period SEMI_1 still filters SEMI_2 deduction")
        void uppercasePeriodFiltered() {
            List<EmployeeDeductions> list = new ArrayList<>();
            list.add(makeRecurringDeduction("SEMI_2"));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID)).thenReturn(list);

            BigDecimal result = payrollService.testComputeEmployeeDeductions(
                EMP_ID, SEMI_1_START, SEMI_1_END, "SEMI_1");

            assertEquals(0, BigDecimal.ZERO.compareTo(result),
                "Uppercase period must still be correctly filtered");
        }

        @Test
        @DisplayName("Null period falls back to monthly — all cutoffs included")
        void nullPeriodIncludesAllCutoffs() {
            List<EmployeeDeductions> list = new ArrayList<>();
            list.add(makeRecurringDeduction("SEMI_2"));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID)).thenReturn(list);

            BigDecimal result = payrollService.testComputeEmployeeDeductions(
                EMP_ID, MONTH_START, MONTH_END, null);

            assertEquals(AMOUNT, result,
                "Null period should fall back to monthly behavior — all active deductions included");
        }

        @Test
        @DisplayName("One-time deduction not affected by cutoff — included on semi_1 if start date matches")
        void oneTimeDeduction_ignoresCutoff() {
            EmployeeDeductions oneTime = new EmployeeDeductions();
            oneTime.setEmployeeId(EMP_ID);
            oneTime.setDeductionId(DEDUCTION_DEF_ID);
            oneTime.setAmount(AMOUNT);
            oneTime.setIsRecurring(false);
            oneTime.setStartDate(LocalDate.of(2024, 1, 10)); // within semi_1
            oneTime.setDeductionCutoff("SEMI_2"); // set to SEMI_2 but is one-time

            List<EmployeeDeductions> list = new ArrayList<>();
            list.add(oneTime);
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID)).thenReturn(list);

            BigDecimal result = payrollService.testComputeEmployeeDeductions(
                EMP_ID, SEMI_1_START, SEMI_1_END, "semi_1");

            assertEquals(AMOUNT, result,
                "One-time deductions should be filtered by start date only, not by cutoff");
        }
    }

    // ------------------------------------------------------------------
    // SECTION 2: computeAdjustmentsSplit tests
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("computeAdjustmentsSplit — cutoff filtering")
    class ComputeAdjustmentsTests {

        @Test
        @DisplayName("BOTH adjustment (earnings) applied on both semi_1 and semi_2")
        void bothAdjustment_appliedBothCutoffs() {
            List<EmployeeAdjustments> list = new ArrayList<>();
            list.add(makeRecurringAdjustment("BOTH", "Earnings"));
            when(employeeAdjustmentsRepository.findByEmployeeId(EMP_ID)).thenReturn(list);

            BigDecimal[] semi1 = payrollService.testComputeAdjustmentsSplit(
                EMP_ID, SEMI_1_START, SEMI_1_END, "semi_1");
            BigDecimal[] semi2 = payrollService.testComputeAdjustmentsSplit(
                EMP_ID, SEMI_2_START, SEMI_2_END, "semi_2");

            assertEquals(0, ADJ_AMOUNT.compareTo(semi1[0]), "Earnings should appear on semi_1");
            assertEquals(0, BigDecimal.ZERO.compareTo(semi1[1]), "No deduction adjustment on semi_1");
            assertEquals(0, ADJ_AMOUNT.compareTo(semi2[0]), "Earnings should appear on semi_2");
            assertEquals(0, BigDecimal.ZERO.compareTo(semi2[1]), "No deduction adjustment on semi_2");
        }

        @Test
        @DisplayName("SEMI_1 adjustment (earnings) skipped on semi_2 period")
        void semi1Adjustment_skippedOnSemi2() {
            List<EmployeeAdjustments> list = new ArrayList<>();
            list.add(makeRecurringAdjustment("SEMI_1", "Earnings"));
            when(employeeAdjustmentsRepository.findByEmployeeId(EMP_ID)).thenReturn(list);

            BigDecimal[] semi2 = payrollService.testComputeAdjustmentsSplit(
                EMP_ID, SEMI_2_START, SEMI_2_END, "semi_2");

            assertEquals(0, BigDecimal.ZERO.compareTo(semi2[0]),
                "SEMI_1 adjustment should not appear on semi_2");
            assertEquals(0, BigDecimal.ZERO.compareTo(semi2[1]));
        }

        @Test
        @DisplayName("Adjustment with cutoff Deduction applied to deductions sum on matching period")
        void adjustmentDeduction_appliedOnMatchingCutoff() {
            List<EmployeeAdjustments> list = new ArrayList<>();
            list.add(makeRecurringAdjustmentDeductionType("SEMI_2"));
            when(employeeAdjustmentsRepository.findByEmployeeId(EMP_ID)).thenReturn(list);

            BigDecimal[] semi2 = payrollService.testComputeAdjustmentsSplit(
                EMP_ID, SEMI_2_START, SEMI_2_END, "semi_2");

            assertEquals(0, BigDecimal.ZERO.compareTo(semi2[0]));
            assertEquals(0, ADJ_AMOUNT.compareTo(semi2[1]),
                "Deduction-type adjustment should reduce earnings");
        }

        @Test
        @DisplayName("Adjustment Deduction skipped on non-matching period")
        void adjustmentDeduction_skippedOnNonMatchingCutoff() {
            List<EmployeeAdjustments> list = new ArrayList<>();
            list.add(makeRecurringAdjustment("SEMI_1", "Deduction"));
            when(employeeAdjustmentsRepository.findByEmployeeId(EMP_ID)).thenReturn(list);

            BigDecimal[] semi2 = payrollService.testComputeAdjustmentsSplit(
                EMP_ID, SEMI_2_START, SEMI_2_END, "semi_2");

            assertEquals(0, BigDecimal.ZERO.compareTo(semi2[0]));
            assertEquals(0, BigDecimal.ZERO.compareTo(semi2[1]),
                "SEMI_1 deduction adjustment should not appear on semi_2");
        }

        @Test
        @DisplayName("Uppercase period SEMI_2 correctly filters SEMI_1 adjustment")
        void uppercasePeriodAdjustments() {
            List<EmployeeAdjustments> list = new ArrayList<>();
            list.add(makeRecurringAdjustment("SEMI_1", "Earnings"));
            when(employeeAdjustmentsRepository.findByEmployeeId(EMP_ID)).thenReturn(list);

            BigDecimal[] result = payrollService.testComputeAdjustmentsSplit(
                EMP_ID, SEMI_2_START, SEMI_2_END, "SEMI_2");

            assertEquals(0, BigDecimal.ZERO.compareTo(result[0]),
                "Uppercase SEMI_2 must still filter out SEMI_1 adjustment");
        }

        @Test
        @DisplayName("Monthly period includes all adjustment cutoffs")
        void monthlyIncludesAllAdjustmentCutoffs() {
            List<EmployeeAdjustments> list = new ArrayList<>();
            list.add(makeRecurringAdjustment("SEMI_2", "Earnings"));
            when(employeeAdjustmentsRepository.findByEmployeeId(EMP_ID)).thenReturn(list);

            BigDecimal[] result = payrollService.testComputeAdjustmentsSplit(
                EMP_ID, MONTH_START, MONTH_END, "monthly");

            assertEquals(ADJ_AMOUNT, result[0],
                "Monthly payroll should include all active adjustments regardless of cutoff");
        }

        @Test
        @DisplayName("Null period includes all adjustment cutoffs (monthly fallback)")
        void nullPeriodIncludesAllAdjustments() {
            List<EmployeeAdjustments> list = new ArrayList<>();
            list.add(makeRecurringAdjustment("SEMI_2", "Earnings"));
            when(employeeAdjustmentsRepository.findByEmployeeId(EMP_ID)).thenReturn(list);

            BigDecimal[] result = payrollService.testComputeAdjustmentsSplit(
                EMP_ID, MONTH_START, MONTH_END, null);

            assertEquals(ADJ_AMOUNT, result[0],
                "Null period should fall back to monthly — all active adjustments included");
        }
    }

    // ------------------------------------------------------------------
    // SECTION 3: getDeductionsBreakdown — existing tests (kept for regression)
    // ------------------------------------------------------------------

    private List<DeductionBreakdownItem> breakdown(LocalDate start, LocalDate end, String period, String cutoff) {
        List<EmployeeDeductions> list = new ArrayList<>();
        list.add(makeRecurringDeduction(cutoff));
        when(employeeDeductionsRepository.findByEmployeeId(EMP_ID)).thenReturn(list);
        return payrollService.getDeductionsBreakdown(EMP_ID, start, end, period);
    }

    @Nested
    @DisplayName("getDeductionsBreakdown — regression tests")
    class BreakdownTests {

        @Test
        @DisplayName("SEMI_2 deduction included on semi_2 period")
        void semi2Cutoff_onSemi2Period_isIncluded() {
            List<DeductionBreakdownItem> result = breakdown(SEMI_2_START, SEMI_2_END, "semi_2", "SEMI_2");
            assertEquals(1, result.size());
            assertEquals(AMOUNT, result.get(0).getAmount());
        }

        @Test
        @DisplayName("SEMI_2 deduction skipped on semi_1 period")
        void semi2Cutoff_onSemi1Period_isSkipped() {
            List<DeductionBreakdownItem> result = breakdown(SEMI_1_START, SEMI_1_END, "semi_1", "SEMI_2");
            assertEquals(0, result.size());
        }

        @Test
        @DisplayName("BOTH deduction included on semi_1 period")
        void bothCutoff_onSemi1Period_isIncluded() {
            List<DeductionBreakdownItem> result = breakdown(SEMI_1_START, SEMI_1_END, "semi_1", "BOTH");
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("BOTH deduction included on semi_2 period")
        void bothCutoff_onSemi2Period_isIncluded() {
            List<DeductionBreakdownItem> result = breakdown(SEMI_2_START, SEMI_2_END, "semi_2", "BOTH");
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("null cutoff defaults to SEMI_2 — included on semi_2")
        void nullCutoff_defaultsToSemi2_includedOnSemi2() {
            List<DeductionBreakdownItem> result = breakdown(SEMI_2_START, SEMI_2_END, "semi_2", null);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("null cutoff defaults to SEMI_2 — skipped on semi_1")
        void nullCutoff_defaultsToSemi2_skippedOnSemi1() {
            List<DeductionBreakdownItem> result = breakdown(SEMI_1_START, SEMI_1_END, "semi_1", null);
            assertEquals(0, result.size());
        }

        @Test
        @DisplayName("Monthly period includes SEMI_2 deduction (intentional — cutoff only applies to semi-monthly)")
        void semi2Cutoff_onMonthlyPeriod_isIncluded() {
            List<DeductionBreakdownItem> result = breakdown(MONTH_START, MONTH_END, "monthly", "SEMI_2");
            assertEquals(1, result.size(),
                "Monthly payroll includes all active deductions — cutoff only restricts semi-monthly runs");
        }
    }
}
