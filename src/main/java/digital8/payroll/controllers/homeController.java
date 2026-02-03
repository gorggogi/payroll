package digital8.payroll.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



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
    
    
}
