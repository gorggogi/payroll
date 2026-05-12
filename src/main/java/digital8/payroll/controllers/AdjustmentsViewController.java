package digital8.payroll.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import digital8.payroll.dto.EmployeeAdjustmentRowDto;
import digital8.payroll.entities.Adjustments;
import digital8.payroll.entities.EmployeeAdjustments;
import digital8.payroll.entities.Employees;
import digital8.payroll.entities.Users;
import digital8.payroll.repositories.AdjustmentsRepository;
import digital8.payroll.repositories.EmployeeAdjustmentsRepository;
import digital8.payroll.repositories.EmployeeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Controller
@RequestMapping({"/admin/adjustments", "/employee/adjustments"})
public class AdjustmentsViewController {

    @Autowired
    private AdjustmentsRepository adjustmentsRepository;
    @Autowired
    private EmployeeAdjustmentsRepository employeeAdjustmentsRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    // ------------------------------------------------------------------ GET

    @GetMapping
    public String adjustmentsPage(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer filterMonth,
            @RequestParam(required = false) Integer filterYear,
            @RequestParam(required = false) String filterType,
            @RequestParam(required = false) String filterRecurring,
            Model model, Authentication authentication) {

        boolean isAdmin = authentication != null
                && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        List<Adjustments> types = adjustmentsRepository.findAllByOrderByAdjustmentNameAsc();
        List<EmployeeAdjustments> assignments;
        List<Employees> employees;

        if (isAdmin) {
            assignments = employeeAdjustmentsRepository.findAll();
            employees = employeeRepository.findAllWithFetch(null, Sort.by(Sort.Direction.ASC, "lastName"));
            if (authentication.getPrincipal() instanceof Users) {
                Employees adminEmp = ((Users) authentication.getPrincipal()).getEmployee();
                if (adminEmp != null) model.addAttribute("emp_id", adminEmp.getEmployeeId());
            }
        } else {
            Integer empId = resolveEmployeeId(authentication);
            assignments = empId != null ? employeeAdjustmentsRepository.findByEmployeeId(empId) : List.of();
            employees = List.of();
            if (empId != null) model.addAttribute("emp_id", empId);
        }

        Map<Integer, String> employeeNames = isAdmin && !employees.isEmpty()
                ? employees.stream().collect(Collectors.toMap(Employees::getEmployeeId,
                        e -> e.getLastName() + ", " + e.getFirstName()))
                : Map.of();
        Map<Integer, String> adjustmentNames = types.stream()
                .collect(Collectors.toMap(Adjustments::getAdjustmentId, Adjustments::getAdjustmentName));
        Map<Integer, String> adjustmentTypeMap = types.stream()
                .collect(Collectors.toMap(Adjustments::getAdjustmentId, Adjustments::getAdjustmentType));

        List<EmployeeAdjustmentRowDto> rows = new ArrayList<>();
        for (EmployeeAdjustments ea : assignments) {
            EmployeeAdjustmentRowDto dto = new EmployeeAdjustmentRowDto();
            dto.setEmployeeAdjustmentId(ea.getEmployeeAdjustmentId());
            dto.setEmployeeId(ea.getEmployeeId());
            dto.setEmployeeName(employeeNames.getOrDefault(ea.getEmployeeId(), "?"));
            dto.setAdjustmentId(ea.getAdjustmentId());
            dto.setAdjustmentName(adjustmentNames.getOrDefault(ea.getAdjustmentId(), "?"));
            dto.setAdjustmentType(adjustmentTypeMap.getOrDefault(ea.getAdjustmentId(), "?"));
            dto.setAmount(ea.getAmount());
            dto.setRecurring(ea.getIsRecurring());
            dto.setStartDate(ea.getStartDate());
            dto.setEndDate(ea.getEndDate());
            dto.setApplyOnCutoff(ea.getApplyOnCutoff());
            rows.add(dto);
        }

        // --- Filtering (mirrors DeductionViewController) ---
        Stream<EmployeeAdjustmentRowDto> stream = rows.stream();

        if (search != null && !search.isBlank()) {
            String term = search.trim().toLowerCase();
            stream = stream.filter(r ->
                    (r.getEmployeeName() != null && r.getEmployeeName().toLowerCase().contains(term))
                 || (r.getAdjustmentName() != null && r.getAdjustmentName().toLowerCase().contains(term)));
        }
        if (filterType != null && !filterType.isBlank() && !filterType.equalsIgnoreCase("all")) {
            String type = filterType.trim();
            stream = stream.filter(r -> type.equals(adjustmentTypeMap.get(r.getAdjustmentId())));
        }
        if (filterRecurring != null && !filterRecurring.isBlank() && !filterRecurring.equalsIgnoreCase("all")) {
            boolean rec = "yes".equalsIgnoreCase(filterRecurring.trim());
            stream = stream.filter(r -> Boolean.valueOf(rec).equals(r.getRecurring()));
        }
        if (filterMonth != null && filterMonth >= 1 && filterMonth <= 12 && filterYear != null) {
            LocalDate fStart = LocalDate.of(filterYear, filterMonth, 1);
            LocalDate fEnd   = fStart.withDayOfMonth(fStart.lengthOfMonth());
            stream = stream.filter(r -> {
                if (Boolean.TRUE.equals(r.getRecurring())) {
                    return r.getStartDate() != null && r.getEndDate() != null
                            && !r.getStartDate().isAfter(fEnd)
                            && !r.getEndDate().isBefore(fStart);
                }
                return r.getStartDate() != null
                        && r.getStartDate().getMonthValue() == filterMonth
                        && r.getStartDate().getYear() == filterYear;
            });
        } else if (filterMonth != null && filterMonth >= 1 && filterMonth <= 12) {
            stream = stream.filter(r -> {
                if (Boolean.TRUE.equals(r.getRecurring())) {
                    return r.getStartDate() != null && r.getEndDate() != null
                            && r.getStartDate().getMonthValue() <= filterMonth
                            && r.getEndDate().getMonthValue() >= filterMonth;
                }
                return r.getStartDate() != null && r.getStartDate().getMonthValue() == filterMonth;
            });
        } else if (filterYear != null) {
            LocalDate yearStart = LocalDate.of(filterYear, 1, 1);
            LocalDate yearEnd   = LocalDate.of(filterYear, 12, 31);
            stream = stream.filter(r -> {
                if (Boolean.TRUE.equals(r.getRecurring())) {
                    return r.getStartDate() != null && r.getEndDate() != null
                            && !r.getStartDate().isAfter(yearEnd)
                            && !r.getEndDate().isBefore(yearStart);
                }
                return r.getStartDate() != null && r.getStartDate().getYear() == filterYear;
            });
        }
        rows = stream.collect(Collectors.toList());

        List<Integer> filterYears = IntStream.rangeClosed(Year.now().getValue() - 5, Year.now().getValue() + 1)
                .boxed().sorted((a, b) -> Integer.compare(b, a)).toList();
        List<String> filterMonthNames = List.of("January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December");

        model.addAttribute("adjustmentTypes", isAdmin ? types : List.of());
        model.addAttribute("assignmentRows", rows);
        model.addAttribute("employees", employees);
        model.addAttribute("search", search);
        model.addAttribute("filterMonth", filterMonth);
        model.addAttribute("filterYear", filterYear);
        model.addAttribute("filterYears", filterYears);
        model.addAttribute("filterMonthNames", filterMonthNames);
        model.addAttribute("filterType", filterType);
        model.addAttribute("filterRecurring", filterRecurring);
        model.addAttribute("adjustmentsFormAction", isAdmin ? "/admin/adjustments" : "/employee/adjustments");
        return "html/adjustments";
    }

    // ------------------------------------------------------------------ Type catalog (mirrors /types)

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/types")
    public String addType(
            @RequestParam String adjustmentName,
            @RequestParam String adjustmentType,
            RedirectAttributes ra) {
        Adjustments a = new Adjustments();
        a.setAdjustmentName(adjustmentName);
        a.setAdjustmentType(adjustmentType != null ? adjustmentType : "Earnings");
        adjustmentsRepository.save(a);
        ra.addFlashAttribute("message", "Adjustment type added.");
        return "redirect:/admin/adjustments";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/types/update")
    public String updateType(
            @RequestParam Integer id,
            @RequestParam String adjustmentName,
            @RequestParam String adjustmentType,
            RedirectAttributes ra) {
        adjustmentsRepository.findById(id).ifPresent(a -> {
            a.setAdjustmentName(adjustmentName);
            a.setAdjustmentType(adjustmentType != null ? adjustmentType : "Earnings");
            adjustmentsRepository.save(a);
        });
        ra.addFlashAttribute("message", "Adjustment type updated.");
        return "redirect:/admin/adjustments";
    }

    // ------------------------------------------------------------------ Assignments

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/assign")
    public String assignAdjustment(
            @RequestParam Integer employeeId,
            @RequestParam Integer adjustmentId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) Boolean isRecurring,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false, defaultValue = "BOTH") String applyOnCutoff,
            RedirectAttributes ra) {
        EmployeeAdjustments ea = new EmployeeAdjustments();
        ea.setEmployeeId(employeeId);
        ea.setAdjustmentId(adjustmentId);
        ea.setAmount(amount);
        ea.setIsRecurring(Boolean.TRUE.equals(isRecurring));
        ea.setStartDate(LocalDate.parse(startDate));
        ea.setEndDate(LocalDate.parse(endDate));
        ea.setApplyOnCutoff(applyOnCutoff);
        employeeAdjustmentsRepository.save(ea);
        ra.addFlashAttribute("message", "Adjustment assigned to employee.");
        return "redirect:/admin/adjustments";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/assign/remove")
    public String removeAssignment(@RequestParam Integer id, RedirectAttributes ra) {
        employeeAdjustmentsRepository.findById(id).ifPresent(employeeAdjustmentsRepository::delete);
        ra.addFlashAttribute("message", "Assignment removed.");
        return "redirect:/admin/adjustments";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/assign/edit")
    public String editAssignment(
            @RequestParam Integer id,
            @RequestParam Integer employeeId,
            @RequestParam Integer adjustmentId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) Boolean isRecurring,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false, defaultValue = "BOTH") String applyOnCutoff,
            RedirectAttributes ra) {
        employeeAdjustmentsRepository.findById(id).ifPresent(ea -> {
            ea.setEmployeeId(employeeId);
            ea.setAdjustmentId(adjustmentId);
            ea.setAmount(amount);
            ea.setIsRecurring(Boolean.TRUE.equals(isRecurring));
            ea.setStartDate(LocalDate.parse(startDate));
            ea.setEndDate(LocalDate.parse(endDate));
            ea.setApplyOnCutoff(applyOnCutoff);
            employeeAdjustmentsRepository.save(ea);
        });
        ra.addFlashAttribute("message", "Adjustment assignment updated.");
        return "redirect:/admin/adjustments";
    }

    // ------------------------------------------------------------------ helper

    private Integer resolveEmployeeId(Authentication authentication) {
        if (authentication == null) return null;
        if (authentication.getPrincipal() instanceof Users) {
            Users user = (Users) authentication.getPrincipal();
            return user.getEmployee() != null ? user.getEmployee().getEmployeeId() : null;
        }
        return null;
    }
}
