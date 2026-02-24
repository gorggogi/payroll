package digital8.payroll.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import digital8.payroll.dto.EmployeeDeductionRowDto;
import digital8.payroll.entities.Deductions;
import digital8.payroll.entities.EmployeeDeductions;
import digital8.payroll.entities.Employees;
import digital8.payroll.entities.Users;
import digital8.payroll.repositories.DeductionsRepository;
import digital8.payroll.repositories.EmployeeDeductionsRepository;
import digital8.payroll.repositories.EmployeeRepository;

@Controller
@RequestMapping({"/admin/deductions" , "/employee/deductions"})
public class DeductionViewController {

    @Autowired
    private DeductionsRepository deductionsRepository;
    @Autowired
    private EmployeeDeductionsRepository employeeDeductionsRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping
    public String deductionsPage(Model model, Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        List<Deductions> types = deductionsRepository.findAllByOrderByDeductionNameAsc();
        List<EmployeeDeductions> assignments;
        List<Employees> employees;

        if (isAdmin) {
            assignments = employeeDeductionsRepository.findAll();
            employees = employeeRepository.findAll();
            if (authentication != null && authentication.getPrincipal() instanceof Users) {
                Employees adminEmp = ((Users) authentication.getPrincipal()).getEmployee();
                if (adminEmp != null) model.addAttribute("emp_id", adminEmp.getEmployeeId());
            }
        } else {
            Integer empId = null;
            if (authentication != null && authentication.getPrincipal() instanceof Users) {
                Employees emp = ((Users) authentication.getPrincipal()).getEmployee();
                if (emp != null) empId = emp.getEmployeeId();
            }
            assignments = empId != null ? employeeDeductionsRepository.findByEmployeeId(empId) : List.of();
            employees = List.of();
            if (empId != null) model.addAttribute("emp_id", empId);
        }

        Map<Integer, String> employeeNames = isAdmin && !employees.isEmpty()
                ? employees.stream().collect(Collectors.toMap(Employees::getEmployeeId, e -> e.getLastName() + ", " + e.getFirstName()))
                : Map.of();
        Map<Integer, String> deductionNames = types.stream()
                .collect(Collectors.toMap(Deductions::getDeductionId, Deductions::getDeductionName));

        List<EmployeeDeductionRowDto> rows = new ArrayList<>();
        for (EmployeeDeductions ed : assignments) {
            EmployeeDeductionRowDto dto = new EmployeeDeductionRowDto();
            dto.setEmployeeDeductionId(ed.getEmployeeDeductionId());
            dto.setEmployeeId(ed.getEmployeeId());
            dto.setEmployeeName(employeeNames.getOrDefault(ed.getEmployeeId(), "?"));
            dto.setDeductionId(ed.getDeductionId());
            dto.setDeductionName(deductionNames.getOrDefault(ed.getDeductionId(), "?"));
            dto.setAmount(ed.getAmount());
            dto.setRecurring(ed.getIsRecurring());
            dto.setStartDate(ed.getStartDate());
            dto.setEndDate(ed.getEndDate());
            rows.add(dto);
        }

        model.addAttribute("deductionTypes", isAdmin ? types : List.of());
        model.addAttribute("assignmentRows", rows);
        model.addAttribute("employees", employees);
        return "html/deductions";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/types")
    public String addType(
            @RequestParam String deductionName,
            @RequestParam String deductionType,
            RedirectAttributes ra) {
        Deductions d = new Deductions();
        d.setDeductionName(deductionName);
        d.setDeductionType(deductionType != null ? deductionType : "Other");
        deductionsRepository.save(d);
        ra.addFlashAttribute("message", "Deduction type added.");
        return "redirect:/admin/deductions";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/assign")
    public String assignDeduction(
            @RequestParam Integer employeeId,
            @RequestParam Integer deductionId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) Boolean isRecurring,
            @RequestParam String startDate,
            @RequestParam String endDate,
            RedirectAttributes ra) {
        EmployeeDeductions ed = new EmployeeDeductions();
        ed.setEmployeeId(employeeId);
        ed.setDeductionId(deductionId);
        ed.setAmount(amount);
        ed.setIsRecurring(isRecurring == null || Boolean.TRUE.equals(isRecurring));
        ed.setStartDate(LocalDate.parse(startDate));
        ed.setEndDate(LocalDate.parse(endDate));
        employeeDeductionsRepository.save(ed);
        ra.addFlashAttribute("message", "Deduction assigned to employee.");
        return "redirect:/admin/deductions";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/assign/remove")
    public String removeAssignment(@RequestParam Integer id, RedirectAttributes ra) {
        employeeDeductionsRepository.findById(id).ifPresent(employeeDeductionsRepository::delete);
        ra.addFlashAttribute("message", "Assignment removed.");
        return "redirect:/admin/deductions";
    }
}
