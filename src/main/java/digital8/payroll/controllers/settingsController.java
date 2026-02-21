package digital8.payroll.controllers;

import digital8.payroll.entities.Employees;
import digital8.payroll.entities.Users;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class settingsController {
    
    @GetMapping("/employee/settings")
    public String employeeSettingsPage  (Model model, Authentication authentication){
        Object principal = authentication != null ? authentication.getPrincipal() : null;

        if (principal instanceof Users) {
            Users user = (Users) principal;
            Employees emp = user.getEmployee();

            if (emp != null){
                model.addAttribute("employee", emp);
                model.addAttribute("employeeName", emp.getFirstName() + " " + emp.getLastName());
                model.addAttribute("user", user);
            }
         }

         return "html/settingsEmployee";
    }
    }

