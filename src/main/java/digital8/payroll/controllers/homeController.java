package digital8.payroll.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ui.Model;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;

import digital8.payroll.repositories.UsersRepository;
import digital8.payroll.entities.Users;

@Controller
public class homeController {

    @Autowired
    private UsersRepository usersRepository;

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
    public String employeeHome(Model model, org.springframework.security.core.Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Users) {
                Users u = (Users) principal;
                if (u.getEmployee() != null) {
                    model.addAttribute("emp_id", u.getEmployee().getEmployeeId());
                    String fullName = u.getEmployee().getFirstName() + " " + u.getEmployee().getLastName();
                    model.addAttribute("employeeName", fullName);
                }
            } else {
                // fallback: try lookup by authentication name (could be email)
                String name = authentication.getName();
                usersRepository.findByEmail(name).ifPresent(user -> {
                    Users u = user;
                    if (u.getEmployee() != null) {
                        model.addAttribute("emp_id", u.getEmployee().getEmployeeId());
                        String fullName = u.getEmployee().getFirstName() + " " + u.getEmployee().getLastName();
                        model.addAttribute("employeeName", fullName);
                    }
                });
            }
        }
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
