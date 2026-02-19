package digital8.payroll.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class attendanceController {
    @GetMapping("/employee/attendance")
    public String forgotPasswordPage() {
        return "html/attendance";
    }
    
}
