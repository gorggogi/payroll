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

import java.util.List;

@Controller
public class attendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @GetMapping("/employee/attendance")
    public String attendancePage(Model model, Authentication authentication) {
        Object principal = authentication != null ? authentication.getPrincipal() : null;

        if (principal instanceof Users) {
            Users user = (Users) principal;
            Employees emp = user.getEmployee();
            if (emp != null) {
                Integer empId = emp.getEmployeeId();
                List<Attendance> records = attendanceRepository.findByEmployeeIdOrderByDateDesc(empId);
                model.addAttribute("attendances", records);
                model.addAttribute("employeeName", emp.getFirstName() + " " + emp.getLastName());
            }
        }

        return "html/attendance";
    }
    
}
