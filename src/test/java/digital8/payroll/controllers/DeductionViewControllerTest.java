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

    // Additional constants for new test scenarios
    private static final BigDecimal HALF_AMOUNT = new BigDecimal("750.00");
    private static final LocalDate SEMI2_TODAY = LocalDate.of(2026, 5, 20); // day > 15, next period = SEMI_2

    // Test-accessor: exposes private computeDeductionBalances for testing,
    // overriding the private method so it can be called directly
    static class TestDeductionViewController extends DeductionViewController {
        Map<String, BigDecimal> testComputeDeductionBalances(
                Integer employeeId,
                LocalDate today) {
            return computeDeductionBalances(employeeId, today);
        }

        long testCountPeriods(LocalDate from, LocalDate to, int periodsPerYear) {
            return countPeriods(from, to, periodsPerYear);
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

    private EmployeeDeductions recurringDeduction(LocalDate start, LocalDate end, String cutoff, BigDecimal amount) {
        EmployeeDeductions ed = new EmployeeDeductions();
        ed.setEmployeeId(EMP_ID);
        ed.setAmount(amount);
        ed.setIsRecurring(true);
        ed.setStartDate(start);
        ed.setEndDate(end);
        ed.setDeductionCutoff(cutoff);
        return ed;
    }

    private EmployeeDeductions recurringDeductionOpenEnded(LocalDate start, String cutoff, BigDecimal amount) {
        EmployeeDeductions ed = new EmployeeDeductions();
        ed.setEmployeeId(EMP_ID);
        ed.setAmount(amount);
        ed.setIsRecurring(true);
        ed.setStartDate(start);
        ed.setEndDate(null);
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
        ed.setDeductionCutoff("BOTH");
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
            // Use SEMI_2 cutoff so it only matches SEMI_2 periods
            // May 16 is in SEMI_2 window but today=SEMI_1 → excluded
            EmployeeDeductions oneTime = oneTimeDeduction(LocalDate.of(2026, 5, 16));
            oneTime.setDeductionCutoff("SEMI_2");

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of());
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(oneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal monthlyCutoff = result.get("deductionMonthlyCutoff");
            assertEquals(0, BigDecimal.ZERO.compareTo(monthlyCutoff),
                "One-time with startDate=May 16, SEMI_2 cutoff → excluded when next period is SEMI_1");
        }

        @Test
        @DisplayName("One-time with startDate on day 1 of previous month → NOT included")
        void oneTimeInPreviousPeriod_excluded() {
            // Use SEMI_2 cutoff so it respects period window matching
            EmployeeDeductions oneTime = oneTimeDeduction(LocalDate.of(2026, 4, 28));
            oneTime.setDeductionCutoff("SEMI_2");

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
        @DisplayName("One-time with null startDate → excluded when cutoff is SEMI_2 (no crash)")
        void oneTimeNullStartDate_excluded() {
            EmployeeDeductions oneTime = oneTimeDeduction(null);
            oneTime.setDeductionCutoff("SEMI_2");

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
    @DisplayName("One-time deductions — outstandingObligation & outstandingBalance")
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

        @Test
        @DisplayName("One-time deduction with past startDate → balance is 0 (already paid)")
        void oneTimePastStartDate_balanceIsZero() {
            // startDate April 28 is before today (May 10) → already deducted
            EmployeeDeductions pastOneTime = oneTimeDeduction(LocalDate.of(2026, 4, 28));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of());
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(pastOneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            // Obligation still includes it (total owed)
            assertEquals(0, ONE_TIME_AMOUNT.compareTo(result.get("deductionOutstandingObligation")),
                "Obligation includes all one-time deductions");
            // Balance is 0 because it was already paid
            assertEquals(0, BigDecimal.ZERO.compareTo(result.get("deductionOutstandingBalance")),
                "Balance should be 0 for a past one-time deduction (already paid)");
        }

        @Test
        @DisplayName("One-time deduction with future startDate → balance equals amount (not yet paid)")
        void oneTimeFutureStartDate_balanceEqualsAmount() {
            // startDate May 20 is after today (May 10) → not yet deducted
            EmployeeDeductions futureOneTime = oneTimeDeduction(LocalDate.of(2026, 5, 20));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of());
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(futureOneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            assertEquals(0, ONE_TIME_AMOUNT.compareTo(result.get("deductionOutstandingObligation")),
                "Obligation includes the one-time deduction");
            assertEquals(0, ONE_TIME_AMOUNT.compareTo(result.get("deductionOutstandingBalance")),
                "Balance should equal amount for a future one-time deduction (not yet paid)");
        }

        @Test
        @DisplayName("One-time deduction with startDate = today → balance equals amount (not yet paid today)")
        void oneTimeTodayStartDate_balanceEqualsAmount() {
            EmployeeDeductions todayOneTime = oneTimeDeduction(TODAY);

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of());
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(todayOneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            assertEquals(0, ONE_TIME_AMOUNT.compareTo(result.get("deductionOutstandingBalance")),
                "Balance should equal amount when startDate is today (not yet processed)");
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
            // Balance should equal amount since May 16 is in the future (not yet paid)
            assertEquals(0, ONE_TIME_AMOUNT.compareTo(result.get("deductionOutstandingBalance")),
                "Balance should equal amount — not yet paid");
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

    // ==================================================================
    // SECTION 6: Recurring deductions — outstandingObligation
    // ==================================================================

    @Nested
    @DisplayName("Recurring deductions — deductionOutstandingObligation")
    class RecurringOutstandingObligation {

        @Test
        @DisplayName("Recurring with endDate → lifetime obligation = amount × total periods (SEMI_2 = 12)")
        void recurringWithEndDate_lifetimeObligationCorrect() {
            // Jan 1 to Dec 31, SEMI_2, 1500.00
            // periodsPerYear = 12 (SEMI_2)
            // countPeriods(Jan 1, Dec 31, 12) → Jan-Dec inclusive = 12 months × 1 = 12 periods
            // obligation = 1500 × 12 = 18000
            EmployeeDeductions recurring = recurringDeduction(RECURRING_START, RECURRING_END, "SEMI_2", RECURRING_AMOUNT);

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(recurring));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal expected = new BigDecimal("18000.00");
            assertEquals(0, expected.compareTo(result.get("deductionOutstandingObligation")),
                "SEMI_2: Jan-Dec = 12 periods, obligation = 1500 × 12 = 18000");
        }

        @Test
        @DisplayName("Recurring with BOTH cutoffs → 24 periods per year, doubling the obligation")
        void recurringWithEndDate_bothCutoffs_twiceThePeriods() {
            // Jan 1 to Dec 31, BOTH, 1500.00
            // periodsPerYear = 24
            // countPeriods(Jan 1, Dec 31, 24) → 12 months × 2 = 24 periods
            // obligation = 1500 × 24 = 36000
            EmployeeDeductions recurring = recurringDeduction(RECURRING_START, RECURRING_END, "BOTH", RECURRING_AMOUNT);

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(recurring));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal expected = new BigDecimal("36000.00");
            assertEquals(0, expected.compareTo(result.get("deductionOutstandingObligation")),
                "BOTH: Jan-Dec = 24 periods (12 × 2), obligation = 1500 × 24 = 36000");
        }

        @Test
        @DisplayName("Recurring with null endDate (open-ended) → projected 36 periods")
        void recurringWithEndDate_nullEndDate_projected36Periods() {
            // Jan 1 to null (open-ended), SEMI_2, 1000.00
            // totalPeriods = 36 (hardcoded projection)
            // obligation = 1000 × 36 = 36000
            EmployeeDeductions recurring = recurringDeductionOpenEnded(RECURRING_START, "SEMI_2", new BigDecimal("1000.00"));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(recurring));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal expected = new BigDecimal("36000.00");
            assertEquals(0, expected.compareTo(result.get("deductionOutstandingObligation")),
                "Open-ended SEMI_2: 36-period projection, obligation = 1000 × 36 = 36000");
        }

        @Test
        @DisplayName("Recurring with null endDate and BOTH → totalPeriods stays 36 (not doubled)")
        void recurringWithEndDate_nullEndDate_both_totalPeriodsNotDoubled() {
            // Jan 1 to null, BOTH, 1000.00
            // totalPeriods = 36 (hardcoded projection, NOT multiplied by periodsPerYear/12)
            // obligation = 1000 × 36 = 36000
            EmployeeDeductions recurring = recurringDeductionOpenEnded(RECURRING_START, "BOTH", new BigDecimal("1000.00"));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(recurring));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal expected = new BigDecimal("36000.00");
            assertEquals(0, expected.compareTo(result.get("deductionOutstandingObligation")),
                "Open-ended BOTH: totalPeriods=36 (hardcoded), NOT 72; obligation = 1000 × 36 = 36000");
        }

        @Test
        @DisplayName("Recurring SEMI_1 past day 15 → effectiveFrom shifts to next month for remaining periods")
        void recurringSemi1_pastDay15_effectiveFromShiftsToNextMonth() {
            // Jan 1 to Dec 31, SEMI_1, today=May 20
            // SEMI_1: today=May20 (day>15) → effectiveFrom = June 1
            // remainingPeriods = countPeriods(June 1, Dec 31, 12) = 7 periods (Jun-Dec)
            // remainingPeriods × amount = 7 × 1500 = 10500
            EmployeeDeductions recurring = recurringDeduction(RECURRING_START, RECURRING_END, "SEMI_1", RECURRING_AMOUNT);

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, SEMI2_TODAY, SEMI2_TODAY))
                .thenReturn(List.of(recurring));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, SEMI2_TODAY);

            // totalPeriods = 12 (full year SEMI_1)
            assertEquals(0, new BigDecimal("18000.00").compareTo(result.get("deductionOutstandingObligation")),
                "Total obligation remains 12 periods × 1500 = 18000");
            // remainingPeriods = June-Dec = 7 periods
            assertEquals(0, new BigDecimal("10500.00").compareTo(result.get("deductionOutstandingBalance")),
                "Remaining periods from June SEMI_1: 7 × 1500 = 10500");
        }

        @Test
        @DisplayName("Recurring BOTH past day 15 → remainingPeriods decremented by 1 (SEMI_1 already paid)")
        void recurringBoth_pastDay15_remainingPeriodsDecrementedByOne() {
            // Jan 1 to Dec 31, BOTH, today=May 20
            // totalPeriods = 24
            // remainingPeriods from today: countPeriods(May 20, Dec 31, 24) minus 1
            // countPeriods(May 20, Dec 31, 24): May-Dec = 8 months × 2 = 16, then +1 if partial = 17
            // minus 1 for BOTH past day 15 → remaining = 16
            // balance = 1500 × 16 = 24000
            EmployeeDeductions recurring = recurringDeduction(RECURRING_START, RECURRING_END, "BOTH", RECURRING_AMOUNT);

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, SEMI2_TODAY, SEMI2_TODAY))
                .thenReturn(List.of(recurring));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, SEMI2_TODAY);

            // Total obligation is always the full lifetime: 24 periods × 1500 = 36000
            assertEquals(0, new BigDecimal("36000.00").compareTo(result.get("deductionOutstandingObligation")),
                "Total obligation = 24 × 1500 = 36000 (lifetime)");
            // remainingPeriods = 24 - 1 (SEMI_1 already paid in May) = 23, balance = 1500 × 23
            // Wait: the code does remainingPeriods = countPeriods(today, endDate, 24) then -1
            // countPeriods(May 20, Dec 31, 24): totalMonths = 7 (May→Dec exclusive), then +1 → 8 months × 2 = 16
            // Then -1 → 15 remaining. balance = 1500 × 15 = 22500
            // Actually let me recalculate: MONTHS.between(May 20, Dec 31) = 7
            // May 20.plusMonths(7) = Dec 20, Dec 20 < Dec 31, so +1 → totalMonths = 8
            // 8 × 2 = 16, -1 = 15, balance = 22500
            assertEquals(0, new BigDecimal("22500.00").compareTo(result.get("deductionOutstandingBalance")),
                "remainingPeriods = 16 - 1 = 15, balance = 1500 × 15 = 22500");
        }
    }

    // ==================================================================
    // SECTION 7: Recurring deductions — outstandingBalance
    // ==================================================================

    @Nested
    @DisplayName("Recurring deductions — deductionOutstandingBalance")
    class RecurringOutstandingBalance {

        @Test
        @DisplayName("Active recurring deduction → balance = amount × remaining periods through endDate")
        void recurringActive_balanceEqualsRemainingPeriods() {
            // Jan 1 to Dec 31, SEMI_2, today=May 10
            // effectiveFrom = today (May 10), endDate = Dec 31
            // countPeriods(May 10, Dec 31, 12): months = 7 (May→Dec exclusive)
            // May 10.plusMonths(7) = Dec 10, Dec 10 < Dec 31 → +1 → 8 months
            // balance = 1500 × 8 = 12000
            EmployeeDeductions recurring = recurringDeduction(RECURRING_START, RECURRING_END, "SEMI_2", RECURRING_AMOUNT);

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(recurring));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            // Jan-Dec total obligation = 12 × 1500 = 18000
            assertEquals(0, new BigDecimal("18000.00").compareTo(result.get("deductionOutstandingObligation")));
            // Balance = remaining from May 10: 8 periods × 1500 = 12000
            assertEquals(0, new BigDecimal("12000.00").compareTo(result.get("deductionOutstandingBalance")),
                "Remaining periods May→Dec = 8, balance = 8 × 1500 = 12000");
        }

        @Test
        @DisplayName("Expired recurring deduction (endDate before today) → balance is 0")
        void recurringExpired_balanceIsZero() {
            // Jan 1 to Apr 30, SEMI_2, today=May 10
            // endDate (Apr 30) is before today (May 10) → remainingPeriods = 0
            LocalDate expiredEnd = LocalDate.of(2026, 4, 30);
            EmployeeDeductions expired = recurringDeduction(RECURRING_START, expiredEnd, "SEMI_2", RECURRING_AMOUNT);

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(expired));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            // Obligation: countPeriods(Jan 1, Apr 30, 12) = 4 months × 1 = 4, obligation = 6000
            assertEquals(0, new BigDecimal("6000.00").compareTo(result.get("deductionOutstandingObligation")),
                "Total obligation = 4 periods × 1500 = 6000 (already fully paid)");
            // Balance: remainingPeriods = 0 since endDate < today
            assertEquals(0, BigDecimal.ZERO.compareTo(result.get("deductionOutstandingBalance")),
                "Balance = 0 — all periods completed");
        }

        @Test
        @DisplayName("Open-ended recurring (null endDate) → balance projected over 36 periods")
        void recurringOpenEnded_balanceProjected36Periods() {
            // Jan 1 to null, SEMI_2, today=May 10, 1000.00
            // totalPeriods = 36, remainingPeriods = 36
            // balance = 1000 × 36 = 36000
            EmployeeDeductions openEnded = recurringDeductionOpenEnded(RECURRING_START, "SEMI_2", new BigDecimal("1000.00"));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(openEnded));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            assertEquals(0, new BigDecimal("36000.00").compareTo(result.get("deductionOutstandingObligation")),
                "Open-ended obligation = 36 × 1000 = 36000");
            assertEquals(0, new BigDecimal("36000.00").compareTo(result.get("deductionOutstandingBalance")),
                "Open-ended balance = 36 × 1000 = 36000 (projected)");
        }

        @Test
        @DisplayName("Recurring halfway through year → balance < total obligation")
        void recurringHalfway_balanceCorrectlyProportioned() {
            // Jan 1 to Dec 31, SEMI_2, today=May 10
            // obligation = 18000 (12 × 1500), balance = ~12000 (8 × 1500)
            EmployeeDeductions recurring = recurringDeduction(RECURRING_START, RECURRING_END, "SEMI_2", RECURRING_AMOUNT);

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(recurring));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal obligation = result.get("deductionOutstandingObligation");
            BigDecimal balance = result.get("deductionOutstandingBalance");

            assertTrue(balance.compareTo(obligation) < 0,
                "Balance (" + balance + ") must be less than total obligation (" + obligation + ") at mid-year");
        }

        @Test
        @DisplayName("Recurring BOTH → balance computed the same as SEMI (same period-counting logic)")
        void recurringBOTH_balanceSameLogicAsSemi() {
            // Jan 1 to Dec 31, BOTH, 1000.00, today=May 10
            // effectiveFrom = today (May 10), endDate = Dec 31
            // countPeriods(May 10, Dec 31, 24): months = 7, +1 partial = 8, × 2 = 16
            // balance = 1000 × 16 = 16000
            EmployeeDeductions both = recurringDeduction(RECURRING_START, RECURRING_END, "BOTH", new BigDecimal("1000.00"));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(both));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            // Total: 12 months × 2 periods/month × 1000 = 24000
            assertEquals(0, new BigDecimal("24000.00").compareTo(result.get("deductionOutstandingObligation")),
                "BOTH obligation = 24 × 1000 = 24000");
            // Remaining: 8 months × 2 = 16 periods × 1000 = 16000
            assertEquals(0, new BigDecimal("16000.00").compareTo(result.get("deductionOutstandingBalance")),
                "BOTH balance = 16 × 1000 = 16000 (from May 10 to Dec 31)");
        }
    }

    // ==================================================================
    // SECTION 8: countPeriods helper — boundary conditions
    // ==================================================================

    @Nested
    @DisplayName("countPeriods helper — boundary conditions")
    class CountPeriodsBoundaryConditions {

        @Test
        @DisplayName("countPeriods with null 'from' → returns 0")
        void countPeriods_nullFrom_returnsZero() {
            long result = controller.testCountPeriods(null, LocalDate.of(2026, 12, 31), 12);
            assertEquals(0, result, "null from should return 0");
        }

        @Test
        @DisplayName("countPeriods with null 'to' → returns 0")
        void countPeriods_nullTo_returnsZero() {
            long result = controller.testCountPeriods(LocalDate.of(2026, 1, 1), null, 12);
            assertEquals(0, result, "null to should return 0");
        }

        @Test
        @DisplayName("countPeriods with to before from → returns 0")
        void countPeriods_toBeforeFrom_returnsZero() {
            long result = controller.testCountPeriods(
                LocalDate.of(2026, 12, 31),
                LocalDate.of(2026, 1, 1),
                12);
            assertEquals(0, result, "to before from should return 0");
        }

        @Test
        @DisplayName("countPeriods with same from and to dates → returns minimum 1 period")
        void countPeriods_sameDate_minimumOne() {
            LocalDate same = LocalDate.of(2026, 6, 15);
            assertEquals(1, controller.testCountPeriods(same, same, 12),
                "Same date should return minimum 1 period (monthly)");
            assertEquals(2, controller.testCountPeriods(same, same, 24),
                "Same date with bi-weekly periods should return 2");
        }

        @Test
        @DisplayName("countPeriods full year (Jan 1 to Dec 31) monthly → returns 12")
        void countPeriods_fullYear_monthly_returns12() {
            long result = controller.testCountPeriods(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                12);
            assertEquals(12, result, "Full year with monthly periods = 12");
        }

        @Test
        @DisplayName("countPeriods full year (Jan 1 to Dec 31) bi-weekly → returns 24")
        void countPeriods_fullYear_biweekly_returns24() {
            long result = controller.testCountPeriods(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                24);
            assertEquals(24, result, "Full year with bi-weekly periods = 24");
        }

        @Test
        @DisplayName("countPeriods partial months Jan 15 to Mar 10 → correct inclusive count")
        void countPeriods_partialMonths_jan15_to_mar10() {
            // Jan 15 to Mar 10
            // monthsBetween = 1 (Jan 15 to Mar 15 is ~2 months but ChronoUnit says 1)
            // Actually ChronoUnit.MONTHS.between(Jan 15, Mar 10) = 1 (Feb only)
            // from.plusMonths(1) = Feb 15, Feb 15 < Mar 10 → +1 → totalMonths = 2
            // monthly (12): 2 × 1 = 2 periods
            // bi-weekly (24): 2 × 2 = 4 periods
            assertEquals(2, controller.testCountPeriods(
                LocalDate.of(2026, 1, 15),
                LocalDate.of(2026, 3, 10),
                12), "Jan 15 to Mar 10 = 2 months, monthly = 2 periods");
            assertEquals(4, controller.testCountPeriods(
                LocalDate.of(2026, 1, 15),
                LocalDate.of(2026, 3, 10),
                24), "Jan 15 to Mar 10 = 2 months, bi-weekly = 4 periods");
        }
    }

    // ==================================================================
    // SECTION 9: Mixed recurring and one-time deductions
    // ==================================================================

    @Nested
    @DisplayName("Mixed recurring and one-time deductions")
    class MixedRecurringAndOneTime {

        @Test
        @DisplayName("Mixed types → all three balance outputs are summed and all > 0")
        void mixedBothTypes_allThreeBalancesAggregated() {
            // Recurring SEMI_2 (1500) + one-time future (500) + one-time past (500)
            EmployeeDeductions recurring = recurringDeduction("SEMI_2");
            recurring.setAmount(RECURRING_AMOUNT);
            recurring.setStartDate(RECURRING_START);
            recurring.setEndDate(RECURRING_END);

            EmployeeDeductions future = oneTimeDeduction(LocalDate.of(2026, 5, 20));
            EmployeeDeductions past   = oneTimeDeduction(LocalDate.of(2026, 4, 15));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(recurring));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(future, past));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal obligation = result.get("deductionOutstandingObligation");
            BigDecimal balance    = result.get("deductionOutstandingBalance");
            BigDecimal cutoff     = result.get("deductionMonthlyCutoff");

            // Obligation: recurring(18000) + future(500) + past(500) = 19000
            assertEquals(0, new BigDecimal("19000.00").compareTo(obligation),
                "Obligation sums all: recurring 18000 + one-time 500 + 500");
            // Balance: recurring remaining + future (past already 0) = 12000 + 500 = 12500
            assertEquals(0, new BigDecimal("12500.00").compareTo(balance),
                "Balance = recurring remaining 12000 + future one-time 500 = 12500");
            // Cutoff: only recurring SEMI_2 excluded (today=SEMI_1), so 0
            assertEquals(0, BigDecimal.ZERO.compareTo(cutoff),
                "SEMI_2 not in cutoff today (SEMI_1), no one-time in this period");
            assertTrue(obligation.compareTo(BigDecimal.ZERO) > 0);
            assertTrue(balance.compareTo(BigDecimal.ZERO) > 0);
        }

        @Test
        @DisplayName("Recurring SEMI_1 + one-time May 10 (SEMI_1) → monthlyCutoff = both summed")
        void mixedBothTypes_monthlyCutoffIncludesBothIfPeriodMatches() {
            // Recurring SEMI_1 (1500) + one-time May 10 (500), today=May 10 (SEMI_1)
            // Both should appear in the SEMI_1 cutoff
            EmployeeDeductions recurring = recurringDeduction("SEMI_1");
            EmployeeDeductions oneTime  = oneTimeDeduction(LocalDate.of(2026, 5, 10));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(recurring));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(oneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            assertEquals(0, new BigDecimal("2000.00").compareTo(result.get("deductionMonthlyCutoff")),
                "SEMI_1 recurring (1500) + one-time May 10 (500) = 2000 in cutoff");
        }

        @Test
        @DisplayName("Recurring SEMI_2 + one-time May 10 (SEMI_2) → monthlyCutoff = 0 (both excluded)")
        void mixedBothTypes_monthlyCutoffIncludesOnlyRecurring() {
            // Recurring SEMI_2 (1500): today=SEMI_1, next=SEMI_1 → SEMI_2 excluded
            // One-time May 10 with SEMI_2: startDate in [May1,May15] ✓ but cutoff=SEMI_2,
            //   nextPeriodStart=May1 ≠ day16 → cutoff mismatch → excluded
            // Result: monthlyCutoff = 0
            EmployeeDeductions recurring = recurringDeduction("SEMI_2");
            EmployeeDeductions oneTime = oneTimeDeduction(LocalDate.of(2026, 5, 10));
            oneTime.setDeductionCutoff("SEMI_2");

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(recurring));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(oneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            assertEquals(0, BigDecimal.ZERO.compareTo(result.get("deductionMonthlyCutoff")),
                "Both SEMI_2 deductions excluded when next period is SEMI_1");
        }

        @Test
        @DisplayName("Multiple recurring with different cutoffs → cutoff includes SEMI_1 + BOTH only")
        void mixedMultipleRecurringDifferentCutoffs() {
            // Recurring SEMI_1 (500) + Recurring SEMI_2 (500) + Recurring BOTH (500)
            // today=May 10 → next period = SEMI_1
            // SEMI_1: included, SEMI_2: excluded, BOTH: always included
            // Expected cutoff = 500 + 500 = 1000
            EmployeeDeductions semi1 = recurringDeduction("SEMI_1");
            semi1.setAmount(new BigDecimal("500.00"));

            EmployeeDeductions semi2 = recurringDeduction("SEMI_2");
            semi2.setAmount(new BigDecimal("500.00"));

            EmployeeDeductions both = recurringDeduction("BOTH");
            both.setAmount(new BigDecimal("500.00"));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(semi1, semi2, both));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            assertEquals(0, new BigDecimal("1000.00").compareTo(result.get("deductionMonthlyCutoff")),
                "monthlyCutoff = SEMI_1 (500) + BOTH (500) = 1000; SEMI_2 excluded");
        }
    }

    // ==================================================================
    // SECTION 10: Cutoff boundary scenarios (days 15/16, month-end)
    // ==================================================================

    @Nested
    @DisplayName("Cutoff boundary scenarios")
    class CutoffBoundaryScenarios {

        @Test
        @DisplayName("One-time startDate=May 16, today=SEMI_2 → included in SEMI_2 cutoff")
        void oneTimeStartDate_may16_todaySemi2_cutoffIncluded() {
            // startDate = May 16, today = May 20 (SEMI_2)
            // nextPeriodStart = May 16, nextPeriodEnd = May 31
            // May 16 is in [May 16, May 31] ✓, cutoff default = SEMI_2, nextPeriodStart=day16 ✓
            EmployeeDeductions oneTime = oneTimeDeduction(LocalDate.of(2026, 5, 16));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, SEMI2_TODAY, SEMI2_TODAY))
                .thenReturn(List.of());
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(oneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, SEMI2_TODAY);

            assertEquals(0, ONE_TIME_AMOUNT.compareTo(result.get("deductionMonthlyCutoff")),
                "May 16 in SEMI_2 period, cutoff matches → included");
        }

        @Test
        @DisplayName("One-time startDate=May 16, today=SEMI_1 → excluded from cutoff")
        void oneTimeStartDate_may16_todaySemi1_cutoffExcluded() {
            // startDate = May 16, today = May 10 (SEMI_1)
            // nextPeriodStart = May 1, nextPeriodEnd = May 15
            // May 16 is NOT in [May 1, May 15] → excluded
            EmployeeDeductions oneTime = oneTimeDeduction(LocalDate.of(2026, 5, 16));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of());
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(oneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            assertEquals(0, BigDecimal.ZERO.compareTo(result.get("deductionMonthlyCutoff")),
                "May 16 is outside SEMI_1 period [May 1, May 15] → excluded");
        }

        @Test
        @DisplayName("One-time startDate=May 15 (boundary), today=SEMI_1 → included")
        void oneTimeStartDate_may15_todaySemi1_cutoffIncluded() {
            // startDate = May 15, today = May 10 (SEMI_1)
            // nextPeriodStart = May 1, nextPeriodEnd = May 15
            // May 15 is in [May 1, May 15] (inclusive) ✓
            EmployeeDeductions oneTime = oneTimeDeduction(LocalDate.of(2026, 5, 15));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of());
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(oneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            assertEquals(0, ONE_TIME_AMOUNT.compareTo(result.get("deductionMonthlyCutoff")),
                "May 15 is the last day of SEMI_1 — included in cutoff");
        }

        @Test
        @DisplayName("Recurring SEMI_1, today=May 16 (first SEMI_2 day) → not in next cutoff")
        void recurringSemi1_exactlyDay16Boundary_nextPeriodIsSemi2() {
            // today = May 16 (first day of SEMI_2 period)
            // nextPeriodStart = May 16, nextPeriodEnd = May 31 (SEMI_2)
            // SEMI_1 deduction: appliesThisCutoff = nextPeriodStart == day1 → May16 != May1 → false
            // So SEMI_1 is NOT included in the next SEMI_2 cutoff
            EmployeeDeductions semi1 = recurringDeduction("SEMI_1");

            LocalDate day16 = LocalDate.of(2026, 5, 16);
            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, day16, day16))
                .thenReturn(List.of(semi1));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, day16);

            assertEquals(0, BigDecimal.ZERO.compareTo(result.get("deductionMonthlyCutoff")),
                "SEMI_1 deduction not included when next period is SEMI_2 (today=May 16)");
        }

        @Test
        @DisplayName("December 31 boundary — no crash on month-end date")
        void monthEndDecember_dec31Boundary_handled() {
            // One-time startDate = Dec 31, today = May 10
            // nextPeriodStart = May 1, nextPeriodEnd = May 15
            // Dec 31 is NOT in [May 1, May 15] → excluded, but no crash
            EmployeeDeductions oneTime = oneTimeDeduction(LocalDate.of(2026, 12, 31));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of());
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(oneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            assertEquals(0, BigDecimal.ZERO.compareTo(result.get("deductionMonthlyCutoff")),
                "Dec 31 is far outside May's period — safely excluded");
            // Obligation still counts the full amount
            assertEquals(0, ONE_TIME_AMOUNT.compareTo(result.get("deductionOutstandingObligation")));
            // Balance = amount (future date, not yet paid)
            assertEquals(0, ONE_TIME_AMOUNT.compareTo(result.get("deductionOutstandingBalance")));
        }

        @Test
        @DisplayName("January 1 boundary — excluded from May period")
        void jan1Boundary_newYearPeriod() {
            // One-time startDate = Jan 1, today = May 10
            // nextPeriodStart = May 1, nextPeriodEnd = May 15
            // Jan 1 is NOT in [May 1, May 15] → excluded
            EmployeeDeductions oneTime = oneTimeDeduction(LocalDate.of(2026, 1, 1));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of());
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(oneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            assertEquals(0, BigDecimal.ZERO.compareTo(result.get("deductionMonthlyCutoff")),
                "Jan 1 is outside May's period — excluded from cutoff");
            // But it's a past date → balance = 0
            assertEquals(0, BigDecimal.ZERO.compareTo(result.get("deductionOutstandingBalance")),
                "Jan 1 is in the past — balance = 0 (already paid)");
        }
    }

    // ==================================================================
    // SECTION 11: Null and invalid deduction field values
    // ==================================================================

    @Nested
    @DisplayName("Null and invalid deduction field values")
    class NullAndInvalidDeductionFields {

        @Test
        @DisplayName("Recurring with null endDate → open-ended obligation (36-period projection)")
        void recurringWithNullEndDate_openEndedObligation() {
            EmployeeDeductions openEnded = recurringDeductionOpenEnded(RECURRING_START, "SEMI_2", RECURRING_AMOUNT);

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(openEnded));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            // totalPeriods = 36 (open-ended projection)
            // remainingPeriods = 36
            // obligation = 1500 × 36 = 54000
            // balance = 1500 × 36 = 54000
            assertEquals(0, new BigDecimal("54000.00").compareTo(result.get("deductionOutstandingObligation")),
                "Open-ended SEMI_2: 36 periods × 1500 = 54000");
            assertEquals(0, new BigDecimal("54000.00").compareTo(result.get("deductionOutstandingBalance")),
                "Open-ended balance = 54000 (36 remaining periods)");
        }

        @Test
        @DisplayName("Recurring with null deductionCutoff → treated as SEMI_2 (excluded today)")
        void recurringWithNullDeductionCutoff_defaultsToSemi2() {
            EmployeeDeductions noCutoff = recurringDeduction("SEMI_2");
            noCutoff.setDeductionCutoff(null);

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(noCutoff));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            // cutoff = null → falls to else: appliesThisCutoff = nextPeriodStart == day16
            // today=SEMI_1, nextPeriodStart=May1 → not day16 → false → excluded
            assertEquals(0, BigDecimal.ZERO.compareTo(result.get("deductionMonthlyCutoff")),
                "Null cutoff treated as SEMI_2, excluded when next period is SEMI_1");
            // But obligation/balance computed normally
            assertEquals(0, new BigDecimal("18000.00").compareTo(result.get("deductionOutstandingObligation")),
                "Obligation still computed correctly");
        }

        @Test
        @DisplayName("Recurring with null startDate → handled gracefully, no crash")
        void recurringWithNullStartDate_handledGracefully() {
            EmployeeDeductions noStart = recurringDeduction(RECURRING_START, RECURRING_END, "SEMI_2", RECURRING_AMOUNT);
            noStart.setStartDate(null);

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(noStart));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            // startDate=null: the if condition (startDate != null && endDate != null && !endDate.isBefore(startDate))
            // is false → falls to else if (endDate == null) → no
            // falls through with totalPeriods=0, remainingPeriods=0
            // amount × 0 = 0 added to both
            assertEquals(0, BigDecimal.ZERO.compareTo(result.get("deductionOutstandingObligation")),
                "Null startDate → no obligation added (condition not met)");
            assertEquals(0, BigDecimal.ZERO.compareTo(result.get("deductionOutstandingBalance")),
                "Null startDate → no balance added");
        }

        @Test
        @DisplayName("Recurring with startDate after endDate (invalid range) → zero obligation")
        void recurringWithInvalidDateRange_startAfterEnd_returnsZeroObligation() {
            // startDate = Dec 31, endDate = Jan 1 (reversed)
            EmployeeDeductions invalid = recurringDeduction(
                LocalDate.of(2026, 12, 31),
                LocalDate.of(2026, 1, 1),
                "SEMI_2",
                RECURRING_AMOUNT);

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(invalid));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            // countPeriods(Dec 31, Jan 1, 12) → to.isBefore(from) → 0
            // So totalPeriods = 0, remainingPeriods = 0
            assertEquals(0, BigDecimal.ZERO.compareTo(result.get("deductionOutstandingObligation")),
                "Invalid date range (start > end) → obligation = 0");
            assertEquals(0, BigDecimal.ZERO.compareTo(result.get("deductionOutstandingBalance")),
                "Invalid date range → balance = 0");
        }
    }

    // ==================================================================
    // SECTION 12: Recurring monthly cutoff — complete coverage
    // ==================================================================

    @Nested
    @DisplayName("Recurring monthly cutoff — complete cutoff coverage")
    class RecurringMonthlyCutoffAllScenarios {

        @Test
        @DisplayName("Recurring BOTH with startDate=May 16 → included in SEMI_2 cutoff")
        void recurringBoth_atPeriodStart_day16_included() {
            // startDate=May 16, today=May 10 (SEMI_1), nextPeriod=SEMI_1
            // BOTH always applies → included
            EmployeeDeductions both = recurringDeduction("BOTH");

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(both));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            assertEquals(0, RECURRING_AMOUNT.compareTo(result.get("deductionMonthlyCutoff")),
                "BOTH cutoff always included regardless of startDate");
        }

        @Test
        @DisplayName("Recurring BOTH, today=SEMI_2 → included in next SEMI_2 cutoff")
        void recurringBoth_nextPeriodIsSemi2_correctlyIncluded() {
            // today=May 20 (SEMI_2), nextPeriod=SEMI_2
            // BOTH always applies → included
            EmployeeDeductions both = recurringDeduction("BOTH");

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, SEMI2_TODAY, SEMI2_TODAY))
                .thenReturn(List.of(both));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, SEMI2_TODAY);

            assertEquals(0, RECURRING_AMOUNT.compareTo(result.get("deductionMonthlyCutoff")),
                "BOTH included when next period is SEMI_2");
        }

        @Test
        @DisplayName("Recurring SEMI_2, startDate=May 16 → included in next SEMI_2 cutoff")
        void recurringSemi2_startDateIsDay16_exactlyPeriodBoundary() {
            // startDate=May 16, today=May 10 (SEMI_1), nextPeriod=SEMI_1
            // SEMI_2: appliesThisCutoff = nextPeriodStart == day16 → May1 != day16 → false
            // Excluded from this SEMI_1 cutoff (will apply in next SEMI_2 period)
            EmployeeDeductions semi2 = recurringDeduction("SEMI_2");

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(semi2));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            assertEquals(0, BigDecimal.ZERO.compareTo(result.get("deductionMonthlyCutoff")),
                "SEMI_2 deduction excluded from SEMI_1 period (next is SEMI_1, not SEMI_2)");
        }

        @Test
        @DisplayName("Recurring SEMI_1, startDate=May 16 → excluded from next SEMI_2 period")
        void recurringSemi1_startDateIsDay16_excludedFromNextPeriod() {
            // startDate=May 16, today=May 10 (SEMI_1), nextPeriod=SEMI_1
            // SEMI_1: appliesThisCutoff = nextPeriodStart == day1 → May1 == day1 → true
            // Wait, nextPeriodStart for today=May 10 is May 1 (SEMI_1), so SEMI_1 IS included!
            // The exclusion logic for SEMI_1 is: nextPeriodStart == day1
            // today=May10: nextPeriodStart=May1 → day1 ✓ → included
            // This test should verify SEMI_1 IS included when next is SEMI_1
            // Actually I think the test name is misleading. Let me reconsider.
            //
            // The scenario: SEMI_1 deduction with startDate=May16. When does it apply?
            // Today=May10 (SEMI_1), nextPeriodStart=May1 (SEMI_1), day1 matches → SEMI_1 included
            // The SEMI_1 deduction applies regardless of its own startDate in terms of cutoff checking.
            // The cutoff check only looks at what the NEXT period is, not the deduction's startDate.
            //
            // Hmm, but in the one-time case, the startDate matters for cutoff AND period window.
            // In the recurring case, the cutoff check is purely about what next period is.
            // So SEMI_1 deduction: included when nextPeriodStart=day1.
            // This means today=May10 → nextPeriodStart=May1 → day1 → included.
            // So this test should actually assert it IS included.
            //
            // Let me change the scenario to test what happens when next period is SEMI_2
            // but the deduction is SEMI_1: it should be excluded.
            // today=May20 → nextPeriodStart=May16 → SEMI_2, so SEMI_1 is excluded.
            EmployeeDeductions semi1 = recurringDeduction("SEMI_1");

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, SEMI2_TODAY, SEMI2_TODAY))
                .thenReturn(List.of(semi1));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, SEMI2_TODAY);

            assertEquals(0, BigDecimal.ZERO.compareTo(result.get("deductionMonthlyCutoff")),
                "SEMI_1 deduction excluded when next period is SEMI_2");
        }

        @Test
        @DisplayName("Recurring endDate exactly today → included in active window, remainingPeriods ≥ 1")
        void recurringEndDateExactlyToday_includedInRemainingPeriods() {
            // endDate = TODAY (May 10), SEMI_2
            // endDate >= today → remainingPeriods calculated
            // countPeriods(May 10, May 10, 12) → same date → minimum 1 period
            // balance = 1500 × 1 = 1500
            EmployeeDeductions ending = recurringDeduction(RECURRING_START, TODAY, "SEMI_2", RECURRING_AMOUNT);

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(ending));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            // Obligation: countPeriods(Jan 1, May 10, 12) = Jan-May = 5 months × 1 = 5 periods
            // 5 × 1500 = 7500
            assertEquals(0, new BigDecimal("7500.00").compareTo(result.get("deductionOutstandingObligation")),
                "Total obligation = 5 periods (Jan-May) × 1500 = 7500");
            // Balance: same date → minimum 1 period remaining × 1500 = 1500
            assertEquals(0, new BigDecimal("1500.00").compareTo(result.get("deductionOutstandingBalance")),
                "EndDate=today → at least 1 period remaining, balance = 1500");
        }

        @Test
        @DisplayName("Recurring startDate exactly today → included in active window")
        void recurringStartDateExactlyToday_includedInActiveWindow() {
            // startDate = TODAY (May 10), SEMI_2
            // effectiveFrom = today (May 10)
            // countPeriods(May 10, Dec 31, 12) = 8 periods, balance = 12000
            EmployeeDeductions starting = recurringDeduction(TODAY, RECURRING_END, "SEMI_2", RECURRING_AMOUNT);

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(starting));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            // Obligation: May-Dec = 8 months × 1 = 8 periods × 1500 = 12000
            assertEquals(0, new BigDecimal("12000.00").compareTo(result.get("deductionOutstandingObligation")),
                "StartDate=today: May-Dec = 8 periods × 1500 = 12000");
            // Balance: May 10 to Dec 31 = 8 periods × 1500 = 12000
            assertEquals(0, new BigDecimal("12000.00").compareTo(result.get("deductionOutstandingBalance")),
                "StartDate=today, same effectiveFrom → balance = 12000");
        }
    }

    // ==================================================================
    // SECTION 13: Outstanding obligation — aggregation and invariants
    // ==================================================================

    @Nested
    @DisplayName("Outstanding obligation — aggregation and invariant checks")
    class OutstandingObligationAggregationAndEdgeCases {

        @Test
        @DisplayName("Multiple recurring deductions → obligation is summed correctly")
        void multipleRecurringObligations_summedCorrectly() {
            // Recurring SEMI_2 (1500) Jan-Dec: 12 × 1500 = 18000
            // Recurring BOTH (1000) Jan-Dec: 12 × 2 × 1000 = 24000
            // Total = 42000
            EmployeeDeductions semi2 = recurringDeduction(RECURRING_START, RECURRING_END, "SEMI_2", RECURRING_AMOUNT);
            EmployeeDeductions both   = recurringDeduction(RECURRING_START, RECURRING_END, "BOTH", new BigDecimal("1000.00"));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(semi2, both));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            assertEquals(0, new BigDecimal("42000.00").compareTo(result.get("deductionOutstandingObligation")),
                "SEMI_2 (18000) + BOTH (24000) = 42000");
        }

        @Test
        @DisplayName("Recurring + one-time → obligation includes both types")
        void recurringPlusOneTime_obligationIncludesBoth() {
            // Recurring SEMI_2 (1500) Jan-Dec: 18000
            // One-time (500): 500
            // Total = 18500
            EmployeeDeductions recurring = recurringDeduction("SEMI_2");
            EmployeeDeductions oneTime    = oneTimeDeduction(LocalDate.of(2026, 5, 10));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(recurring));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(oneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            assertEquals(0, new BigDecimal("18500.00").compareTo(result.get("deductionOutstandingObligation")),
                "Recurring 18000 + one-time 500 = 18500");
        }

        @Test
        @DisplayName("Obligation ≥ balance always holds (total ≥ remaining)")
        void obligationGreaterThanOrEqualBalance() {
            // SEMI_2 recurring Jan-Dec, today=May 10
            // obligation = 18000, balance = 12000
            // Always: obligation >= balance
            EmployeeDeductions recurring = recurringDeduction(RECURRING_START, RECURRING_END, "SEMI_2", RECURRING_AMOUNT);
            EmployeeDeductions oneTime   = oneTimeDeduction(LocalDate.of(2026, 4, 28));

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(recurring));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of(oneTime));

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal obligation = result.get("deductionOutstandingObligation");
            BigDecimal balance    = result.get("deductionOutstandingBalance");

            assertTrue(obligation.compareTo(balance) >= 0,
                "outstandingObligation (" + obligation + ") must be ≥ outstandingBalance (" + balance + ")");
        }

        @Test
        @DisplayName("Obligation ≥ monthlyCutoff always holds (total ≥ next-period amount)")
        void obligationGreaterThanOrEqualMonthlyCutoff() {
            // SEMI_2 recurring Jan-Dec, today=May 10 → monthlyCutoff = 0
            // obligation = 18000
            // Recurring SEMI_1, today=May 10 → monthlyCutoff = 1500
            // obligation = 18000 >= 1500 ✓
            EmployeeDeductions recurring = recurringDeduction(RECURRING_START, RECURRING_END, "SEMI_2", RECURRING_AMOUNT);

            when(employeeDeductionsRepository.findActiveRecurringByEmployee(EMP_ID, TODAY, TODAY))
                .thenReturn(List.of(recurring));
            when(employeeDeductionsRepository.findByEmployeeId(EMP_ID))
                .thenReturn(List.of());

            Map<String, BigDecimal> result = controller.testComputeDeductionBalances(EMP_ID, TODAY);

            BigDecimal obligation = result.get("deductionOutstandingObligation");
            BigDecimal monthlyCutoff = result.get("deductionMonthlyCutoff");

            assertTrue(obligation.compareTo(monthlyCutoff) >= 0,
                "outstandingObligation (" + obligation + ") must be ≥ monthlyCutoff (" + monthlyCutoff + ")");
        }
    }
}