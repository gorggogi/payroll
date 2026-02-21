package digital8.payroll.controllers;

import digital8.payroll.entities.Attendance;
import digital8.payroll.entities.Employees;
import digital8.payroll.entities.Users;
import digital8.payroll.repositories.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class attendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @GetMapping({"/employee/attendance", "/admin/attendance"})
    public String attendancePage(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            HttpServletRequest request,
            Model model,
            Authentication authentication) {
        model.addAttribute("attendanceFormAction", request.getRequestURI());
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();
        int selectedYear = year != null ? year : currentYear;
        int selectedMonth = (month != null && month >= 1 && month <= 12) ? month : currentMonth;

        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("selectedYear", selectedYear);
        String monthName = Month.of(selectedMonth).toString().charAt(0) + Month.of(selectedMonth).toString().substring(1).toLowerCase();
        model.addAttribute("captionMonthYear", monthName + " " + selectedYear);
        List<Integer> years = new ArrayList<>();
        for (int y = currentYear - 5; y <= currentYear + 2; y++) years.add(y);
        model.addAttribute("years", years);

        Object principal = authentication != null ? authentication.getPrincipal() : null;
        if (principal instanceof Users) {
            Users user = (Users) principal;
            Employees emp = user.getEmployee();
            if (emp != null) {
                Integer empId = emp.getEmployeeId();
                List<Attendance> all = attendanceRepository.findByEmployeeIdOrderByDateDesc(empId);
                List<Attendance> filtered = all.stream()
                        .filter(a -> a.getAttendance_date() != null
                                && a.getAttendance_date().getMonthValue() == selectedMonth
                                && a.getAttendance_date().getYear() == selectedYear)
                        .collect(Collectors.toList());
                model.addAttribute("attendances", filtered);
                model.addAttribute("employeeName", emp.getFirstName() + " " + emp.getLastName());
                model.addAttribute("emp_id", empId);
                model.addAttribute("emp_payType", emp.getPayType());

                BigDecimal total = filtered.stream()
                        .map(Attendance::getWork_hours)
                        .filter(h -> h != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                model.addAttribute("totalHoursRendered", total);
            }
        }
        if (!model.containsAttribute("attendances")) {
            model.addAttribute("attendances", List.<Attendance>of());
        }
        if (!model.containsAttribute("totalHoursRendered")) {
            model.addAttribute("totalHoursRendered", BigDecimal.ZERO);
        }
        return "html/attendance";
    }
}
