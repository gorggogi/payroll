package digital8.payroll.controllers;

import digital8.payroll.dto.WeeklyScheduleRowDto;
import digital8.payroll.entities.Attendance;
import digital8.payroll.entities.EmployeeScheduleAssignment;
import digital8.payroll.entities.Employees;
import digital8.payroll.entities.Users;
import digital8.payroll.entities.WeeklyScheduleTemplate;
import digital8.payroll.entities.WeeklyScheduleTemplateDay;
import digital8.payroll.repositories.AttendanceRepository;
import digital8.payroll.repositories.EmployeeRepository;
import digital8.payroll.repositories.EmployeeScheduleAssignmentRepository;
import digital8.payroll.repositories.WeeklyScheduleTemplateDayRepository;
import digital8.payroll.repositories.WeeklyScheduleTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class attendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private WeeklyScheduleTemplateRepository weeklyScheduleTemplateRepository;

    @Autowired
    private WeeklyScheduleTemplateDayRepository weeklyScheduleTemplateDayRepository;

    @Autowired
    private EmployeeScheduleAssignmentRepository employeeScheduleAssignmentRepository;

    
    @GetMapping("/admin/attendance/shifts")
    public String shiftingPage(
            @RequestParam(required = false) Integer templateId,
            @RequestParam(required = false) Integer scheduleYear,
            @RequestParam(required = false) Integer scheduleMonth,
            Model model) {

        List<Employees> employees = employeeRepository.findAll(Sort.by(Sort.Direction.ASC, "lastName", "firstName"));
        model.addAttribute("employees", employees);

        LocalDate today = LocalDate.now();
        int sy = scheduleYear != null ? scheduleYear : today.getYear();
        int sm = (scheduleMonth != null && scheduleMonth >= 1 && scheduleMonth <= 12) ? scheduleMonth : today.getMonthValue();
        if (sy < 2000 || sy > 2100) {
            sy = today.getYear();
        }
        model.addAttribute("scheduleYear", sy);
        model.addAttribute("scheduleMonth", sm);

        List<Integer> scheduleYears = new ArrayList<>();
        for (int y = today.getYear() - 2; y <= today.getYear() + 2; y++) {
            scheduleYears.add(y);
        }
        model.addAttribute("scheduleYears", scheduleYears);

        List<WeeklyScheduleTemplate> scheduleTemplates =
                weeklyScheduleTemplateRepository.findByScheduleYearAndScheduleMonthOrderByTemplateNameAsc(sy, sm);
        model.addAttribute("scheduleTemplates", scheduleTemplates);

        Map<Integer, Long> assignmentCountByTemplateId = new HashMap<>();
        for (WeeklyScheduleTemplate t : scheduleTemplates) {
            assignmentCountByTemplateId.put(t.getTemplateId(), employeeScheduleAssignmentRepository.countByTemplateId(t.getTemplateId()));
        }
        model.addAttribute("assignmentCountByTemplateId", assignmentCountByTemplateId);

        Integer selTemplateId = templateId;
        if (selTemplateId == null && !scheduleTemplates.isEmpty()) {
            selTemplateId = scheduleTemplates.get(0).getTemplateId();
        }
        if (selTemplateId != null && !scheduleTemplates.isEmpty()) {
            boolean foundInMonth = false;
            for (WeeklyScheduleTemplate t : scheduleTemplates) {
                if (t.getTemplateId().equals(selTemplateId)) {
                    foundInMonth = true;
                    break;
                }
            }
            if (!foundInMonth) {
                selTemplateId = scheduleTemplates.get(0).getTemplateId();
            }
        }
        final Integer selectedTemplateId = selTemplateId;
        model.addAttribute("selectedTemplateId", selectedTemplateId);

        String selectedTemplateName = "";
        if (selectedTemplateId != null) {
            WeeklyScheduleTemplate selected = null;
            for (WeeklyScheduleTemplate t : scheduleTemplates) {
                if (t.getTemplateId().equals(selectedTemplateId)) {
                    selected = t;
                    break;
                }
            }
            if (selected == null) {
                selected = weeklyScheduleTemplateRepository.findById(selectedTemplateId).orElse(null);
            }
            if (selected != null) {
                selectedTemplateName = selected.getTemplateName() != null ? selected.getTemplateName() : "";
                model.addAttribute("weeklyScheduleRows", buildTemplateDayRows(selectedTemplateId));
                model.addAttribute("assignedEmployeeIds", employeeScheduleAssignmentRepository.findEmployeeIdsByTemplateAndMonth(
                        selectedTemplateId, selected.getScheduleYear(), selected.getScheduleMonth()));
            } else {
                model.addAttribute("weeklyScheduleRows", List.<WeeklyScheduleRowDto>of());
                model.addAttribute("assignedEmployeeIds", List.<Integer>of());
            }
        } else {
            model.addAttribute("weeklyScheduleRows", List.<WeeklyScheduleRowDto>of());
            model.addAttribute("assignedEmployeeIds", List.<Integer>of());
        }
        model.addAttribute("selectedTemplateName", selectedTemplateName);

        return "html/shifting";
    }

    @PostMapping("/admin/attendance/schedule-template/create")
    public String createScheduleTemplate(
            @RequestParam("templateName") String templateName,
            @RequestParam("scheduleYear") Integer scheduleYear,
            @RequestParam("scheduleMonth") Integer scheduleMonth,
            RedirectAttributes redirectAttributes) {

        if (templateName == null || templateName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Schedule class name is required.");
            return redirectForShifting(scheduleYear, scheduleMonth, null);
        }
        if (scheduleYear == null || scheduleMonth == null || scheduleMonth < 1 || scheduleMonth > 12) {
            redirectAttributes.addFlashAttribute("errorMessage", "Valid month and year required.");
            return "redirect:/admin/attendance/shifts";
        }

        WeeklyScheduleTemplate t = new WeeklyScheduleTemplate();
        t.setTemplateName(unquote(templateName.trim()));
        t.setScheduleYear(scheduleYear);
        t.setScheduleMonth(scheduleMonth);
        weeklyScheduleTemplateRepository.save(t);
        redirectAttributes.addFlashAttribute("successMessage", "Schedule class created.");
        return redirectForShifting(scheduleYear, scheduleMonth, t.getTemplateId());
    }

    @PostMapping("/admin/attendance/schedule-template/save-days")
    public String saveScheduleTemplateDays(
            @RequestParam("templateId") Integer templateId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        WeeklyScheduleTemplate tpl = weeklyScheduleTemplateRepository.findById(templateId).orElse(null);
        if (tpl == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Schedule class not found.");
            return "redirect:/admin/attendance/shifts";
        }

        final int sy = tpl.getScheduleYear();
        final int sm = tpl.getScheduleMonth();

        try {
            for (int dow = 1; dow <= 7; dow++) {
                String rest = request.getParameter("d" + dow + "_rest");
                String inRaw = request.getParameter("d" + dow + "_in");
                String outRaw = request.getParameter("d" + dow + "_out");
                boolean isRest = "true".equals(rest);
                LocalTime in = parseLocalTime(inRaw);
                LocalTime out = parseLocalTime(outRaw);

                if (isRest) {
                    WeeklyScheduleTemplateDay row = weeklyScheduleTemplateDayRepository
                            .findByTemplateIdAndDayOfWeek(templateId, dow)
                            .orElseGet(WeeklyScheduleTemplateDay::new);
                    row.setTemplateId(templateId);
                    row.setDayOfWeek(dow);
                    row.setRestDay(true);
                    row.setTimeIn(null);
                    row.setTimeOut(null);
                    weeklyScheduleTemplateDayRepository.save(row);
                } else if (in != null && out != null) {
                    WeeklyScheduleTemplateDay row = weeklyScheduleTemplateDayRepository
                            .findByTemplateIdAndDayOfWeek(templateId, dow)
                            .orElseGet(WeeklyScheduleTemplateDay::new);
                    row.setTemplateId(templateId);
                    row.setDayOfWeek(dow);
                    row.setRestDay(false);
                    row.setTimeIn(in);
                    row.setTimeOut(out);
                    weeklyScheduleTemplateDayRepository.save(row);
                } else {
                    weeklyScheduleTemplateDayRepository.deleteByTemplateIdAndDayOfWeek(templateId, dow);
                }
            }
            redirectAttributes.addFlashAttribute("successMessage", "Schedule days saved.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to save schedule days: " + e.getMessage());
        }

        return redirectForShifting(sy, sm, templateId);
    }

    @PostMapping("/admin/attendance/schedule-template/assign")
    public String assignScheduleTemplate(
            @RequestParam("assignTemplateId") Integer templateId,
            @RequestParam(required = false) List<Integer> assignEmployeeIds,
            RedirectAttributes redirectAttributes) {

        WeeklyScheduleTemplate tpl = weeklyScheduleTemplateRepository.findById(templateId).orElse(null);
        if (tpl == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Schedule class not found.");
            return "redirect:/admin/attendance/shifts";
        }

        int y = tpl.getScheduleYear();
        int m = tpl.getScheduleMonth();
        List<Integer> checked = assignEmployeeIds != null ? assignEmployeeIds : List.of();
        Set<Integer> checkedSet = new HashSet<>(checked);

        try {
            List<Integer> onThisTemplate = employeeScheduleAssignmentRepository.findEmployeeIdsByTemplateAndMonth(templateId, y, m);
            for (Integer empId : onThisTemplate) {
                if (!checkedSet.contains(empId)) {
                    employeeScheduleAssignmentRepository.deleteByEmployeeIdAndScheduleYearAndScheduleMonth(empId, y, m);
                }
            }
            for (Integer empId : checkedSet) {
                if (!employeeRepository.existsById(empId)) {
                    continue;
                }
                Optional<EmployeeScheduleAssignment> ex =
                        employeeScheduleAssignmentRepository.findByEmployeeIdAndScheduleYearAndScheduleMonth(empId, y, m);
                EmployeeScheduleAssignment row = ex.orElseGet(EmployeeScheduleAssignment::new);
                row.setEmployeeId(empId);
                row.setTemplateId(templateId);
                row.setScheduleYear(y);
                row.setScheduleMonth(m);
                employeeScheduleAssignmentRepository.save(row);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Employees linked to this schedule.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to assign employees: " + e.getMessage());
        }

        return redirectForShifting(y, m, templateId);
    }

    @PostMapping("/admin/attendance/schedule-template/delete/{id}")
    public String deleteScheduleTemplate(
            @PathVariable("id") Integer templateId,
            RedirectAttributes redirectAttributes) {

        WeeklyScheduleTemplate tpl = weeklyScheduleTemplateRepository.findById(templateId).orElse(null);
        int y = tpl != null ? tpl.getScheduleYear() : LocalDate.now().getYear();
        int m = tpl != null ? tpl.getScheduleMonth() : LocalDate.now().getMonthValue();

        try {
            weeklyScheduleTemplateRepository.deleteById(templateId);
            redirectAttributes.addFlashAttribute("successMessage", "Schedule class deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete: " + e.getMessage());
        }

        return redirectForShifting(y, m, null);
    }

    private String redirectForShifting(Integer year, Integer month, Integer templateId) {
        LocalDate today = LocalDate.now();
        int y = year != null ? year : today.getYear();
        int mo = (month != null && month >= 1 && month <= 12) ? month : today.getMonthValue();
        StringBuilder sb = new StringBuilder("redirect:/admin/attendance/shifts?scheduleYear=")
                .append(y).append("&scheduleMonth=").append(mo);
        if (templateId != null) {
            sb.append("&templateId=").append(templateId);
        }
        return sb.toString();
    }

    private List<WeeklyScheduleRowDto> buildTemplateDayRows(Integer templateId) {
        List<WeeklyScheduleTemplateDay> saved =
                weeklyScheduleTemplateDayRepository.findByTemplateIdOrderByDayOfWeekAsc(templateId);
        Map<Integer, WeeklyScheduleTemplateDay> byDay = saved.stream()
                .collect(Collectors.toMap(WeeklyScheduleTemplateDay::getDayOfWeek, s -> s, (a, b) -> a));

        int[] order = {7, 1, 2, 3, 4, 5, 6};
        String[] labels = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        List<WeeklyScheduleRowDto> rows = new ArrayList<>();
        for (int i = 0; i < order.length; i++) {
            rows.add(WeeklyScheduleRowDto.fromTemplateDay(order[i], labels[i], byDay.get(order[i])));
        }
        return rows;
    }

    private LocalTime parseLocalTime(String raw) {
        if (raw == null) return null;
        String v = unquote(raw.trim());
        if (v.isEmpty()) return null;

        // Accept HH:mm or HH:mm:ss (also ISO when provided)
        try {
            return LocalTime.parse(v);
        } catch (DateTimeParseException ignored) {
            DateTimeFormatter[] formatters = new DateTimeFormatter[]{
                    DateTimeFormatter.ofPattern("H:mm"),
                    DateTimeFormatter.ofPattern("HH:mm"),
                    DateTimeFormatter.ofPattern("H:mm:ss"),
                    DateTimeFormatter.ofPattern("HH:mm:ss"),
            };
            for (DateTimeFormatter f : formatters) {
                try {
                    return LocalTime.parse(v, f);
                } catch (DateTimeParseException ignored2) {
                    // keep trying
                }
            }
            return null;
        }
    }

    private String unquote(String v) {
        if (v == null) return null;
        String s = v;
        if (s.length() >= 2) {
            if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
                s = s.substring(1, s.length() - 1);
            }
        }
        return s;
    }

    @GetMapping({"/employee/attendance", "/admin/attendance"})
    public String attendancePage(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer empId,
            HttpServletRequest request,
            Model model,
            Authentication authentication) {
        model.addAttribute("attendanceFormAction", request.getRequestURI());
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();
        int selectedYear = year != null ? year : currentYear;
        int selectedMonth = (month != null && month >= 1 && month <= 12) ? month : currentMonth;

        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("selectedYear", selectedYear);
        String monthName = Month.of(selectedMonth).toString().charAt(0) + Month.of(selectedMonth).toString().substring(1).toLowerCase();
        model.addAttribute("captionMonthYear", monthName + " " + selectedYear);
        List<Integer> years = new ArrayList<>();
        for (int y = currentYear - 5; y <= currentYear + 2; y++) years.add(y);
        model.addAttribute("years", years);

        boolean isAdmin = authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        // Logged-in user's own employee id (for nav links / "my payroll")
        Integer selfEmpId = null;
        if (authentication != null && authentication.getPrincipal() instanceof Users) {
            Users currentUser = (Users) authentication.getPrincipal();
            if (currentUser.getEmployee() != null) {
                selfEmpId = currentUser.getEmployee().getEmployeeId();
            }
        }
        Integer targetEmpId = null;
        Employees targetEmp = null;

        if (isAdmin && empId != null && request.getRequestURI().startsWith("/admin")) {
            targetEmpId = empId;
            targetEmp = employeeRepository.findById(empId).orElse(null);
            model.addAttribute("viewingEmployeeId", empId);
        }
        if (targetEmp == null) {
            Object principal = authentication != null ? authentication.getPrincipal() : null;
            if (principal instanceof Users) {
                Users user = (Users) principal;
                targetEmp = user.getEmployee();
                if (targetEmp != null) targetEmpId = targetEmp.getEmployeeId();
            }
        }

        if (targetEmpId != null && targetEmp != null) {
            List<Attendance> all = attendanceRepository.findByEmployeeIdOrderByDateDesc(targetEmpId);
            List<Attendance> filtered = all.stream()
                    .filter(a -> a.getAttendance_date() != null
                            && a.getAttendance_date().getMonthValue() == selectedMonth
                            && a.getAttendance_date().getYear() == selectedYear)
                    .collect(Collectors.toList());
            model.addAttribute("attendances", filtered);
            model.addAttribute("employeeName", targetEmp.getFirstName() + " " + targetEmp.getLastName());
            model.addAttribute("emp_id", targetEmpId); // currently viewed employee
            model.addAttribute("emp_payType", targetEmp.getPayType());
            BigDecimal total = filtered.stream()
                    .map(Attendance::getWork_hours)
                    .filter(h -> h != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("totalHoursRendered", total);
        }
        // For nav: always use the logged-in user's own employee id when available
        if (selfEmpId != null) {
            model.addAttribute("self_emp_id", selfEmpId);
        }
        if (!model.containsAttribute("attendances")) {
            model.addAttribute("attendances", List.<Attendance>of());
        }
        if (!model.containsAttribute("totalHoursRendered")) {
            model.addAttribute("totalHoursRendered", BigDecimal.ZERO);
        }
        return "html/attendance";
    }
}
