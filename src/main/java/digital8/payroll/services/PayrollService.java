package digital8.payroll.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import digital8.payroll.repositories.EmployeeRepository;
import digital8.payroll.repositories.AttendanceRepository;
import digital8.payroll.entities.PayrollItems;
import digital8.payroll.entities.Employees;
import digital8.payroll.entities.Attendance;

import java.math.BigDecimal;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PayrollService {

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private AttendanceRepository attendanceRepository;

	public List<PayrollItems> computePayroll(Integer empId, String period, String monthName) {
		Optional<Employees> empOpt = employeeRepository.findById(empId);
		if (empOpt.isEmpty()) return new ArrayList<>();

		Employees emp = empOpt.get();
		BigDecimal monthlySalary = emp.getBasicSalary();
		if (monthlySalary == null) monthlySalary = BigDecimal.ZERO;

		// determine effective period: prefer explicit param, otherwise use employee payType
		String effectivePeriod = period;
		if (effectivePeriod == null || effectivePeriod.isBlank()) {
			String pt = emp.getPayType();
			effectivePeriod = (pt != null) ? pt : "monthly";
		}
		boolean isBiweekly = "biweekly".equalsIgnoreCase(effectivePeriod);
		BigDecimal basicPay = isBiweekly ? monthlySalary.divide(new BigDecimal(2)) : monthlySalary;

		// Determine month filter
		Month month = null;
		if (monthName != null) {
			try {
				month = Month.valueOf(monthName.toUpperCase());
			} catch (Exception e) { month = null; }
		}

		// Fetch attendance and sum overtime hours for the month (if provided) or all records otherwise
		List<Attendance> records = attendanceRepository.findByEmployeeIdOrderByDateDesc(empId);
		BigDecimal totalOvertime = BigDecimal.ZERO;
		for (Attendance a : records) {
			if (month != null) {
				if (a.getAttendance_date() == null) continue;
				if (a.getAttendance_date().getMonth() != month) continue;
			}
			if (a.getOvertime_hours() != null) totalOvertime = totalOvertime.add(a.getOvertime_hours());
		}

		// Calculate hourly rate - assume 208 working hours per month
		BigDecimal hoursPerMonth = new BigDecimal(208);
		BigDecimal hourlyRate = hoursPerMonth.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : monthlySalary.divide(hoursPerMonth, 6, BigDecimal.ROUND_HALF_UP);

		BigDecimal overtimePay = totalOvertime.multiply(hourlyRate).multiply(new BigDecimal("1.5"));

		BigDecimal grossPay = basicPay.add(overtimePay);

		// Simple placeholder deductions (could be replaced with real formulas)
		BigDecimal sss = BigDecimal.ZERO;
		BigDecimal philhealth = BigDecimal.ZERO;
		BigDecimal pagibig = BigDecimal.ZERO;
		BigDecimal tax = BigDecimal.ZERO;
		BigDecimal otherDeductions = BigDecimal.ZERO;

		BigDecimal totalDeductions = sss.add(philhealth).add(pagibig).add(tax).add(otherDeductions);
		BigDecimal netPay = grossPay.subtract(totalDeductions);

		PayrollItems item = new PayrollItems();
		item.setEmployeeId(empId);
		item.setBasicPay(basicPay);
		item.setOvertimePay(overtimePay);
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
}
