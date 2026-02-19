package digital8.payroll.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class homeController {

    @GetMapping({"/", "/index"})
    public String loginPage() {
        return "html/index";
    }
    
    @GetMapping("/forgotPassword")
    public String forgotPasswordPage() {
        return "html/forgotPassword";
    }

    @GetMapping("/admin/home")
    public String adminHome() {
        return "html/homeAdmin";
    }

    @GetMapping("/employee/home")
    public String employeeHome() {
        return "html/homeEmployee";
    }

    @GetMapping("/admin/employees") 
    public String employees() {
        return "html/employees";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "html/access-denied";
    }

    @GetMapping("/generateHash") // Temporary for testing
    @ResponseBody
    public String generateHash(@RequestParam String password) {
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = 
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        String hash = encoder.encode(password);
        return "Password: " + password + "<br>Hash: " + hash;
    }
}
