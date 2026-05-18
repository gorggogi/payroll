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
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
public class DeductionViewController {

    @Autowired
    private DeductionsRepository deductionsRepository;
    @Autowired
    private EmployeeDeductionsRepository employeeDeductionsRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping({"/admin/deductions", "/employee/deductions"})
    public String deductionsPage(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer filterMonth,
            @RequestParam(required = false) Integer filterYear,
            @RequestParam(required = false) String filterType,
            @RequestParam(required = false) String filterRecurring,
            Model model, Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        List<Deductions> types = deductionsRepository.findAllByOrderByDeductionNameAsc();
        List<EmployeeDeductions> assignments;
        List<Employees> employees;

        if (isAdmin) {
            Employees adminEmp = null;
            if (authentication != null && authentication.getPrincipal() instanceof Users) {
                adminEmp = ((Users) authentication.getPrincipal()).getEmployee();
            }
            assignments = employeeDeductionsRepository.findAll();
            employees = employeeRepository.findAllWithFetch(null, Sort.by(Sort.Direction.ASC, "lastName"));
            if (adminEmp != null) model.addAttribute("emp_id", adminEmp.getEmployeeId());
            // Admin page does not show personal balance cards — use "My Deductions" instead
            model.addAttribute("deductionOutstandingObligation", BigDecimal.ZERO);
            model.addAttribute("deductionMonthlyCutoff", BigDecimal.ZERO);
            model.addAttribute("deductionOutstandingBalance", BigDecimal.ZERO);
        } else {
            Integer empId = null;
            if (authentication != null && authentication.getPrincipal() instanceof Users) {
                Employees emp = ((Users) authentication.getPrincipal()).getEmployee();
                if (emp != null) empId = emp.getEmployeeId();
            }
            assignments = empId != null ? employeeDeductionsRepository.findByEmployeeId(empId) : List.of();
            employees = List.of();
            if (empId != null) model.addAttribute("emp_id", empId);

            Map<String, BigDecimal> balances = computeDeductionBalances(empId, LocalDate.now());
            model.addAttribute("deductionOutstandingObligation", balances.get("deductionOutstandingObligation"));
            model.addAttribute("deductionMonthlyCutoff", balances.get("deductionMonthlyCutoff"));
            model.addAttribute("deductionOutstandingBalance", balances.get("deductionOutstandingBalance"));
        }

        Map<Integer, String> employeeNames = isAdmin && !employees.isEmpty()
                ? employees.stream().collect(Collectors.toMap(Employees::getEmployeeId, e -> e.getLastName() + ", " + e.getFirstName()))
                : Map.of();
        Map<Integer, String> deductionNames = types.stream()
                .collect(Collectors.toMap(Deductions::getDeductionId, Deductions::getDeductionName));
        Map<Integer, String> deductionTypeMap = types.stream()
                .collect(Collectors.toMap(Deductions::getDeductionId, Deductions::getDeductionType));

        List<EmployeeDeductionRowDto> rows = new ArrayList<>();
        for (EmployeeDeductions ed : assignments) {
            EmployeeDeductionRowDto dto = new EmployeeDeductionRowDto();
            dto.setEmployeeDeductionId(ed.getEmployeeDeductionId());
            dto.setEmployeeId(ed.getEmployeeId());
            dto.setEmployeeName(employeeNames.getOrDefault(ed.getEmployeeId(), "?"));
            dto.setDeductionId(ed.getDeductionId());
            dto.setDeductionName(deductionNames.getOrDefault(ed.getDeductionId(), "?"));
            dto.setDeductionType(deductionTypeMap.getOrDefault(ed.getDeductionId(), "?"));
            dto.setAmount(ed.getAmount());
            dto.setRecurring(ed.getIsRecurring());
            dto.setStartDate(ed.getStartDate());
            dto.setEndDate(ed.getEndDate());
            dto.setDeductionCutoff(ed.getDeductionCutoff());
            rows.add(dto);
        }

        Stream<EmployeeDeductionRowDto> stream = rows.stream();
        if (search != null && !search.isBlank()) {
            String term = search.trim().toLowerCase();
            stream = stream.filter(r -> (r.getEmployeeName() != null && r.getEmployeeName().toLowerCase().contains(term))
                    || (r.getDeductionName() != null && r.getDeductionName().toLowerCase().contains(term)));
        }
        if (filterType != null && !filterType.isBlank() && !"all".equalsIgnoreCase(filterType)) {
            String type = filterType.trim();
            stream = stream.filter(r -> type.equals(deductionTypeMap.get(r.getDeductionId())));
        }
        if (filterRecurring != null && !filterRecurring.isBlank() && !"all".equalsIgnoreCase(filterRecurring)) {
            boolean recurring = "yes".equalsIgnoreCase(filterRecurring.trim());
            stream = stream.filter(r -> Boolean.valueOf(recurring).equals(r.getRecurring()));
        }
        // Month/Year filter: for recurring deductions, check if the filter period
        // overlaps the deduction's [startDate, endDate] window. For one-time deductions,
        // match only the exact start month/year (the single date the deduction applies).
        if (filterMonth != null && filterMonth >= 1 && filterMonth <= 12 && filterYear != null) {
            LocalDate filterPeriodStart = LocalDate.of(filterYear, filterMonth, 1);
            LocalDate filterPeriodEnd   = filterPeriodStart.withDayOfMonth(filterPeriodStart.lengthOfMonth());
            stream = stream.filter(r -> {
                if (Boolean.TRUE.equals(r.getRecurring())) {
                    // Active if the filter period overlaps [startDate, endDate]
                    return r.getStartDate() != null && r.getEndDate() != null
                            && !r.getStartDate().isAfter(filterPeriodEnd)
                            && !r.getEndDate().isBefore(filterPeriodStart);
                }
                // One-time: match if startDate falls exactly in that month/year
                return r.getStartDate() != null
                        && r.getStartDate().getMonthValue() == filterMonth
                        && r.getStartDate().getYear() == filterYear;
            });
        } else if (filterMonth != null && filterMonth >= 1 && filterMonth <= 12) {
            // Month only (no year selected) — same overlap/exact logic without year constraint
            stream = stream.filter(r -> {
                if (Boolean.TRUE.equals(r.getRecurring())) {
                    return r.getStartDate() != null && r.getStartDate().getMonthValue() <= filterMonth
                            && r.getEndDate() != null && r.getEndDate().getMonthValue() >= filterMonth;
                }
                return r.getStartDate() != null && r.getStartDate().getMonthValue() == filterMonth;
            });
        } else if (filterYear != null) {
            // Year only — recurring: active any time within that year; one-time: started that year
            stream = stream.filter(r -> {
                if (Boolean.TRUE.equals(r.getRecurring())) {
                    LocalDate yearStart = LocalDate.of(filterYear, 1, 1);
                    LocalDate yearEnd   = LocalDate.of(filterYear, 12, 31);
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

        model.addAttribute("deductionTypes", isAdmin ? types : List.of());
        model.addAttribute("assignmentRows", rows);
        model.addAttribute("employees", employees);
        model.addAttribute("search", search);
        model.addAttribute("filterMonth", filterMonth);
        model.addAttribute("filterYear", filterYear);
        model.addAttribute("filterYears", filterYears);
        model.addAttribute("filterMonthNames", filterMonthNames);
        model.addAttribute("filterType", filterType);
        model.addAttribute("filterRecurring", filterRecurring);
        model.addAttribute("deductionsFormAction", isAdmin ? "/admin/deductions" : "/employee/deductions");
        return "html/deductions";
    }

    /**
     * Admin "My Deductions" page — shows the admin's own deductions
     * in the employee-style view with balance cards.
     */
    @GetMapping("/admin/my-deductions")
    public String adminMyDeductionsPage(Model model, Authentication authentication) {
        Users user = (Users) authentication.getPrincipal();
        Employees emp = user.getEmployee();
        Integer empId = emp != null ? emp.getEmployeeId() : null;

        model.addAttribute("emp_id", empId);

        // Balance cards for the admin's own deductions
        Map<String, BigDecimal> balances = computeDeductionBalances(empId, LocalDate.now());
        model.addAttribute("deductionOutstandingObligation", balances.get("deductionOutstandingObligation"));
        model.addAttribute("deductionMonthlyCutoff", balances.get("deductionMonthlyCutoff"));
        model.addAttribute("deductionOutstandingBalance", balances.get("deductionOutstandingBalance"));

        // Get admin's own deductions and build row DTOs
        List<EmployeeDeductions> assignments = empId != null
                ? employeeDeductionsRepository.findByEmployeeId(empId) : List.of();
        List<Deductions> types = deductionsRepository.findAllByOrderByDeductionNameAsc();
        Map<Integer, String> deductionNames = types.stream()
                .collect(Collectors.toMap(Deductions::getDeductionId, Deductions::getDeductionName));
        Map<Integer, String> deductionTypeMap = types.stream()
                .collect(Collectors.toMap(Deductions::getDeductionId, Deductions::getDeductionType));

        List<EmployeeDeductionRowDto> rows = new ArrayList<>();
        for (EmployeeDeductions ed : assignments) {
            EmployeeDeductionRowDto dto = new EmployeeDeductionRowDto();
            dto.setEmployeeDeductionId(ed.getEmployeeDeductionId());
            dto.setEmployeeId(ed.getEmployeeId());
            dto.setDeductionId(ed.getDeductionId());
            dto.setDeductionName(deductionNames.getOrDefault(ed.getDeductionId(), "?"));
            dto.setDeductionType(deductionTypeMap.getOrDefault(ed.getDeductionId(), "?"));
            dto.setAmount(ed.getAmount());
            dto.setRecurring(ed.getIsRecurring());
            dto.setStartDate(ed.getStartDate());
            dto.setEndDate(ed.getEndDate());
            dto.setDeductionCutoff(ed.getDeductionCutoff());
            rows.add(dto);
        }

        List<Integer> filterYears = IntStream.rangeClosed(Year.now().getValue() - 5, Year.now().getValue() + 1)
                .boxed().sorted((a, b) -> Integer.compare(b, a)).toList();
        List<String> filterMonthNames = List.of("January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December");

        model.addAttribute("assignmentRows", rows);
        model.addAttribute("filterYears", filterYears);
        model.addAttribute("filterMonthNames", filterMonthNames);
        model.addAttribute("deductionsFormAction", "/admin/my-deductions");
        return "html/deductionsAdminSelf";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/deductions/types")
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
    @PostMapping("/admin/deductions/types/update")
    public String updateType(
            @RequestParam Integer id,
            @RequestParam String deductionName,
            @RequestParam String deductionType,
            RedirectAttributes ra) {
        deductionsRepository.findById(id).ifPresent(d -> {
            d.setDeductionName(deductionName);
            d.setDeductionType(deductionType != null ? deductionType : "Other");
            deductionsRepository.save(d);
        });
        ra.addFlashAttribute("message", "Deduction type updated.");
        return "redirect:/admin/deductions";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/deductions/assign")
    public String assignDeduction(
            @RequestParam Integer employeeId,
            @RequestParam Integer deductionId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) Boolean isRecurring,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false, defaultValue = "BOTH") String deductionCutoff,
            RedirectAttributes ra) {
        EmployeeDeductions ed = new EmployeeDeductions();
        ed.setEmployeeId(employeeId);
        ed.setDeductionId(deductionId);
        ed.setAmount(amount);
        ed.setIsRecurring(Boolean.TRUE.equals(isRecurring));
        ed.setStartDate(LocalDate.parse(startDate));
        ed.setEndDate(LocalDate.parse(endDate));
        ed.setDeductionCutoff(deductionCutoff);
        employeeDeductionsRepository.save(ed);
        ra.addFlashAttribute("message", "Deduction assigned to employee.");
        return "redirect:/admin/deductions";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/deductions/assign/remove")
    public String removeAssignment(@RequestParam Integer id, RedirectAttributes ra) {
        employeeDeductionsRepository.findById(id).ifPresent(employeeDeductionsRepository::delete);
        ra.addFlashAttribute("message", "Assignment removed.");
        return "redirect:/admin/deductions";
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/deductions/assign/edit")
    public String editAssignment(
            @RequestParam Integer id,
            @RequestParam Integer employeeId,
            @RequestParam Integer deductionId,
            @RequestParam java.math.BigDecimal amount,
            @RequestParam(required = false) Boolean isRecurring,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false, defaultValue = "BOTH") String deductionCutoff,
            RedirectAttributes ra) {
        employeeDeductionsRepository.findById(id).ifPresent(ed -> {
            ed.setEmployeeId(employeeId);
            ed.setDeductionId(deductionId);
            ed.setAmount(amount);
            ed.setIsRecurring(Boolean.TRUE.equals(isRecurring));
            ed.setStartDate(java.time.LocalDate.parse(startDate));
            ed.setEndDate(java.time.LocalDate.parse(endDate));
            ed.setDeductionCutoff(deductionCutoff);
            employeeDeductionsRepository.save(ed);
        });
        ra.addFlashAttribute("message", "Deduction assignment updated.");
        return "redirect:/admin/deductions";
    }

    protected Map<String, BigDecimal> computeDeductionBalances(Integer employeeId, LocalDate today) {
        BigDecimal outstandingObligation = BigDecimal.ZERO;
        BigDecimal monthlyCutoff = BigDecimal.ZERO;
        BigDecimal outstandingBalance = BigDecimal.ZERO;
    
        if (employeeId == null) {
            return Map.of(
                "deductionOutstandingObligation", outstandingObligation,
                "deductionMonthlyCutoff", monthlyCutoff,
                "deductionOutstandingBalance", outstandingBalance
            );
        }
    
        java.time.YearMonth currentMonth = java.time.YearMonth.from(today);
        LocalDate nextPeriodStart;
        LocalDate nextPeriodEnd;
        if (today.getDayOfMonth() <= 15) {
            nextPeriodStart = currentMonth.atDay(1);
            nextPeriodEnd   = currentMonth.atDay(15);
        } else {
            nextPeriodStart = currentMonth.atDay(16);
            nextPeriodEnd   = currentMonth.atEndOfMonth();
        }
    
        // Get active recurring deductions whose window overlaps today
        List<EmployeeDeductions> activeRecurring = employeeDeductionsRepository
            .findActiveRecurringByEmployee(employeeId, today, today);
    
        // One-time deductions for this employee
        List<EmployeeDeductions> oneTime = employeeDeductionsRepository.findByEmployeeId(employeeId)
            .stream()
            .filter(ed -> !Boolean.TRUE.equals(ed.getIsRecurring()))
            .toList();

        // Process recurring deductions
        for (EmployeeDeductions ed : activeRecurring) {
            BigDecimal amount = ed.getAmount() != null ? ed.getAmount() : BigDecimal.ZERO;
            LocalDate startDate = ed.getStartDate();
            LocalDate endDate = ed.getEndDate();
            int periodsPerYear = "BOTH".equals(ed.getDeductionCutoff()) ? 24 : 12;
    
            // Monthly cutoff: what applies to the next payroll period
            boolean appliesThisCutoff;
            String cutoff = ed.getDeductionCutoff();
            if ("BOTH".equals(cutoff)) {
                appliesThisCutoff = true;
            } else if ("SEMI_1".equals(cutoff)) {
                appliesThisCutoff = nextPeriodStart.equals(currentMonth.atDay(1));
            } else {
                appliesThisCutoff = nextPeriodStart.equals(currentMonth.atDay(16));
            }
            if (appliesThisCutoff) {
                monthlyCutoff = monthlyCutoff.add(amount);
            }
    
            // Outstanding Obligation: total lifetime obligation (startDate → endDate)
            // This is the full amount the employee committed to pay across all periods
            long totalPeriods = 0;
            long remainingPeriods = 0;
            if (startDate != null && endDate != null && !endDate.isBefore(startDate)) {
                totalPeriods = countPeriods(startDate, endDate, periodsPerYear);
                if (!endDate.isBefore(today)) {
                    // Adjust effective start date based on cutoff type:
                    // - SEMI_1: if past day 15, this month's deduction already happened
                    // - SEMI_2: deduction is in 2nd half, no early cutoff
                    // - BOTH: if past day 15, SEMI_1 for this month already happened
                    LocalDate effectiveFrom = today;
                    if ("SEMI_1".equals(cutoff) && today.getDayOfMonth() > 15) {
                        effectiveFrom = today.plusMonths(1).withDayOfMonth(1);
                    }
                    if (!effectiveFrom.isAfter(endDate)) {
                        remainingPeriods = countPeriods(effectiveFrom, endDate, periodsPerYear);
                    }
                    // For BOTH: SEMI_1 this month already paid when past day 15
                    if ("BOTH".equals(cutoff) && today.getDayOfMonth() > 15) {
                        remainingPeriods = Math.max(0, remainingPeriods - 1);
                    }
                }
            } else if (endDate == null) {
                // Open-ended: assume 36 periods (18 months) as a projection
                totalPeriods = 36;
                remainingPeriods = 36;
            }
            outstandingObligation = outstandingObligation.add(amount.multiply(BigDecimal.valueOf(totalPeriods)));

            // Outstanding Balance: what's still left to pay (remaining periods from today → endDate)
            outstandingBalance = outstandingBalance.add(amount.multiply(BigDecimal.valueOf(remainingPeriods)));
        }
    
        // Process one-time deductions
        for (EmployeeDeductions ed : oneTime) {
            BigDecimal amount = ed.getAmount() != null ? ed.getAmount() : BigDecimal.ZERO;
            outstandingObligation = outstandingObligation.add(amount);

            // One-time balance: if startDate has not yet passed, it hasn't been paid
            if (ed.getStartDate() == null || !ed.getStartDate().isBefore(today)) {
                // Not yet paid (startDate is today or in the future, or null)
                outstandingBalance = outstandingBalance.add(amount);
            }
            // else: startDate is in the past → already deducted, balance contribution = 0

            // One-time: included in next payroll if startDate falls within that period window
            // AND the deduction's cutoff matches the next period's cutoff
            if (ed.getStartDate() != null
                    && !ed.getStartDate().isBefore(nextPeriodStart)
                    && !ed.getStartDate().isAfter(nextPeriodEnd)) {
                String cutoff = ed.getDeductionCutoff();
                boolean cutoffMatches = "BOTH".equals(cutoff)
                        || ("SEMI_1".equals(cutoff) && nextPeriodStart.equals(currentMonth.atDay(1)))
                        || ("SEMI_2".equals(cutoff) && nextPeriodStart.equals(currentMonth.atDay(16)));
                if (cutoffMatches) {
                    monthlyCutoff = monthlyCutoff.add(amount);
                }
            }
        }
    
        return Map.of(
            "deductionOutstandingObligation", outstandingObligation,
            "deductionMonthlyCutoff", monthlyCutoff,
            "deductionOutstandingBalance", outstandingBalance
        );
    }

    /**
     * Counts the number of pay periods between two dates.
     * For periodsPerYear=24 (BOTH cutoffs): ~2 periods per month (semi-monthly).
     * For periodsPerYear=12 (SEMI_1 or SEMI_2): ~1 period per month.
     * Uses month-based counting for accuracy rather than day-based approximation.
     */
    protected long countPeriods(LocalDate from, LocalDate to, int periodsPerYear) {
        if (from == null || to == null || to.isBefore(from)) {
            return 0;
        }
        // Calculate total months between the two dates (inclusive of partial months)
        long totalMonths = java.time.temporal.ChronoUnit.MONTHS.between(from, to);
        // Add 1 to include the starting month's period
        // Account for remaining days within the last partial month
        if (from.plusMonths(totalMonths).isBefore(to) || from.plusMonths(totalMonths).isEqual(to)) {
            totalMonths += 1;
        }
        // periodsPerYear=24 → 2 periods/month, periodsPerYear=12 → 1 period/month
        int periodsPerMonth = periodsPerYear / 12;
        return Math.max(1, totalMonths * periodsPerMonth);
    }
}

