package digital8.payroll.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @PostMapping("/validateLogin")
    public String validateLogin(Employees emp, RedirectAttributes redi) {
        redi.addFlashAttribute("message", "User has been saved...");
        service.save(employees);
        return "html/homeEmployee";
    }
}
