package digital8.payroll.controllers;

import digital8.payroll.entities.Employees;
import digital8.payroll.entities.Users;
import digital8.payroll.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class settingsController {

    @Autowired

    UserService userService;
    
    @GetMapping({"/employee/settings", "/admin/settings"})
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
    @PostMapping("/employee/settings/change-password")
    public String changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        
        Object principal = authentication != null ? authentication.getPrincipal() : null;
        
        if (!(principal instanceof Users)) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not authenticated");
            return "redirect:/employee/settings";
        }
        
        Users user = (Users) principal;
        
    
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match");
            return "redirect:/employee/settings";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 6 characters");
            return "redirect:/employee/settings";
        }
      
        boolean success = userService.changePassword(user, currentPassword, newPassword);
        
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect");
        }
        
        return "redirect:/employee/settings";
    }
}
    

