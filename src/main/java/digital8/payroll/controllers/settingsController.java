package digital8.payroll.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class settingsController {
    @GetMapping("/employee/settings")
    public String forgotPasswordPage() {
        return "html/settingsEmployee";
    }
}
