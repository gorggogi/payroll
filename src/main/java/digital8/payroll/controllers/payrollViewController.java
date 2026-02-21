package digital8.payroll.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import digital8.payroll.repositories.UsersRepository;
import digital8.payroll.entities.Users;

@Controller
@RequestMapping("/payroll")
public class payrollViewController {

    @Autowired
    private UsersRepository usersRepository;

    @GetMapping("/{empId}")
    public String payrollPage(@PathVariable Integer empId, Model model, Principal principal) {
        model.addAttribute("emp_id", empId);
        // Optionally set employee name if available from logged-in user
        if (principal != null) {
            String email = principal.getName();
            usersRepository.findByEmail(email).ifPresent(u -> {
                if (u.getEmployee() != null) {
                    String fullName = u.getEmployee().getFirstName() + " " + u.getEmployee().getLastName();
                    model.addAttribute("employeeName", fullName);
                }
            });
        }
        return "html/payroll";
    }
}
