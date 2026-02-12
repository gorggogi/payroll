package digital8.payroll.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import digital8.payroll.entities.Users;
import digital8.payroll.services.UserService;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@Controller
public class AuthController {
    @Autowired
    private UserService userService;    

    @GetMapping({"/", "/index"})
    public String LoginPage() {
        return "html/index";
    }
    
    @PostMapping("/login")
    public String login(
        @RequestParam String email, 
        @RequestParam String password,
        HttpSession session,
        Model model) {

        Users user = userService.authenticate(email, password);

        if(user != null){
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("roleName", user.getRole());

        String roleName = user.getRole().getRoleName();
        if ("ADMIN".equals(roleName)){
            return "redirect:/admin/home";
        }

        else {
            return "redirect:/employee/home";
        }

    } else {
        System.out.println("Login failed. " + email);
        model.addAttribute("Error", "Invalid email or password");
        return "html/index";
    }
    
    }
    
@GetMapping("/logout")
public String logout(HttpSession session) {
    session.invalidate();
    return "redirect:/index";
}

@GetMapping("/forgotPassword")
public String forgotPasswordPage() {
    return "html/forgotPassword";
}


}
