package digital8.payroll.controllers;

import digital8.payroll.entities.Employees;
import digital8.payroll.entities.Users;
import digital8.payroll.services.UserService;
import digital8.payroll.services.EmployeeService;

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

    @Autowired
    EmployeeService employeeService;
    
    @GetMapping({"/employee/settings"})
    public String employeeSettingsPage  (Model model, Authentication authentication){
        Object principal = authentication != null ? authentication.getPrincipal() : null;

        if (principal instanceof Users) {
            Users user = (Users) principal;
            Employees emp = user.getEmployee();

            if (emp != null){
                model.addAttribute("employee", emp);
                model.addAttribute("emp_id", emp.getEmployeeId());
                model.addAttribute("employeeName", emp.getFirstName() + " " + emp.getLastName());
                model.addAttribute("user", user);
            }
         }

         return "html/settingsEmployee";
    }
    
    @GetMapping({"/admin/settings"})
    public String adminSettingsPage  (Model model, Authentication authentication){
        Object principal = authentication != null ? authentication.getPrincipal() : null;

        if (principal instanceof Users) {
            Users user = (Users) principal;
            Employees emp = user.getEmployee();

            if (emp != null){
                model.addAttribute("employee", emp);
                model.addAttribute("emp_id", emp.getEmployeeId());
                model.addAttribute("employeeName", emp.getFirstName() + " " + emp.getLastName());
                model.addAttribute("user", user);
            }
         }

         return "html/settingsAdmin";
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

    @PostMapping("/admin/settings/change-password")
    public String adminChangePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        Object principal = authentication != null ? authentication.getPrincipal() : null;
        if (!(principal instanceof Users)) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not authenticated");
            return "redirect:/admin/settings";
        }
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match");
            return "redirect:/admin/settings";
        }
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 6 characters");
            return "redirect:/admin/settings";
        }
        boolean success = userService.changePassword((Users) principal, currentPassword, newPassword);
        if (success) redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
        else redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect");
        return "redirect:/admin/settings";
    }

    @PostMapping("/admin/settings/profile")
    public String updateAdminProfile(
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "middleName", required = false) String middleName,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "contactNumber", required = false) String contactNumber,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "employeeNumber", required = false) String employeeNumber,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        Object principal = authentication != null ? authentication.getPrincipal() : null;
        if (!(principal instanceof Users)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Not authenticated");
            return "redirect:/admin/settings";
        }
        Users user = (Users) principal;
        Employees emp = user.getEmployee();
        if (emp == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Employee record not found");
            return "redirect:/admin/settings";
        }
        if (email != null && !email.isBlank()) {
            if (!userService.updateEmail(user, email.trim())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email already in use");
                return "redirect:/admin/settings";
            }
        }
        if (firstName != null && !firstName.isBlank()) emp.setFirstName(firstName.trim());
        if (lastName != null && !lastName.isBlank()) emp.setLastName(lastName.trim());
        if (middleName != null) emp.setMiddleName(middleName.trim());
        if (contactNumber != null) emp.setContactNumber(contactNumber.trim());
        if (address != null) emp.setAddress(address.trim());
        if (employeeNumber != null && !employeeNumber.isBlank()) {
            if (employeeService.isEmployeeNumberTakenByOther(employeeNumber.trim(), emp.getEmployeeId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Employee number already in use");
                return "redirect:/admin/settings";
            }
            emp.setEmployeeNumber(employeeNumber.trim());
        }
        employeeService.updateEmployee(emp.getEmployeeId(), emp);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated.");
        return "redirect:/admin/settings";
    }

}
    

