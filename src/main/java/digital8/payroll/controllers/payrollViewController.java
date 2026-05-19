package digital8.payroll.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import digital8.payroll.dto.DeductionBreakdownItem;
import digital8.payroll.entities.Users;
import digital8.payroll.repositories.PayrollItemsRepository;
import digital8.payroll.repositories.UsersRepository;
import digital8.payroll.entities.PayrollItems;
import digital8.payroll.services.PayrollService;
import digital8.payroll.services.EmployeeService;

@Controller
@RequestMapping("/payroll")
public class payrollViewController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private PayrollService payrollService;
    @Autowired
    private PayrollItemsRepository payrollItemsRepository;
    @Autowired
    private UsersRepository usersRepository;

    /**
     * Nav and templates sometimes hit /payroll or /payroll/ when no employee id is in the URL.
     * Redirect to the signed-in user's payroll when we can resolve their employee id.
     */
    @GetMapping(path = { "", "/" })
    public RedirectView payrollRoot(Authentication authentication) {
        Integer empId = resolveCurrentUserEmployeeId(authentication);
        if (empId != null) {
            return new RedirectView("/payroll/" + empId, false);
        }
        boolean admin = authentication != null && authentication.getPrincipal() instanceof Users
                && ((Users) authentication.getPrincipal()).getRole() != null
                && "ADMIN".equalsIgnoreCase(((Users) authentication.getPrincipal()).getRole().getRoleName());
        return new RedirectView(admin ? "/admin/home" : "/employee/home", false);
    }

    @GetMapping("/{empId}")
    public Object payrollPage(
            @PathVariable Integer empId,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String period,
            Model model,
            Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Users) {
            Users user = (Users) authentication.getPrincipal();
            boolean isAdmin = user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getRoleName());
            if (!isAdmin && (user.getEmployee() == null || !empId.equals(user.getEmployee().getEmployeeId()))) {
                Integer ownId = user.getEmployee() != null ? user.getEmployee().getEmployeeId() : null;
                if (ownId != null) {
                    UriComponentsBuilder b = UriComponentsBuilder.fromPath("/payroll/" + ownId);
                    if (month != null && !month.isBlank()) b.queryParam("month", month);
                    if (year != null) b.queryParam("year", year);
                    if (period != null && !period.isBlank()) b.queryParam("period", period);
                    return new RedirectView(b.build().toUriString(), false);
                }
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this payroll.");
            }
        }
        model.addAttribute("emp_id", empId);

        employeeService.getEmployeeById(empId).ifPresent(emp -> {
                model.addAttribute("employeeName", emp.getFirstName() + " " + emp.getLastName());
                model.addAttribute("employee", emp);
        });
        if (!model.containsAttribute("employeeName")) {
            model.addAttribute("employeeName", "Employee");
        }

        int currentYear = java.time.Year.now().getValue();
        int selectedYear = (year != null) ? year : currentYear;
        String monthName = (month != null && !month.isBlank()) ? month : Month.of(java.time.LocalDate.now().getMonthValue()).name();
        String effectivePeriod = (period != null && !period.isBlank()) ? period : "monthly";
        List<PayrollItems> items = payrollService.computePayroll(empId, effectivePeriod, monthName, selectedYear);
        if (items == null || items.isEmpty()) {
            items = payrollItemsRepository.findByEmployeeIdOrderByPayrollItemIdDesc(empId);
        }
        model.addAttribute("payrollItems", items != null ? items : List.of());
        model.addAttribute("selectedMonth", monthName);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("selectedPeriod", effectivePeriod);
        model.addAttribute("payrollYears", java.util.List.of(currentYear + 1, currentYear, currentYear - 1));

        Month monthEnum;
        try {
            monthEnum = monthName != null ? Month.valueOf(monthName.toUpperCase()) : Month.from(java.time.LocalDate.now());
        } catch (Exception e) {
            monthEnum = Month.from(java.time.LocalDate.now());
        }
        LocalDate[] bounds = payrollService.getPayrollPeriodBounds(selectedYear, monthEnum, effectivePeriod);
        List<DeductionBreakdownItem> deductionsBreakdown =
                payrollService.getDeductionsBreakdown(empId, bounds[0], bounds[1], effectivePeriod);
        model.addAttribute("otherDeductionsBreakdown", deductionsBreakdown != null ? deductionsBreakdown : List.of());

        return "html/payroll";
    }

    private Integer resolveCurrentUserEmployeeId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        if (authentication.getPrincipal() instanceof Users) {
            Users user = (Users) authentication.getPrincipal();
            if (user.getEmployee() != null) {
                return user.getEmployee().getEmployeeId();
            }
            return null;
        }
        return usersRepository.findByEmail(authentication.getName())
                .map(u -> u.getEmployee() != null ? u.getEmployee().getEmployeeId() : null)
                .orElse(null);
    }
}
