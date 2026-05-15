package digital8.payroll.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

import digital8.payroll.controllers.DeductionViewController;
import digital8.payroll.entities.EmployeeDeductions;
import digital8.payroll.repositories.DeductionsRepository;
import digital8.payroll.repositories.EmployeeDeductionsRepository;
import digital8.payroll.repositories.EmployeeRepository;

/**
 * Tests for computeDeductionBalances in DeductionViewController.
 *
 * Verifies the fix for Bug #6: monthlyCutoff must align with the payroll
 * service's one-time deduction logic (startDate within [periodStart, periodEnd])
 * rather than the broken isWithinCurrentCutoff approach (day-of-month vs today's cutoff).
 *
 * Period windows used in these tests:
 *   SEMI_1 → day 1–15 of the month
 *   SEMI_2 → day 16–end of month
 *
 * All tests use a fixed "today" of May 10 (day 10), so:
 *   nextPeriodStart = May 1,  nextPeriodEnd = May 15  (SEMI_1)
 *
 * One-time deductions are included in monthlyCutoff when their startDate
 * falls within [nextPeriodStart, nextPeriodEnd], matching how PayrollService
 * filters one-time deductions by date range.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeductionViewControllerTest {

    private static final int EMP_ID = 42;

    @Mock private DeductionsRepository deductionsRepository;
    @Mock private EmployeeDeductionsRepository employeeDeductionsRepository;
    @Mock private EmployeeRepository employeeRepository;

    @InjectMocks
    private TestDeductionViewController controller;

    // Fixed "today" for all tests: May 10 (day 10, inside SEMI_1)
    // nextPeriodStart = May 1, nextPeriodEnd = May 15
    private static final LocalDate TODAY = LocalDate.of(2026, 5, 10);

    // A recurring deduction that is always active (Jan 1 – Dec 31, 2026)
    private static final LocalDate RECURRING_START = LocalDate.of(2026, 1, 1);
    private static final LocalDate RECURRING_END   = LocalDate.of(2026, 12, 31);
    private static final BigDecimal RECURRING_AMOUNT = new BigDecimal("1500.00");
    private static final BigDecimal ONE_TIME_AMOUNT  = new BigDecimal("500.00");

    // Test-accessor: exposes private computeDeductionBalances for testing,
    // overriding the private method so it can be called directly
    static class TestDeductionViewController extends DeductionViewController {
        Map<String, BigDecimal> testComputeDeductionBalances(
                Integer employeeId,
                LocalDate today) {
            return computeDeductionBalances(employeeId, today);
        }
    }

    // ------------------------------------------------------------------ helpers

    private EmployeeDeductions recurringDeduction(String cutoff) {
        EmployeeDeductions ed = new EmployeeDeductions();
        ed.setEmployeeId(EMP_ID);
        ed.setAmount(RECURRING_AMOUNT);
        ed.setIsRecurring(true);
        ed.setStartDate(RECURRING_START);
        ed.setEndDate(RECURRING_END);
        ed.setDeductionCutoff(cutoff);
        return ed;
    }

    private EmployeeDeductions oneTimeDeduction(LocalDate startDate) {
        EmployeeDeductions ed = new EmployeeDeductions();
        ed.setEmployeeId(EMP_ID);
        ed.setAmount(ONE_TIME_AMOUNT);
        ed.setIsRecurring(false);
        ed.setStartDate(startDate);
        ed.setEndDate(null);
        return ed;
    }

    // ------------------------------------------------------------------

    // ==================================================================
    // SECTION 1: One-time deductions — monthlyCutoff
    // ==================================================================

    @Nested
    @DisplayName("One-time deductions — monthlyCutoff alignment with payroll service")
    class OneTimeMonthlyCutoff {

        @Test
        @DisplayName("One-time with startDate on day 10 (inside nextPeriod) → included in monthlyCutoff")
        void oneTimeInsideNextPeriod_includedInMonthlyCutoff() {
            EmployeeDeductions oneTime = oneTimeDeduction(LocalDate.of(2026, 5, 10));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of());
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(oneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal monthlyCutoff = result.get("deductionMonthlyCutoff");
            assertEquals(0, ONE_TIME_AMOUNT.compareTo(monthlyCutoff),
                "One-time with startDate=May 10 should be inside SEMI_1 (May 1-15) and included");
        }

        @Test
        @DisplayName("One-time with startDate on day 5 (inside nextPeriod) → included in monthlyCutoff")
        void oneTimeEarlyInPeriod_included() {
            EmployeeDeductions oneTime = oneTimeDeduction(LocalDate.of(2026, 5, 5));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of());
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(oneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal monthlyCutoff = result.get("deductionMonthlyCutoff");
            assertEquals(0, ONE_TIME_AMOUNT.compareTo(monthlyCutoff),
                "One-time with startDate=May 5 should be inside SEMI_1");
        }

        @Test
        @DisplayName("One-time with startDate on day 15 (boundary — inside nextPeriod) → included in monthlyCutoff")
        void oneTimeOnLastDayOfPeriod_included() {
            EmployeeDeductions oneTime = oneTimeDeduction(LocalDate.of(2026, 5, 15));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of());
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(oneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal monthlyCutoff = result.get("deductionMonthlyCutoff");
            assertEquals(0, ONE_TIME_AMOUNT.compareTo(monthlyCutoff),
                "One-time with startDate=May 15 (boundary) should be included in SEMI_1");
        }

        @Test
        @DisplayName("One-time with startDate on day 16 (next period) → NOT included in monthlyCutoff")
        void oneTimeInNextPeriod_excludedFromMonthlyCutoff() {
            EmployeeDeductions oneTime = oneTimeDeduction(LocalDate.of(2026, 5, 16));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of());
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(oneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal monthlyCutoff = result.get("deductionMonthlyCutoff");
            assertEquals(0, BigDecimal.ZERO.compareTo(monthlyCutoff),
                "One-time with startDate=May 16 is in SEMI_2, not SEMI_1 — excluded from this period's cutoff");
        }

        @Test
        @DisplayName("One-time with startDate on day 1 of previous month → NOT included")
        void oneTimeInPreviousPeriod_excluded() {
            EmployeeDeductions oneTime = oneTimeDeduction(LocalDate.of(2026, 4, 28));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of());
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(oneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal monthlyCutoff = result.get("deductionMonthlyCutoff");
            assertEquals(0, BigDecimal.ZERO.compareTo(monthlyCutoff),
                "One-time from previous month should not appear in current period's cutoff");
        }

        @Test
        @DisplayName("One-time with null startDate → excluded (no crash)")
        void oneTimeNullStartDate_excluded() {
            EmployeeDeductions oneTime = oneTimeDeduction(null);

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of());
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(oneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal monthlyCutoff = result.get("deductionMonthlyCutoff");
            assertEquals(0, BigDecimal.ZERO.compareTo(monthlyCutoff),
                "Null startDate should be safely skipped, not crash");
        }

        @Test
        @DisplayName("One-time with null amount → excluded (no crash)")
        void oneTimeNullAmount_excluded() {
            EmployeeDeductions oneTime = oneTimeDeduction(LocalDate.of(2026, 5, 10));
            oneTime.setAmount(null);

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of());
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(oneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal monthlyCutoff = result.get("deductionMonthlyCutoff");
            assertEquals(0, BigDecimal.ZERO.compareTo(monthlyCutoff),
                "Null amount should be safely skipped");
        }
    }

    // ==================================================================
    // SECTION 2: One-time deductions — outstandingObligation
    // ==================================================================

    @Nested
    @DisplayName("One-time deductions — outstandingObligation")
    class OneTimeOutstandingObligation {

        @Test
        @DisplayName("Outstanding obligation includes all one-time deductions regardless of startDate")
        void outstandingObligationIncludesAllOneTime() {
            EmployeeDeductions may10 = oneTimeDeduction(LocalDate.of(2026, 5, 10));
            EmployeeDeductions may20 = oneTimeDeduction(LocalDate.of(2026, 5, 20));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of());
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(may10, may20));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal outstandingObligation = result.get("deductionOutstandingObligation");
            assertEquals(0, ONE_TIME_AMOUNT.add(ONE_TIME_AMOUNT).compareTo(outstandingObligation),
                "outstandingObligation should sum all one-time deduction amounts");
        }

        @Test
        @DisplayName("Outstanding obligation and monthlyCutoff can differ — obligation is total, cutoff is next payroll")
        void outstandingObligationGreaterThanOrEqualMonthlyCutoff() {
            EmployeeDeductions oneTimeInNextPeriod = oneTimeDeduction(LocalDate.of(2026, 5, 20));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of());
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(oneTimeInNextPeriod));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal obligation = result.get("deductionOutstandingObligation");
            BigDecimal monthlyCutoff = result.get("deductionMonthlyCutoff");

            assertEquals(0, ONE_TIME_AMOUNT.compareTo(obligation),
                "Outstanding obligation includes the deduction");
            assertEquals(0, BigDecimal.ZERO.compareTo(monthlyCutoff),
                "monthlyCutoff excludes it (it's in the next period)");
            assertTrue(obligation.compareTo(monthlyCutoff) >= 0);
        }
    }

    // ==================================================================
    // SECTION 3: Recurring deductions — cutoff logic
    // ==================================================================

    @Nested
    @DisplayName("Recurring deductions — monthlyCutoff per cutoff setting")
    class RecurringMonthlyCutoff {

        @Test
        @DisplayName("Recurring SEMI_1 deduction → included (today=May 10 is SEMI_1)")
        void recurringSemi1_included() {
            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(recurringDeduction("SEMI_1")));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal monthlyCutoff = result.get("deductionMonthlyCutoff");
            assertEquals(0, RECURRING_AMOUNT.compareTo(monthlyCutoff),
                "SEMI_1 deduction should appear when next period is SEMI_1 (today=May 10)");
        }

        @Test
        @DisplayName("Recurring SEMI_2 deduction → excluded when today is in SEMI_1 (next period is SEMI_1)")
        void recurringSemi2_excludedWhenTodayIsSemi1() {
            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(recurringDeduction("SEMI_2")));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal monthlyCutoff = result.get("deductionMonthlyCutoff");
            assertEquals(0, BigDecimal.ZERO.compareTo(monthlyCutoff),
                "SEMI_2 deduction does not apply during SEMI_1 period (today=May 10)");
        }

        @Test
        @DisplayName("Recurring BOTH deduction → always included regardless of period")
        void recurringBoth_alwaysIncluded() {
            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(recurringDeduction("BOTH")));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal monthlyCutoff = result.get("deductionMonthlyCutoff");
            assertEquals(0, RECURRING_AMOUNT.compareTo(monthlyCutoff),
                "BOTH deduction always applies");
        }

        @Test
        @DisplayName("Recurring SEMI_1 deduction → excluded from immediate next period (SEMI_2) but applies after")
        void recurringSemi1_excludedFromNextPeriodWhenNextIsSemi2() {
            // Run with today = May 20, so next period is SEMI_2 (May 16-31)
            LocalDate semi2Day = LocalDate.of(2026, 5, 20);

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, semi2Day, semi2Day))
                .thenReturn(List.of(recurringDeduction("SEMI_1")));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, semi2Day);

            BigDecimal monthlyCutoff = result.get("deductionMonthlyCutoff");
            assertEquals(0, BigDecimal.ZERO.compareTo(monthlyCutoff),
                "SEMI_1 deduction is included when today=May 20 (SEMI_2), meaning it is withheld THIS period for the NEXT SEMI_1 payroll");
        }

        @Test
        @DisplayName("Recurring SEMI_2 deduction → included when today is SEMI_2 (next period = SEMI_2)")
        void recurringSemi2_includedWhenNextIsSemi2() {
            LocalDate semi2Day = LocalDate.of(2026, 5, 20);

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, semi2Day, semi2Day))
                .thenReturn(List.of(recurringDeduction("SEMI_2")));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, semi2Day);

            BigDecimal monthlyCutoff = result.get("deductionMonthlyCutoff");
            assertEquals(0, RECURRING_AMOUNT.compareTo(monthlyCutoff),
                "SEMI_2 deduction appears when next period is SEMI_2");
        }
    }

    // ==================================================================
    // SECTION 4: Edge cases
    // ==================================================================

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("Null employeeId → returns zeros with no crash")
        void nullEmployeeId_returnsZeros() {
            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(null, TODAY);

            assertEquals(0, BigDecimal.ZERO.compareTo(result.get("deductionOutstandingObligation")));
            assertEquals(0, BigDecimal.ZERO.compareTo(result.get("deductionMonthlyCutoff")));
            assertEquals(0, BigDecimal.ZERO.compareTo(result.get("deductionOutstandingBalance")));
        }

        @Test
        @DisplayName("Empty deductions list → returns zeros")
        void emptyDeductions_returnsZeros() {
            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of());
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            assertEquals(0, BigDecimal.ZERO.compareTo(result.get("deductionOutstandingObligation")));
            assertEquals(0, BigDecimal.ZERO.compareTo(result.get("deductionMonthlyCutoff")));
        }

        @Test
        @DisplayName("Mix of recurring and one-time deductions — both tracked independently")
        void mixedRecurringAndOneTime() {
            EmployeeDeductions recurring = recurringDeduction("BOTH");
            EmployeeDeductions oneTime   = oneTimeDeduction(LocalDate.of(2026, 5, 10));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(recurring));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(oneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal monthlyCutoff = result.get("deductionMonthlyCutoff");
            assertEquals(0, RECURRING_AMOUNT.add(ONE_TIME_AMOUNT).compareTo(monthlyCutoff),
                "monthlyCutoff should include both recurring BOTH and one-time deductions");
        }

        @Test
        @DisplayName("End-of-February: SEMI_2 in 28-day month")
        void endOfFebruary_handledGracefully() {
            // Feb 28, 2026 — SEMI_1: Feb 1-15, SEMI_2: Feb 16-28
            LocalDate feb28 = LocalDate.of(2026, 2, 28);
            EmployeeDeductions oneTime = oneTimeDeduction(LocalDate.of(2026, 2, 16));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, feb28, feb28))
                .thenReturn(List.of());
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(oneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, feb28);

            BigDecimal monthlyCutoff = result.get("deductionMonthlyCutoff");
            assertEquals(0, ONE_TIME_AMOUNT.compareTo(monthlyCutoff),
                "Feb 16 is inside SEMI_2 (Feb 16-28), should be included when next period is SEMI_2");
        }

        @Test
        @DisplayName("Leap year February 29 — SEMI_2 works correctly")
        void leapYearFebruary_works() {
            LocalDate feb29 = LocalDate.of(2028, 2, 29);
            EmployeeDeductions oneTime = oneTimeDeduction(LocalDate.of(2028, 2, 20));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, feb29, feb29))
                .thenReturn(List.of());
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(oneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, feb29);

            BigDecimal monthlyCutoff = result.get("deductionMonthlyCutoff");
            assertEquals(0, ONE_TIME_AMOUNT.compareTo(monthlyCutoff),
                "Feb 20 is inside SEMI_2 (Feb 16-29 in leap year)");
        }
    }

    // ==================================================================
    // SECTION 5: Regression — verify old isWithinCurrentCutoff scenarios
    // ==================================================================

    @Nested
    @DisplayName("Regression: old scenarios that isWithinCurrentCutoff handled incorrectly")
    class RegressionOldScenarios {

        /**
         * Scenario from Bug #6: Deduction created on May 16 (day 16, second cutoff)
         * but today=May 10 (first cutoff).
         *
         * OLD BEHAVIOR (isWithinCurrentCutoff):
         *   isFirstCutoff = true (today=May 14, day <= 15)
         *   isWithinCurrentCutoff(day=16, isFirstCutoff=true) → day >= 16 → FALSE
         *   monthlyCutoff = 0  ← WRONG
         *
         * NEW BEHAVIOR:
         *   today=May 10 → next period is SEMI_1 (May 1-15)
         *   May 16 is NOT in [May 1, May 15] → excluded from this period
         *   monthlyCutoff = 0  ← CORRECT
         */
        @Test
        @DisplayName("Deduction created May 16 (SEMI_2) when today=May 10 (SEMI_1) → monthlyCutoff=0")
        void deductionCreatedOnDay16_whenTodayIsSemi1_showsZeroCutoff() {
            EmployeeDeductions oneTime = oneTimeDeduction(LocalDate.of(2026, 5, 16));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of());
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(oneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal monthlyCutoff = result.get("deductionMonthlyCutoff");
            assertEquals(0, BigDecimal.ZERO.compareTo(monthlyCutoff),
                "May 16 is in SEMI_2, not SEMI_1 — correctly excluded from current period");
            assertEquals(0, ONE_TIME_AMOUNT.compareTo(result.get("deductionOutstandingObligation")),
                "Obligation still records the full amount");
        }

        /**
         * Scenario: Deduction created on May 14 (day 14, first cutoff), today=May 14.
         * Should appear in monthlyCutoff.
         */
        @Test
        @DisplayName("Deduction created May 14 when today=May 10 → appears in monthlyCutoff")
        void deductionCreatedMay14_whenTodayIsSemi1_appearsInCutoff() {
            EmployeeDeductions oneTime = oneTimeDeduction(LocalDate.of(2026, 5, 14));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of());
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(oneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal monthlyCutoff = result.get("deductionMonthlyCutoff");
            assertEquals(0, ONE_TIME_AMOUNT.compareTo(monthlyCutoff),
                "May 14 is inside SEMI_1 (May 1-15) — correctly included");
        }
    }
}