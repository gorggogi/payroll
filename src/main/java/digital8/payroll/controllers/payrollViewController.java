package digital8.payroll.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.Month;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import digital8.payroll.repositories.UsersRepository;
import digital8.payroll.repositories.PayrollItemsRepository;
import digital8.payroll.entities.PayrollItems;
import digital8.payroll.services.PayrollService;

@Controller
@RequestMapping("/payroll")
public class payrollViewController {

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private PayrollService payrollService;
    @Autowired
    private PayrollItemsRepository payrollItemsRepository;

    @GetMapping("/{empId}")
    public String payrollPage(
            @PathVariable Integer empId,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String period,
            Model model,
            Principal principal) {
        model.addAttribute("emp_id", empId);

        if (principal != null) {
            String email = principal.getName();
            usersRepository.findByEmail(email).ifPresent(u -> {
                if (u.getEmployee() != null) {
                    String fullName = u.getEmployee().getFirstName() + " " + u.getEmployee().getLastName();
                    model.addAttribute("employeeName", fullName);
                }
            });
        }

        String monthName = (month != null && !month.isBlank()) ? month : Month.of(java.time.LocalDate.now().getMonthValue()).name();
        List<PayrollItems> items = payrollService.computePayroll(empId, period, monthName);
        if (items == null || items.isEmpty()) {
            items = payrollItemsRepository.findByEmployeeIdOrderByPayrollItemIdDesc(empId);
        }
        model.addAttribute("payrollItems", items != null ? items : List.of());
        model.addAttribute("selectedMonth", monthName);
        model.addAttribute("selectedPeriod", period != null ? period : "");

        return "html/payroll";
    }
}
