package digital8.payroll.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import digital8.payroll.entities.Users;
import digital8.payroll.entities.Employees;

@Controller
@RequestMapping({ "/admin/adjustments", "/employee/adjustments" })
public class AdjustmentsViewController {

    @GetMapping
    public String adjustmentsPage(Model model, Authentication authentication) {
        Integer empId = null;
        if (authentication != null && authentication.getPrincipal() instanceof Users) {
            Users user = (Users) authentication.getPrincipal();
            Employees emp = user.getEmployee();
            if (emp != null) {
                empId = emp.getEmployeeId();
                model.addAttribute("employeeName", emp.getFirstName() + " " + emp.getLastName());
            }
        }
        model.addAttribute("emp_id", empId != null ? empId : 0);
        if (!model.containsAttribute("employeeName")) {
            model.addAttribute("employeeName", "Employee");
        }
        return "html/adjustments";
    }
}
