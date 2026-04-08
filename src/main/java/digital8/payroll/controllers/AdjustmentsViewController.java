package digital8.payroll.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import digital8.payroll.entities.Users;
import digital8.payroll.entities.Employees;
import digital8.payroll.repositories.UsersRepository;

@Controller
@RequestMapping({ "/admin/adjustments", "/employee/adjustments" })
public class AdjustmentsViewController {

    @Autowired
    private UsersRepository usersRepository;

    @GetMapping
    public String adjustmentsPage(Model model, Authentication authentication) {
        Users user = null;
        if (authentication != null && authentication.getPrincipal() instanceof Users) {
            user = (Users) authentication.getPrincipal();
        } else if (authentication != null) {
            user = usersRepository.findByEmail(authentication.getName()).orElse(null);
        }
        Integer empId = null;
        if (user != null && user.getEmployee() != null) {
            Employees emp = user.getEmployee();
            empId = emp.getEmployeeId();
            model.addAttribute("employeeName", emp.getFirstName() + " " + emp.getLastName());
        }
        if (empId != null) {
            model.addAttribute("emp_id", empId);
            model.addAttribute("self_emp_id", empId);
        }
        if (!model.containsAttribute("employeeName")) {
            model.addAttribute("employeeName", "Employee");
        }
        return "html/adjustments";
    }
}
