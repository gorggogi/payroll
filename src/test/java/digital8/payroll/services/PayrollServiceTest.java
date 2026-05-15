package digital8.payroll.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import digital8.payroll.entities.Attendance;
import digital8.payroll.entities.Deductions;
import digital8.payroll.entities.EmployeeDeductions;
import digital8.payroll.entities.Employees;
import digital8.payroll.entities.PayrollItems;
import digital8.payroll.entities.SssTable;
import digital8.payroll.repositories.AttendanceRepository;
import digital8.payroll.repositories.DeductionsRepository;
import digital8.payroll.repositories.EmployeeDeductionsRepository;
import digital8.payroll.repositories.EmployeeRepository;
import digital8.payroll.repositories.SssTableRepository;
import digital8.payroll.repositories.TaxTableRepository;
import digital8.payroll.repositories.PhilhealthTableRepository;
import digital8.payroll.repositories.PagibigTableRepository;

@ExtendWith(MockitoExtension.class)
public class PayrollServiceTest {

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

    private void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), 
            "Expected: " + expected + " but was: " + actual);
    }

    @Test
    void testRegularEmployeeBelow30k() {
        // Setup Employee (21,000 Monthly Rate)
        Employees emp = new Employees();
        emp.setEmploymentType("Regular");
        emp.setBasicSalary(new BigDecimal("21000"));
        when(employeeRepository.findById(1)).thenReturn(Optional.of(emp));

        // Setup Attendance (80 hours worked, 4 hours OT, 33 mins late)
        Attendance att = new Attendance();
        att.setAttendance_date(LocalDate.of(2024, Month.JANUARY, 10)); // Fixed date for test
        att.setWork_hours(new BigDecimal("80"));
        att.setOvertime_hours(new BigDecimal("4"));
        att.setLate_minutes(33);
        att.setUndertime_minutes(0);
        
        List<Attendance> attendances = new ArrayList<>();
        attendances.add(att);
        when(attendanceRepository.findByEmployeeIdOrderByDateDesc(1)).thenReturn(attendances);

        // Setup SSS Table (Premium base is 20,000 -> SSS deduction is 1,000)
        SssTable sssRow = new SssTable();
        sssRow.setRangeFrom(new BigDecimal("19750"));
        sssRow.setRangeTo(new BigDecimal("20249"));
        sssRow.setEmployeeShare(new BigDecimal("1000.00"));
        List<SssTable> sssTables = new ArrayList<>();
        sssTables.add(sssRow);
        when(sssTableRepository.findByEffectiveYearOrderByRangeFromAsc(anyInt())).thenReturn(sssTables);

        // Setup Employee Deductions (Cash Advance = 1250)
        EmployeeDeductions ed = new EmployeeDeductions();
        ed.setAmount(new BigDecimal("1250"));
        ed.setDeductionId(101);
        ed.setStartDate(LocalDate.of(2024, 1, 1));
        ed.setEndDate(LocalDate.of(2024, 1, 31));
        List<EmployeeDeductions> deductions = new ArrayList<>();
        deductions.add(ed);
        when(employeeDeductionsRepository.findByEmployeeId(1)).thenReturn(deductions);
        
        Deductions cashAdvanceDef = new Deductions();
        cashAdvanceDef.setDeductionName("Cash Advance");
        cashAdvanceDef.setDeductionType("Advance");
        when(deductionsRepository.findById(101)).thenReturn(Optional.of(cashAdvanceDef));

        // Execute
        List<PayrollItems> result = payrollService.computePayroll(1, "monthly", "JANUARY", 2024);
        
        // Asserts
        assertEquals(1, result.size());
        PayrollItems item = result.get(0);

        // Rates
        assertBigDecimalEquals("1050.00", item.getDailyRate());
        assertBigDecimalEquals("131.25", item.getHourlyRate());
        // 131.25 / 60 = 2.1875 -> half_up is 2.19 if scaled to 2, but we scaled to 6 then 2. 
        // Let's just check the string representation after half up.
        // Actually the service sets it to scale 2: item.setPerMinuteRate(perMinuteRate.setScale(SCALE, ROUND));
        assertBigDecimalEquals("2.19", item.getPerMinuteRate());

        // Earnings
        assertBigDecimalEquals("10500.00", item.getBasicPay());
        // 131.25 * 4 * 1.25 = 656.25 for regular employee
        assertBigDecimalEquals("656.25", item.getOvertimePay());
        assertBigDecimalEquals("11156.25", item.getTotalEarnings());

        // Non-statutory deductions
        // 33 mins * 2.19 = 72.27 (rounded perMinuteRate: 2.19)
        assertBigDecimalEquals("72.27", item.getLateUndertimeDeduction());
        // Cash advance is now part of adjustmentDeductions (unified)
        assertBigDecimalEquals("1250.00", item.getAdjustmentDeductions());
        
        // Service fee = 11156.25 - (72.27 + 1250.00) = 9833.98
        assertBigDecimalEquals("9833.98", item.getServiceFee());

        // Statutory
        assertBigDecimalEquals("1000.00", item.getSss());
        // Philhealth: 21000 * 5% / 2 = 525
        assertBigDecimalEquals("525.00", item.getPhilhealth());
        // Pag-ibig: 21000 * 2% = 420
        assertBigDecimalEquals("420.00", item.getPagibig());
        // Tax: 21000 * 10% - 2395.90 = -295.90 -> 0.00
        assertBigDecimalEquals("0.00", item.getTax());

        // Semi Monthly = (1000 + 525 + 420 + 0) / 2 = 972.50
        assertBigDecimalEquals("972.50", item.getSemiMonthlyContributions());

        // Net Pay = 9833.98 - 972.50 = 8861.48
        assertBigDecimalEquals("8861.48", item.getNetPay());
    }

    @Test
    void testRegularEmployeeAbove30k() {
        // Setup Employee (36,000 Monthly Rate)
        Employees emp = new Employees();
        emp.setEmploymentType("Regular");
        emp.setBasicSalary(new BigDecimal("36000"));
        when(employeeRepository.findById(2)).thenReturn(Optional.of(emp));

        // Setup Attendance (empty for simplicity)
        when(attendanceRepository.findByEmployeeIdOrderByDateDesc(2)).thenReturn(new ArrayList<>());
        when(employeeDeductionsRepository.findByEmployeeId(2)).thenReturn(new ArrayList<>());

        // Setup SSS Table (Premium base is 36,000 -> typically maxes at 35,000 MSC, let's just make it return 1750)
        SssTable sssRow = new SssTable();
        sssRow.setRangeFrom(new BigDecimal("34750"));
        sssRow.setRangeTo(new BigDecimal("100000"));
        sssRow.setEmployeeShare(new BigDecimal("1750.00"));
        List<SssTable> sssTables = new ArrayList<>();
        sssTables.add(sssRow);
        when(sssTableRepository.findByEffectiveYearOrderByRangeFromAsc(anyInt())).thenReturn(sssTables);

        // Execute
        List<PayrollItems> result = payrollService.computePayroll(2, "monthly", "JANUARY", 2024);
        
        // Asserts
        assertEquals(1, result.size());
        PayrollItems item = result.get(0);
        
        // Statutory
        assertBigDecimalEquals("1750.00", item.getSss());
        // Philhealth: 36000 * 5% / 2 = 900
        assertBigDecimalEquals("900.00", item.getPhilhealth());
        // Pag-ibig: 36000 * 2% = 720
        assertBigDecimalEquals("720.00", item.getPagibig());
        // Tax: 36000 * 10% - 2395.90 = 3600 - 2395.90 = 1204.10
        assertBigDecimalEquals("1204.10", item.getTax());
        
        // Semi Monthly = (1750 + 900 + 720 + 1204.10) / 2 = 4574.10 / 2 = 2287.05
        assertBigDecimalEquals("2287.05", item.getSemiMonthlyContributions());
    }

    @Test
    void testJobOrderEmployee() {
        // Setup Employee (50,000 Monthly Rate)
        Employees emp = new Employees();
        emp.setEmploymentType("Job Order");
        emp.setBasicSalary(new BigDecimal("50000"));
        when(employeeRepository.findById(3)).thenReturn(Optional.of(emp));

        // Setup Attendance (160 hours worked, 10 hours OT)
        Attendance att = new Attendance();
        att.setAttendance_date(LocalDate.of(2024, Month.JANUARY, 10)); // Fixed date for test
        att.setWork_hours(new BigDecimal("160"));
        att.setOvertime_hours(new BigDecimal("10"));
        
        List<Attendance> attendances = new ArrayList<>();
        attendances.add(att);
        when(attendanceRepository.findByEmployeeIdOrderByDateDesc(3)).thenReturn(attendances);

        when(employeeDeductionsRepository.findByEmployeeId(3)).thenReturn(new ArrayList<>());

        // Execute
        List<PayrollItems> result = payrollService.computePayroll(3, "monthly", "JANUARY", 2024);
        
        // Asserts
        assertEquals(1, result.size());
        PayrollItems item = result.get(0);

        // Rates
        assertBigDecimalEquals("2500.00", item.getDailyRate());
        assertBigDecimalEquals("312.50", item.getHourlyRate());

        // Earnings
        // Basic: 312.50 * 160 = 50000.00
        assertBigDecimalEquals("50000.00", item.getBasicPay());
        
        // Overtime for Job Order is 1.0 multiplier
        // 312.50 * 10 * 1.0 = 3125.00
        assertBigDecimalEquals("3125.00", item.getOvertimePay());
        
        // Total Earnings = 53125.00
        assertBigDecimalEquals("53125.00", item.getTotalEarnings());

        // Statutory
        // Job Orders don't pay SSS, Philhealth, Pag-ibig
        assertBigDecimalEquals("0.00", item.getSss());
        assertBigDecimalEquals("0.00", item.getPhilhealth());
        assertBigDecimalEquals("0.00", item.getPagibig());
        
        // Tax is 5% EWT on total earnings
        // 53125.00 * 5% = 2656.25
        assertBigDecimalEquals("2656.25", item.getTax());
    }

    @Test
    void testAllowanceIsFixedPerCutoffAndStatutoryUsesBasicSalaryOnly() {
        Employees emp = new Employees();
        emp.setEmploymentType("Regular");
        emp.setBasicSalary(new BigDecimal("21000"));
        emp.setAllowance(new BigDecimal("3000"));
        when(employeeRepository.findById(4)).thenReturn(Optional.of(emp));

        Attendance att = new Attendance();
        att.setAttendance_date(LocalDate.of(2024, Month.JANUARY, 10));
        att.setWork_hours(new BigDecimal("80"));
        att.setOvertime_hours(new BigDecimal("4"));
        att.setLate_minutes(33);
        att.setUndertime_minutes(0);
        when(attendanceRepository.findByEmployeeIdOrderByDateDesc(4)).thenReturn(List.of(att));

        SssTable sssRow = new SssTable();
        sssRow.setRangeFrom(new BigDecimal("20750"));
        sssRow.setRangeTo(new BigDecimal("21249"));
        sssRow.setEmployeeShare(new BigDecimal("1000.00"));
        when(sssTableRepository.findByEffectiveYearOrderByRangeFromAsc(anyInt())).thenReturn(List.of(sssRow));

        when(employeeDeductionsRepository.findByEmployeeId(4)).thenReturn(new ArrayList<>());

        List<PayrollItems> result = payrollService.computePayroll(4, "semi_1", "JANUARY", 2024);

        assertEquals(1, result.size());
        PayrollItems item = result.get(0);

        assertBigDecimalEquals("1500.00", item.getAllowances());
        assertBigDecimalEquals("12656.25", item.getTotalEarnings());
        assertBigDecimalEquals("525.00", item.getPhilhealth());
        assertBigDecimalEquals("420.00", item.getPagibig());
        assertBigDecimalEquals("972.50", item.getSemiMonthlyContributions());
    }
}
