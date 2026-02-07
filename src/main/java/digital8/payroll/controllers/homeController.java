package digital8.payroll.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class homeController {
    @GetMapping("/index")
    public String index() {
        return "html/index";
    }
    @GetMapping("/adminHome")
    public String adminHome() {
        return "html/homeAdmin";
    }

    @GetMapping("/employeeHome")
    public String employeeHome() {
        return "html/homeEmployee";
    }

    @GetMapping("/employees") 
    public String employees() {
        return "html/employees";
    }

// @PostMapping("/validateLogin")
// public String validateLogin(Employees emp, RedirectAttributes redi) {
//     redi.addFlashAttribute("message", "User has been saved...");
//     service.save(employees);
//     return "html/homeEmployee";
// }
}

    
