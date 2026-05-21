package digital8.payroll.controllers;

import digital8.payroll.HourFormatUtils;
import digital8.payroll.dto.WeeklyScheduleRowDto;
import digital8.payroll.entities.Attendance;
import digital8.payroll.entities.EmployeeScheduleAssignment;
import digital8.payroll.entities.Employees;
import digital8.payroll.entities.Users;
import digital8.payroll.entities.OvertimeRequest;
import digital8.payroll.entities.WeeklyScheduleTemplate;
import digital8.payroll.entities.WeeklyScheduleTemplateDay;
import digital8.payroll.repositories.AttendanceRepository;
import digital8.payroll.repositories.EmployeeRepository;
import digital8.payroll.repositories.EmployeeScheduleAssignmentRepository;
import digital8.payroll.repositories.WeeklyScheduleTemplateDayRepository;
import digital8.payroll.repositories.WeeklyScheduleTemplateRepository;
import digital8.payroll.services.OvertimeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Month;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Controller
public class attendanceController {
    private static final Logger log = LoggerFactory.getLogger(attendanceController.class);
        @GetMapping("/admin/attendance/overtime/self")
        public String adminSelfOvertimePage(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            HttpServletRequest request,
            Model model,
            Authentication authentication) {
        boolean isAdmin = authentication != null
            && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (!isAdmin) {
            return "redirect:/login";
        }
        Integer selfEmpId = null;
        Employees selfEmp = null;
        if (authentication != null && authentication.getPrincipal() instanceof Users) {
            Users currentUser = (Users) authentication.getPrincipal();
            if (currentUser.getEmployee() != null) {
            selfEmp = currentUser.getEmployee();
            selfEmpId = selfEmp.getEmployeeId();
            }
        }
        if (selfEmpId == null || selfEmp == null) {
            return "redirect:/admin/attendance/overtime";
        }
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();
        int selectedYear = year != null ? year : currentYear;
        int selectedMonth = (month != null && month >= 1 && month <= 12) ? month : currentMonth;
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("selectedYear", selectedYear);
        String monthName = Month.of(selectedMonth).toString().charAt(0)
            + Month.of(selectedMonth).toString().substring(1).toLowerCase();
        model.addAttribute("captionMonthYear", monthName + " " + selectedYear);
        List<Integer> years = new ArrayList<>();
        for (int y = currentYear - 5; y <= currentYear + 2; y++) {
            years.add(y);
        }
        model.addAttribute("years", years);
        List<Attendance> all = attendanceRepository.findByEmployeeIdOrderByDateDesc(selfEmpId);
        List<Attendance> filtered = all.stream()
            .filter(a -> a.getAttendance_date() != null
                && a.getAttendance_date().getMonthValue() == selectedMonth
                && a.getAttendance_date().getYear() == selectedYear)
            .collect(Collectors.toList());
        populateComputedWorkHours(filtered);
        model.addAttribute("attendances", filtered);
        model.addAttribute("employeeName", selfEmp.getFirstName() + " " + selfEmp.getLastName());
        model.addAttribute("emp_id", selfEmpId);
        BigDecimal totalOt = filtered.stream()
            .map(Attendance::getOvertime_hours)
            .filter(h -> h != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("totalOvertimeHours", totalOt);
        model.addAttribute("totalOvertimeHoursDisplay", HourFormatUtils.formatHours(totalOt));
        BigDecimal totalHoursWithOt = filtered.stream()
            .map(a -> (a.getWork_hours() != null ? a.getWork_hours() : BigDecimal.ZERO)
                .add(a.getOvertime_hours() != null ? a.getOvertime_hours() : BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("totalHoursWithOvertime", totalHoursWithOt);
        model.addAttribute("totalHoursWithOvertimeDisplay", HourFormatUtils.formatHours(totalHoursWithOt));
        model.addAttribute("overtimeRequestByDate",
            overtimeRequestService.latestRequestByWorkDateForMonth(selfEmpId, selectedYear, selectedMonth));
        List<OvertimeRequest> myRequests = overtimeRequestService.listForEmployeeInMonth(selfEmpId, selectedYear, selectedMonth);
        model.addAttribute("myOvertimeRequests", myRequests);
        model.addAttribute("approverNameByRequestId", overtimeRequestService.approverNameByRequestId(myRequests));
        model.addAttribute("self_emp_id", selfEmpId);
        return "html/overtimeAdminSelf";
        }

        @PostMapping("/admin/attendance/overtime/self")
        public String submitAdminSelfOvertimeRequest(
            @RequestParam LocalDate workDate,
            @RequestParam String overtimeIn,
            @RequestParam String overtimeOut,
            @RequestParam String reason,
            @RequestParam("attachment") MultipartFile attachment,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

            if (!(authentication.getPrincipal() instanceof Users)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Not signed in.");
                return "redirect:/index";
            }
            Users user = (Users) authentication.getPrincipal();
            if (user.getEmployee() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "No employee profile linked.");
                return "redirect:/adminHome";
            }
            if (workDate == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Work date is required.");
                return "redirect:/admin/attendance/overtime/self";
            }
            LocalTime in = parseLocalTime(overtimeIn);
            LocalTime out = parseLocalTime(overtimeOut);
            if (in == null || out == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid overtime in or out time.");
                return "redirect:/admin/attendance/overtime/self";
            }
            if (attachment == null || attachment.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Attachment is required.");
                return "redirect:/admin/attendance/overtime/self";
            }
            try {
                // Save file
                String uploadsDir = "uploads/ot_attachments";
                java.nio.file.Path uploadsPath = java.nio.file.Paths.get(uploadsDir);
                if (!java.nio.file.Files.exists(uploadsPath)) {
                    java.nio.file.Files.createDirectories(uploadsPath);
                }
                String originalFilename = attachment.getOriginalFilename();
                String safeFilename = java.util.UUID.randomUUID() + "_" + (originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_") : "attachment");
                java.nio.file.Path filePath = uploadsPath.resolve(safeFilename);
                attachment.transferTo(filePath);

                String attachmentPath = filePath.toString().replace("\\", "/");
                overtimeRequestService.submitWithAttachment(
                    user.getEmployee().getEmployeeId(), workDate, in, out, reason, attachmentPath);
                redirectAttributes.addFlashAttribute("successMessage", "Overtime request submitted for approval.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            }
            return "redirect:/admin/attendance/overtime/self";
        }
    private static final String TIME_ADJUSTMENTS_PREVIEW_SESSION_KEY = "timeAdjustmentsPreviewRows";

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

    @Autowired
    private OvertimeRequestService overtimeRequestService;

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
                model.addAttribute("selectedTemplateIndefinite", selected.isIndefinite());
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

    @PostMapping("/admin/attendance/schedule-template/toggle-indefinite")
    public String toggleIndefiniteScheduleTemplate(
            @RequestParam("templateId") Integer templateId,
            @RequestParam(value = "indefinite", defaultValue = "false") boolean indefinite,
            RedirectAttributes redirectAttributes) {

        WeeklyScheduleTemplate tpl = weeklyScheduleTemplateRepository.findById(templateId).orElse(null);
        if (tpl == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Schedule class not found.");
            return "redirect:/admin/attendance/shifts";
        }
        
        tpl.setIndefinite(indefinite);
        weeklyScheduleTemplateRepository.save(tpl);
        
        redirectAttributes.addFlashAttribute("successMessage", "Schedule updated.");
        return redirectForShifting(tpl.getScheduleYear(), tpl.getScheduleMonth(), templateId);
    }

    @PostMapping("/admin/attendance/schedule-template/create")
    public String createScheduleTemplate(
            @RequestParam("templateName") String templateName,
            @RequestParam("scheduleYear") Integer scheduleYear,
            @RequestParam("scheduleMonth") Integer scheduleMonth,
            @RequestParam(value = "indefinite", defaultValue = "false") boolean indefinite,
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
        t.setIndefinite(indefinite);
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

    @PostMapping("/admin/attendance/schedule-template/unassign")
    public String unassignScheduleTemplateEmployee(
            @RequestParam("templateId") Integer templateId,
            @RequestParam("employeeId") Integer employeeId,
            RedirectAttributes redirectAttributes) {

        WeeklyScheduleTemplate tpl = weeklyScheduleTemplateRepository.findById(templateId).orElse(null);
        if (tpl == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Schedule class not found.");
            return "redirect:/admin/attendance/shifts";
        }

        try {
            employeeScheduleAssignmentRepository.deleteByEmployeeIdAndScheduleYearAndScheduleMonth(
                    employeeId, tpl.getScheduleYear(), tpl.getScheduleMonth());
            redirectAttributes.addFlashAttribute("successMessage", "Employee unassigned.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to unassign employee: " + e.getMessage());
        }

        return redirectForShifting(tpl.getScheduleYear(), tpl.getScheduleMonth(), templateId);
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

        DateTimeFormatter[] formatters = new DateTimeFormatter[]{
                DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("H:mm"),
                DateTimeFormatter.ofPattern("HH:mm:ss"),
                DateTimeFormatter.ofPattern("H:mm:ss"),
                DateTimeFormatter.ofPattern("hh:mm a"),
                DateTimeFormatter.ofPattern("h:mm a"),
                DateTimeFormatter.ofPattern("hh:mm a"),
                DateTimeFormatter.ofPattern("h:mm a"),
        };
        for (DateTimeFormatter f : formatters) {
            try {
                return LocalTime.parse(v, f);
            } catch (DateTimeParseException ignored) {
            }
        }
        String ampm = v.replaceAll(".*?(AM|PM).*", "$1").toUpperCase();
        if (ampm.equals("AM") || ampm.equals("PM")) {
            String timeOnly = v.replaceAll("(AM|PM)", "").trim();
            String[] hm = timeOnly.split(":");
            if (hm.length >= 2) {
                int h = Integer.parseInt(hm[0].trim());
                int m = hm[1].trim().length() > 0 ? Integer.parseInt(hm[1].trim().split("[^0-9]")[0]) : 0;
                if (ampm.equals("PM") && h != 12) h += 12;
                if (ampm.equals("AM") && h == 12) h = 0;
                return LocalTime.of(h, m);
            }
        }
        return null;
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

    @GetMapping({"/admin/attendance/overtime", "/employee/attendance/overtime"})
    public String overtimePage(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer empId,
            HttpServletRequest request,
            Model model,
            Authentication authentication) {

        model.addAttribute("overtimeFilterAction", request.getRequestURI());
        boolean isAdmin = authentication != null
            && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("overtimeSaveAction", "/admin/attendance/overtime/save");
        if (isAdmin) {
            model.addAttribute("overtimeRequestAction", "/admin/attendance/overtime/save");
        } else {
            model.addAttribute("overtimeRequestAction", "/employee/attendance/overtime/request");
        }

        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();
        int selectedYear = year != null ? year : currentYear;
        int selectedMonth = (month != null && month >= 1 && month <= 12) ? month : currentMonth;

        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("selectedYear", selectedYear);
        String monthName = Month.of(selectedMonth).toString().charAt(0)
                + Month.of(selectedMonth).toString().substring(1).toLowerCase();
        model.addAttribute("captionMonthYear", monthName + " " + selectedYear);
        List<Integer> years = new ArrayList<>();
        for (int y = currentYear - 5; y <= currentYear + 2; y++) {
            years.add(y);
        }
        model.addAttribute("years", years);

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
                if (targetEmp != null) {
                    targetEmpId = targetEmp.getEmployeeId();
                }
            }
        }

        if (isAdmin) {
            model.addAttribute("allEmployees",
                    employeeRepository.findAll(Sort.by(Sort.Direction.ASC, "lastName", "firstName")));
        } else {
            model.addAttribute("allEmployees", List.<Employees>of());
        }

        if (targetEmpId != null && targetEmp != null) {
            List<Attendance> all = attendanceRepository.findByEmployeeIdOrderByDateDesc(targetEmpId);
            List<Attendance> filtered = all.stream()
                    .filter(a -> a.getAttendance_date() != null
                            && a.getAttendance_date().getMonthValue() == selectedMonth
                            && a.getAttendance_date().getYear() == selectedYear)
                    .collect(Collectors.toList());
            populateComputedWorkHours(filtered);
            model.addAttribute("attendances", filtered);
            model.addAttribute("employeeName", targetEmp.getFirstName() + " " + targetEmp.getLastName());
            model.addAttribute("emp_id", targetEmpId);
            BigDecimal totalOt = filtered.stream()
                    .map(Attendance::getOvertime_hours)
                    .filter(h -> h != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("totalOvertimeHours", totalOt);
            model.addAttribute("totalOvertimeHoursDisplay", HourFormatUtils.formatHours(totalOt));
            BigDecimal totalHoursWithOt = filtered.stream()
                    .map(a -> (a.getWork_hours() != null ? a.getWork_hours() : BigDecimal.ZERO)
                            .add(a.getOvertime_hours() != null ? a.getOvertime_hours() : BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("totalHoursWithOvertime", totalHoursWithOt);
            model.addAttribute("totalHoursWithOvertimeDisplay", HourFormatUtils.formatHours(totalHoursWithOt));
            model.addAttribute("overtimeRequestByDate",
                    overtimeRequestService.latestRequestByWorkDateForMonth(targetEmpId, selectedYear, selectedMonth));
        }

        if (selfEmpId != null) {
            model.addAttribute("self_emp_id", selfEmpId);
        }
        if (!model.containsAttribute("attendances")) {
            model.addAttribute("attendances", List.<Attendance>of());
        }
        if (!model.containsAttribute("overtimeRequestByDate")) {
            model.addAttribute("overtimeRequestByDate", Map.<LocalDate, OvertimeRequest>of());
        }
        if (!model.containsAttribute("totalOvertimeHours")) {
            model.addAttribute("totalOvertimeHours", BigDecimal.ZERO);
        }
        if (!model.containsAttribute("totalOvertimeHoursDisplay")) {
            model.addAttribute("totalOvertimeHoursDisplay", "0:00");
        }
        if (!model.containsAttribute("totalHoursWithOvertime")) {
            model.addAttribute("totalHoursWithOvertime", BigDecimal.ZERO);
        }
        if (!model.containsAttribute("totalHoursWithOvertimeDisplay")) {
            model.addAttribute("totalHoursWithOvertimeDisplay", "0:00");
        }
        if (!model.containsAttribute("emp_id") && selfEmpId != null) {
            model.addAttribute("emp_id", selfEmpId);
        }
        if (!model.containsAttribute("employeeName")) {
            model.addAttribute("employeeName", "");
        }

        if (!isAdmin && selfEmpId != null) {
            List<OvertimeRequest> myRequests = overtimeRequestService.listForEmployeeInMonth(selfEmpId, selectedYear, selectedMonth);
            model.addAttribute("myOvertimeRequests", myRequests);
            model.addAttribute("approverNameByRequestId", overtimeRequestService.approverNameByRequestId(myRequests));
        } else {
            model.addAttribute("myOvertimeRequests", List.<OvertimeRequest>of());
            model.addAttribute("approverNameByRequestId", Map.<Integer, String>of());
        }

        boolean adminOvertimePage = isAdmin && request.getRequestURI().startsWith("/admin");
        model.addAttribute("adminOvertimePage", adminOvertimePage);
        if (adminOvertimePage && authentication != null && authentication.getPrincipal() instanceof Users) {
            Users u = (Users) authentication.getPrincipal();
            Integer adminEmpId = u.getEmployee() != null ? u.getEmployee().getEmployeeId() : null;
            model.addAttribute("pendingOvertimeRequests",
                    overtimeRequestService.listPendingExcludingEmployee(adminEmpId));
        } else {
            model.addAttribute("pendingOvertimeRequests", List.<OvertimeRequest>of());
        }

        return "html/overtime";
    }
    @GetMapping("/admin/attendance/time-adjustments")
    public String adjustmentsPage(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer empId,
            HttpServletRequest request,
            Model model,
            Authentication authentication) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object previewRows = session.getAttribute(TIME_ADJUSTMENTS_PREVIEW_SESSION_KEY);
            if (previewRows != null) {
                model.addAttribute("previewRows", previewRows);
            }
        }
        return "html/time-adjustments";
    }

    @PostMapping("/admin/attendance/time-adjustments/upload")
    public String uploadTimeAdjustments(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "startDate", required = false) String startDateRaw,
            @RequestParam(value = "endDate", required = false) String endDateRaw,
            @RequestParam(value = "overwrite", required = false) Boolean overwrite,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Upload failed: file is empty.");
            return "redirect:/admin/attendance/time-adjustments";
        }

        final boolean shouldOverwrite = Boolean.TRUE.equals(overwrite);
        final LocalDate startDate = parseDateFlexible(startDateRaw);
        final LocalDate endDate = parseDateFlexible(endDateRaw);
        if ((startDateRaw != null && !startDateRaw.trim().isEmpty() && startDate == null)
                || (endDateRaw != null && !endDateRaw.trim().isEmpty() && endDate == null)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid start/end date filter.");
            return "redirect:/admin/attendance/time-adjustments";
        }
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            redirectAttributes.addFlashAttribute("errorMessage", "End date must be after or equal to start date.");
            return "redirect:/admin/attendance/time-adjustments";
        }

        // key: biometricId|date
        Map<String, ImportAccumulator> aggregated = new TreeMap<>();
        int lineNo = 0;
        int parsedRows = 0;
        int skippedRows = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                String raw = line == null ? "" : line.trim();
                if (raw.isEmpty()) {
                    continue;
                }

                String[] parts = raw.split("\\s+");
                if (parts.length != 7) {
                    skippedRows++;
                    errors.add("Line " + lineNo + ": expected 7 columns, found " + parts.length + ".");
                    continue;
                }

                String biometricId;
                LocalDate logDate;
                LocalTime logTime;
                Integer logType;
                try {
                    biometricId = parts[0].trim();
                    if (biometricId.isEmpty()) {
                        throw new IllegalArgumentException("empty biometric id");
                    }
                    logDate = parseDateFlexible(parts[1]);
                    logTime = parseTimeFlexible(parts[2]);
                    logType = Integer.parseInt(parts[4]);
                    if (logDate == null || logTime == null) {
                        throw new IllegalArgumentException("invalid date/time");
                    }
                } catch (Exception ex) {
                    skippedRows++;
                    errors.add("Line " + lineNo + ": invalid biometricId/date/time/logType.");
                    continue;
                }
                if (startDate != null && logDate.isBefore(startDate)) {
                    continue;
                }
                if (endDate != null && logDate.isAfter(endDate)) {
                    continue;
                }

                if (!"1".equals(parts[3]) || !"15".equals(parts[5]) || !"0".equals(parts[6])) {
                    skippedRows++;
                    errors.add("Line " + lineNo + ": expected fixed values [1, 15, 0] in columns 4, 6, 7.");
                    continue;
                }

                if (!(logType == 0 || logType == 1 || logType == 2 || logType == 3)) {
                    skippedRows++;
                    errors.add("Line " + lineNo + ": log type must be 0, 1, 2, or 3.");
                    continue;
                }

                String key = biometricId + "|" + logDate;
                ImportAccumulator acc = aggregated.computeIfAbsent(key, k -> new ImportAccumulator(biometricId, logDate));
                acc.accept(logType, logTime);
                parsedRows++;
            }
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Upload failed: " + e.getMessage());
            return "redirect:/admin/attendance/time-adjustments";
        }

        List<TimeAdjustmentPreviewRow> previewRows = new ArrayList<>();
        int readyForApproval = 0;
        int skippedUnknownBiometric = 0;

        for (ImportAccumulator acc : aggregated.values()) {
            Optional<Employees> empOpt = employeeRepository.findByBiometricId(acc.biometricId);
            if (empOpt.isEmpty()) {
                skippedUnknownBiometric++;
                continue;
            }
            Employees emp = empOpt.get();
            ShiftMatchResult shiftMatch = validateAgainstShift(emp.getEmployeeId(), acc.logDate);
            if (!shiftMatch.allowed) {
                skippedRows++;
                errors.add("Biometric " + acc.biometricId + " on " + acc.logDate + ": " + shiftMatch.reason);
                continue;
            }
            String employeeName = (emp.getLastName() != null ? emp.getLastName() : "")
                    + ", " + (emp.getFirstName() != null ? emp.getFirstName() : "");
            String key = emp.getEmployeeId() + "|" + acc.logDate;
            previewRows.add(new TimeAdjustmentPreviewRow(
                    key,
                    emp.getEmployeeId(),
                    emp.getEmployeeNumber(),
                    acc.biometricId,
                    employeeName,
                    acc.logDate.toString(),
                    new ArrayList<>(acc.inCandidates),
                    new ArrayList<>(acc.outCandidates),
                    acc.defaultIn(),
                    acc.defaultOut(),
                    "12:00 PM",
                    "1:00 PM",
                    shiftMatch.shiftLabel,
                    shouldOverwrite
            ));
            readyForApproval++;
        }
        request.getSession(true).setAttribute(TIME_ADJUSTMENTS_PREVIEW_SESSION_KEY, previewRows);

        StringBuilder summary = new StringBuilder();
        summary.append("Parsed rows: ").append(parsedRows)
                .append("\nRows ready for approval: ").append(readyForApproval)
                .append("\nSkipped unknown biometric IDs: ").append(skippedUnknownBiometric)
                .append("\nSkipped invalid rows: ").append(skippedRows);
        if (!errors.isEmpty()) {
            summary.append("\n\nValidation issues:\n");
            int maxErr = Math.min(20, errors.size());
            for (int i = 0; i < maxErr; i++) {
                summary.append("- ").append(errors.get(i)).append("\n");
            }
            if (errors.size() > maxErr) {
                summary.append("- ... ").append(errors.size() - maxErr).append(" more");
            }
        }

        redirectAttributes.addFlashAttribute("uploadImportSummary", summary.toString());
        if (readyForApproval > 0) {
            redirectAttributes.addFlashAttribute("successMessage", "Biometric logs loaded. Review and approve below.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "No logs available for approval.");
        }
        return "redirect:/admin/attendance/time-adjustments";
    }

    @PostMapping("/admin/attendance/time-adjustments/approve")
    public String approveTimeAdjustments(
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "No unverified logs to approve.");
            return "redirect:/admin/attendance/time-adjustments";
        }
        Object data = session.getAttribute(TIME_ADJUSTMENTS_PREVIEW_SESSION_KEY);
        if (!(data instanceof List<?> rowsRaw) || rowsRaw.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "No unverified logs to approve.");
            return "redirect:/admin/attendance/time-adjustments";
        }

        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        for (Object o : rowsRaw) {
            if (!(o instanceof TimeAdjustmentPreviewRow row)) {
                continue;
            }
            String approvedFlag = request.getParameter("verify_" + row.key);
            if (!"true".equalsIgnoreCase(approvedFlag)) {
                skipped++;
                continue;
            }
            String inRaw = request.getParameter("timeInEdit_" + row.key);
            String outRaw = request.getParameter("timeOutEdit_" + row.key);
            LocalTime in = parseTimeFlexible(inRaw);
            LocalTime out = parseTimeFlexible(outRaw);
            LocalDate date = parseDateFlexible(row.logDate);
            if (in == null || out == null || date == null) {
                skipped++;
                continue;
            }
            ShiftMatchResult shiftMatch = validateAgainstShift(row.employeeId, date);
            if (!shiftMatch.allowed || shiftMatch.shiftIn == null || shiftMatch.shiftOut == null) {
                skipped++;
                continue;
            }
            Optional<Attendance> existingOpt = attendanceRepository.findAttendanceOnDate(row.employeeId, date);
            if (existingOpt.isPresent() && !row.overwrite) {
                skipped++;
                continue;
            }
            Attendance attendance = existingOpt.orElseGet(Attendance::new);
            AttendanceMetrics metrics = computeAttendanceMetrics(
                    in,
                    out,
                    shiftMatch.shiftIn,
                    shiftMatch.shiftOut);
            attendance.setEmployeeId(row.employeeId);
            attendance.setAttendance_date(date);
            attendance.setTime_in(in);
            attendance.setTime_out(out);
            attendance.setWork_hours(metrics.workHours);
            attendance.setLate_minutes(metrics.lateMinutes);
            attendance.setOvertime_hours(resolveOvertimeHours(attendance.getOvertime_hours(), metrics.overtimeHours));
            attendance.setUndertime_minutes(metrics.undertimeMinutes);
            attendance.setStatus("Present");
            attendanceRepository.save(attendance);
            if (existingOpt.isPresent()) {
                updated++;
            } else {
                inserted++;
            }
        }
        session.removeAttribute(TIME_ADJUSTMENTS_PREVIEW_SESSION_KEY);
        redirectAttributes.addFlashAttribute("successMessage",
                "Approved logs saved. Inserted: " + inserted + ", updated: " + updated + ", skipped: " + skipped + ".");
        return "redirect:/admin/attendance/time-adjustments";
    }

    private LocalDate parseDateFlexible(String raw) {
        if (raw == null) return null;
        String v = raw.trim();
        if (v.isEmpty()) return null;
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy")
        );
        for (DateTimeFormatter f : formatters) {
            try {
                return LocalDate.parse(v, f);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private LocalTime parseTimeFlexible(String raw) {
        if (raw == null) return null;
        String v = raw.trim();
        if (v.isEmpty()) return null;
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("HH:mm:ss"),
                DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("hh:mm:ss a"),
                DateTimeFormatter.ofPattern("hh:mm a")
        );
        for (DateTimeFormatter f : formatters) {
            try {
                return LocalTime.parse(v.toUpperCase(), f);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private static final class ImportAccumulator {
        private final String biometricId;
        private final LocalDate logDate;
        private final TreeSet<String> inCandidates = new TreeSet<>();
        private final TreeSet<String> outCandidates = new TreeSet<>();

        private ImportAccumulator(String biometricId, LocalDate logDate) {
            this.biometricId = biometricId;
            this.logDate = logDate;
        }

        private void accept(Integer logType, LocalTime logTime) {
            // 0=IN, 3=BREAKIN (fallback IN), 1=OUT, 2=BREAKOUT (fallback OUT)
            if (logType == 0 || logType == 3) {
                inCandidates.add(logTime.toString());
            }
            if (logType == 1 || logType == 2) {
                outCandidates.add(logTime.toString());
            }
        }

        private String defaultIn() {
            return inCandidates.isEmpty() ? "" : inCandidates.first();
        }

        private String defaultOut() {
            return outCandidates.isEmpty() ? "" : outCandidates.last();
        }
    }

    private static final class TimeAdjustmentPreviewRow {
        private final String key;
        private final Integer employeeId;
        private final String employeeNumber;
        private final String biometricId;
        private final String employeeName;
        private final String logDate;
        private final List<String> inCandidates;
        private final List<String> outCandidates;
        private final String selectedIn;
        private final String selectedOut;
        private final String breakOut;
        private final String breakIn;
        private final String shiftLabel;
        private final boolean overwrite;

        private TimeAdjustmentPreviewRow(String key,
                                         Integer employeeId,
                                         String employeeNumber,
                                         String biometricId,
                                         String employeeName,
                                         String logDate,
                                         List<String> inCandidates,
                                         List<String> outCandidates,
                                         String selectedIn,
                                         String selectedOut,
                                         String breakOut,
                                         String breakIn,
                                         String shiftLabel,
                                         boolean overwrite) {
            this.key = key;
            this.employeeId = employeeId;
            this.employeeNumber = employeeNumber;
            this.biometricId = biometricId;
            this.employeeName = employeeName;
            this.logDate = logDate;
            this.inCandidates = inCandidates;
            this.outCandidates = outCandidates;
            this.selectedIn = selectedIn;
            this.selectedOut = selectedOut;
            this.breakOut = breakOut;
            this.breakIn = breakIn;
            this.shiftLabel = shiftLabel;
            this.overwrite = overwrite;
        }

        public String getKey() { return key; }
        public Integer getEmployeeId() { return employeeId; }
        public String getEmployeeNumber() { return employeeNumber; }
        public String getBiometricId() { return biometricId; }
        public String getEmployeeName() { return employeeName; }
        public String getLogDate() { return logDate; }
        public List<String> getInCandidates() { return inCandidates; }
        public List<String> getOutCandidates() { return outCandidates; }
        public String getSelectedIn() { return selectedIn; }
        public String getSelectedOut() { return selectedOut; }
        public String getBreakOut() { return breakOut; }
        public String getBreakIn() { return breakIn; }
        public String getShiftLabel() { return shiftLabel; }
        public boolean isOverwrite() { return overwrite; }
        public String getDisplayDate() {
            LocalDate d = LocalDate.parse(logDate);
            return d.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        }
        public String format12h(String raw) {
            if (raw == null || raw.isBlank()) {
                return "";
            }
            LocalTime t = LocalTime.parse(raw);
            return t.format(DateTimeFormatter.ofPattern("hh:mm a"));
        }
        public String getSelectedIn12h() { return format12h(selectedIn); }
        public String getSelectedOut12h() { return format12h(selectedOut); }
    }

    private ShiftMatchResult validateAgainstShift(Integer employeeId, LocalDate date) {
        Optional<EmployeeScheduleAssignment> assignmentOpt = resolveShiftAssignment(employeeId, date);
        if (assignmentOpt.isEmpty()) {
            return ShiftMatchResult.denied("employee has no shift assignment for this month");
        }
        EmployeeScheduleAssignment assignment = assignmentOpt.get();
        int dow = date.getDayOfWeek().getValue();
        Optional<WeeklyScheduleTemplateDay> dayOpt =
                weeklyScheduleTemplateDayRepository.findByTemplateIdAndDayOfWeek(assignment.getTemplateId(), dow);
        if (dayOpt.isEmpty()) {
            return ShiftMatchResult.denied("no shift day setup for " + date.getDayOfWeek().getDisplayName(TextStyle.SHORT, java.util.Locale.ENGLISH));
        }
        WeeklyScheduleTemplateDay day = dayOpt.get();
        if (day.isRestDay()) {
            return ShiftMatchResult.restDay("day is marked as rest day");
        }
        if (day.getTimeIn() == null || day.getTimeOut() == null) {
            return ShiftMatchResult.denied("shift has no time in/out setup");
        }
        String label = day.getTimeIn().format(DateTimeFormatter.ofPattern("hh:mm a"))
                + " - " + day.getTimeOut().format(DateTimeFormatter.ofPattern("hh:mm a"));
        return ShiftMatchResult.allowed(label, day.getTimeIn(), day.getTimeOut());
    }

    private AttendanceMetrics computeAttendanceMetrics(
            LocalTime actualIn,
            LocalTime actualOut,
            LocalTime shiftIn,
            LocalTime shiftOut) {
        if (actualIn == null || actualOut == null || shiftIn == null || shiftOut == null) {
            return AttendanceMetrics.zero();
        }

        long shiftStart = shiftIn.toSecondOfDay() / 60L;
        long shiftDuration = clockMinutesBetween(shiftIn, shiftOut);
        if (shiftDuration <= 0) {
            return AttendanceMetrics.zero();
        }
        long shiftEnd = shiftStart + shiftDuration;

        long actualStart = normalizeMinuteToTarget(actualIn, shiftStart);
        long actualDuration = clockMinutesBetween(actualIn, actualOut);
        if (actualDuration <= 0) {
            return AttendanceMetrics.zero();
        }
        long actualEnd = actualStart + actualDuration;

        long regularMinutes = Math.max(0L, Math.min(actualEnd, shiftEnd) - Math.max(actualStart, shiftStart));
        long lateMinutes = Math.max(0L, Math.min(actualStart, shiftEnd) - shiftStart);
        long undertimeMinutes = Math.max(0L, shiftEnd - Math.max(actualEnd, shiftStart));
        long overtimeMinutes = Math.max(0L, actualEnd - Math.max(shiftEnd, actualStart));

        return new AttendanceMetrics(
                minutesToHours(regularMinutes),
                minutesToHours(overtimeMinutes),
                safeIntMinutes(lateMinutes),
                safeIntMinutes(undertimeMinutes));
    }

    private int safeIntMinutes(long minutes) {
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, minutes));
    }

    private Optional<EmployeeScheduleAssignment> resolveShiftAssignment(Integer employeeId, LocalDate date) {
        if (employeeId == null || date == null) {
            return Optional.empty();
        }
        Optional<EmployeeScheduleAssignment> exact =
                employeeScheduleAssignmentRepository.findByEmployeeIdAndScheduleYearAndScheduleMonth(
                        employeeId, date.getYear(), date.getMonthValue());
        if (exact.isPresent()) {
            return exact;
        }
        return employeeScheduleAssignmentRepository.findLatestAssignmentOnOrBefore(
                employeeId, date.getYear(), date.getMonthValue());
    }

    private BigDecimal resolveOvertimeHours(BigDecimal storedOvertime, BigDecimal computedOvertime) {
        BigDecimal stored = storedOvertime != null
                ? storedOvertime.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal computed = computedOvertime != null
                ? computedOvertime.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return stored.max(computed);
    }

    private void populateComputedWorkHours(List<Attendance> attendances) {
        for (Attendance attendance : attendances) {
            if (attendance == null || attendance.getTime_in() == null || attendance.getTime_out() == null) {
                continue;
            }
            log.debug("[POPULATE] attId={}, time_in={}, time_out={}, shiftOverride='{}'",
                    attendance.getAttendanceId(), attendance.getTime_in(), attendance.getTime_out(),
                    attendance.getShiftOverride());
            BigDecimal workHours = attendance.getWork_hours();
            Integer lateMinutes = attendance.getLate_minutes();
            Integer undertimeMinutes = attendance.getUndertime_minutes();
            BigDecimal overtimeHours = attendance.getOvertime_hours();
            if (workHours == null || workHours.compareTo(BigDecimal.ZERO) <= 0) {
                workHours = minutesToHours(clockMinutesBetween(attendance.getTime_in(), attendance.getTime_out()));
            }

            AttendanceMetrics metrics = new AttendanceMetrics(
                    minutesToHours(clockMinutesBetween(attendance.getTime_in(), attendance.getTime_out())),
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    0,
                    0);
            if (attendance.getEmployeeId() != null && attendance.getAttendance_date() != null) {
                ShiftMatchResult shiftMatch;
                if (attendance.getShiftOverride() != null && !attendance.getShiftOverride().isBlank()) {
                    shiftMatch = parseShiftOverride(attendance.getShiftOverride());
                } else {
                    shiftMatch = validateAgainstShift(attendance.getEmployeeId(), attendance.getAttendance_date());
                }
                log.debug("[POPULATE] attId={}, shiftMatch: allowed={}, restDay={}, shiftIn={}, shiftOut={}",
                        attendance.getAttendanceId(), shiftMatch.allowed, shiftMatch.restDay,
                        shiftMatch.shiftIn, shiftMatch.shiftOut);
                if (shiftMatch.restDay) {
                    workHours = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                    lateMinutes = 0;
                    undertimeMinutes = 0;
                    overtimeHours = attendance.getOvertime_hours() != null
                            ? attendance.getOvertime_hours()
                            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                } else if (shiftMatch.allowed && shiftMatch.shiftIn != null && shiftMatch.shiftOut != null) {
                    metrics = computeAttendanceMetrics(
                            attendance.getTime_in(),
                            attendance.getTime_out(),
                            shiftMatch.shiftIn,
                            shiftMatch.shiftOut);
                    workHours = metrics.workHours;
                    lateMinutes = metrics.lateMinutes;
                    undertimeMinutes = metrics.undertimeMinutes;
                    overtimeHours = resolveOvertimeHours(attendance.getOvertime_hours(), metrics.overtimeHours);
                }
            }

            attendance.setWork_hours(workHours != null
                    ? workHours.setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            attendance.setLate_minutes(lateMinutes != null ? Math.max(0, lateMinutes) : 0);
            attendance.setUndertime_minutes(undertimeMinutes != null ? Math.max(0, undertimeMinutes) : 0);
            attendance.setOvertime_hours(overtimeHours != null
                    ? overtimeHours.setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
    }

    private ShiftMatchResult parseShiftOverride(String override) {
        if (override == null || override.isBlank()) {
            log.warn("[PARSE_OVERRIDE] empty shift override");
            return ShiftMatchResult.denied("empty shift override");
        }
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                "(\\d{1,2}:\\d{2}(?::\\d{2})?)\\s*(AM|PM)?\\s*-\\s*(\\d{1,2}:\\d{2}(?::\\d{2})?)\\s*(AM|PM)?",
                java.util.regex.Pattern.CASE_INSENSITIVE).matcher(override.trim());
        if (!m.matches()) {
            log.warn("[PARSE_OVERRIDE] invalid format: '{}'", override);
            return ShiftMatchResult.denied("invalid shift override format: " + override);
        }
        String inRaw = m.group(1);
        String outRaw = m.group(3);
        String ampmIn = m.group(2);
        String ampmOut = m.group(4);
        LocalTime shiftIn = parseShiftTime(inRaw, ampmIn);
        LocalTime shiftOut = parseShiftTime(outRaw, ampmOut);
        log.debug("[PARSE_OVERRIDE] override='{}', shiftIn={}, shiftOut={}", override, shiftIn, shiftOut);
        if (shiftIn == null || shiftOut == null) {
            return ShiftMatchResult.denied("unparseable shift override times: " + override);
        }
        return ShiftMatchResult.allowed(override, shiftIn, shiftOut);
    }

    private LocalTime parseShiftTime(String time, String ampm) {
        if (time == null) return null;
        time = time.trim();
        if (time.isEmpty()) return null;
        if (ampm != null && !ampm.isBlank()) {
            time = time + " " + ampm.trim();
        }
        return parseLocalTime(time);
    }

    private String formatShiftLabel12h(String label24h) {
        if (label24h == null || label24h.isBlank()) return label24h;
        try {
            String[] parts = label24h.split("\\s*-\\s*");
            if (parts.length != 2) return label24h;
            LocalTime in = parseLocalTime(parts[0].trim());
            LocalTime out = parseLocalTime(parts[1].trim());
            if (in == null || out == null) return label24h;
            return in.format(DateTimeFormatter.ofPattern("hh:mm a")) + " - " + out.format(DateTimeFormatter.ofPattern("hh:mm a"));
        } catch (Exception e) {
            return label24h;
        }
    }

    private long clockMinutesBetween(LocalTime start, LocalTime end) {
        long minutes = ChronoUnit.MINUTES.between(start, end);
        if (minutes < 0) {
            minutes += 24L * 60L;
        }
        return minutes;
    }

    private long normalizeMinuteToTarget(LocalTime time, long targetMinute) {
        long baseMinute = time.toSecondOfDay() / 60L;
        long best = baseMinute;
        long bestDistance = Math.abs(baseMinute - targetMinute);
        long[] offsets = new long[] { -24L * 60L, 24L * 60L };
        for (long offset : offsets) {
            long candidate = baseMinute + offset;
            long distance = Math.abs(candidate - targetMinute);
            if (distance < bestDistance) {
                best = candidate;
                bestDistance = distance;
            }
        }
        return best;
    }

    private BigDecimal minutesToHours(long minutes) {
        return BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private static final class ShiftMatchResult {
        private final boolean allowed;
        private final boolean restDay;
        private final String reason;
        private final String shiftLabel;
        private final LocalTime shiftIn;
        private final LocalTime shiftOut;

        private ShiftMatchResult(boolean allowed, boolean restDay, String reason, String shiftLabel, LocalTime shiftIn, LocalTime shiftOut) {
            this.allowed = allowed;
            this.restDay = restDay;
            this.reason = reason;
            this.shiftLabel = shiftLabel;
            this.shiftIn = shiftIn;
            this.shiftOut = shiftOut;
        }

        private static ShiftMatchResult allowed(String shiftLabel, LocalTime shiftIn, LocalTime shiftOut) {
            return new ShiftMatchResult(true, false, "", shiftLabel, shiftIn, shiftOut);
        }

        private static ShiftMatchResult denied(String reason) {
            return new ShiftMatchResult(false, false, reason, "N/A", null, null);
        }

        private static ShiftMatchResult restDay(String reason) {
            return new ShiftMatchResult(false, true, reason, "N/A", null, null);
        }
    }

    private static final class AttendanceMetrics {
        private final BigDecimal workHours;
        private final BigDecimal overtimeHours;
        private final int lateMinutes;
        private final int undertimeMinutes;

        private AttendanceMetrics(
                BigDecimal workHours,
                BigDecimal overtimeHours,
                int lateMinutes,
                int undertimeMinutes) {
            this.workHours = workHours != null
                    ? workHours.setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            this.overtimeHours = overtimeHours != null
                    ? overtimeHours.setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            this.lateMinutes = Math.max(0, lateMinutes);
            this.undertimeMinutes = Math.max(0, undertimeMinutes);
        }

        private static AttendanceMetrics zero() {
            return new AttendanceMetrics(
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    0,
                    0);
        }
    }

    @PostMapping("/employee/attendance/overtime/request")
    public String submitOvertimeRequest(
            @RequestParam LocalDate workDate,
            @RequestParam String overtimeIn,
            @RequestParam String overtimeOut,
            @RequestParam String reason,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (!(authentication.getPrincipal() instanceof Users)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Not signed in.");
            return "redirect:/index";
        }
        Users user = (Users) authentication.getPrincipal();
        if (user.getEmployee() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "No employee profile linked.");
            return "redirect:/employee/home";
        }
        if (workDate == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Work date is required.");
            return redirectOvertime(false, LocalDate.now().getYear(), LocalDate.now().getMonthValue(), null);
        }
        LocalTime in = parseLocalTime(overtimeIn);
        LocalTime out = parseLocalTime(overtimeOut);
        if (in == null || out == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid overtime in or out time.");
            int y = workDate != null ? workDate.getYear() : LocalDate.now().getYear();
            int m = workDate != null ? workDate.getMonthValue() : LocalDate.now().getMonthValue();
            return redirectOvertime(false, y, m, null);
        }
        try {
            String attachmentPath = null;
            if (attachment != null && !attachment.isEmpty()) {
                // Save file to uploads/ot_attachments/ in the project directory
                String uploadDir = System.getProperty("user.dir") + "/uploads/ot_attachments/";
                java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
                if (!java.nio.file.Files.exists(uploadPath)) {
                    java.nio.file.Files.createDirectories(uploadPath);
                }
                String originalFilename = attachment.getOriginalFilename();
                String ext = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
                }
                String fileName = "ot_" + user.getEmployee().getEmployeeId() + "_" + System.currentTimeMillis() + ext;
                java.nio.file.Path filePath = uploadPath.resolve(fileName);
                attachment.transferTo(filePath.toFile());
                // Save only the relative path for DB/storage
                attachmentPath = "uploads/ot_attachments/" + fileName;
            }
            overtimeRequestService.submitWithAttachment(
                user.getEmployee().getEmployeeId(), workDate, in, out, reason, attachmentPath);
            redirectAttributes.addFlashAttribute("successMessage", "Overtime request submitted for approval.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        int y = workDate != null ? workDate.getYear() : LocalDate.now().getYear();
        int m = workDate != null ? workDate.getMonthValue() : LocalDate.now().getMonthValue();
        return redirectOvertime(false, y, m, null);
    }

    @PostMapping("/admin/attendance/overtime/approve/{id}")
    public String approveOvertimeRequest(
            @PathVariable("id") Integer id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (!(authentication.getPrincipal() instanceof Users)) {
            return "redirect:/index";
        }
        Users user = (Users) authentication.getPrincipal();
        try {
            overtimeRequestService.approve(id, user.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Overtime approved and applied to daily time record.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/attendance/overtime";
    }

    @PostMapping("/admin/attendance/overtime/reject/{id}")
    public String rejectOvertimeRequest(
            @PathVariable("id") Integer id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (!(authentication.getPrincipal() instanceof Users)) {
            return "redirect:/index";
        }
        Users user = (Users) authentication.getPrincipal();
        try {
            overtimeRequestService.reject(id, user.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Overtime request rejected.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/attendance/overtime";
    }

    @PostMapping("/admin/attendance/overtime/save")
    public String saveOvertime(
            HttpServletRequest request,
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam(required = false) Integer empId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (authentication == null
                || !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Not allowed.");
            return "redirect:/admin/home";
        }

        if (month == null || month < 1 || month > 12 || year == null || year < 2000 || year > 2100) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid month or year.");
            return redirectOvertime(true, year, month, empId);
        }

        Integer targetEmpId = null;
        if (empId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Select an employee.");
            return redirectOvertime(true, year, month, null);
        }
        targetEmpId = empId;
        if (!employeeRepository.existsById(targetEmpId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Employee not found.");
            return redirectOvertime(true, year, month, null);
        }

        int updated = 0;
        for (Map.Entry<String, String[]> e : request.getParameterMap().entrySet()) {
            String key = e.getKey();
            if (!key.startsWith("ot_")) {
                continue;
            }
            String idPart = key.substring(3);
            int attendanceId;
            try {
                attendanceId = Integer.parseInt(idPart);
            } catch (NumberFormatException ex) {
                continue;
            }
            String raw = e.getValue() != null && e.getValue().length > 0 ? e.getValue()[0] : "";
            Optional<Attendance> opt = attendanceRepository.findById(attendanceId);
            if (opt.isEmpty()) {
                continue;
            }
            Attendance a = opt.get();
            if (!a.getEmployeeId().equals(targetEmpId)) {
                continue;
            }
            if (a.getAttendance_date() == null
                    || a.getAttendance_date().getMonthValue() != month
                    || a.getAttendance_date().getYear() != year) {
                continue;
            }
            a.setOvertime_hours(parseNonNegativeOvertimeHours(raw));
            attendanceRepository.save(a);
            updated++;
        }

        if (updated > 0) {
            redirectAttributes.addFlashAttribute("successMessage", "Overtime hours saved (" + updated + " record(s)).");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "No overtime changes to save.");
        }

        return redirectOvertime(true, year, month, empId);
    }

    private String redirectOvertime(boolean admin, Integer year, Integer month, Integer empId) {
        int y = year != null ? year : LocalDate.now().getYear();
        int m = (month != null && month >= 1 && month <= 12) ? month : LocalDate.now().getMonthValue();
        String base = admin ? "redirect:/admin/attendance/overtime?" : "redirect:/employee/attendance/overtime?";
        StringBuilder sb = new StringBuilder(base);
        sb.append("year=").append(y).append("&month=").append(m);
        if (admin && empId != null) {
            sb.append("&empId=").append(empId);
        }
        return sb.toString();
    }

    private BigDecimal parseNonNegativeOvertimeHours(String raw) {
        if (raw == null) {
            return BigDecimal.ZERO;
        }
        String v = raw.trim();
        if (v.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            BigDecimal x = new BigDecimal(v).setScale(2, RoundingMode.HALF_UP);
            if (x.signum() < 0) {
                return BigDecimal.ZERO;
            }
            return x;
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
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
            populateComputedWorkHours(filtered);
            Map<LocalDate, String> shiftLabelByDate = new LinkedHashMap<>();
            for (Attendance attendance : filtered) {
                LocalDate attendanceDate = attendance.getAttendance_date();
                if (attendanceDate == null) {
                    continue;
                }
                if (attendance.getShiftOverride() != null && !attendance.getShiftOverride().isBlank()) {
                    shiftLabelByDate.put(attendanceDate, formatShiftLabel12h(attendance.getShiftOverride()));
                } else {
                    ShiftMatchResult shiftMatch = validateAgainstShift(targetEmpId, attendanceDate);
                    shiftLabelByDate.put(attendanceDate, shiftMatch.allowed ? shiftMatch.shiftLabel : shiftMatch.reason);
                }
            }
            model.addAttribute("attendances", filtered);
            model.addAttribute("shiftLabelByDate", shiftLabelByDate);
            model.addAttribute("employeeName", targetEmp.getFirstName() + " " + targetEmp.getLastName());
            model.addAttribute("emp_id", targetEmpId); // currently viewed employee
            model.addAttribute("emp_payType", targetEmp.getPayType());
            BigDecimal total = filtered.stream()
                    .map(Attendance::getWork_hours)
                    .filter(h -> h != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("totalHoursRendered", total);
            model.addAttribute("totalHoursRenderedDisplay", HourFormatUtils.formatHours(total));
            int totalUndertimeMinutes = filtered.stream()
                    .map(Attendance::getUndertime_minutes)
                    .filter(m -> m != null)
                    .reduce(0, Integer::sum);
            model.addAttribute("totalUndertimeMinutes", totalUndertimeMinutes);
        }
        // For nav: always use the logged-in user's own employee id when available
        if (selfEmpId != null) {
            model.addAttribute("self_emp_id", selfEmpId);
        }
        if (!model.containsAttribute("attendances")) {
            model.addAttribute("attendances", List.<Attendance>of());
        }
        if (!model.containsAttribute("shiftLabelByDate")) {
            model.addAttribute("shiftLabelByDate", Map.<LocalDate, String>of());
        }
        if (!model.containsAttribute("totalHoursRendered")) {
            model.addAttribute("totalHoursRendered", BigDecimal.ZERO);
        }
        if (!model.containsAttribute("totalHoursRenderedDisplay")) {
            model.addAttribute("totalHoursRenderedDisplay", "0:00");
        }
        if (!model.containsAttribute("totalUndertimeMinutes")) {
            model.addAttribute("totalUndertimeMinutes", 0);
        }
        return "html/attendance";
    }

    @PostMapping("/admin/attendance/shift/edit")
    public String editAttendanceShift(
            @RequestParam(value = "attendanceId", required = false) List<Integer> attendanceIds,
            @RequestParam(value = "shiftIn", required = false) List<String> shiftIns,
            @RequestParam(value = "shiftOut", required = false) List<String> shiftOuts,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer empId,
            RedirectAttributes ra,
            HttpServletRequest request) {

        if (attendanceIds != null) {
            for (int i = 0; i < attendanceIds.size(); i++) {
                Integer attId = attendanceIds.get(i);
                String shiftIn = shiftIns != null && i < shiftIns.size() ? shiftIns.get(i) : null;
                String shiftOut = shiftOuts != null && i < shiftOuts.size() ? shiftOuts.get(i) : null;

                log.debug("[SHIFT_SAVE] attId={}, shiftIn='{}', shiftOut='{}'", attId, shiftIn, shiftOut);

                Attendance attendance = attendanceRepository.findById(attId)
                        .orElseThrow(() -> new IllegalArgumentException("Attendance not found: " + attId));

                String shiftLabel = null;
                if (shiftIn != null && !shiftIn.isBlank() && shiftOut != null && !shiftOut.isBlank()) {
                    shiftLabel = shiftIn + " - " + shiftOut;
                }
                log.debug("[SHIFT_SAVE] attId={}, shiftLabel='{}'", attId, shiftLabel);
                attendance.setShiftOverride(shiftLabel);
                attendanceRepository.save(attendance);
                populateComputedWorkHours(List.of(attendance));
                attendanceRepository.save(attendance);
                log.debug("[SHIFT_SAVE] attId={}, after recalc: late={}, undertime={}, overtime={}, workHours={}",
                        attId, attendance.getLate_minutes(), attendance.getUndertime_minutes(),
                        attendance.getOvertime_hours(), attendance.getWork_hours());
            }
        }

        ra.addFlashAttribute("successMessage", "Shift updated successfully.");

        StringBuilder redirectUrl = new StringBuilder("/admin/attendance");
        String sep = "?";
        if (month != null) { redirectUrl.append(sep).append("month=").append(month); sep = "&"; }
        if (year != null)  { redirectUrl.append(sep).append("year=").append(year);  sep = "&"; }
        if (empId != null) { redirectUrl.append(sep).append("empId=").append(empId); }
        return "redirect:" + redirectUrl;
    }
}
