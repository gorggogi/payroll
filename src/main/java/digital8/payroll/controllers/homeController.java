package digital8.payroll.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import digital8.payroll.entities.Users;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class homeController {
    // @GetMapping("/index")
    // public String index() {
    //     return "html/index";
    // }
    @GetMapping("/admin/home")
    public String adminHome(HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        
        if (user == null){
            return "redirect:/index";
        }

        if (!"ADMIN".equals(user.getRole().getRoleName())){
            return "redirect:/access-denied";
        }
        return "html/homeAdmin";
    }

    @GetMapping("/employee/home")
    public String employeeHome(HttpSession session) {

        Users user = (Users) session.getAttribute("user");
        
        if (user == null){
            return "redirect:/index";
        }
        
        return "html/homeEmployee";
    }

    @GetMapping("/employees") 
    public String employees(HttpSession session) {

        Users user = (Users) session.getAttribute("user");

        if (user == null) {

            return "redirect:/index";
            
        }
        if (!"ADMIN".equals(user.getRole().getRoleName())){
            return "redirect:/access-denied";
        }
        return "html/employees";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "html/access-denied";
    }
    

// @PostMapping("/validateLogin")
// public String validateLogin(Employees emp, RedirectAttributes redi) {
//     redi.addFlashAttribute("message", "User has been saved...");
//     service.save(employees);
//     return "html/homeEmployee";
// }
}

    
