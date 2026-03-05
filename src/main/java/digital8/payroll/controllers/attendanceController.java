package digital8.payroll.controllers;

import digital8.payroll.entities.Attendance;
import digital8.payroll.entities.Employees;
import digital8.payroll.entities.Users;
import digital8.payroll.repositories.AttendanceRepository;
import digital8.payroll.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping({"/employee/attendance", "/admin/attendance"})
    public String attendancePage(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer empId,
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

        boolean isAdmin = authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        // Logged-in user's own employee id (for nav links / "my payroll")
        Integer selfEmpId = null;
        if (authentication != null && authentication.getPrincipal() instanceof Users) {
            Users currentUser = (Users) authentication.getPrincipal();
            if (currentUser.getEmployee() != null) {
                selfEmpId = currentUser.getEmployee().getEmployeeId();
            }
        }
        Integer targetEmpId = null;
        Employees targetEmp = null;

        if (isAdmin && empId != null && request.getRequestURI().startsWith("/admin")) {
            targetEmpId = empId;
            targetEmp = employeeRepository.findById(empId).orElse(null);
            model.addAttribute("viewingEmployeeId", empId);
        }
        if (targetEmp == null) {
            Object principal = authentication != null ? authentication.getPrincipal() : null;
            if (principal instanceof Users) {
                Users user = (Users) principal;
                targetEmp = user.getEmployee();
                if (targetEmp != null) targetEmpId = targetEmp.getEmployeeId();
            }
        }

        if (targetEmpId != null && targetEmp != null) {
            List<Attendance> all = attendanceRepository.findByEmployeeIdOrderByDateDesc(targetEmpId);
            List<Attendance> filtered = all.stream()
                    .filter(a -> a.getAttendance_date() != null
                            && a.getAttendance_date().getMonthValue() == selectedMonth
                            && a.getAttendance_date().getYear() == selectedYear)
                    .collect(Collectors.toList());
            model.addAttribute("attendances", filtered);
            model.addAttribute("employeeName", targetEmp.getFirstName() + " " + targetEmp.getLastName());
            model.addAttribute("emp_id", targetEmpId); // currently viewed employee
            model.addAttribute("emp_payType", targetEmp.getPayType());
            BigDecimal total = filtered.stream()
                    .map(Attendance::getWork_hours)
                    .filter(h -> h != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("totalHoursRendered", total);
        }
        // For nav: always use the logged-in user's own employee id when available
        if (selfEmpId != null) {
            model.addAttribute("self_emp_id", selfEmpId);
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
